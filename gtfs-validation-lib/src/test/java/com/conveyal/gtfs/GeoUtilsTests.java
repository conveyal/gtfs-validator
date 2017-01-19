package com.conveyal.gtfs;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.conveyal.gtfs.model.ProjectedCoordinate;
import com.conveyal.gtfs.service.GeoUtils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

import junit.framework.Assert;
 
public class GeoUtilsTests {

	@Test
	public void testProjectionConversion() {
		
		// lat/lon coords
		// note that they are reversed
		Coordinate coord1 = new Coordinate(47.604201,-122.311123);
		Coordinate coord2 = new Coordinate(47.565297, -122.300823);
		
		ProjectedCoordinate projCoord1 = GeoUtils.convertLatLonToEuclidean(coord1);
		ProjectedCoordinate projCoord2 = GeoUtils.convertLatLonToEuclidean(coord2);
		
		// XY are reversed too
		// This should be the x coord
		assertEquals(551778.8, projCoord1.y, 1.0);
		assertEquals(5272540.2, projCoord1.x, 1.0);
	
		Double distance =  projCoord1.distance(projCoord2);
		
		assertEquals(distance, 4392.666986979272,1.0);
		
		coord1 = new Coordinate(38.925922, -77.044788);
		coord2 = new Coordinate(38.891726, -76.999470);
		
		projCoord1 = GeoUtils.convertLatLonToEuclidean(coord1);
		projCoord2 = GeoUtils.convertLatLonToEuclidean(coord2);
	
		distance =  projCoord1.distance(projCoord2);
		
		Assert.assertEquals(distance, 5464.52118132449);
		
	}
	@Test
	public void getGeometryFromCoordTest(){
		Geometry g = GeoUtils.getGeometryFromCoordinate(47.604201, -122.311123);
		// transposed again...
//		System.out.println(g);
		assertEquals(551778.8, g.getCoordinate().y,1.0);
		assertEquals(5272540.2, g.getCoordinate().x,1.0);
	}
}
