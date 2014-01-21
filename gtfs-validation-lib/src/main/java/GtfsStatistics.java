package main.java;

import java.util.Date;

import org.onebusaway.gtfs.impl.GtfsDaoImpl;
import org.onebusaway.gtfs.model.ServiceCalendar;
import org.onebusaway.gtfs.model.ServiceCalendarDate;

public class GtfsStatistics {
	
//	Provides statistics for: 
//		Agencies
//		Routes
//		Trips
// 	    Stops
//		Stop Times
//		Calendar Date ranges
//		Calendar Service exceptions
	
	private GtfsDaoImpl gtfsDao = null;
	
	public GtfsStatistics(GtfsDaoImpl dao) {
		gtfsDao = dao;		
	}

	public Integer getAgencyCount() {
		return gtfsDao.getAllAgencies().size();
	}

	public Integer getRouteCount() {
		return gtfsDao.getAllRoutes().size();
	}
	
	public Integer getTripCount() {
		return gtfsDao.getAllTrips().size();
	}

	public Integer getStopCount() {
		return gtfsDao.getAllStops().size();
	}
	
	public Integer getStopTimesCount() {
		return gtfsDao.getAllStopTimes().size();
	}

	public Integer getStopTimeCount() {
		return gtfsDao.getAllStopTimes().size();
	}
	
	
	// calendar date range start/end assume a service calendar based schedule
	// returns null for schedules without calendar service schedules
	
	public Date getCalendarServiceRangeStart() {
	
		Date startDate = null;
		
		for(ServiceCalendar serviceCalendar : gtfsDao.getAllCalendars()) {
		
			if(startDate == null || serviceCalendar.getStartDate().getAsDate().before(startDate))
				startDate = serviceCalendar.getStartDate().getAsDate();	
		}
		
		return startDate;

	}
	
	public Date getCalendarServiceRangeEnd() {
		
		Date endDate = null;
		
		for(ServiceCalendar serviceCalendar : gtfsDao.getAllCalendars()) {
		
			if(endDate == null || serviceCalendar.getStartDate().getAsDate().after(endDate))
				endDate = serviceCalendar.getStartDate().getAsDate();	
		}
		
		return endDate;
	}
	
	public Date getCalendarDateStart() {
		
		Date startDate = null;
		
		for(ServiceCalendarDate serviceCalendarDate : gtfsDao.getAllCalendarDates()) {
		
			if(startDate == null || serviceCalendarDate.getDate().getAsDate().before(startDate))
				startDate = serviceCalendarDate.getDate().getAsDate();	
		}
		
		return startDate;

	}
	
	public Date getCalendarDateEnd() {
		
		Date endDate = null;
		
		for(ServiceCalendarDate serviceCalendarDate : gtfsDao.getAllCalendarDates()) {
		
			if(endDate == null || serviceCalendarDate.getDate().getAsDate().after(endDate))
				endDate = serviceCalendarDate.getDate().getAsDate();	
		}
		
		return endDate;
	}

}


