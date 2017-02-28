gtfs-validator
==============

A Java framework for GTFS validation and statistics.

[![Build Status](https://travis-ci.org/conveyal/gtfs-validator.svg?branch=master)](https://travis-ci.org/conveyal/gtfs-validator) 

How is this different than the Google-supported validator?
=============
The Google TransitFeed-based [validator](https://github.com/google/transitfeed/blob/master/feedvalidator.py) is written in Python, and is quite slow on large feeds. I also find the code rather hard to understand. While it supports extensions, few have extended it.

This validator uses the [Onebusaway-GTFS](https://github.com/OneBusAway/onebusaway-gtfs-modules) library, written in Java and is far faster at processing large feeds. 


Using this framework
==============
There are then multiple options for use:

1. Use the JAR provided in the releases. `java -jar gtfs-validator.jar yourGtfs.zip`

2. Import the services provided and build your own validation.

3. Use the gtfs-validator-json and gtfs-validator-webapp according to the directions in those folders. 

==============
ValidatorMain

The ValidatorMain class logs a number of common GTFS errors to Standard Out on the console when run as a JAR. This includes:

 * Problems with route names
 * Bad shape coordinates
 * Unused stops
 * Duplicate stops
 * Missing stop coordinates
 * Missing stop times for trips
 * Stop times out of sequence
 * Duplicated trips (same times)
 * Overlapping trips when block_id is present
 * Missing shapes and shape coordinates
 * “Reversed” shapes with directions that do not agree with stop times.
 * Exhaustively going through the calendar and printing active service IDs and number of trips for that day.
 * Dates with no active service
