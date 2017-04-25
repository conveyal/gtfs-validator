package com.conveyal.gtfs.validator.json.serialization;

import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;

import com.conveyal.gtfs.validator.json.FeedValidationResultSet;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;

/**
 * Serialize validation results to JSON, using Jackson
 * @author mattwigway
 */
public class JsonSerializer extends Serializer {
	private ObjectMapper mapper;
	private ObjectWriter writer;

	
	/**
	 * Create a JSON serializer for these validation results.
	 * @param results
	 */
	public JsonSerializer (FeedValidationResultSet results) {
		super(results);
		mapper = new ObjectMapper();
		mapper.addMixInAnnotations(Rectangle2D.class, Rectangle2DMixIn.class);
		SimpleModule deser = new SimpleModule();
		deser.addDeserializer(Rectangle2D.class, new Rectangle2DDeserializer());
		mapper.registerModule(deser);
		SimpleFilterProvider filters = new SimpleFilterProvider();
		filters.addFilter("bbox", SimpleBeanPropertyFilter.filterOutAllExcept("west", "east", "south", "north"));
		writer = mapper.writer(filters);
	}
	
	/**
	 * Serialize to JSON
	 * @return a string containing the serialized JSON
	 */
	public Object serialize() throws JsonProcessingException {
		return writer.writeValueAsString(results);
	}

	/**
	 * Serialize to JSON and write to file.
	 * @param file the file to write the JSON to
	 */
	public void serializeToFile(File file) throws JsonGenerationException, JsonMappingException, IOException {
		writer.writeValue(file, results);
	}
}
