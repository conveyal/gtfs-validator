
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
import org.onebusaway.gtfs.serialization.GtfsReader;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public class CalendarDateVerificationServiceLarge extends UnitTestBaseUtil{
	static GtfsRelationalDaoImpl gtfsMDao = null;
	static GtfsDaoImpl gtfsDao = null;
	static GtfsStatisticsService gtfsStats = null;
	static CalendarDateVerificationService cdvs = null;
	static ConcurrentHashMap<AgencyAndId, AtomicInteger> tripCounts = null;
	static Date calStart = null;
	static Date calEnd = null;

	@BeforeClass 
	public static void setUpClass() {      
		GtfsReader reader = new GtfsReader();
		gtfsMDao = new GtfsRelationalDaoImpl();

		File gtfsFile = new File("src/test/resources/brooklyn-a6-small.zip");

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
		calStart = gtfsStats.getCalendarServiceRangeStart();
		calEnd = gtfsStats.getCalendarServiceRangeEnd();
	}
	
	@Before
	public void SetUp(){
		setDummyPrintStream();
	}

	@Test 
	public void countOfServiceIdsInCalendarAndCalendarDates(){
		int serviceIdCount = tripCounts.size();
		Assert.assertEquals(19, serviceIdCount);
	}

	@Test
	public void feedCalendarExtents(){
		assertEquals("start incorrect", "Sun Jan 03 00:00:00 EST 2016", calStart.toString());
		assertEquals("end incorrect", "Sat Apr 02 00:00:00 EDT 2016", calEnd.toString());

	}
	
	
	//Test for a bug with OneBusAway regarding DST. 
	//
	//@Test
	public void somethingIsUpWithMarch13(){
		
		TreeMap<Calendar, Integer> tripCounts= cdvs.getTripCountForDates();
		
		Date mar13d = new Date(1457856000000L);
		Calendar mar13 = new GregorianCalendar();
		mar13.setTimeZone(TimeZone.getTimeZone("America/New_York"));
		mar13.setTime(mar13d);
		
		TreeMap<Calendar, TreeSet<AgencyAndId>> dates = cdvs.getServiceIdsForDates();
		
		TreeSet<AgencyAndId> serviceforMar13 = dates.get(mar13);
		
		assertTrue(serviceforMar13.size() > 0);
			
		String message = mar13.getTime().toString() + " is not present";
		assertTrue(message, tripCounts.containsKey(mar13));
		
		Integer mar13Trips = tripCounts.get(mar13);
		
		assertTrue("0 trips", mar13Trips > 0);
		
	}
// also fails with DST bug. 
//	@Test
	public void tripEveryDay(){
		Calendar aDay = new GregorianCalendar();
		aDay.setTime(calStart);
		
		 TreeMap<Calendar, Integer> tripCountForDates = cdvs.getTripCountForDates();
		
		while (aDay.getTime().compareTo(calEnd) < 0){
			
		
			int todaysTrips = 0;
			try {
				todaysTrips = tripCountForDates.get(aDay.getTime());
			} catch (Exception e) {
				String message = aDay.getTime().toString() + " not present";
				assertTrue(message, false);
			}
		
			assertNotNull(todaysTrips);
			assertTrue(todaysTrips > 0);
			
			aDay.add(Calendar.DATE, 1);
		}
	}

}
