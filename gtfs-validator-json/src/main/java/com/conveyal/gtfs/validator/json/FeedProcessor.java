package com.conveyal.gtfs.validator.json;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.logging.Logger;

import org.onebusaway.gtfs.impl.GtfsDaoImpl;
import org.onebusaway.gtfs.serialization.GtfsReader;
import org.onebusaway.gtfs.services.GtfsDao;

import com.conveyal.gtfs.service.GtfsValidationService;
import com.conveyal.gtfs.service.StatisticsService;
import com.conveyal.gtfs.service.impl.GtfsStatisticsService;

/**
 * Process a feed and return the validation results and the statistics.
 * @author mattwigway
 */
public class FeedProcessor {
	private File feed;
	private GtfsDao dao;
	private FeedValidationResults output;
	private static Logger _log = Logger.getLogger(FeedProcessor.class.getName());
	
	/**
	 * Create a feed processor for the given feed
	 * @param feed
	 */
	public FeedProcessor (File feed) {
		this.feed = feed;
		this.output = new FeedValidationResults();
	}
	
	/**
	 * Load the feed and run the validator and calculate statistics.
	 * @throws IOException
	 */
	public void run () throws IOException {
		load();
		validate();
		calculateStats();
	}
	
	/**
	 * Load the feed into memory for processing. This is generally called from {@link #run}.
	 * @throws IOException 
	 */
	public void load () throws IOException {
		_log.fine("Loading GTFS");
		GtfsDaoImpl dao = new GtfsDaoImpl();
		GtfsReader reader = new GtfsReader();
		reader.setEntityStore(dao);
		reader.setInputLocation(feed);
		reader.run();
		this.dao = dao;
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
	}
	
	/**
	 * Calculate statistics for the GTFS feed.
	 */
	public void calculateStats () {
		_log.fine("Calculating statistics");
		
		StatisticsService stats = new GtfsStatisticsService(dao);
		
		output.agencyCount = stats.getAgencyCount();
		output.routeCount = stats.getRouteCount();
		output.tripCount = stats.getTripCount();
		output.stopTimesCount = stats.getStopTimesCount();
		
		Date calDateStart = stats.getCalendarDateStart();
		Date calSvcStart = stats.getCalendarServiceRangeStart();
		Date calDateEnd = stats.getCalendarDateEnd();
		Date calSvcEnd = stats.getCalendarServiceRangeEnd();
		
		output.startDate = calDateStart.before(calSvcStart) ? calDateStart : calSvcStart;
		output.endDate = calDateEnd.after(calSvcEnd) ? calDateEnd : calSvcEnd;
	}
	
	public FeedValidationResults getOutput () {
		return output;
	}
}
