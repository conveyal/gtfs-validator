package com.conveyal.gtfs.model.comparators;

import java.util.Comparator;

import org.onebusaway.gtfs.model.StopTime;

public class StopTimeComparator implements Comparator<StopTime> {

	public int compare(StopTime a, StopTime b) {
		return new Integer(a.getStopSequence()).compareTo(new Integer(b.getStopSequence()));
	}
}
