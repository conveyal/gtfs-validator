package com.conveyal.gtfs.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.onebusaway.gtfs.impl.GtfsRelationalDaoImpl;
import org.onebusaway.gtfs.impl.calendar.CalendarServiceDataFactoryImpl;
import org.onebusaway.gtfs.model.Agency;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.ServiceCalendarDate;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.gtfs.services.calendar.CalendarService;

import com.conveyal.gtfs.model.InvalidValue;
import com.conveyal.gtfs.model.Priority;
import com.conveyal.gtfs.model.ValidationResult;
import com.conveyal.gtfs.service.impl.GtfsStatisticsService;


public class CalendarDateVerificationService {

	private static GtfsRelationalDaoImpl gtfsMDao = null;
	private static GtfsStatisticsService stats = null;
	private static CalendarService calendarService = null;
	private static Calendar start = null;
	private static Calendar end = null;
	private static TimeZone tz = null;
	private static ServiceDate from;
	private static ServiceDate to;
	private static String aid = null;

	public CalendarDateVerificationService(GtfsRelationalDaoImpl gmd){
		gtfsMDao = gmd;
		stats = new GtfsStatisticsService(gmd);
		calendarService = CalendarServiceDataFactoryImpl.createService(gmd);

		start = Calendar.getInstance();
		end = Calendar.getInstance();
		
		from = new ServiceDate(stats.getCalendarServiceRangeStart());
		to = new ServiceDate(stats.getCalendarServiceRangeEnd());

		Collection<Agency> agencies = stats.getAllAgencies();

		Agency a = agencies.iterator().next();
		
		//Do you know how many time zones there are in the Soviet Union?
//		if (agencies.size() == 1){
			aid = a.getId();
//		}
//		else { 
//			for (Agency b: agencies){
//				if (firstTz != b.getTimezone()){
//					System.out.println(firstTz + b.getTimezone());
//					System.err.println("Warning: This file may have two time zones");
//				}
//			}
//		}

		tz = calendarService.getTimeZoneForAgencyId(aid);
		start.setTimeZone(tz);
		end.setTimeZone(tz);

	}
	public ConcurrentHashMap<AgencyAndId, AtomicInteger> getTripCountsForAllServiceIDs() {
		// better way than this loop
		// for each route
		// geTripsPerRoute, then increment their calendarID counts.
		
		ConcurrentHashMap<AgencyAndId, AtomicInteger> tripsPerCalHash = new ConcurrentHashMap<AgencyAndId, AtomicInteger>();
		gtfsMDao.getAllRoutes()
			.forEach(r -> gtfsMDao.getTripsForRoute(r)
					.forEach(t -> {
						tripsPerCalHash.putIfAbsent(t.getServiceId(), new AtomicInteger(0));
						tripsPerCalHash.get(t.getServiceId()).incrementAndGet();
					}));
		
		return tripsPerCalHash;
	}
/*
 * @return a TreeMap (sorted by calendar) with the number of trips per day.  
 */
	public TreeMap<Calendar, Integer> getTripCountForDates() {

		ConcurrentHashMap<AgencyAndId, AtomicInteger> tripsPerServHash = getTripCountsForAllServiceIDs();
		TreeMap<Calendar, Integer> tripsPerDateHash = new TreeMap<Calendar, Integer>();

		start.setTime(from.getAsDate(tz));
		
		end.setTime(to.getAsDate(tz));
		
		if (start == null){
			throw new IllegalArgumentException("Calendar Date Range Improperly Set");
		}

		while(!start.after(end)){
			Integer tripCount =0;
			ServiceDate targetDay = new ServiceDate(start);
			Calendar targetDayAsCal = targetDay.getAsCalendar(tz);
			
			for (AgencyAndId sid : calendarService.getServiceIdsOnDate(targetDay)){
				//System.out.println(targetDay.getAsCalendar(tz).getTime().toString() + " " +sid.toString());
				if (tripsPerDateHash.containsKey(targetDayAsCal)){
					tripCount = tripsPerDateHash.get(targetDayAsCal);
				}
				if (tripsPerServHash.containsKey(sid)){
					tripCount = tripCount + tripsPerServHash.get(sid).get();
				}
			}
			
//			System.out.println(targetDay.getAsCalendar(tz).getTime().toString() + " " +  tripCount);
			
			tripsPerDateHash.put(targetDay.getAsCalendar(tz), tripCount);
			start.add(Calendar.DATE, 1);
		}

		return tripsPerDateHash;
	}

	public TreeMap<Calendar, ArrayList<AgencyAndId>> getServiceIdsForDates(){
		TreeMap<Calendar, ArrayList<AgencyAndId>> serviceIdsForDates = new TreeMap<Calendar, ArrayList<AgencyAndId>>();

		start.setTime(from.getAsDate(tz));
		end.setTime(to.getAsDate(tz));
		
		Collection<ServiceCalendarDate> allCalendarDates = gtfsMDao.getAllCalendarDates();
		ConcurrentHashMap<ServiceDate, ArrayList<AgencyAndId>> dateAdditions = getCalendarDateAdditions(allCalendarDates);
		ConcurrentHashMap<ServiceDate, ArrayList<AgencyAndId>> dateRemovals = getCalendarDateRemovals(allCalendarDates);
		
		while(!start.after(end)){

			ArrayList<AgencyAndId> serviceIdsForTargetDay = new ArrayList<AgencyAndId>();

			ServiceDate targetDay = new ServiceDate(start);

			calendarService.getServiceIdsOnDate(targetDay).forEach(sid -> serviceIdsForTargetDay.add(sid));
			
			dateAdditions.getOrDefault(targetDay, new ArrayList<AgencyAndId>()).forEach(sid -> serviceIdsForTargetDay.add(sid));
			
			dateRemovals.getOrDefault(targetDay, new ArrayList<AgencyAndId>()).forEach(sid -> serviceIdsForTargetDay.remove(sid));
		

			serviceIdsForDates.put(targetDay.getAsCalendar(tz), serviceIdsForTargetDay);
			start.add(Calendar.DATE, 1);
		}
		return serviceIdsForDates;

	}
	
	public HashSet<AgencyAndId> getActiveServiceIdsOnly(){
		return getServiceIdsForDates().values().stream().collect(HashSet::new, HashSet::addAll, HashSet::addAll);
	}

	public ArrayList<Calendar> getDatesWithNoTrips(){
		ArrayList<Calendar> datesWithNoTrips = new ArrayList<Calendar>();
		TreeMap<Calendar, Integer> tc = getTripCountForDates();
		for(Map.Entry<Calendar, Integer> d: tc.entrySet()){
			if (d.getValue()==0){
				datesWithNoTrips.add(d.getKey());
			}
		}
		return datesWithNoTrips;
	}

	//I got 99 problems, and a calendar is one
	public ValidationResult getCalendarProblems(){
		SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
		
		ValidationResult vr = new ValidationResult();
		ArrayList<Calendar> datesWithNoTrips = getDatesWithNoTrips();
		for (Calendar d: datesWithNoTrips){
			String dateFormatted = fmt.format(d.getTime());
			InvalidValue iv = new InvalidValue("calendar", "service_id", dateFormatted, "NoServiceOnThisDate",
					"There is no service on " + dateFormatted, null, Priority.HIGH);
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
	
	public String getTripDataForEveryDay(){
		StringBuilder s = new StringBuilder();
		ServiceIdHelper helper = new ServiceIdHelper();
		SimpleDateFormat df = new SimpleDateFormat("E, yyyy-MM-dd");
		Calendar yesterday = Calendar.getInstance();
		yesterday.add(Calendar.DAY_OF_MONTH, -1);
				
		TreeMap<Calendar, Integer> tc = getTripCountForDates();
		for(Calendar d: tc.keySet()){
			if (d.before(yesterday)){
				continue;
			}
			s.append("\n#### " + df.format(d.getTime()));
			s.append("\n number of trips on this day: " + tc.get(d));

			ArrayList<AgencyAndId> aid = getServiceIdsForDates().get(d);
			Collections.sort(aid);
			for (AgencyAndId sid : aid){
				s.append("\n" + helper.getHumanReadableCalendarFromServiceId(sid.toString()));
			}
			
		}
		return s.toString();
	}
	public TimeZone getTz() {
		return tz;
	}
	public void setTz(TimeZone tz) {
		CalendarDateVerificationService.tz = tz;
	}
	
	private ConcurrentHashMap<ServiceDate, ArrayList<AgencyAndId>> getCalendarDateAdditions(Collection<ServiceCalendarDate> allCalendarDates){
		ConcurrentHashMap<ServiceDate, ArrayList<AgencyAndId>> calDateMap = new ConcurrentHashMap<>();
		allCalendarDates.stream().filter(d -> d.getExceptionType() ==1)
				.forEach(d -> {
					calDateMap.computeIfAbsent(d.getDate(), k-> new ArrayList<AgencyAndId>()).add(d.getServiceId());
				});;	
		
		return calDateMap;
	}
	
	private ConcurrentHashMap<ServiceDate, ArrayList<AgencyAndId>> getCalendarDateRemovals(Collection<ServiceCalendarDate> allCalendarDates){
		ConcurrentHashMap<ServiceDate, ArrayList<AgencyAndId>> calDateMap = new ConcurrentHashMap<>();
		allCalendarDates.stream().filter(d -> d.getExceptionType() ==2)
				.forEach(d -> {
					calDateMap.computeIfAbsent(d.getDate(), k-> new ArrayList<AgencyAndId>()).add(d.getServiceId());
				});;	
		
		return calDateMap;
	}
	


}
