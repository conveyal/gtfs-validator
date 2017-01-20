package com.conveyal.gtfs.model;

public class HumanReadableServiceID {

	private String depot;
	private String serviceId;
	
	public HumanReadableServiceID() {
		super();
		depot = null;
		serviceId = "test";
	}

	public String getDepot() {
		return depot;
	}

	public void setDepot(String depot) {
		this.depot = depot;
	}

	public String getServiceId() {
		return serviceId;
	}

	public void setServiceId(String serviceId) {
		this.serviceId = serviceId;
	}
	
	public void appendToServiceId(String serviceId) {
		this.serviceId += serviceId;
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		if (depot != null) sb.append("for Depot " + this.depot + ", ");
		sb.append(this.serviceId);
		return sb.toString();
	}
}
