package com.conveyal.gtfs.validator.json.test;

import com.conveyal.gtfs.validator.json.FeedProcessor;
import com.conveyal.gtfs.validator.json.FeedValidationResultSet;
import com.conveyal.gtfs.validator.json.backends.FileSystemFeedBackend;
import com.conveyal.gtfs.validator.json.serialization.JsonSerializer;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.TimeZone;
import java.util.stream.Stream;

import static org.junit.Assert.fail;

public class JsonOutputTest {

    @BeforeClass
    public static void setUpClass() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    private Stream<Path> getZipFiles() throws IOException {
        Path thisDir = Paths.get("src/test/resources");
        return Files.list(thisDir).filter(p -> p.getFileName().toString().endsWith(".zip"));
    }

    @Test
    public void testAllGtfs() {
        System.out.println("Starting JSON output test (output suppressed)");

        PrintStream originalStream = System.out;

        try (Stream<Path> paths = getZipFiles()) {
            paths.forEach(p -> {
                try {
                    outputJson(new String[]{p.toString()});
                } catch (IOException e) {
                    e.printStackTrace();
                    fail(e.getMessage());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        } finally {
            System.setOut(originalStream);
        }
    }

    /**
     * Writes the results of validation to JSON for each provided GTFS dataset
     *
     * @param paths array of String paths to GTFS zip files
     * @throws IOException
     */
    private void outputJson(String[] paths) throws IOException {
        for (String path : paths) {
            FileSystemFeedBackend backend = new FileSystemFeedBackend();
            FeedValidationResultSet results = new FeedValidationResultSet();
            File input = backend.getFeed(path);
            FeedProcessor processor = new FeedProcessor(input);
            processor.run();
            results.add(processor.getOutput());
            JsonSerializer serializer = new JsonSerializer(results);
            String saveFilePath = input.getName() + "_out.json";
            serializer.serializeToFile(new File(saveFilePath));
        }
    }
}

