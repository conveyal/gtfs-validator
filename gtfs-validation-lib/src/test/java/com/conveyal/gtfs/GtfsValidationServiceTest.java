package com.conveyal.gtfs;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;
import org.onebusaway.csv_entities.exceptions.CsvEntityIOException;
import org.onebusaway.csv_entities.exceptions.MissingRequiredFieldException;
import org.onebusaway.gtfs.impl.GtfsDaoImpl;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.serialization.GtfsReader;

import com.conveyal.gtfs.model.DuplicateStops;
import com.conveyal.gtfs.model.ValidationResult;
import com.conveyal.gtfs.service.GtfsValidationService;
import com.conveyal.gtfs.service.InputOutOfRange;
import com.conveyal.gtfs.service.impl.GtfsStatisticsService;
 
public class GtfsValidationServiceTest {
 
	static GtfsDaoImpl gtfsStore1 = null;
	static GtfsDaoImpl gtfsStore2 = null;
	
	static GtfsValidationService gtfsValidation1 = null;
	static GtfsValidationService gtfsValidation2 = null;
	
	static MissingRequiredFieldException mrf = null;
	
	@BeforeClass 
    public static void setUpClass() {      
        System.out.println("GtfsStatisticsTest setup");
        
        gtfsStore1 = new GtfsDaoImpl();
        gtfsStore2 = new GtfsDaoImpl();
        
        GtfsReader gtfsReader1 = new GtfsReader();
        GtfsReader gtfsReader2 = new GtfsReader();
        
        File gtfsFile1 = new File("src/test/resources/test_gtfs1.zip");
        File gtfsFile2 = new File("src/test/resources/test_gtfs2.zip");

        
        try {
			
        	gtfsReader1.setInputLocation(gtfsFile1);
        	gtfsReader2.setInputLocation(gtfsFile2);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
        
    	gtfsReader1.setEntityStore(gtfsStore1);
    	gtfsReader2.setEntityStore(gtfsStore2);
    	
    	try {
    		gtfsReader1.run();
    		gtfsReader2.run();
		} catch (Exception e) {
			e.printStackTrace();
		}
    	
    	try {
    		gtfsValidation1 = new GtfsValidationService(gtfsStore1);
    		gtfsValidation2 = new GtfsValidationService(gtfsStore2);
		} catch (Exception e) {
			e.printStackTrace();
		}
	
	}
	
	@Test
	public void validateRoutes() {
		ValidationResult result = gtfsValidation2.validateRoutes();

		Assert.assertEquals(result.invalidValues.size(), 6);
		
	}
	
	@Test
	public void validateTrips() {
		ValidationResult result = gtfsValidation2.validateTrips();

		Assert.assertEquals(result.invalidValues.size(), 10);
		
	}
	
	@Test
	public void duplicateStops() {
		ValidationResult result = new ValidationResult();
		
		result = gtfsValidation1.duplicateStops();
		Assert.assertEquals(result.invalidValues.size(), 0);
		

		// try duplicate stop test to confirm that stops within the buffer limit are found
		result = gtfsValidation1.duplicateStops(25.0);
		Assert.assertEquals(result.invalidValues.size(), 1);
		
		// try same test to confirm that buffers below the limit don't detect duplicates
		result = gtfsValidation1.duplicateStops(5.0);
		Assert.assertEquals(result.invalidValues.size(), 0);
	}
 
	
	@Test
	public void reversedTripShapes() {
		
		ValidationResult result = gtfsValidation1.listReversedTripShapes();
		
		Assert.assertEquals(result.invalidValues.size(), 1);
		
		// try again with an unsually high distanceMultiplier value 
		result = gtfsValidation1.listReversedTripShapes(100.0);
		
		Assert.assertEquals(result.invalidValues.size(), 0);
		
	}
	
	@Test
	public void completeBadGtfsTest() {
		
		GtfsDaoImpl gtfsStore = new GtfsDaoImpl();
      
        GtfsReader gtfsReader = new GtfsReader();
        
        File gtfsFile = new File("src/test/resources/st_gtfs_bad.zip");
        
        try {
			
        	gtfsReader.setInputLocation(gtfsFile);
        		
		} catch (IOException e) {
			e.printStackTrace();
		}
        
    	gtfsReader.setEntityStore(gtfsStore);
    	 
    	
    	try {
    		gtfsReader.run();
		} catch (Exception e) {
			e.printStackTrace();
		}
    	
    	try {
    		GtfsValidationService gtfsValidation = new GtfsValidationService(gtfsStore);
    		
    		ValidationResult results = gtfsValidation.validateRoutes();
    		results.add(gtfsValidation.validateTrips());
    		
    		Assert.assertEquals(results.invalidValues.size(), 1);
    		
    		System.out.println(results.invalidValues.size());
    		
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	@Test
	public void completeGoodGtfsTest() {
		
		GtfsDaoImpl gtfsStore = new GtfsDaoImpl();
      
        GtfsReader gtfsReader = new GtfsReader();
        
        File gtfsFile = new File("src/test/resources/st_gtfs_good.zip");
        
        try {
			
        	gtfsReader.setInputLocation(gtfsFile);
        		
		} catch (IOException e) {
			e.printStackTrace();
		}
        
    	gtfsReader.setEntityStore(gtfsStore);
    	 
    	
    	try {
    		gtfsReader.run();
		} catch (Exception e) {
			e.printStackTrace();
		}
    	
    	try {
    		GtfsValidationService gtfsValidation = new GtfsValidationService(gtfsStore);
    		
    		ValidationResult results = gtfsValidation.validateRoutes();
    		results.add(gtfsValidation.validateTrips());
    		
    		Assert.assertEquals(results.invalidValues.size(), 0);
    		
    		System.out.println(results.invalidValues.size());
    		
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	
	
}
