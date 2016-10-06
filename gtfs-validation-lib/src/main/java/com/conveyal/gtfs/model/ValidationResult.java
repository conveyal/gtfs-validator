package com.conveyal.gtfs.model;

import java.io.Serializable;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;


public class ValidationResult implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static Logger _log = Logger.getLogger(ValidationResult.class.getName());
			
	public Set<InvalidValue> invalidValues = new TreeSet<InvalidValue>();
	
	public void add(InvalidValue iv) {
//		_log.info(iv.toString());
		invalidValues.add(iv);
	}
	
	public void append(ValidationResult vr) {
		invalidValues.addAll(vr.invalidValues);
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		for (InvalidValue iv: invalidValues){
			sb.append(iv);
		}
		return sb.toString();
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
