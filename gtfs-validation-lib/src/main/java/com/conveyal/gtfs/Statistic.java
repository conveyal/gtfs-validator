package com.conveyal.gtfs;

import java.util.Date;

import org.onebusaway.gtfs.model.Agency;

/**
 * Model object representing statistics about GTFS. 
 *
 */
public class Statistic {
	private String agencyId;
	private Integer routeCount;
	private Integer tripCount;
	private Integer stopCount;
	private Integer stopTimeCount;
	private Date calendarStartDate;
	private Date calendarEndDate;
	
	public String getAgencyId() {
		return agencyId;
	}
	public void setAgencyId(String agencyId) {
		this.agencyId = agencyId;
	}
	public Integer getRouteCount() {
		return routeCount;
	}
	public void setRouteCount(Integer routeCount) {
		this.routeCount = routeCount;
	}
	public Integer getTripCount() {
		return tripCount;
	}
	public void setTripCount(Integer tripCount) {
		this.tripCount = tripCount;
	}
	public Integer getStopCount() {
		return stopCount;
	}
	public void setStopCount(Integer stopCount) {
		this.stopCount = stopCount;
	}
	public Integer getStopTimeCount() {
		return stopTimeCount;
	}
	public void setStopTimeCount(Integer stopTimeCount) {
		this.stopTimeCount = stopTimeCount;
	}
	public Date getCalendarStartDate() {
		return calendarStartDate;
	}
	public void setCalendarStartDate(Date calendarStartDate) {
		this.calendarStartDate = calendarStartDate;
	}
	public Date getCalendarEndDate() {
		return calendarEndDate;
	}
	public void setCalendarEndDate(Date calendarEndDate) {
		this.calendarEndDate = calendarEndDate;
	}
	
	
}
