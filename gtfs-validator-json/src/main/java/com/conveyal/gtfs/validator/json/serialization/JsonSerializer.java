package com.conveyal.gtfs.validator.json.serialization;

import java.io.File;
import java.io.IOException;

import com.conveyal.gtfs.validator.json.FeedValidationResults;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Serialize validation results to JSON, using Jackson
 * @author mattwigway
 */
public class JsonSerializer extends Serializer {
	private ObjectMapper mapper;
	
	/**
	 * Create a JSON serializer for these validation results.
	 * @param results
	 */
	public JsonSerializer (FeedValidationResults results) {
		super(results);
		mapper = new ObjectMapper();
	}
	
	/**
	 * Serialize to JSON
	 * @return a string containing the serialized JSON
	 */
	public Object serialize() throws JsonProcessingException {
		return mapper.writeValueAsString(results);
	}

	/**
	 * Serialize to JSON and write to file.
	 * @param file the file to write the JSON to
	 */
	public void serializeToFile(File file) throws JsonGenerationException, JsonMappingException, IOException {
		mapper.writeValue(file, results);
	}

}
