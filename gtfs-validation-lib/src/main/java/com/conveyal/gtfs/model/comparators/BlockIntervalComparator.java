package com.conveyal.gtfs.model.comparators;

import java.util.Comparator;

import com.conveyal.gtfs.model.BlockInterval;

public class BlockIntervalComparator implements Comparator<BlockInterval> {

	public int compare(BlockInterval a, BlockInterval b) {
		return new Integer(a.getStartTime()).compareTo(new Integer(b.getStartTime()));
	}
}
