## gtfs-validator-json: run feeds through the conveyal [gtfs-validator library](https://www.github.com/conveyal/gtfs-validator) and generate JSON-formatted reports

This is a modular project; it can be used directly from the command line, e.g.:

`java -Xmx6G -jar gtfs-validator-json.jar /path/to/gtfs.zip /path/to/output.json`

or you can wire the classes together yourself. There are several important components:
- FeedBackends: these represent a way to store feeds (for instance, file systems or s3 buckets). The only requirement is that each feed can be retrieved from an ID that can be stored as a string.
- Serializers: these represent how to serialize a FeedValidationResults object to a stream. Right now we use JSON, one could also imagine many other potential formats.
- FeedProcessor: this takes a feed, runs validation, and returns FeedValidationResults. There is generally no reason to subclass this.