package com.conveyal.gtfs.validator;

import java.io.File;
import java.io.IOException;

import org.onebusaway.gtfs.impl.GtfsRelationalDaoImpl;
import org.onebusaway.gtfs.serialization.GtfsReader;

import com.conveyal.gtfs.model.InvalidValue;
import com.conveyal.gtfs.model.ValidationResult;
import com.conveyal.gtfs.service.GtfsValidationService;

/**
 * Provides a main class for running the GTFS validator.
 * @author mattwigway
 */
public class ValidatorMain {

	public static void main(String[] args) {
		if (args.length != 1) {
			System.err.println("Usage: gtfs-validator /path/to/gtfs.zip");
			return;
		}
		
		File inputGtfs = new File(args[0]);
		
		System.err.println("Reading GTFS from " + inputGtfs.getPath());
		
		GtfsRelationalDaoImpl dao = new GtfsRelationalDaoImpl();
		GtfsReader reader = new GtfsReader();
		
		try {
			reader.setInputLocation(inputGtfs);
			reader.setEntityStore(dao);
			reader.run();
		} catch (IOException e) {
			System.err.println("Could not read file " + inputGtfs.getPath() +
					"; does it exist and is it readable?");
			return;
		}

		System.err.println("Read GTFS");
		
		GtfsValidationService validationService = new GtfsValidationService(dao);
		
		System.err.println("Validating routes");
		ValidationResult routes = validationService.validateRoutes();
		
		System.err.println("Validating trips");
		ValidationResult trips = validationService.validateTrips();
		
		System.err.println("Checking for duplicate stops");
		ValidationResult stops = validationService.duplicateStops();
		
		System.err.println("Checking for reversed trip shapes");
		ValidationResult shapes = validationService.listReversedTripShapes();
		
		System.err.println("Validation complete");
		
		// Make the report
		
		System.out.println("## Validation Results");
		System.out.println("Routes:" + getValidationSummary(routes));
		System.out.println("Trips: " + getValidationSummary(trips));
		System.out.println("Stops: " + getValidationSummary(stops));
		System.out.println("Shapes: " + getValidationSummary(shapes));
		
		System.out.println("\n### Routes");
		System.out.println(getValidationReport(routes));
		// no need for another line feed here to separate them, as one is added by getValidationReport and another by
		// System.out.println
		
		System.out.println("\n### Trips");
		System.out.println(getValidationReport(trips));
		
		System.out.println("\n### Stops");
		System.out.println(getValidationReport(stops));
		
		System.out.println("\n### Shapes");
		System.out.println(getValidationReport(shapes));
	}
	
	/**
	 * Return a single-line summary of a ValidationResult
	 */
	public static String getValidationSummary(ValidationResult result) {
		return result.invalidValues.size() + " errors/warnings";
	}
	
	/**
	 * Return a human-readable, markdown-formatted multiline exhaustive report on a ValidationResult.
	 */
	public static String getValidationReport(ValidationResult result) {
		if (result.invalidValues.size() == 0)
			return "Hooray! No errors here (at least, none that we could find).\n";
		
		StringBuilder sb = new StringBuilder(1024);
		
		// loop over each invalid value, and take advantage of InvalidValue.toString to create a line about the error
		for (InvalidValue v : result.invalidValues) {
			sb.append("- ");
			sb.append(v.toString());
			sb.append('\n');
		}
		
		return sb.toString();
	}

}
