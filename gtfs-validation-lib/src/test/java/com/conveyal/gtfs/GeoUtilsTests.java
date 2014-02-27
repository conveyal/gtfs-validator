package com.conveyal.gtfs;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;
import org.onebusaway.csv_entities.exceptions.CsvEntityIOException;
import org.onebusaway.csv_entities.exceptions.MissingRequiredFieldException;
import org.onebusaway.gtfs.impl.GtfsDaoImpl;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.serialization.GtfsReader;

import com.conveyal.gtfs.model.DuplicateStops;
import com.conveyal.gtfs.model.ValidationResult;
import com.conveyal.gtfs.service.GeoUtils;
import com.conveyal.gtfs.service.GtfsValidationService;
import com.conveyal.gtfs.service.InputOutOfRange;
import com.conveyal.gtfs.service.ProjectedCoordinate;
import com.conveyal.gtfs.service.impl.GtfsStatisticsService;
import com.vividsolutions.jts.geom.Coordinate;
 
public class GeoUtilsTests {

	@Test
	public void testProjectionConversion() {
		
		// lat/lon coords
		Coordinate coord1 = new Coordinate(47.604201, -122.311123);
		Coordinate coord2 = new Coordinate(47.565297, -122.300823);
		
		ProjectedCoordinate projCoord1 = GeoUtils.convertLatLonToEuclidean(coord1);
		ProjectedCoordinate projCoord2 = GeoUtils.convertLatLonToEuclidean(coord2);
	
		Double distance =  projCoord1.distance(projCoord2);
		
		Assert.assertEquals(distance, 4392.666986979272);
		
		coord1 = new Coordinate(38.925922, -77.044788);
		coord2 = new Coordinate(38.891726, -76.999470);
		
		projCoord1 = GeoUtils.convertLatLonToEuclidean(coord1);
		projCoord2 = GeoUtils.convertLatLonToEuclidean(coord2);
	
		distance =  projCoord1.distance(projCoord2);
		
		Assert.assertEquals(distance, 5464.52118132449);
		
	}
	
}
