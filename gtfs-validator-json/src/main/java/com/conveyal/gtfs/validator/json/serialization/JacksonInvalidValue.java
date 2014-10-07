package com.conveyal.gtfs.validator.json.serialization;

import javax.xml.bind.annotation.XmlElement;

import com.conveyal.gtfs.model.InvalidValue;

public class JacksonInvalidValue extends InvalidValue {
	public JacksonInvalidValue(String affectedEntity, String affectedField,
			String affectedEntityId, String problemType,
			String problemDescription, Object problemData) {
		super(affectedEntity, affectedField, affectedEntityId, problemType,
				problemDescription, problemData);
	}

	@XmlElement
	public String affectedEntity;
	
	public String affectedField;
	
	public String affectedEntityId;
	
	public String problemType;
	
	public String problemDescription;
	
	public Object problemData;
}
