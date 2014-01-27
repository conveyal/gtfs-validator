package com.conveyal.gtfs;

import java.util.Date;

public interface BaseStatistics {

	Integer getAgencyCount();
	Integer getRouteCount();
	Integer getTripCount();
	Integer getStopCount();
	Integer getStopTimesCount();
	Date getCalendarServiceRangeStart();
	Date getCalendarServiceRangeEnd();
	Integer getRouteCount(String agencyId);
	Integer getTripCount(String agencyId);
	Integer getStopCount(String agencyId);
	Integer getStopTimesCount(String agencyId);
	Date getCalendarServiceRangeStart(String agencyId);
	Date getCalendarServiceRangeEnd(String agencyId);
	
}
