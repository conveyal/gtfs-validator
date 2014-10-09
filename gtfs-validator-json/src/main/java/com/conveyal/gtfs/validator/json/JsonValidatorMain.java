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
        
        // Since we're processing multiple feeds (potentially), use a FeedValidationResultSet to save the output
        FeedValidationResultSet results = new FeedValidationResultSet();
        
        // default name is directory name
        results.name = new File(args[0]).getAbsoluteFile().getParentFile().getName();
        
        // loop over all arguments except the last (which is the name of the JSON file)
        // TODO: throw all feeds in a queue and run as many threads as we have cores to do the validation
        // not exactly urgent, since for all of New York State this takes only a few minutes on my laptop
        for (int i = 0; i < args.length - 1; i++) {
            File input = backend.getFeed(args[i]);
            System.err.println("Processing feed " + input.getName());
            FeedProcessor processor = new FeedProcessor(input);
            try {
                processor.run();
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("Unable to access input GTFS " + input.getPath() + ". Does the file exist and do I have permission to read it?");
                return;
            }

            results.add(processor.getOutput());
        }
        
        JsonSerializer serializer = new JsonSerializer(results);
        // TODO: error handling
        serializer.serializeToFile(new File(args[args.length - 1]));

    }

}
