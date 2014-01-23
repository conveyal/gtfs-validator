package main.java.validation;

import org.onebusaway.gtfs.model.Stop;

public class DuplicateStops {

	public Stop stop1;
	public Stop stop2;
	
	public DuplicateStops(Stop s1, Stop s2) {
		stop1 = s1;
		stop2 = s2;
	}
	
}
