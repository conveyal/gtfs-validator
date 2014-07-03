package com.conveyal.gtfs.service;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Collection;
import java.util.Set;

import org.onebusaway.gtfs.impl.calendar.CalendarServiceDataFactoryImpl;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.ServiceCalendar;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.gtfs.services.GtfsDao;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs.services.calendar.CalendarService;

import com.conveyal.gtfs.model.Statistic;
import com.conveyal.gtfs.service.impl.GtfsStatisticsService;


public class TripCountForDateService {

	private static GtfsMutableRelationalDao gtfsMDao = null;
	private GtfsStatisticsService stats = null;
	private CalendarService calendarService = null;


	public TripCountForDateService(GtfsMutableRelationalDao gmd){
		gtfsMDao = gmd;
		stats = new GtfsStatisticsService(gmd);
		CalendarServiceDataFactoryImpl factory = new CalendarServiceDataFactoryImpl();
		factory.setGtfsDao(gmd);
		calendarService = CalendarServiceDataFactoryImpl.createService(gmd);
		

	}
	public HashMap<AgencyAndId, Integer> getTripCountForServiceIDs() {
		HashMap<AgencyAndId, Integer> tripsPerCalHash = new HashMap<AgencyAndId, Integer>();
		for (ServiceCalendar serviceCalendar : gtfsMDao.getAllCalendars()) {
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

		HashMap<AgencyAndId, Integer> tripsPerServHash = getTripCountForServiceIDs();
		HashMap<Date, Integer> tripsPerDateHash = new HashMap<Date, Integer>();

		Date from = stats.getCalendarServiceRangeStart();
		Date to = stats.getCalendarServiceRangeEnd();

		Calendar start = Calendar.getInstance();
		start.setTime(from);
		Calendar end = Calendar.getInstance();
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
					tripsPerServHash.put(sid, tripCount);
					System.out.println(targetDay + " " + tripCount);
				}
				
			}
			tripsPerDateHash.put(targetDay.getAsDate(), tripCount);
			start.add(Calendar.DATE, 1);
		}

		return tripsPerDateHash;

	}
	public static String formatTripCountForServiceIDs(TripCountForDateService t){
		return Arrays.toString(t.getTripCountForServiceIDs().entrySet().toArray());
	}
	
}
