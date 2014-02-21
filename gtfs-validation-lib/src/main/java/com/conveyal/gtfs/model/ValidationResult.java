package com.conveyal.gtfs.model;

import java.util.ArrayList;
import java.util.List;


public class ValidationResult {
	
	public List<InvalidValue> invalidValues = new ArrayList<InvalidValue>();
	
	public void add(InvalidValue iv) {
		System.out.println(iv);
		invalidValues.add(iv);
	}
	
	public void add(ValidationResult vr) {
		invalidValues.addAll(vr.invalidValues);
	}
}
