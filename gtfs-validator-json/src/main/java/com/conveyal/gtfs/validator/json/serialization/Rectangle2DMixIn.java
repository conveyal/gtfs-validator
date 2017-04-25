package com.conveyal.gtfs.validator.json.serialization;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * From https://github.com/conveyal/gtfs-data-manager/blob/master/app/controllers/api/Rectangle2DMixIn.java
 */
// ignore all by default
@JsonFilter("bbox")
public abstract class Rectangle2DMixIn {
    // stored as lon, lat
    @JsonProperty("west") public abstract double getMinX();
    @JsonProperty("east") public abstract double getMaxX();
    @JsonProperty("north") public abstract double getMaxY();
    @JsonProperty("south") public abstract double getMinY();
}