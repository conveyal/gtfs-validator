package com.conveyal.gtfs;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;
import org.onebusaway.gtfs.impl.GtfsDaoImpl;
import org.onebusaway.gtfs.impl.GtfsRelationalDaoImpl;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.serialization.GtfsReader;
import org.onebusaway.gtfs.services.GtfsDao;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs.services.GtfsRelationalDao;

import com.conveyal.gtfs.service.CalendarDateVerificationService;
import com.conveyal.gtfs.service.impl.GtfsStatisticsService;

public class TripCountForDateServiceTest {
	static GtfsMutableRelationalDao gtfsMDao = null;
	static GtfsDaoImpl gtfsDao = null;
	static GtfsStatisticsService gtfsStats = null;
	static CalendarDateVerificationService cdvs = null;
	@BeforeClass 
    public static void setUpClass() {      
        System.out.println("GtfsStatisticsTest setup");
		
		GtfsReader reader = new GtfsReader();
		gtfsMDao = new GtfsRelationalDaoImpl();
				               
        File gtfsFile = new File("src/test/resources/st_gtfs_dupservice.zip");
        
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
        
    	reader.setEntityStore(gtfsMDao);
    	System.out.println(reader.getEntityStore());

    	try {
			reader.run();
		} catch (IOException e) {
			e.printStackTrace();
		}
    	
    	gtfsStats = new GtfsStatisticsService(gtfsMDao);
    	cdvs = new CalendarDateVerificationService(gtfsMDao);
    }

	@Test
	public void tripCountForServiceId(){
		System.out.println("Trips per Service ID: " + cdvs.getTripCountsForAllServiceIDs().toString());
		int sundayTrips = cdvs.getTripCountsForAllServiceIDs().get(AgencyAndId.convertFromString("SoundTransit_SU"));
		Assert.assertEquals(sundayTrips,75);
	}
	
	@Test
	public void tripCountForDateWithMultipleServiceIDs(){
		System.out.println("Trips per Service Date on date with Multiple Calendar Date Entries");
		Date d = new Date(1392526800000L);
		int firstSunday = cdvs.getTripCountForDates().get(d);
		Assert.assertEquals(firstSunday, 421);
		}
	
	@Test
	public void tripCountForDateWithOneServiceID(){
		System.out.println("Trips per Service Date on date with One Calendar Entry");
		Date d = new Date(1393131600000L);
		int regularSunday = cdvs.getTripCountForDates().get(d);
		Assert.assertEquals(regularSunday, 75);
		}

	@Test
	public void serviceIdsForDateWithMultipleServiceIDs(){
		System.out.println("Testing for multiple calendar dates entries");
		Date d = new Date(1392526800000L);
		ArrayList<AgencyAndId> idsOnFirstSunday = cdvs.getServiceIdsForDate().get(d);
		Assert.assertTrue(idsOnFirstSunday.size() > 1);
	}
}
