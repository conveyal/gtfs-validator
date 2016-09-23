package com.conveyal.gtfs.model;

import java.util.Comparator;

import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;


public class BlockInterval implements Comparable<BlockInterval> {
	Trip trip;
	Integer startTime;
	StopTime firstStop;
	StopTime lastStop;

	public Trip getTrip() {
		return trip;
	}

	public void setTrip(Trip trip) {
		this.trip = trip;
	}

	public Integer getStartTime() {
		return startTime;
	}

	public void setStartTime(Integer startTime) {
		this.startTime = startTime;
	}

	public StopTime getFirstStop() {
		return firstStop;
	}

	public void setFirstStop(StopTime firstStop) {
		this.firstStop = firstStop;
	}

	public StopTime getLastStop() {
		return lastStop;
	}

	public void setLastStop(StopTime lastStop) {
		this.lastStop = lastStop;
	}

	public int compareTo(BlockInterval o) {
		return new Integer(this.firstStop.getArrivalTime())
				.compareTo(new Integer(o.firstStop.getArrivalTime()));
	}
	

}

