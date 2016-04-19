package com.conveyal.gtfs;


import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Set;
import java.util.TimeZone;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;
import org.onebusaway.gtfs.impl.GtfsDaoImpl;
import org.onebusaway.gtfs.impl.GtfsRelationalDaoImpl;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.ServiceCalendarDate;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.gtfs.serialization.GtfsReader;
import org.onebusaway.gtfs.services.GtfsDao;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs.services.GtfsRelationalDao;

import com.conveyal.gtfs.service.CalendarDateVerificationService;
import com.conveyal.gtfs.service.impl.GtfsStatisticsService;

public class CalendarDateVerificationServiceTest {
	static GtfsMutableRelationalDao gtfsMDao = null;
	static GtfsDaoImpl gtfsDao = null;
	static GtfsStatisticsService gtfsStats = null;
	static CalendarDateVerificationService cdvs = null;
	static HashMap<AgencyAndId, Integer> tripCounts = null;
	
	
	@BeforeClass 
    public static void setUpClass() {      
        System.out.println("GtfsStatisticsTest setup");
		
		GtfsReader reader = new GtfsReader();
		gtfsMDao = new GtfsRelationalDaoImpl();
				               
        File gtfsFile = new File("src/test/resources/nyc_gtfs_si.zip");
        
        try {
			reader.setInputLocation(gtfsFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
        
    	reader.setEntityStore(gtfsMDao);

    	try {
			reader.run();
		} catch (IOException e) {
			e.printStackTrace();
		}
    	
    	gtfsStats = new GtfsStatisticsService(gtfsMDao);
    	cdvs = new CalendarDateVerificationService(gtfsMDao);
    	
    	tripCounts = cdvs.getTripCountsForAllServiceIDs();
    }

	@Test
	public void tripCountForServiceId(){
		System.out.println("Trips per Service ID: " + cdvs.getTripCountsForAllServiceIDs().toString());
		int sundayTrips = tripCounts.get(AgencyAndId.convertFromString("MTA NYCT_YU_A5-Sunday"));
		Assert.assertEquals(sundayTrips,110);
	}
	@Test 
	public void countOfServiceIdsInCalendarAndCalendarDates(){
		int serviceIdCount = tripCounts.size();
		Assert.assertEquals(9, serviceIdCount);
	}
	
	@Test
	public void tripCountForDateWithMultipleServiceIDs(){
		System.out.println("Trips per Service Date on date with Multiple Calendar Entries");
		Date d = new Date(1427947200000L);
		Calendar c = new GregorianCalendar();
		c.setTime(d);
		
		
		System.out.println(cdvs.getServiceIdsForDates().get(c));
		int regWeekday = cdvs.getTripCountForDates().get(c);
		Assert.assertEquals(regWeekday, 191);
		}
	
	@Test
	public void tripCountOnHoliday(){
		System.out.println("Trips per Service Date on a holiday (only using calendar_dates)");
		Date day = new Date(1428033600000L);
		Calendar d = new GregorianCalendar();
		d.setTime(day);
		d.setTimeZone(cdvs.getTz());
		
		HashMap<Calendar, Integer> serviceMap = cdvs.getTripCountForDates();
		assert(serviceMap.size() > 0);
		
		assertTrue(serviceMap.containsKey(d));
		
		int goodFriday = serviceMap.get(d);
		assertEquals(goodFriday, 160);
		}

	@Test
	public void serviceIdsForDateWithMultipleServiceIDs(){
		System.out.println("Testing for multiple calendar dates entries");
		Date day = new Date(1427947200000L);
		Calendar d = new GregorianCalendar();
		d.setTime(day);
		d.setTimeZone(cdvs.getTz());
		
		ArrayList<AgencyAndId> idsOnWeekday = cdvs.getServiceIdsForDates().get(d);
		Assert.assertTrue(idsOnWeekday.size() > 1);
	}
	@Test
	public void serviceCalendarforCalendarDate(){
		Date d = new Date(1428033603000L);
		ServiceDate sd = new ServiceDate(d);
		Set<AgencyAndId> calendars = CalendarDateVerificationService.getCalendarsForDate(sd);
		Assert.assertEquals("MTA NYCT_YU_J5-Weekday".trim(), calendars.toArray()[0].toString().trim());
	}
	
	@Test
	public void timeZoneTest(){
		String tz = cdvs.getTz().getID();
		assertEquals("Time Zone not America/New_York", tz, "America/New_York");
	}
		
}
