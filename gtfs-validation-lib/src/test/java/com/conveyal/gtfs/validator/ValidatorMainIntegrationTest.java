package com.conveyal.gtfs.validator;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.TimeZone;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.BeforeClass;
import org.junit.Test;

public class ValidatorMainIntegrationTest {
	@BeforeClass
	public static void setUpClass(){
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
	}

	private Stream<Path> getZipFiles() throws IOException {
		Path thisDir = Paths.get("src/test/resources");
		
		return Files.list(thisDir).filter(p -> p.getFileName().toString().endsWith(".zip"));
	
	}

	@Test
	public void testAllGtfs() {
		System.out.println("Starting Integration Level Test on ValidatorMain (output suppressed)");
		
		PrintStream originalStream = System.out;
		
		PrintStream dummyStream = new  PrintStream(new OutputStream() {
			@Override
			public void write(int b) throws IOException {}
		});
		
		System.setOut(dummyStream);
		
		try (Stream<Path> paths = getZipFiles()) {
			paths
			.filter(p -> !p.endsWith("gtfs_two_agencies.zip"))
			.forEach(p -> ValidatorMain.main(new String[] {p.toString()}));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail(e.getMessage());
		} catch (Exception e){
			e.printStackTrace();
			fail(e.getMessage());
		} finally {
			System.setOut(originalStream);
		}
	}
}

