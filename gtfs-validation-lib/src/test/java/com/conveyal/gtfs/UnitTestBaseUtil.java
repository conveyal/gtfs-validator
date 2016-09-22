package com.conveyal.gtfs;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

public class UnitTestBaseUtil {

	public UnitTestBaseUtil() {
		super();
	}

	protected void setDummyPrintStream() {
		PrintStream dummyStream = new  PrintStream(new OutputStream() {
			@Override
			public void write(int b) throws IOException {}
		});
		System.setOut(dummyStream);
	}

}