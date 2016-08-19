//largely copied from https://github.com/camsys/onebusaway-nyc/blob/master/onebusaway-nyc-transit-data-federation/src/main/java/org/onebusaway/nyc/transit_data_federation/bundle/tasks/stif/model/ServiceCode.java

package com.conveyal.gtfs.service;
import java.util.HashMap;

import com.conveyal.gtfs.model.HumanReadableServiceID;

public class ServiceIdHelper {

	public HumanReadableServiceID getHumanReadableCalendarFromServiceId(String id) {
		HumanReadableServiceID sid=  new HumanReadableServiceID();

		try {
			String[] serviceIdParts = id.split("_");

			if(serviceIdParts.length != 3){
				serviceIdParts = id.split("-");
				if (serviceIdParts.length > 1){
					sid.setServiceId(id);
				}
			}
			String[] serviceIdSubparts = serviceIdParts[2].split("-");

			String[] depotParts = serviceIdParts[1].split("-");
			
			sid.setDepot(depotParts[depotParts.length -1]);
			
			String pickCode = serviceIdSubparts[0].toUpperCase();

			char pickCodeWithoutYear = pickCode.toCharArray()[0];
			if(pickCodeWithoutYear <= 'G') {
				if (id.contains("Weekday")) {
					if (id.contains("SDon")) {
						sid.setServiceId("WEEKDAY_SCHOOL_OPEN");
					} else {
						sid.setServiceId("WEEKDAY_SCHOOL_CLOSED");
					}
				} else if (id.contains("Saturday")) {
					sid.setServiceId("SATURDAY");
				} else if (id.contains("Sunday")) {
					sid.setServiceId("SUNDAY");
				} else
					sid.setServiceId(null);
			} else {
				// holiday code
				sid.setServiceId(ServiceCode.serviceCodeForGtfsId.get(Character.toString(pickCodeWithoutYear)).name());
			}
			if (id.contains("BM") || id.contains("b4")){
				sid.appendToServiceId(" Trips Starting Before Midnight");;
			}
		} catch (Exception e) {
			sid.setServiceId(id);
		}

		return sid;
	}
	private enum ServiceCode {
		WEEKDAY_SCHOOL_OPEN, 
		WEEKDAY_SCHOOL_CLOSED, 
		SATURDAY, 
		SUNDAY, 
		MLK, 
		PRESIDENTS_DAY, 
		MEMORIAL_DAY, 
		GOOD_FRIDAY, 
		LABOR_DAY, 
		JULY_FOURTH, 
		COLUMBUS_DAY, 
		THANKSGIVING, 
		DAY_AFTER_THANKSGIVING, 
		CHRISTMAS_EVE, 
		CHRISTMAS_DAY, 
		CHRISTMAS_DAY_OBSERVED, 
		CHRISTMAS_WEEK, 
		NEW_YEARS_EVE, 
		NEW_YEARS_DAY, 
		NEW_YEARS_DAY_OBSERVED;

		static HashMap<String, ServiceCode> serviceCodeForGtfsId = new HashMap<String, ServiceCode>();
		static HashMap<ServiceCode, String> letterCodeForServiceCode = new HashMap<ServiceCode, String>();

		static {
			mapServiceCode("1", WEEKDAY_SCHOOL_OPEN);
			mapServiceCode("11", WEEKDAY_SCHOOL_CLOSED);
			mapServiceCode("2", SATURDAY);
			mapServiceCode("3", SUNDAY);
			mapServiceCode("E", WEEKDAY_SCHOOL_OPEN);
			mapServiceCode("C", WEEKDAY_SCHOOL_CLOSED);
			mapServiceCode("A", SATURDAY);
			mapServiceCode("D", SUNDAY);
			mapServiceCode("H", MLK);
			mapServiceCode("I", PRESIDENTS_DAY);
			mapServiceCode("J", GOOD_FRIDAY);
			mapServiceCode("K", MEMORIAL_DAY);
			mapServiceCode("M", JULY_FOURTH);
			mapServiceCode("N", LABOR_DAY);
			mapServiceCode("O", COLUMBUS_DAY);
			mapServiceCode("R", THANKSGIVING);
			mapServiceCode("S", DAY_AFTER_THANKSGIVING);
			mapServiceCode("T", CHRISTMAS_EVE);
			mapServiceCode("U", CHRISTMAS_DAY);
			mapServiceCode("V", CHRISTMAS_DAY_OBSERVED);
			mapServiceCode("W", CHRISTMAS_WEEK);
			mapServiceCode("X", NEW_YEARS_EVE);
			mapServiceCode("Y", NEW_YEARS_DAY);
			mapServiceCode("Z", NEW_YEARS_DAY_OBSERVED);
		}

		private static void mapServiceCode(String string, ServiceCode serviceCode) {
			serviceCodeForGtfsId.put(string, serviceCode);
			if (Character.isLetter(string.charAt(0))) {
				letterCodeForServiceCode.put(serviceCode, string);
			}
		}
		

//		private boolean isHoliday() {
//			return !(this == WEEKDAY_SCHOOL_OPEN || this == WEEKDAY_SCHOOL_CLOSED
//					|| this == SATURDAY || this == SUNDAY);
//		}
	}}
