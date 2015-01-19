package org.almouro.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class FileUtils {
	public static final String LINE_BREAK = System.getProperty("line.separator");
	public static final String TAB = "\t";

	public static BufferedReader getBufferedReader(String file) throws Exception {
		return new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
	}

	public static BufferedWriter getBufferedWriter(String file) throws IOException {
		return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));
	}
}
