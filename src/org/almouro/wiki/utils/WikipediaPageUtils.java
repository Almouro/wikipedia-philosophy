package org.almouro.wiki.utils;

import java.io.BufferedWriter;
import java.io.IOException;

import org.almouro.util.FileUtils;

public class WikipediaPageUtils {

	private static String[] MEDIA_PREFIX = new String[] { "File", "Image" };

	private static String[] WIKIPEDIA_NAMESPACE_PREFIX = new String[] { "Talk", "User",
			"Wikipedia", "MediaWiki", "Template", "Help", "Category", "Portal", "Book", "Draft",
			"Education Program", "TimedText", "Module", "Topic", "WP", "WT", "Special", "CAT",
			"MOS", "H", "P", "T", "MP", "WikiProject", "Wikiproject", "Mos", "MoS" };

	public static boolean isSpecialPage(String title) {
		for (String prefix : WIKIPEDIA_NAMESPACE_PREFIX) {
			if (doesTitleStartWithWikiPrefix(title, prefix)
					|| doesTitleStartWithWikiPrefix(title, prefix + " Talk"))
				return true;
		}
		return isMediaPage(title);
	}

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
		String filePath = outputFolder + pageTitle.replaceAll("[:\\\\/*\"?|<>]", "_") + "_page"
				+ pageNumber + ".txt";
		BufferedWriter writer = FileUtils.getBufferedWriter(filePath);

		writer.write(pageTitle);
		writer.write(FileUtils.LINE_BREAK + FileUtils.LINE_BREAK);
		writer.write(pageText);
		writer.flush();
		writer.close();
	}
}
