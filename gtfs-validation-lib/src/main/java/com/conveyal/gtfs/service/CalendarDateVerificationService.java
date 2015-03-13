package com.conveyal.gtfs.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.onebusaway.gtfs.impl.calendar.CalendarServiceDataFactoryImpl;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.ServiceCalendar;
import org.onebusaway.gtfs.model.ServiceCalendarDate;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs.services.calendar.CalendarService;

import com.conveyal.gtfs.model.InvalidValue;
import com.conveyal.gtfs.model.Priority;
import com.conveyal.gtfs.model.ValidationResult;
import com.conveyal.gtfs.service.impl.GtfsStatisticsService;


public class CalendarDateVerificationService {

	private static GtfsMutableRelationalDao gtfsMDao = null;
	private static GtfsStatisticsService stats = null;
	private static CalendarService calendarService = null;
	private static Calendar start = null;
	private static Calendar end = null;
	private static Date from = null;
	private static Date to = null;

	public CalendarDateVerificationService(GtfsMutableRelationalDao gmd){
		gtfsMDao = gmd;
		stats = new GtfsStatisticsService(gmd);
		calendarService = CalendarServiceDataFactoryImpl.createService(gmd);
				
		start = Calendar.getInstance();
		end = Calendar.getInstance();
		from = stats.getCalendarServiceRangeStart();
		to = stats.getCalendarServiceRangeEnd();

	}
	public HashMap<AgencyAndId, Integer> getTripCountsForAllServiceIDs() {
		HashMap<AgencyAndId, Integer> tripsPerCalHash = new HashMap<AgencyAndId, Integer>();
		for (ServiceCalendar serviceCalendar : gtfsMDao.getAllCalendars()) {
			int tripCount =0;
			for (Trip t: gtfsMDao.getAllTrips()){
				if (t.getServiceId().equals(serviceCalendar.getServiceId())){
					tripCount++;
				}
				tripsPerCalHash.put(serviceCalendar.getServiceId(), tripCount);
			}}
		for (ServiceCalendarDate serviceCalendar : gtfsMDao.getAllCalendarDates()) {
			int tripCount =0;
			for (Trip t: gtfsMDao.getAllTrips()){
				if (t.getServiceId().equals(serviceCalendar.getServiceId())){
					tripCount++;
				}
				tripsPerCalHash.put(serviceCalendar.getServiceId(), tripCount);
			}}
		return tripsPerCalHash;
	}

	public HashMap<Date, Integer> getTripCountForDates() {

		HashMap<AgencyAndId, Integer> tripsPerServHash = getTripCountsForAllServiceIDs();
		HashMap<Date, Integer> tripsPerDateHash = new HashMap<Date, Integer>();

		start.setTime(from);
		end.setTime(to);

		while( !start.after(end)){
			Integer tripCount =0;
			ServiceDate targetDay = new ServiceDate(start);

			for (AgencyAndId sid : calendarService.getServiceIdsOnDate(targetDay)){
				if (tripsPerDateHash.containsKey(targetDay)){
					tripCount = tripsPerDateHash.get(targetDay);
				}
				if (tripsPerServHash.containsKey(sid)){
					tripCount = tripCount + tripsPerServHash.get(sid);
				}
			}
			tripsPerDateHash.put(targetDay.getAsDate(), tripCount);
			start.add(Calendar.DATE, 1);
		}
		
		return tripsPerDateHash;
	}
	
	public HashMap<Date, ArrayList<AgencyAndId>> getServiceIdsForDate(){
		HashMap<Date, ArrayList<AgencyAndId>> serviceIdsForDates = new HashMap<Date, ArrayList<AgencyAndId>>();
		
		start.setTime(from);
		end.setTime(to);
		
		while( !start.after(end)){
			ArrayList<AgencyAndId> serviceIdsForTargetDay = new ArrayList<AgencyAndId>();
			ServiceDate targetDay = new ServiceDate(start);

			for (AgencyAndId sid : calendarService.getServiceIdsOnDate(targetDay)){
				serviceIdsForTargetDay.add(sid);
				}
			for (ServiceCalendarDate serviceCalendar : gtfsMDao.getAllCalendarDates()) {
				if (serviceCalendar.getDate() == targetDay && serviceCalendar.getExceptionType() == 1){
					AgencyAndId sid = serviceCalendar.getServiceId();
					serviceIdsForTargetDay.add(sid);
				}
			}
			serviceIdsForDates.put(targetDay.getAsDate(), serviceIdsForTargetDay);
			start.add(Calendar.DATE, 1);
		}
		return serviceIdsForDates;
		
	}
	
	public ArrayList<Date> getDatesWithNoTrips(){
		ArrayList<Date> datesWithNoTrips = new ArrayList<Date>();
		HashMap<Date, Integer> tc = getTripCountForDates();
		for(Map.Entry<Date, Integer> d: tc.entrySet()){
			if (d.getValue()==0){
				datesWithNoTrips.add(d.getKey());
			}
		}
		return datesWithNoTrips;
	}

	//I got 99 problems, and a calendar is one
	public ValidationResult getCalendarProblems(){
		ValidationResult vr = new ValidationResult();
		ArrayList<Date> datesWithNoTrips = getDatesWithNoTrips();
		for (Date d: datesWithNoTrips){
			InvalidValue iv = new InvalidValue("calendar", "service_id", d.toString(), "NoServiceOnThisDate", "There is no service on " + d.toString(), null, Priority.HIGH);
			vr.add(iv);
		}
		
		//TODO add checks for dates with significant decreases in service (e.g. missing depot) 
		
		return vr;
	}
	
	public static Set<AgencyAndId> getCalendarsForDate(ServiceDate date) {
		return calendarService.getServiceIdsOnDate(date);
	}
	
	public static String formatTripCountForServiceIDs(CalendarDateVerificationService t){
		return Arrays.toString(t.getTripCountsForAllServiceIDs().entrySet().toArray());
	}
	
}
