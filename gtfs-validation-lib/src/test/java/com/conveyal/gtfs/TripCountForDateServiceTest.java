package com.conveyal.gtfs;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
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

import com.conveyal.gtfs.service.TripCountForDateService;
import com.conveyal.gtfs.service.impl.GtfsStatisticsService;

public class TripCountForDateServiceTest {
	static GtfsMutableRelationalDao gtfsMDao = null;
	static GtfsDaoImpl gtfsDao = null;
	static GtfsStatisticsService gtfsStats = null;
	static TripCountForDateService tcfds = null;
	@BeforeClass 
    public static void setUpClass() {      
        System.out.println("GtfsStatisticsTest setup");
		
		GtfsReader reader = new GtfsReader();
		gtfsDao = new GtfsDaoImpl();  
				               
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
        
    	reader.setEntityStore(gtfsDao);
    	System.out.println(reader.getEntityStore());

    	try {
			reader.run();
		} catch (IOException e) {
			e.printStackTrace();
		}
    	
    	gtfsStats = new GtfsStatisticsService(gtfsMDao);
    	tcfds = new TripCountForDateService(gtfsMDao);
    }

	@Test
	public void tripCountForServiceId(){
		System.out.println("Trips per Service ID: " + tcfds.getTripCountForServiceIDs().toString());
		int sundayTrips = tcfds.getTripCountForServiceIDs().get(AgencyAndId.convertFromString("SoundTransit_SU"));
		Assert.assertEquals(sundayTrips,75);
	}
	@Test
	public void tripCountForDate(){
		System.out.println("Trips per Service Date");
		Date d = new Date(1392575866000L);
		int firstSunday = tcfds.getTripCountForDates().get(d);
		Assert.assertEquals(firstSunday, 75);
		
	}
}
