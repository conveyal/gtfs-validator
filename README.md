gtfs-validator
==============

A Java framework for GTFS validation and statistics.

Using this framework
==============
Check out this repository and import using Maven.

There are then multiple options for use:


1. Use the …/gtfs/validator/ValidatorMain class, passing in the GTFS file as the only program argument (check Eclipse Run Configurations). See below for more.

2. Import the services provided and build your own validation.

3. Use the gtfs-validator-json and gtfs-validator-webapp according to the directions in those folders. 

==============
ValidatorMain

The ValidatorMain class logs a number of common GTFS errors to Standard Out on the console. This includes:

 * Problems with route names
 * Bad shape coordinates
 * Unused stops
 * Duplicate stops
 * Missing stop coordinates
 * Missing stop times for trips
 * Stop times out of sequence
 * Duplicated trips in trips.txt
 * Overlapping trips when block_id is present
 * Missing shapes and shape coordinates
 * “Reversed” shapes with directions that do not agree with stop times.
 * Exhaustively going through the calendar and printing active service IDs and number of trips for that day.
 * Dates with no active service
