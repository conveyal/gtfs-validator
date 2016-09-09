package com.conveyal.gtfs;

import java.io.File;
import java.io.IOException;
import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;
import org.onebusaway.csv_entities.exceptions.MissingRequiredFieldException;
import org.onebusaway.gtfs.impl.GtfsDaoImpl;
import org.onebusaway.gtfs.impl.GtfsRelationalDaoImpl;
import org.onebusaway.gtfs.serialization.GtfsReader;

import com.conveyal.gtfs.model.ValidationResult;
import com.conveyal.gtfs.service.GtfsValidationService;
import com.conveyal.gtfs.validator.ValidatorMain;
 
public class GtfsValidationServiceTest {
 
	static GtfsRelationalDaoImpl gtfsStore1 = null;
	static GtfsRelationalDaoImpl gtfsStore2 = null;
	
	static GtfsValidationService gtfsValidation1 = null;
	static GtfsValidationService gtfsValidation2 = null;
	
	static MissingRequiredFieldException mrf = null;
	
	@BeforeClass 
    public static void setUpClass() {      
        System.out.println("GtfsStatisticsTest setup");
        
        gtfsStore1 = new GtfsRelationalDaoImpl();
        gtfsStore2 = new GtfsRelationalDaoImpl();
        
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
		
		Assert.assertEquals(5, result.invalidValues.size());
		
	}
	// Test originally did not pass as some trips got included twice. 
	@Test
	public void validateTrips() {
		ValidationResult result = gtfsValidation2.validateTrips();
		Assert.assertEquals(8,result.invalidValues.size());
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
		
		// try again with an unusually high distanceMultiplier value 
		result = gtfsValidation1.listReversedTripShapes(50000.0);
		
		Assert.assertEquals(result.invalidValues.size(), 0);
		
	}
	
	@Test
	public void completeBadGtfsTest() {
		
		GtfsRelationalDaoImpl gtfsStore = new GtfsRelationalDaoImpl();
      
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
    		results.append(gtfsValidation.validateTrips());
    		
    		Assert.assertEquals(results.invalidValues.size(), 5);
    		
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	@Test
	public void completeGoodGtfsTest() {
		
		GtfsRelationalDaoImpl gtfsStore = new GtfsRelationalDaoImpl();
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
    		results.append(gtfsValidation.validateTrips());
    		
    		Assert.assertEquals(results.invalidValues.size(), 0);
    		
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	
	
}
