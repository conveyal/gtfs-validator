package com.conveyal.gtfs;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;
import org.onebusaway.gtfs.impl.GtfsDaoImpl;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.serialization.GtfsReader;

import com.conveyal.gtfs.service.impl.GtfsStatisticsService;
 
public class GtfsStatisticsServiceTest {
 
	static GtfsDaoImpl store = null;
	static GtfsStatisticsService gtfsStats = null;
	
	@BeforeClass 
    public static void setUpClass() {      
        System.out.println("GtfsStatisticsTest setup");
        
        store = new GtfsDaoImpl();
        GtfsReader reader = new GtfsReader();
        
        File gtfsFile = new File("src/test/resources/st_gtfs_good.zip");
        
        try {
			reader.setInputLocation(gtfsFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
        
        try {
			reader.setInputLocation(gtfsFile);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
        
    	reader.setEntityStore(store);

    	try {
			reader.run();
		} catch (IOException e) {
			e.printStackTrace();
		}
    	
    	gtfsStats = new GtfsStatisticsService(store);
    }
	
	@Test
	public void agencyCount() {
		System.out.println("Agency count: " + gtfsStats.getAgencyCount());
		Assert.assertEquals(gtfsStats.getAgencyCount(), new Integer(1));
	}
 
	@Test
	public void routeCount() {
		System.out.println("Route count: " + gtfsStats.getRouteCount());
		Assert.assertEquals(gtfsStats.getRouteCount(), new Integer(16));
	}
	
	@Test
	public void tripCount() {
		System.out.println("Trip count: " + gtfsStats.getTripCount());
		Assert.assertEquals(gtfsStats.getTripCount(), new Integer(559));
	}
 
	@Test
	public void stopCount() {
		System.out.println("Stop count: " + gtfsStats.getStopCount());
		Assert.assertEquals(gtfsStats.getStopCount(), new Integer(93));
	}
	
	@Test
	public void stopTimeCount() {
		System.out.println("Stop time count: " + gtfsStats.getStopTimesCount());
		Assert.assertEquals(gtfsStats.getStopTimesCount(), new Integer(7345));
	}
	
	@Test
	public void calendarDateRangeStart() {
		System.out.println("Calendar date start: " + gtfsStats.getCalendarDateStart().getTime());
		Assert.assertEquals(gtfsStats.getCalendarDateStart(), new Date(1401076800000l));
	}
	
	@Test
	public void calendarDateRangeEnd() {
		System.out.println("Calendar date end: " + gtfsStats.getCalendarDateEnd().getTime());
		Assert.assertEquals(gtfsStats.getCalendarDateEnd(), new Date(1401076800000l));
	}
	
	@Test
	public void tripCountForServiceId(){
		System.out.println("Trips per Service ID: " + gtfsStats.getTripCountForServiceIDs().toString());
		int sundayTrips = gtfsStats.getTripCountForServiceIDs().get(AgencyAndId.convertFromString("SoundTransit_SU"));
		Assert.assertEquals(sundayTrips,75);
	}
	
}

