package com.conveyal.gtfs;

import java.io.File;
import java.util.HashSet;

import org.junit.BeforeClass;
import org.junit.Test;
import org.onebusaway.csv_entities.exceptions.MissingRequiredFieldException;
import org.onebusaway.gtfs.impl.GtfsRelationalDaoImpl;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.serialization.GtfsReader;

import com.conveyal.gtfs.model.ValidationResult;
import com.conveyal.gtfs.service.CalendarDateVerificationService;
import com.conveyal.gtfs.service.GtfsValidationService;

import junit.framework.Assert;

public class ValidateTripsWasFailingBlocksTest extends UnitTestBaseUtil {
	static GtfsValidationService gtfsValidation1 = null;
	static GtfsRelationalDaoImpl gtfsMDao = null;
	static MissingRequiredFieldException mrf = null;
	static CalendarDateVerificationService cdvs = null;
	
	@BeforeClass 
	public static void setUpClass() {     
		gtfsMDao = new GtfsRelationalDaoImpl();
		GtfsReader gtfsReader1 = new GtfsReader();
		
		try {

			File gtfsFile1 = new File("src/test/resources/GTFS_MTABC_A7_SCS.zip");
			gtfsReader1.setInputLocation(gtfsFile1);
			gtfsReader1.setEntityStore(gtfsMDao);
			gtfsReader1.run();

			gtfsValidation1 = new GtfsValidationService(gtfsMDao);
			 cdvs = new CalendarDateVerificationService(gtfsMDao);
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	@Test
	public void validateTrips() {
		ValidationResult result = gtfsValidation1.validateTrips();
		Assert.assertEquals(0,result.invalidValues.size());
	}
	
	@Test
	public void testListOfActiveServiceIds(){
		Assert.assertTrue(cdvs.getActiveServiceIdsOnly().size() == 1);
	}


}
