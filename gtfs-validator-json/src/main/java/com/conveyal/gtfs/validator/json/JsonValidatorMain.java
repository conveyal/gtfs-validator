package com.conveyal.gtfs.validator.json;

import java.io.File;
import java.io.IOException;

import com.conveyal.gtfs.validator.json.backends.FileSystemFeedBackend;
import com.conveyal.gtfs.validator.json.serialization.JsonSerializer;

public class JsonValidatorMain {

    /**
     * Take an input GTFS and an output file and write JSON to that output file summarizing validation of the GTFS.
     * @param args
     */
    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("usage: java -Xmx[several]G input_gtfs.zip [other_gtfs.zip third_gtfs.zip . . .] output_file.json");
            return;
        }

        // We use a file system backend because we're not doing anything fancy, just reading local GTFS
        FileSystemFeedBackend backend = new FileSystemFeedBackend();
        File input = backend.getFeed(args[0]);
        FeedProcessor processor = new FeedProcessor(input);
        try {
            processor.run();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Unable to access input GTFS " + input.getPath() + ". Does the file exist and do I have permission to read it?");
            return;
        }
        FeedValidationResults out = processor.getOutput();
        JsonSerializer serializer = new JsonSerializer(out);
        // TODO: error handling
        serializer.serializeToFile(new File(args[1]));

    }

}
