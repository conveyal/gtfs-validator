package com.conveyal.gtfs;

import java.util.Date;

/**
 *	Provides statistics for:
 * <or> 
 * <li>Agencies
 * <li>Routes
 * <li>Trips
 * <li>Stops
 * <li>Stop Times
 * <li>Calendar Date ranges
 * <li>Calendar Service exceptions
 * </or>
 * @author dev
 *
 */
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

	Statistic getStatistic(String agencyId);
}
