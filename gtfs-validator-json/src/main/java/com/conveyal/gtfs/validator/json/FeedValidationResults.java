package com.conveyal.gtfs.validator.json;

import java.util.Date;

import com.conveyal.gtfs.model.ValidationResult;

/**
 * A class to hold all of the results of a validation on a single feed.
 * Not to be confused with {@link com.conveyal.gtfs.model.ValidationResult}, which holds all instances of
 * a particular type of error.
 * @author matthewc
 *
 */
public class FeedValidationResults {
	public ValidationResult routes;
	public ValidationResult stops;
	public ValidationResult trips;
	public ValidationResult shapes;

	// statistics
	int agencyCount;
	int routeCount;
	int tripCount;
	int stopTimesCount;
	
	/** The first date the feed has service, either in calendar.txt or calendar_dates.txt */
	Date startDate;
	
	/** The last date the feed has service, either in calendar.txt or calendar_dates.txt */
	Date endDate;
}