package com.conveyal.gtfs;

import static org.junit.Assert.*;

import org.junit.Test;

import com.conveyal.gtfs.model.HumanReadableServiceID;
import com.conveyal.gtfs.service.ServiceIdHelper;

public class ServiceIdHelperTest {

	@Test
	public void test() {
		ServiceIdHelper h = new ServiceIdHelper();
		HumanReadableServiceID s = h.getHumanReadableCalendarFromServiceId("MTA NYCT_JG_C6-Weekday-SDon-BM");
		
		assertNotNull(s);
		assertTrue(s.getDepot().equals("JG"));
		assertTrue(s.getServiceId().equals("WEEKDAY_SCHOOL_OPEN Trips Starting Before Midnight"));
		
		s = h.getHumanReadableCalendarFromServiceId("MTA NYCT_FP_J6-Weekday");
		assertTrue(s.getDepot().equals("FP"));
		assertTrue(s.getServiceId().equals("GOOD_FRIDAY"));
		
		s = h.getHumanReadableCalendarFromServiceId("MTABC_BPPC6-BP_C6-Weekday-30");
		assertTrue(s.getDepot().equals("BP"));
		assertTrue(s.getServiceId().equals("WEEKDAY_SCHOOL_CLOSED"));
		
		
		s = h.getHumanReadableCalendarFromServiceId("AC Transit_1606SU-D4-Weekday-10");
		assertTrue(s.getDepot().equals("D4"));
	}

}
