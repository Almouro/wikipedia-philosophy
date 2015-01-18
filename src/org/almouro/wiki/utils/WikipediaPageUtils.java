package org.almouro.wiki.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class WikipediaPageUtils {

	public static final String LINE_BREAK = System.getProperty("line.separator");

	private static String[] MEDIA_PREFIX = new String[] { "File", "Image" };

	// not exhaustive -> Easier to check list of wikipedia pages
	// TODO WIKIPEDIA_NAMESPACE_PREFIX
	/*
	 * private static String[] WIKIPEDIA_NAMESPACE_PREFIX = new String[]
	 * {"Talk", "User", "Wikipedia", "MediaWiki", "Template", "Help",
	 * "Category", "Portal", "Book", "Draft", "Education Program", "TimedText",
	 * "Module", "Topic"};
	 */

	public static String getNextPage(String pageText) {
		return getFormattedTitle(new NextPageGetter().readAndGetLink(pageText));
	}

	private static boolean doesTitleStartWithWikiPrefix(String title, String prefix) {
		String pref = prefix + ":";
		return title.startsWith(pref) || title.startsWith(":" + pref);
	}

	public static boolean isMediaPage(String title) {
		for (String mediaPrefix : MEDIA_PREFIX) {
			if (doesTitleStartWithWikiPrefix(title, mediaPrefix))
				return true;
		}
		return false;
	}

	/*
	 * 1. Ignore # links
	 * 
	 * 2. The MediaWiki software is configured so that a page title (as stored
	 * in the database) cannot begin with a lower-case letter, and links that
	 * begin with a lower-case letter are treated as if capitalized, i.e.
	 * [[foo]] is treated the same as [[Foo]].
	 */
	public static String getFormattedTitle(String str) {
		String[] split = str.split("#");
		if (split.length == 0)
			return str;

		str = split[0];

		return str.length() > 0 ? str.substring(0, 1).toUpperCase() + str.substring(1) : "";
	}

	/**
	 * For debugging purposes, output all pages in a folder
	 * 
	 * @param outputFolder
	 * @param pageTitle
	 * @param pageText
	 * @param pageNumber
	 * @throws IOException
	 */
	public static void writePageToFile(String outputFolder, String pageTitle, String pageText,
			long pageNumber) throws IOException {
		
		FileOutputStream fs = new FileOutputStream(new File(outputFolder
				+ pageTitle.replaceAll("[:\\\\/*\"?|<>]", "_") + "_page" + pageNumber + ".txt"));

		fs.write(pageTitle.getBytes());
		fs.write((LINE_BREAK + LINE_BREAK).getBytes());
		fs.write(pageText.getBytes());

		fs.close();
	}
}
