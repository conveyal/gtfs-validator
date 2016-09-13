package com.conveyal.gtfs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;

import org.junit.BeforeClass;
import org.junit.Test;
import org.onebusaway.gtfs.impl.GtfsDaoImpl;
import org.onebusaway.gtfs.impl.GtfsRelationalDaoImpl;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.serialization.GtfsReader;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;

import com.conveyal.gtfs.service.CalendarDateVerificationService;
import com.conveyal.gtfs.service.impl.GtfsStatisticsService;

public class MultipleTimeZoneTests {
	static GtfsMutableRelationalDao gtfsMDao = null;
	static GtfsDaoImpl gtfsDao = null;
	static GtfsStatisticsService gtfsStats = null;
	static CalendarDateVerificationService cdvs = null;
	static HashMap<AgencyAndId, Integer> tripCounts = null;
	static Date calStart = null;
	static Date calEnd = null;

	@BeforeClass 
	public static void setUpClass() {      
		System.out.println("GtfsStatisticsTest setup");

		GtfsReader reader = new GtfsReader();
		gtfsMDao = new GtfsRelationalDaoImpl();

		File gtfsFile = new File("src/test/resources/gtfs_two_agencies.zip");

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
				
	}

	@Test(expected=IllegalArgumentException.class)
	public void shouldThrowOnMultipleTimeZones() {
		cdvs = new CalendarDateVerificationService(gtfsMDao);
	}
}
