## gtfs-validator-webapp: inspect the output of the GTFS validator

This is a UI for viewing the results of a GTFS validator run on a set of feeds. It reads in a JSON file, which for now must be called out.json in the same folder as index.html, and then displays the results ina browser-based UI.

To set up the project, first run gtfs-validator-json on a feed or set of feeds. Put the JSON file generated into the gtfs-validator-webapp directory, and call it out.json. In that directory, run `component build` to build the app and then start an HTTP server (easiest way is `python3 -m http.server 8000 -b 127.0.0.1`). Browse to the server to see the results.
