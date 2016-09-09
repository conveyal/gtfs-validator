package com.conveyal.gtfs;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;
import org.onebusaway.gtfs.impl.GtfsDaoImpl;
import org.onebusaway.gtfs.serialization.GtfsReader;

import com.conveyal.gtfs.service.impl.GtfsStatisticsService;

public class GtfsStatisticsServiceCalendarDatesTest {
	
	static GtfsDaoImpl store = null;
	static GtfsStatisticsService gtfsStats = null;

	@BeforeClass
	public static void setUp() throws Exception {
       
        store = new GtfsDaoImpl();
        GtfsReader reader = new GtfsReader();
        
        File gtfsFile = new File("src/test/resources/test_gtfs1.zip");
        
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
	public void test() {
		assertNotNull(gtfsStats.getCalendarDateStart());
	}

}
