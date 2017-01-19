package com.conveyal.gtfs.model.comparators;

import java.util.Comparator;

import org.onebusaway.gtfs.model.ShapePoint;

// used in the TreeMap of ShapePoints 
public class ShapePointComparator implements Comparator<ShapePoint> {

	public int compare(ShapePoint a, ShapePoint b) {
		return new Integer(a.getSequence()).compareTo(new Integer(b.getSequence()));
	}
}
