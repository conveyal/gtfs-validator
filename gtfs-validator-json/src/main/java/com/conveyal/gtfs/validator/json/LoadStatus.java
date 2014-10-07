package com.conveyal.gtfs.validator.json;

/** Why a GTFS feed failed to load */
public enum LoadStatus {
	SUCCESS, INVALID_ZIP_FILE, OTHER_FAILURE, MISSING_REQUIRED_FIELD;
}
