package com.conveyal.gtfs;


import com.conveyal.gtfs.service.CalendarDateVerificationService;
import com.conveyal.gtfs.service.impl.GtfsStatisticsService;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.onebusaway.gtfs.impl.GtfsDaoImpl;
import org.onebusaway.gtfs.impl.GtfsRelationalDaoImpl;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.gtfs.serialization.GtfsReader;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CalendarDateVerificationServiceTest extends UnitTestBaseUtil {
	static GtfsRelationalDaoImpl gtfsMDao = null;
	static GtfsDaoImpl gtfsDao = null;
	static GtfsStatisticsService gtfsStats = null;
	static CalendarDateVerificationService cdvs = null;
	static ConcurrentHashMap<AgencyAndId, AtomicInteger> tripCounts = null;
	
	
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
	@Before
	public void SetUp(){
		setDummyPrintStream();
	}

	@Test
	public void tripCountForServiceId(){
		int sundayTrips = tripCounts.get(AgencyAndId.convertFromString("MTA NYCT_YU_A5-Sunday")).get();
		Assert.assertEquals(sundayTrips,110);
	}
	@Test 
	public void countOfServiceIdsInCalendarAndCalendarDates(){
		int serviceIdCount = tripCounts.size();
		Assert.assertEquals(9, serviceIdCount);
	}
	
	@Test
	public void tripCountForDateWithMultipleServiceIDs(){
		Date d = new Date(1427947200000L);
		Calendar c = new GregorianCalendar();
		c.setTime(d);
		
		int regWeekday = cdvs.getTripCountForDates().get(c);
		Assert.assertEquals(regWeekday, 191);
		}
	
	@Test
	public void tripCountOnHoliday(){
		
		Date day = new Date(1428033600000L);
		Calendar d = new GregorianCalendar();
		d.setTime(day);
		d.setTimeZone(cdvs.getTz());
		
		TreeMap<Calendar, Integer> serviceMap = cdvs.getTripCountForDates();
		assert(serviceMap.size() > 0);
		
		assertTrue(serviceMap.containsKey(d));
		
		int goodFriday = serviceMap.get(d);
		assertEquals(goodFriday, 160);
		}

	@Test
	public void serviceIdsForDateWithMultipleServiceIDs(){
		Date day = new Date(1427947200000L);
		Calendar d = new GregorianCalendar();
		d.setTime(day);
		d.setTimeZone(cdvs.getTz());
		
		TreeSet<AgencyAndId> idsOnWeekday = cdvs.getServiceIdsForDates().get(d);
		idsOnWeekday.forEach(t -> System.out.println(t.getId()));
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
