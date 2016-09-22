package com.conveyal.gtfs.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import org.onebusaway.gtfs.impl.calendar.CalendarServiceDataFactoryImpl;
import org.onebusaway.gtfs.model.Agency;
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
	private static TimeZone tz = null;
	private static ServiceDate from;
	private static ServiceDate to;
	private static String aid = null;

	public CalendarDateVerificationService(GtfsMutableRelationalDao gmd){
		gtfsMDao = gmd;
		stats = new GtfsStatisticsService(gmd);
		calendarService = CalendarServiceDataFactoryImpl.createService(gmd);

		start = Calendar.getInstance();
		end = Calendar.getInstance();
		
		from = new ServiceDate(stats.getCalendarServiceRangeStart());
		to = new ServiceDate(stats.getCalendarServiceRangeEnd());

		Collection<Agency> agencies = stats.getAllAgencies();

		//Do you know how many time zones there are in the Soviet Union?
		if (agencies.size() == 1){
			Agency a = agencies.iterator().next();
			aid = a.getId();
		}
		else { // check if all agencies have the same tz
			ConcurrentMap<String, AtomicLong> timeZones = new ConcurrentHashMap<String, AtomicLong>();
			for (Agency a : agencies){
				//concurrent map FTW!
				timeZones.putIfAbsent(a.getId(), new AtomicLong(0));
				timeZones.get(a.getId()).incrementAndGet();
			}
			if (timeZones.size() == 1){
				Agency a = agencies.iterator().next();
				aid = a.getId();
			}
		}

		if (aid != null ){
			tz = calendarService.getTimeZoneForAgencyId(aid);
		}
		else { 
			throw new IllegalArgumentException("File contains two time zones, which is not allowed by the GTFS spec");
		}
		
		start.setTimeZone(tz);
		end.setTimeZone(tz);

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
/*
 * @return a TreeMap (sorted by calendar) with the number of trips per day.  
 */
	public TreeMap<Calendar, Integer> getTripCountForDates() {

		HashMap<AgencyAndId, Integer> tripsPerServHash = getTripCountsForAllServiceIDs();
		TreeMap<Calendar, Integer> tripsPerDateHash = new TreeMap<Calendar, Integer>();
		System.out.println(from.getAsDate(tz).toString());
		System.out.println(tz.getID());
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
					tripCount = tripCount + tripsPerServHash.get(sid);
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

		while(!start.after(end)){

			ArrayList<AgencyAndId> serviceIdsForTargetDay = new ArrayList<AgencyAndId>();

			ServiceDate targetDay = new ServiceDate(start);

			for (AgencyAndId sid : calendarService.getServiceIdsOnDate(targetDay)){
				serviceIdsForTargetDay.add(sid);
				//				System.out.println(start.getTime().toString() + sid.getId());
			}
			for (ServiceCalendarDate serviceCalendar : gtfsMDao.getAllCalendarDates()) {
				//System.out.println("cal: " + serviceCalendar + " ex " + serviceCalendar.getExceptionType());
				if (serviceCalendar.getDate() == targetDay && serviceCalendar.getExceptionType() == 1){
					AgencyAndId sid = serviceCalendar.getServiceId();
					serviceIdsForTargetDay.add(sid);
				}
				if (serviceCalendar.getDate() == targetDay && serviceCalendar.getExceptionType() == 2){
					AgencyAndId sid = serviceCalendar.getServiceId();
					serviceIdsForTargetDay.remove(sid);
				}
			}

			serviceIdsForDates.put(targetDay.getAsCalendar(tz), serviceIdsForTargetDay);
			start.add(Calendar.DATE, 1);
		}
		return serviceIdsForDates;

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
			InvalidValue iv = new InvalidValue("calendar", "service_id", dateFormatted, "NoServiceOnThisDate", "There is no service on " + dateFormatted, null, Priority.HIGH);
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
		Calendar today = Calendar.getInstance();
		
		TreeMap<Calendar, Integer> tc = getTripCountForDates();
		for(Calendar d: tc.keySet()){
			if (d.before(today)){
				continue;
			}
			s.append("\n#### " + df.format(d.getTime()));
			s.append("\n number of trips on this day: " + tc.get(d));
			s.append("\n has the following Services active");
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

}
