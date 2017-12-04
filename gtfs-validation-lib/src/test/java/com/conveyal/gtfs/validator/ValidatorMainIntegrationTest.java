package com.conveyal.gtfs.validator;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.TimeZone;
import java.util.stream.Stream;

import org.junit.BeforeClass;
import org.junit.Test;

import com.conveyal.gtfs.UnitTestBaseUtil;

public class ValidatorMainIntegrationTest extends UnitTestBaseUtil {
	@BeforeClass
	public static void setUpClass(){
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
	}

	private Stream<Path> getZipFiles() throws IOException {
		Path thisDir = Paths.get("src/test/resources");

		if (!thisDir.toFile().exists())
			thisDir = Paths.get("gtfs-validation-lib/src/test/resources");

		if (!thisDir.toFile().exists()) {
			System.err.println("invalid working directory=" + Paths.get(""));
		}

		return Files.list(thisDir).filter(p -> p.getFileName().toString().endsWith(".zip"));
	}

//	@Test
	public void testProblem(){
		
//		setDummyPrintStream();
		
		ValidatorMain.main(new String[] {"src/test/resources/"
				+ "st_gtfs_good"
				+ ".zip"});
	}

	@Test
	public void testAllGtfs() {
		System.out.println("Starting Integration Level Test on ValidatorMain (output suppressed)");
		
		PrintStream originalStream = System.out;

		setDummyPrintStream();
		
		try (Stream<Path> paths = getZipFiles()) {
			paths
			.filter(p -> !p.endsWith("gtfs_two_agencies.zip"))
			.filter(p -> !p.endsWith("20170119.zip"))
			.forEach(p -> ValidatorMain.main(new String[] {p.toString()}));
		} catch (IOException e) {
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

