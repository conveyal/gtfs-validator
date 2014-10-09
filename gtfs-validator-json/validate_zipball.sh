#!/bin/bash
# Run a set of feed stored in a zipfile through the validator
# Usage: validate_zipball.sh zipball.zip report.json
# The JSON report generated can be displayed with the Web UI

JARPATH=`dirname $0`/target/gtfs-validator-json.jar
TEMPROOT=`mktemp -d`

TEMP=${TEMPROOT}/$(basename -s .zip "$1")
mkdir "$TEMPROOT"

# unzip the gtfs
unzip "$1" -d "$TEMP"

# run the validator
java -Xmx8G -jar "$JARPATH" "${TEMP}"/*.zip "$2"

# clean up
rm -rf "$TEMPROOT"

