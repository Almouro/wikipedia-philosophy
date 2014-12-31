package org.almouro.wiki.utils;

public class WikipediaPageUtils {

    private static String[] MEDIA_PREFIX = new String[] { "File", "Image" };

    // not exhaustive -> Easier to check list of wikipedia pages
    /*
     * private static String[] WIKIPEDIA_NAMESPACE_PREFIX = new String[]
     * {"Talk", "User", "Wikipedia", "MediaWiki", "Template", "Help",
     * "Category", "Portal", "Book", "Draft", "Education Program", "TimedText",
     * "Module", "Topic"};
     */

    public static String getNextPage(String pageText) {
	return getFormattedTitle(new NextPageGetter().readAndGetLink(pageText));
    }

    private static boolean doesTitleStartWithWikiPrefix(String title,
	    String prefix) {
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
	str = str.split("#")[0];

	return str.length() > 0 ? str.substring(0, 1).toUpperCase()
		+ str.substring(1) : "";
    }
}
