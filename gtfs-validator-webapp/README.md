## gtfs-validator-webapp: inspect the output of the GTFS validator

This is a UI for viewing the results of a GTFS validator run on a set of feeds. It reads in a JSON file, which for now must be called out.json in the same folder as index.html, and then displays the results in a browser-based UI.

### Building gtfs-validator-webapp
*  Install [node.js](https://nodejs.org/en/) in gtfs-validator-webapp directory.
*  Open command prompt and execute, `npm install` in gtfs-validator-webapp directory. This will install all the required dependencies using *package.json* file.
*  Finally execute command `gulp build` in gtfs-alidator-webapp directory. This will build the app using *gulpfile.js* and produces build.js and build.css files in gtfs-validator-webapp/build directory.

### Viewing web results
Run gtfs-validator-json on a feed or set of feeds. Put the JSON file generated into the gtfs-validator-webapp directory, and call it `out.json`.

After starting a local server, view the web report by providing URL [localhost:8080/index.html?report=http://localhost:8080/out.json](localhost:8080/index.html?report=http://localhost:8080/out.json).

