package com.conveyal.gtfs.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.services.GtfsDao;

import com.conveyal.gtfs.model.DuplicateStops;

public class GtfsValidationService {
	
	private GtfsDao gtfsDao = null;
	
	public GtfsValidationService(GtfsDao dao) {
		gtfsDao = dao;		
	}

	public List<DuplicateStops> duplicateStops() {
		
		List<DuplicateStops> duplicateStops = new ArrayList<DuplicateStops>();
		
		Collection<Stop> stops = gtfsDao.getAllStops();
		
		for(Stop stop1 : stops) {
			for(Stop stop2 : stops) {
				
				if(stop1.getLat() == stop2.getLat() && stop1.getLon() == stop2.getLon())
					duplicateStops.add(new DuplicateStops(stop1, stop2));
			}
		}
		
		return duplicateStops;
	}
}


