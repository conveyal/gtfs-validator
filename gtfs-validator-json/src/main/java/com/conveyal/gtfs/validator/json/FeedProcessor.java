package com.conveyal.gtfs.validator.json;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;
import java.util.zip.ZipException;

import org.onebusaway.csv_entities.exceptions.CsvEntityIOException;
import org.onebusaway.csv_entities.exceptions.MissingRequiredFieldException;
import org.onebusaway.gtfs.impl.GtfsRelationalDaoImpl;
import org.onebusaway.gtfs.model.Agency;
import org.onebusaway.gtfs.serialization.GtfsReader;

import com.conveyal.gtfs.model.InvalidValue;
import com.conveyal.gtfs.service.GtfsValidationService;
import com.conveyal.gtfs.service.StatisticsService;
import com.conveyal.gtfs.service.impl.GtfsStatisticsService;

/**
 * Process a feed and return the validation results and the statistics.
 * @author mattwigway
 */
public class FeedProcessor {
	private File feed;
	private GtfsRelationalDaoImpl dao;
	private FeedValidationResult output;
	private static Logger _log = Logger.getLogger(FeedProcessor.class.getName());
	
	/**
	 * Create a feed processor for the given feed
	 * @param feed
	 */
	public FeedProcessor (File feed) {
		this.feed = feed;
		this.output = new FeedValidationResult();
	}
	
	/**
	 * Load the feed and run the validator and calculate statistics.
	 * @throws IOException
	 */
	public void run () throws IOException {
		load();
		if (output.loadStatus.equals(LoadStatus.SUCCESS)) {
			validate();
			calculateStats();
		}
	}
	
	/**
	 * Load the feed into memory for processing. This is generally called from {@link #run}.
	 * @throws IOException 
	 */
	public void load () throws IOException {
		_log.fine("Loading GTFS");
		
		// check if the file is accessible
		if (!feed.exists() || !feed.canRead())
			throw new IOException("File does not exist or not readable");
		
		output.feedFileName = feed.getName();
		
		// note: we have two references because a GtfsDao is not mutable and we can't load to it,
		// but a GtfsDaoImpl is.
		GtfsRelationalDaoImpl dao = new GtfsRelationalDaoImpl();
		this.dao = dao;
		GtfsReader reader = new GtfsReader();
		reader.setEntityStore(dao);
		// Exceptions here mean a problem with the file 
		try {
			reader.setInputLocation(feed);
			reader.run();
			output.loadStatus = LoadStatus.SUCCESS;
		}
		catch (ZipException e) {
			output.loadStatus = LoadStatus.INVALID_ZIP_FILE;
			output.loadFailureReason = "Invalid ZIP file, not a ZIP file, or file corrupted";
		}
		catch (CsvEntityIOException e) {
			Throwable cause = e.getCause();
			if (cause instanceof MissingRequiredFieldException) {
				output.loadStatus = LoadStatus.MISSING_REQUIRED_FIELD;
				output.loadFailureReason = cause.getMessage();
			}
			else if (cause instanceof IndexOutOfBoundsException) {
			    output.loadStatus = LoadStatus.INCORRECT_FIELD_COUNT_IMPROPER_QUOTING;
			    output.loadFailureReason = e.getMessage() + " (perhaps improper quoting)";
			}
			    
			else {
				output.loadStatus = LoadStatus.OTHER_FAILURE;
				output.loadFailureReason = "Unknown failure";
			}
		}
		catch (IOException e) {
			output.loadStatus = LoadStatus.OTHER_FAILURE;
		}
	}
	
	/**
	 * Run the GTFS validator
	 */
	public void validate () {
		GtfsValidationService validator = new GtfsValidationService(dao);
		
		_log.fine("Validating routes");
		output.routes = validator.validateRoutes();
		_log.fine("Validating trips");
		output.trips = validator.validateTrips();
		_log.fine("Finding duplicate stops");
		output.stops = validator.duplicateStops();
		_log.fine("Checking shapes");
		output.shapes = validator.listReversedTripShapes();
		
		// even though unused stops are found by validating trips, they make more sense as stop-level warnings
		// move them over
		Iterator<InvalidValue> tripIt = output.trips.invalidValues.iterator();
		
		while (tripIt.hasNext()) {
			InvalidValue next = tripIt.next();
			if (next.problemType.equals("UnusedStop")) {
				output.stops.invalidValues.add(next);
				tripIt.remove();
			}					
		}
	}
	
	/**
	 * Calculate statistics for the GTFS feed.
	 */
	public void calculateStats () {
		_log.fine("Calculating statistics");
		
		StatisticsService stats = new GtfsStatisticsService(dao);

		Optional<Date> optionalCalDateStart = Optional.empty();
		Optional<Date> optionalCalDateEnd = Optional.empty();
		Date calDateStart = null;
		Date calDateEnd = null;

		output.agencyCount = stats.getAgencyCount();
		output.routeCount = stats.getRouteCount();
		output.tripCount = stats.getTripCount();
		output.stopTimesCount = stats.getStopTimesCount();
		output.bounds = stats.getBounds();

		optionalCalDateStart = stats.getCalendarDateStart();
		if(optionalCalDateStart.isPresent()) {
			calDateStart = optionalCalDateStart.get();
		}
		Date calSvcStart = stats.getCalendarServiceRangeStart();
		optionalCalDateEnd = stats.getCalendarDateEnd();
		if(optionalCalDateEnd.isPresent()) {
			calDateEnd = optionalCalDateEnd.get();
		}
		Date calSvcEnd = stats.getCalendarServiceRangeEnd();
		
		if (calDateStart == null && calSvcStart == null)
			// no service . . . this is bad
			output.startDate = null;
		else if (calDateStart == null)
			output.startDate = calSvcStart;
		else if (calSvcStart == null)
			output.startDate = calDateStart;
		else
			output.startDate = calDateStart.before(calSvcStart) ? calDateStart : calSvcStart;
		
		if (calDateEnd == null && calSvcEnd == null)
			// no service . . . this is bad
			output.endDate = null;
		else if (calDateEnd == null)
			output.endDate = calSvcEnd;
		else if (calSvcEnd == null)
			output.endDate = calDateEnd;
		else
			output.endDate = calDateEnd.after(calSvcEnd) ? calDateEnd : calSvcEnd;
		
		Collection<Agency> agencies = dao.getAllAgencies();
		output.agencies = new HashSet<String>(agencies.size());
		for (Agency agency : agencies) {
			String agencyId = agency.getId();
			output.agencies.add(agencyId == null || agencyId.isEmpty() ? agency.getName() : agencyId);
		}
	}
	
	public FeedValidationResult getOutput () {
		return output;
	}
}
