package com.conveyal.gtfs.model;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.StopTime;
/*
 * This is a model of convenience for holding onto metadata on trip patterns.
 * As GTFS does not model patterns explicitly, it uses the tuple
 * (route_id)-(shape_id)-(length(stopTimes))
 * to distinguish between them.
 * 
 * This model is intended for speedily checking if something that looks like the same pattern has been seen before.
 * 
 * If this is too simplistic, then a List<stops> could be added in the future. 
 * 
 * from TCRP report 135, a trip/service pattern is: 
 * 
 * The unique sequence of stops  associated with each type of trip on a route.  
 * If all trips operate from one end to the other on a common path the route has one service pattern.  
 * Branches, deviations, or short turns introduce additional service patterns.  
 * Service patterns are a fundamental component of scheduling and provide the framework for 
 * tracking running time, generating revenue trips, and identifying deadhead movements for the route.
 */
public class TripPatternCollection {
	private Set<TripPattern> patterns;
	
	public TripPatternCollection(int estimatedSize){
		patterns = new LinkedHashSet<TripPattern>(estimatedSize);
	}
	
	public void add(Route routeId, AgencyAndId shapeId, List<StopTime> stops){
		TripPattern tp = new TripPattern(routeId, shapeId, stops.size());
		patterns.add(tp);
	}
	
	public Boolean addIfNotPresent(Route routeId, AgencyAndId shapeId, List<StopTime> stops){
		TripPattern tp = new TripPattern(routeId, shapeId, stops.size());
		Boolean present = patterns.contains(tp);
		if (!present) {
			patterns.add(tp);
		}
		return present;
	}
	
	
	private class TripPattern{
		private String routeId;
		private String shapeId;
		private int stopHash;
		
		private TripPattern(Route route, AgencyAndId shape, int stopHash) {
			super();
			this.routeId = route.toString();
			this.shapeId = shape.toString();
			this.stopHash = stopHash;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((routeId == null) ? 0 : routeId.hashCode());
			result = prime * result + ((shapeId == null) ? 0 : shapeId.hashCode());
			result = prime * result + stopHash;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			TripPattern other = (TripPattern) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (routeId == null) {
				if (other.routeId != null)
					return false;
			} else if (!routeId.equals(other.routeId))
				return false;
			if (shapeId == null) {
				if (other.shapeId != null)
					return false;
			} else if (!shapeId.equals(other.shapeId))
				return false;
			if (stopHash != other.stopHash)
				return false;
			return true;
		}

		private TripPatternCollection getOuterType() {
			return TripPatternCollection.this;
		}
		
	}
}
