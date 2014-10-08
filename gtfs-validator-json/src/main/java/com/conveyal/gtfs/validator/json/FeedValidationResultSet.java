package com.conveyal.gtfs.validator.json;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents the results of a single validator run on multiple GTFS files.
 * @author mattwigway
 */
public class FeedValidationResultSet {
    /** The name of this feedset. In the simplest case this is the name of the directory
     * containing the first feed.
     */
    public String name;

    /**
     * The date of this run
     */
    public Date date;

    // note: the following three fields are private because applications should not manipulate them
    // directly, as they track internal state. We add a JsonProperty annotation to each so that it
    // is exported in the JSON. While this theoretically embeds one format into code that is generally
    // intended to be format-generic, it shouldn't be an issue because the annotation should simply have
    // no effect outside of Jackson.
    
    /**
     * The number of feeds validated.
     */
    @JsonProperty
    private int feedCount;

    /**
     * The number of feeds that were able to be loaded (they may have had validation errors,
     * but nothing that makes them unusable, at least not from a technical standpoint).
     */
    @JsonProperty
    private int loadCount;

    /**
     * The validation results of all of the feeds.
     */
    @JsonProperty
    private Set<FeedValidationResult> results;

    /**
     * Add a feed validation result to this result set.
     */
    public void add(FeedValidationResult results) {
        this.results.add(results);
        feedCount++;
        if (LoadStatus.SUCCESS.equals(results.loadStatus))
            loadCount++;
    }
    
    /**
     * Create a new FeedValidationResultSet with the given initial capacity.
     * @param capacity initial capacity
     */
    public FeedValidationResultSet (int capacity) {
        this.results = new HashSet<FeedValidationResult>(capacity);
        this.date = new Date();
    }
    
    /**
     * Create a new FeedValidationResultSet with a reasonable default initial capacity.
     */
    public FeedValidationResultSet () {
        this(16);
    }
}
