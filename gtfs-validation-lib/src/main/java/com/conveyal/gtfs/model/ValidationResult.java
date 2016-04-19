package com.conveyal.gtfs.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;


public class ValidationResult implements Serializable {

	private static Logger _log = Logger.getLogger(ValidationResult.class.getName());
			
	public List<InvalidValue> invalidValues = new ArrayList<InvalidValue>();
	
	public void add(InvalidValue iv) {
		_log.info(iv.toString());
		invalidValues.add(iv);
	}
	
	public void add(ValidationResult vr) {
		invalidValues.addAll(vr.invalidValues);
	}
	
		
	public boolean containsBoth(String one, String two, String type){
		for (InvalidValue iv: invalidValues){
			if (iv.problemDescription.contains(one) 
					&& iv.problemDescription.contains(two)
					&& iv.affectedEntity == type)	{
				return true;
			}
		}
		return false;
	}
}
