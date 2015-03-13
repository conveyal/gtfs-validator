package com.conveyal.gtfs.validator.json.backends;

import java.io.File;

/**
 * A simple feed backend that simply gets data from the file system. Feed IDs are file paths.
 * Note that this means that there may be multiple IDs which refer to the same feed.
 * @author matthewc
 *
 */
public class FileSystemFeedBackend implements FeedBackend {
	
	/**
	 * Get the feed at path feedId on the file system. This is trivial but allows for compatibility
	 * with more complicated backends.
	 * @param feedId The path the feed
	 * @return a file object referring to the feed
	 */
	public File getFeed(String feedId) {
		return new File(feedId);
	}
}
