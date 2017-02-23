
package com.conveyal.gtfs.service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

import org.onebusaway.gtfs.impl.GtfsRelationalDaoImpl;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.ServiceCalendar;
import org.onebusaway.gtfs.model.ServiceCalendarDate;
import org.onebusaway.gtfs.model.ShapePoint;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;

import com.conveyal.gtfs.GtfsStatisticsServiceCalendarDatesTest;
import com.conveyal.gtfs.model.BlockInterval;
import com.conveyal.gtfs.model.DuplicateStops;
import com.conveyal.gtfs.model.InputOutOfRange;
import com.conveyal.gtfs.model.InvalidValue;
import com.conveyal.gtfs.model.Priority;
import com.conveyal.gtfs.model.TripPatternCollection;
import com.conveyal.gtfs.model.ValidationResult;
import com.conveyal.gtfs.model.comparators.BlockIntervalComparator;
import com.conveyal.gtfs.model.comparators.StopTimeComparator;
import com.conveyal.gtfs.service.impl.GtfsStatisticsService;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.index.strtree.STRtree;

public class GtfsValidationService {

	static GeometryFactory geometryFactory = new GeometryFactory();

	private GtfsRelationalDaoImpl gtfsDao = null;
	private GtfsStatisticsService statsService = null;
	private CalendarDateVerificationService cdvs = null;

	public GtfsValidationService(GtfsRelationalDaoImpl dao)  {

		gtfsDao = dao;		
		statsService = new GtfsStatisticsService(dao);
	}

	/**
	 * Checks for invalid route values. Returns a ValidationResult object listing invalid/missing data.
	 * 
	 */
	public ValidationResult validateRoutes() {

		ValidationResult result = new ValidationResult();

		for(Route route : gtfsDao.getAllRoutes()) {

			String routeId = route.getId().toString();

			String shortName = "";
			String longName = "";
			String desc = "";

			if(route.getShortName() != null)
				shortName = route.getShortName().trim().toLowerCase();

			if(route.getLongName() != null)
				longName = route.getLongName().trim().toLowerCase();

			if(route.getDesc() != null)
				desc = route.getDesc().toLowerCase();


			//RouteShortAndLongNamesAreBlank
			if(longName.isEmpty() && shortName.isEmpty())
				result.add(new InvalidValue("route", "route_short_name,route_long_name", routeId , "RouteShortAndLongNamesAreBlank", "", null, Priority.HIGH));

			//ValidateRouteShortNameIsTooLong
			if(shortName.length() > 9)
				result.add(new InvalidValue("route", "route_short_name", routeId, "ValidateRouteShortNameIsTooLong", "route_short_name is " +  shortName.length() + " chars ('" +  shortName + "')" , null, Priority.MEDIUM));

			//ValidateRouteLongNameContainShortName
			if(!longName.isEmpty() && !shortName.isEmpty() &&longName.contains(shortName))
				result.add(new InvalidValue("route", "route_short_name,route_long_name", routeId, "ValidateRouteLongNameContainShortName", "'" + longName + "' contains '" + shortName + "'", null, Priority.MEDIUM));

			//ValidateRouteDescriptionSameAsRouteName
			if(!desc.isEmpty() && (desc.equals(shortName) || desc.equals(longName)))
				result.add(new InvalidValue("route", "route_short_name,route_long_name,route_desc", routeId, "ValidateRouteDescriptionSameAsRouteName", "", null, Priority.MEDIUM));

			//ValidateRouteTypeInvalidValid
			if(route.getType() < 0 || route.getType() > 7)
				result.add(new InvalidValue("route", "route_type", routeId, "ValidateRouteTypeInvalidValid", "route_type is " + route.getType(), null,  Priority.HIGH));

		}

		return result;

	}

	/**
	 * Checks for invalid trip values. Returns a ValidationResult object listing invalid/missing data.
	 * 
	 */
	public ValidationResult validateTrips() {

		ValidationResult result = new ValidationResult();

		// map stop time sequences to trip id

		HashMap<String, ArrayList<StopTime>> tripStopTimes = new HashMap<String, ArrayList<StopTime>>(statsService.getStopTimesCount() *2);

		HashSet<String> usedStopIds = new HashSet<String>(statsService.getStopCount() *2);

		String tripId;
		
		cdvs = new CalendarDateVerificationService(gtfsDao);
		HashSet<AgencyAndId> activeServiceIds = cdvs.getActiveServiceIdsOnly();
		

		for(StopTime stopTime : gtfsDao.getAllStopTimes()) {

			tripId = stopTime.getTrip().getId().toString();

			if(!tripStopTimes.containsKey(tripId))
				tripStopTimes.put(tripId, new ArrayList<StopTime>());

			tripStopTimes.get(tripId).add(stopTime);

			if (stopTime.getStop() != null && stopTime.getStop().getId() != null) {
				usedStopIds.add(stopTime.getStop().getId().toString());
			}

		}

//		// create service calendar date map
//
//
		@SuppressWarnings("deprecation")
		int reasonableNumberOfDates = statsService.getNumberOfDays() *2;

		HashMap<String, HashSet<Date>> serviceCalendarDates = new HashMap<String, HashSet<Date>>(reasonableNumberOfDates);
		//TODO: factor out.
		for(ServiceCalendar calendar : gtfsDao.getAllCalendars()) {

			Date startDate = calendar.getStartDate().getAsDate();
			Date endDate = calendar.getEndDate().getAsDate();

			HashSet<Date> datesActive = new HashSet<Date>(reasonableNumberOfDates);

			Date currentDate = startDate;

			HashSet<Integer> daysActive = new HashSet<Integer>();

			if(calendar.getSunday() == 1)
				daysActive.add(Calendar.SUNDAY);
			else if(calendar.getMonday() == 1)
				daysActive.add(Calendar.MONDAY);
			else if(calendar.getTuesday() == 1)
				daysActive.add(Calendar.TUESDAY);
			else if(calendar.getWednesday() == 1)
				daysActive.add(Calendar.WEDNESDAY);
			else if(calendar.getThursday() == 1)
				daysActive.add(Calendar.THURSDAY);
			else if(calendar.getFriday() == 1)
				daysActive.add(Calendar.FRIDAY);
			else if(calendar.getSaturday() == 1)
				daysActive.add(Calendar.SATURDAY);

			while(currentDate.before(endDate) || currentDate.equals(endDate)) {

				Calendar cal = Calendar.getInstance();
				cal.setTime(currentDate);

				if(daysActive.contains(cal.get(Calendar.DAY_OF_WEEK)))
					datesActive.add(currentDate);

				cal.add(Calendar.DATE, 1); 
				currentDate = cal.getTime();
			}

			serviceCalendarDates.put(calendar.getServiceId().getId(), datesActive);

		}

		// add/remove service exceptions
		for(ServiceCalendarDate calendarDate : gtfsDao.getAllCalendarDates()) {

			String serviceId = calendarDate.getServiceId().getId();
			int exceptionType = calendarDate.getExceptionType();

			if(serviceCalendarDates.containsKey(serviceId)) {

				if(exceptionType == 1)
					serviceCalendarDates.get(serviceId).add(calendarDate.getDate().getAsDate());
				else if (exceptionType == 2 && serviceCalendarDates.get(serviceId).contains(calendarDate.getDate().getAsDate()))
					serviceCalendarDates.get(serviceId).remove(calendarDate.getDate().getAsDate());
			}
			// handle service ids that don't appear in calendar.txt
			// for instance, feeds that have no calendar.txt (e.g. TriMet, NJ Transit)
			// and rely exclusively on calendar_dates.txt
			else if (exceptionType == 1) {
				HashSet<Date> calendarDates = new HashSet<Date>();
				calendarDates.add(calendarDate.getDate().getAsDate());
				serviceCalendarDates.put(serviceId, calendarDates);
			}

		}

		// check for unused stops 

		for(Stop stop : gtfsDao.getAllStops()) {

			String stopId = stop.getId().toString();

			if(!usedStopIds.contains(stopId)) {
				result.add(new InvalidValue("stop", "stop_id", stopId, "UnusedStop", "Stop Id " + stopId + " is not used in any trips." , null, Priority.LOW));
			}
		}


		HashMap<String, ArrayList<BlockInterval>> blockIntervals = new HashMap<String, ArrayList<BlockInterval>>();

		HashMap<String, String> duplicateTripHash = new HashMap<String, String>();

		String tripKey, blockId;
		for(Trip trip : gtfsDao.getAllTrips()) {

			tripId = trip.getId().toString();

			ArrayList<StopTime> stopTimes = tripStopTimes.get(tripId);

			if(stopTimes == null || stopTimes.isEmpty()) {
				InvalidValue iv = new InvalidValue("trip", "trip_id", tripId, "NoStopTimesForTrip", "Trip Id " + tripId + " has no stop times." , null, Priority.HIGH);
				iv.route = trip.getRoute();
				result.add(iv);
				continue;
			}

			Collections.sort(stopTimes, new StopTimeComparator());

			StopTime previousStopTime = null;
			for(StopTime stopTime : stopTimes) {

				if(stopTime.getDepartureTime() < stopTime.getArrivalTime()) {
					InvalidValue iv = 
							new InvalidValue("stop_time", "trip_id", tripId, "StopTimeDepartureBeforeArrival", "Trip Id " + tripId + " stop sequence " + stopTime.getStopSequence() + " departs before arriving.", null, Priority.HIGH);
					iv.route = trip.getRoute();
					result.add(iv);
				}

				// check for null previous stop time and negative arrival time (int value is -999 if arrival time is empty, e.g. non-timepoint)
				if(previousStopTime != null && stopTime.getArrivalTime() > 0) {

					if(stopTime.getArrivalTime() < previousStopTime.getDepartureTime()) {
						System.out.println(stopTime.getArrivalTime());
						InvalidValue iv =
								new InvalidValue("stop_time", "trip_id", tripId, "StopTimesOutOfSequence", "Trip Id " + tripId + " stop sequence " + stopTime.getStopSequence() + " arrives before departing " + previousStopTime.getStopSequence(), null, Priority.HIGH);
						iv.route = trip.getRoute();
						result.add(iv);

						// only capturing first out of sequence stop for now -- could consider collapsing duplicates based on tripId
						break;					
					}

				}

				previousStopTime = stopTime;
			}


			// store trip intervals by block id

			blockId = "";

			if(trip.getBlockId() != null){
				blockId = trip.getBlockId();
			}

			if(!blockId.isEmpty() && activeServiceIds.contains(trip.getServiceId())) {

				BlockInterval blockInterval = new BlockInterval();
				blockInterval.setTrip(trip);
				blockInterval.setStartTime( stopTimes.get(0).getDepartureTime());
				blockInterval.setFirstStop(stopTimes.get(0));
				blockInterval.setLastStop(stopTimes.get(stopTimes.size() -1));

				if(!blockIntervals.containsKey(blockId))
					blockIntervals.put(blockId, new ArrayList<BlockInterval>());

				blockIntervals.get(blockId).add(blockInterval);

			}

			// check for duplicate trips starting at the same time with the same service id

			String stopIds = "";

			for(StopTime stopTime : stopTimes) {
				if (stopTime.getStop() != null && stopTime.getStop().getId() != null) {
					stopIds += stopTime.getStop().getId().toString() + ",";
				}
			}

			tripKey = trip.getServiceId().getId() + "_"+ blockId + "_" + stopTimes.get(0).getDepartureTime() +"_" + stopTimes.get(stopTimes.size() -1).getArrivalTime() + "_" + stopIds;

			if(duplicateTripHash.containsKey(tripKey)) {
				String duplicateTripId = duplicateTripHash.get(tripKey);
				InvalidValue iv =
						new InvalidValue("trip", "trip_id", tripId, "DuplicateTrip", "Trip Ids " + duplicateTripId + " & " + tripId + " are duplicates" , null, Priority.LOW);
				iv.route = trip.getRoute();
				result.add(iv);

			}
			else
				duplicateTripHash.put(tripKey, tripId);
		}
		

		// check for overlapping trips within block
			for(Entry<String, ArrayList<BlockInterval>> blockIdset : blockIntervals.entrySet()) {

				blockId = blockIdset.getKey();
				ArrayList<BlockInterval> intervals = blockIntervals.get(blockId);

				Collections.sort(intervals, new BlockIntervalComparator());

				int iOffset = 0;
				for(BlockInterval i1 : intervals) { 
					for(BlockInterval i2 : intervals.subList(iOffset, intervals.size() - 1)) {


						String tripId1 = i1.getTrip().getId().toString();
						String tripId2 = i2.getTrip().getId().toString();


						if(!tripId1.equals(tripId2)) {
							// if trips don't overlap, skip 
							if(i1.getLastStop().getDepartureTime() <= i2.getFirstStop().getArrivalTime() 
									|| i2.getLastStop().getDepartureTime() <= i1.getFirstStop().getArrivalTime())
								continue;

							// if trips have same service id they overlap
							if(i1.getTrip().getServiceId().getId().equals(i2.getTrip().getServiceId().getId())) {
								// but if they are already in the result set, ignore
								if (!result.containsBoth(tripId1, tripId2, "trip")){
									InvalidValue iv =
											new InvalidValue("trip", "block_id", blockId, "OverlappingTripsInBlock", "Trip Ids " + tripId1 + " & " + tripId2 + " overlap and share block Id " + blockId , null, Priority.HIGH);
									// not strictly correct; they could be on different routes
									iv.route = i1.getTrip().getRoute();
									result.add(iv);
								}
							}

							else {
								// if trips don't share service id check to see if service dates fall on the same days/day of week

//								try {
									
									for(Date d1 : serviceCalendarDates.get(i1.getTrip().getServiceId().getId())) {

										if(serviceCalendarDates.get(i2.getTrip().getServiceId().getId()).contains(d1)) {
											InvalidValue iv = new InvalidValue("trip", "block_id", blockId, "OverlappingTripsInBlock", "Trip Ids " + tripId1 + " & " + tripId2 + " overlap and share block Id " + blockId , null, Priority.HIGH);
											iv.route = i1.getTrip().getRoute();
											result.add(iv);
											break;
										}
//									}
								//} catch (Exception e) {
									//System.out.println("Could not find :"+ i1.getTrip().getServiceId().getId().toString());
								}
							}
						}
					}
				}
			}
			
		// check for reversed trip shapes and add to result list 
		result.append(this.listReversedTripShapes());

		return result;

	}


	/**
	 * Returns a list of coincident DuplicateStops. 
	 * @throws InputOutOfRange if lat/lon of stops can't be transformed to EPSG:4326
	 * 
	 */
	public ValidationResult duplicateStops()  {
		// default duplicate stops as coincident with a two meter buffer
		return duplicateStops(2.0);
	}

	/**
	 * Returns a list of coincident DuplicateStops. 
	 * 
	 * @param the buffer distance for two stops to be considered duplicate
	 * 
	 */
	public ValidationResult duplicateStops(Double bufferDistance)  {

		ValidationResult result = new ValidationResult();

		Collection<Stop> stops = gtfsDao.getAllStops();

		STRtree stopIndex = new STRtree();

		HashMap<String, Geometry> stopProjectedGeomMap = new HashMap<String, Geometry>(statsService.getStopCount() * 2);

		for(Stop stop : stops) {

			try{
				Geometry geom = GeoUtils.getGeometryFromCoordinate(stop.getLat(), stop.getLon());

				stopIndex.insert(geom.getEnvelopeInternal(), stop);

				stopProjectedGeomMap.put(stop.getId().toString(), geom);

			} catch (IllegalArgumentException iae) {
				result.add(new InvalidValue("stop", "duplicateStops", stop.toString(), "MissingCoordinates", "stop " + stop + " is missing coordinates", null, Priority.MEDIUM));
			}

		}

		stopIndex.build();

		List<DuplicateStops> duplicateStops = new ArrayList<DuplicateStops>();

		for(Geometry stopGeom : stopProjectedGeomMap.values()) {

			Geometry bufferedStopGeom = stopGeom.buffer(bufferDistance);

			@SuppressWarnings("unchecked")
			List<Stop> stopCandidates = (List<Stop>)stopIndex.query(bufferedStopGeom.getEnvelopeInternal());

			if(stopCandidates.size() > 1) {

				for(Stop stop1 : stopCandidates) {
					for(Stop stop2 : stopCandidates) {

						if(stop1.getId() != stop2.getId()) {

							Boolean stopPairAlreadyFound = false;
							for(DuplicateStops duplicate : duplicateStops) {

								if((duplicate.stop1.getId().getAgencyId().equals(stop1.getId().getAgencyId()) && duplicate.stop2.getId().getAgencyId().equals(stop2.getId().getAgencyId())) || 
										(duplicate.stop2.getId().getAgencyId().equals(stop1.getId().getAgencyId()) && duplicate.stop1.getId().getAgencyId().equals(stop2.getId().getAgencyId())))
									stopPairAlreadyFound = true;
							}

							if(stopPairAlreadyFound)
								continue;

							Geometry stop1Geom = stopProjectedGeomMap.get(stop1.getId().toString());
							Geometry stop2Geom = stopProjectedGeomMap.get(stop2.getId().toString());

							double distance = stop1Geom.distance(stop2Geom);

							// if stopDistance is within bufferDistance consider duplicate
							if(distance <= bufferDistance){

								// TODO: a good place to check if stops are part of a station grouping

								DuplicateStops duplicateStop = new DuplicateStops(stop1, stop2, distance);
								duplicateStops.add(duplicateStop);
								result.add(new InvalidValue("stop", "stop_lat,stop_lon", duplicateStop.getStopIds(), "DuplicateStops", duplicateStop.toString(), duplicateStop, Priority.LOW));

							}
						}

					}
				}
			}
		}

		return result;
	}

	public ValidationResult listReversedTripShapes() {
		return listReversedTripShapes(1.0);
	}
	/**
	 * Check for stops that are further away from a shape than expected
	 * @param minDistance expected max distance from shape to not be included in 
	 * @return
	 */
	public ValidationResult listStopsAwayFromShape(Double minDistance){

		List<AgencyAndId> shapeIds = gtfsDao.getAllShapeIds();
		TripPatternCollection tripPatterns = new TripPatternCollection(shapeIds.size() *2);
		String problemDescription = "Stop is more than " + minDistance + "m from shape";

		ValidationResult result = new ValidationResult();			

		Geometry shapeLine, stopGeom;
		Stop stop;
		Route routeId;
		List<StopTime> stopTimes;
		List<Trip> tripsForShape;

		for (AgencyAndId shapeId : shapeIds){

			shapeLine = GeoUtils.getGeomFromShapePoints(
					gtfsDao.getShapePointsForShapeId(shapeId));
			tripsForShape = gtfsDao.getTripsForShapeId(shapeId);

			for (Trip trip: tripsForShape){
				//filter that list by trip patterns, 
				//where a pattern is a distinct combo of route, shape, and stopTimes
				routeId = trip.getRoute();
				stopTimes = gtfsDao.getStopTimesForTrip(trip);

				if (!tripPatterns.addIfNotPresent(routeId, shapeId, stopTimes)){

					// if any stop is more than minDistance, add to ValidationResult 
					for (StopTime stopTime : stopTimes){
						stop = stopTime.getStop();

						try{
							stopGeom = GeoUtils.getGeometryFromCoordinate(
									stop.getLat(), stop.getLon());
							if (shapeLine.distance(stopGeom) > minDistance){
								String problem = stop.getId().toString() + " on "+ shapeId.getId();
								InvalidValue iv = new InvalidValue(
										"shape", "shape_lat,shape_lon", problem, "StopOffShape", 
										problemDescription, shapeId.getId(), Priority.MEDIUM);
								result.add(iv);
							}
						}
						catch (Exception e){ 
							result.add(new InvalidValue("stop", "shapeId", shapeId.toString() , "Illegal stopCoord for shape", "", null, Priority.MEDIUM));
						}
					}
				}
			}



			//

		}
		return result;
	}

	public ValidationResult listReversedTripShapes(Double distanceMultiplier) {

		ValidationResult result = new ValidationResult();

		Collection<Trip> trips = gtfsDao.getAllTrips();

		Collection<StopTime> stopTimes = gtfsDao.getAllStopTimes();

		int numTrips = gtfsDao.getAllTrips().size();

		HashMap<String, StopTime> firstStopMap = new HashMap<String, StopTime>(numTrips *2);
		HashMap<String, StopTime> lastStopMap = new HashMap<String, StopTime>(numTrips *2);

		// map first and last stops for each trip id

		for(StopTime stopTime : stopTimes) {
			String tripId = stopTime.getTrip().getId().toString();

			if(firstStopMap.containsKey(tripId)) {
				if(firstStopMap.get(tripId).getStopSequence() > stopTime.getStopSequence())
					firstStopMap.put(tripId, stopTime);
			}
			else 
				firstStopMap.put(tripId, stopTime);

			if(lastStopMap.containsKey(tripId)) {
				if(lastStopMap.get(tripId).getStopSequence() < stopTime.getStopSequence())
					lastStopMap.put(tripId, stopTime);
			}
			else 
				lastStopMap.put(tripId, stopTime);
		}

		Collection<ShapePoint> shapePoints = gtfsDao.getAllShapePoints();

		HashMap<String, ShapePoint> firstShapePoint = new HashMap<String, ShapePoint>(numTrips *2);
		HashMap<String, ShapePoint> lastShapePoint = new HashMap<String, ShapePoint>(numTrips *2);

		// map first and last shape points

		for(ShapePoint shapePoint : shapePoints) {

			String shapeId = shapePoint.getShapeId().getId();

			if(firstShapePoint.containsKey(shapeId)) {
				if(firstShapePoint.get(shapeId).getSequence() > shapePoint.getSequence())
					firstShapePoint.put(shapeId, shapePoint);
			}
			else 
				firstShapePoint.put(shapeId, shapePoint);

			if(lastShapePoint.containsKey(shapeId)) {
				if(lastShapePoint.get(shapeId).getSequence() < shapePoint.getSequence())
					lastShapePoint.put(shapeId, shapePoint);
			}
			else 
				lastShapePoint.put(shapeId, shapePoint);

		}

		String tripId, shapeId;
		StopTime firstStop, lastStop;
		Coordinate firstStopCoord, lastStopCoord, firstShapeCoord, lastShapeCoord;
		Geometry firstShapeGeom, lastShapeGeom, firstStopGeom, lastStopGeom;

		for(Trip trip : trips) {

			tripId = trip.getId().toString();
			if (trip.getShapeId() == null) {
				InvalidValue iv = new InvalidValue("trip", "shape_id", tripId, "MissingShape", "Trip " + tripId + " is missing a shape", null, Priority.MEDIUM);
				iv.route = trip.getRoute();
				result.add(iv);
				continue;
			}
			shapeId = trip.getShapeId().getId();

			firstStop = firstStopMap.get(tripId);
			lastStop = lastStopMap.get(tripId);

			firstStopCoord = null;
			lastStopCoord = null;
			firstShapeGeom = null;
			lastShapeGeom = null;
			firstStopGeom = null;
			lastStopGeom = null;
			firstShapeCoord = null;
			lastShapeCoord = null;
			try {
				firstStopCoord = new Coordinate(firstStop.getStop().getLat(), firstStop.getStop().getLon());
				lastStopCoord = new Coordinate(lastStop.getStop().getLat(), lastStop.getStop().getLon());

				firstStopGeom = geometryFactory.createPoint(GeoUtils.convertLatLonToEuclidean(firstStopCoord));
				lastStopGeom = geometryFactory.createPoint(GeoUtils.convertLatLonToEuclidean(lastStopCoord));

				firstShapeCoord = new Coordinate(firstShapePoint.get(shapeId).getLat(), firstShapePoint.get(shapeId).getLon());
				lastShapeCoord = new Coordinate(lastShapePoint.get(shapeId).getLat(), firstShapePoint.get(shapeId).getLon());

				firstShapeGeom = geometryFactory.createPoint(GeoUtils.convertLatLonToEuclidean(firstShapeCoord));
				lastShapeGeom = geometryFactory.createPoint(GeoUtils.convertLatLonToEuclidean(lastShapeCoord));
			} catch (Exception any) {
				InvalidValue iv = new InvalidValue("trip", "shape_id", tripId, "MissingCoordinates", "Trip " + tripId + " is missing coordinates", null, Priority.MEDIUM);
				iv.route = trip.getRoute();
				result.add(iv);
				continue;
			}


			firstShapeCoord = new Coordinate(firstShapePoint.get(shapeId).getLat(), firstShapePoint.get(shapeId).getLon());
			lastShapeCoord = new Coordinate(lastShapePoint.get(shapeId).getLat(), firstShapePoint.get(shapeId).getLon());

			Double distanceFirstStopToStart = firstStopGeom.distance(firstShapeGeom);
			Double distanceFirstStopToEnd = firstStopGeom.distance(lastShapeGeom);

			Double distanceLastStopToEnd = lastStopGeom.distance(lastShapeGeom);
			Double distanceLastStopToStart = lastStopGeom.distance(firstShapeGeom);

			// check if first stop is x times closer to end of shape than the beginning or last stop is x times closer to start than the end
			if(distanceFirstStopToStart > (distanceFirstStopToEnd * distanceMultiplier) && distanceLastStopToEnd > (distanceLastStopToStart * distanceMultiplier)) {
				InvalidValue iv =
						new InvalidValue("trip", "shape_id", tripId, "ReversedTripShape", "Trip " + tripId + " references reversed shape " + shapeId, null, Priority.MEDIUM);
				iv.route = trip.getRoute();
				result.add(iv);
			}
		}

		return result;

	}

}


