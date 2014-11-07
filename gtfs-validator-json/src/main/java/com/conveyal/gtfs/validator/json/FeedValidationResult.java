package com.conveyal.gtfs.validator.json;

import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.util.Collection;
import java.util.Date;

import com.conveyal.gtfs.model.ValidationResult;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A class to hold all of the results of a validation on a single feed.
 * Not to be confused with {@link com.conveyal.gtfs.model.ValidationResult}, which holds all instances of
 * a particular type of error.
 * @author mattwigway
 *
 */
public class FeedValidationResult implements Serializable {
	/** Were we able to load the GTFS at all (note that this should only indicate corrupted files,
	 * not missing ones; that should raise an exception instead.)
	 */
        @JsonProperty
	public LoadStatus loadStatus;
	
	/**
	 * Additional description of why the feed failed to load.
	 */
	public String loadFailureReason;
	
	/**
	 * The name of the feed on the file system
	 */
	public String feedFileName;
	
	/**
	 * All of the agencies in the feed
	 */
	public Collection<String> agencies;
	
	public ValidationResult routes;
	public ValidationResult stops;
	public ValidationResult trips;
	public ValidationResult shapes;

	// statistics
	public int agencyCount;
	public int routeCount;
	public int tripCount;
	public int stopTimesCount;
	
	/** The first date the feed has service, either in calendar.txt or calendar_dates.txt */
	public Date startDate;
	
	/** The last date the feed has service, either in calendar.txt or calendar_dates.txt */
	public Date endDate;
	
	/** The bounding box of the stops in this feed */
	public Rectangle2D bounds;
}