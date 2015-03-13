package com.conveyal.gtfs.validator.json.serialization;

import java.io.File;

import com.conveyal.gtfs.validator.json.FeedValidationResultSet;

/**
 * Basic code for a serializer for a feed validation result set
 * @author mattwigway
 */
public abstract class Serializer {
	protected FeedValidationResultSet results;

	/**
	 * Create a serializer for these validation results
	 * @param results
	 */
	public Serializer (FeedValidationResultSet results) {
		this.results = results;
	}
	
	/** Serialize the results wrapped by this class to whatever format is appropriate
	 * @return serialized data 
	 * @throws Exception */
	public abstract Object serialize () throws Exception;
	
	/** Serialize the results wrapped by this class, and write them to file
	 * @param file the file to write results to
	 * @throws Exception
	 */
	public abstract void serializeToFile (File file) throws Exception;
}
