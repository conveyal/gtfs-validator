package com.conveyal.gtfs.validator.json.serialization;

import java.io.File;
import java.io.IOException;

import com.conveyal.gtfs.validator.json.FeedValidationResults;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

/**
 * Basic code for a serializer for feed validation results
 * @author mattwigway
 */
public abstract class Serializer {
	protected FeedValidationResults results;

	/**
	 * Create a serializer for these validation results
	 * @param results
	 */
	public Serializer (FeedValidationResults results) {
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
