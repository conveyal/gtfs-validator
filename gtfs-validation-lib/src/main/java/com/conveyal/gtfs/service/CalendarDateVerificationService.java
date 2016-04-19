package com.conveyal.gtfs.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
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
		else { // fall back to UTC
			System.err.println("DANGER! unsafe use of UTC timezone");
			tz = TimeZone.getTimeZone("UTC");
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

	public HashMap<Calendar, Integer> getTripCountForDates() {

		HashMap<AgencyAndId, Integer> tripsPerServHash = getTripCountsForAllServiceIDs();
		HashMap<Calendar, Integer> tripsPerDateHash = new HashMap<Calendar, Integer>();

		start.setTime(from.getAsDate(tz));
		
		end.setTime(to.getAsDate(tz));

		while(start.before(end)){
			Integer tripCount =0;
			ServiceDate targetDay = new ServiceDate(start);
			
			for (AgencyAndId sid : calendarService.getServiceIdsOnDate(targetDay)){
				//System.out.println(targetDay.getAsCalendar(tz).getTime().toString() + " " +sid.toString());
				if (tripsPerDateHash.containsKey(targetDay)){
					tripCount = tripsPerDateHash.get(targetDay);
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

	public HashMap<Calendar, ArrayList<AgencyAndId>> getServiceIdsForDates(){
		HashMap<Calendar, ArrayList<AgencyAndId>> serviceIdsForDates = new HashMap<Calendar, ArrayList<AgencyAndId>>();

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
					//System.out.println(serviceCalendar + sid.toString());
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
		HashMap<Calendar, Integer> tc = getTripCountForDates();
		for(Map.Entry<Calendar, Integer> d: tc.entrySet()){
			if (d.getValue()==0){
				datesWithNoTrips.add(d.getKey());
			}
		}
		return datesWithNoTrips;
	}

	//I got 99 problems, and a calendar is one
	public ValidationResult getCalendarProblems(){
		ValidationResult vr = new ValidationResult();
		ArrayList<Calendar> datesWithNoTrips = getDatesWithNoTrips();
		for (Calendar d: datesWithNoTrips){
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
	public TimeZone getTz() {
		return tz;
	}
	public void setTz(TimeZone tz) {
		CalendarDateVerificationService.tz = tz;
	}

}
