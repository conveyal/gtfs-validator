package com.conveyal.gtfs.model;

public class InvalidValue {

	public String affectedEntity;
	
	public String affectedField;
	
	public String affectedEntityId;
	
	public String problemType;
	
	public String problemDescription;
	
	public Object problemData;
	
	public InvalidValue(String affectedEntity,  String affectedField, String affectedEntityId, String problemType, String problemDescription, Object problemData) {
		
		this.affectedEntity = affectedEntity;
		this.affectedField = affectedField;
		this.affectedEntityId = affectedEntityId;
		this.problemType =  problemType;
		this.problemDescription = problemDescription;
		this.problemData = problemData;
	}
	
	public String toString() {
		
		return problemType + "\t" + affectedEntityId + ":\t"  + problemDescription;
		
	}
	
}
