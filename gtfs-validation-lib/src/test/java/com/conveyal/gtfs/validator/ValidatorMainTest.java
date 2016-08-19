package com.conveyal.gtfs.validator;

import static org.junit.Assert.*;

import java.util.Date;
import java.util.Optional;

import org.junit.Test;

public class ValidatorMainTest {

	@Test
	public void testEarlier() {
		
		Optional<Date> o = Optional.of(new Date(0L));
		Date d = new Date(100000L);
		
		Date returned = ValidatorMain.getEarliestDate(o, d);
		
		assertSame(o.get(), returned);
	}

	@Test
	public void testEarlier2() {
		
		Optional<Date> o = Optional.of(new Date(10L));
		Date d = new Date(0L);
		
		Date returned = ValidatorMain.getEarliestDate(o, d);
		
		assertSame(d, returned);
	}
	
	@Test
	public void testLater() {
		
		Optional<Date> o = Optional.of(new Date(100000L));
		Date d = new Date(0L);
		
		Date returned = ValidatorMain.getLatestDate(o, d);
		
		assertSame(o.get(), returned);
	}
	@Test
	public void testLater2() {
		
		Optional<Date> o = Optional.of(new Date(0));
		Date d = new Date(10L);
		
		Date returned = ValidatorMain.getLatestDate(o, d);
		
		assertSame(d, returned);
	}
	
}
