package com.conveyal.gtfs.validator.json.backends;

import java.io.File;

/**
 * A backend that stores feed data.
 * We could conceptually have one for dat, one for S3, one for robotic tape libraries . . .
 * Inspired by https://github.com/conveyal/transit-data-dashboard/blob/master/app/updaters/FeedStorer.java
 * 
 * @author mattwigway
 */
public interface FeedBackend {
	/**
	 * Get a feed from this backend.
	 * @param feedId the feed to retrieve
	 * @return a File object referring to the feed
	 */
	public File getFeed(String feedId);
}
