package org.almouro.wikihadoop;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;

import org.almouro.wiki.PageInfo;
import org.junit.Test;

public class PagesXmlReaderTest {

    private static final String TEST_DATA_FOLDER = "test/org/almouro/wikihadoop/test-data/";

    private static String getFileAsString(String filePath) throws IOException {
	return new String(Files.readAllBytes(Paths.get(filePath)));
    }

    private static String getPageText(String pageName) throws IOException {
	return getFileAsString(TEST_DATA_FOLDER + pageName + ".xml");
    }

    @Test
    public void testReadPagesMultiple() throws IOException {
	Iterator<PageInfo> pages = PagesXmlReader.readPagesFromXml(
		getPageText("multiple")).iterator();
	int pageCount = 0;
	for (; pages.hasNext(); ++pageCount)
	    pages.next();

	assertEquals(2, pageCount);
    }

    @Test
    public void testReadPagesXmlSimple() throws IOException {
	Iterable<PageInfo> pages = PagesXmlReader
		.readPagesFromXml(getPageText("simple"));
	for (PageInfo page : pages) {
	    assertEquals("Simple page", page.getTitle());
	    assertEquals("Next page", page.getNextPage());
	    assertEquals(false, page.isRedirectedPage());
	}
    }

    @Test
    public void testReadPagesXmlWithRedirect() throws IOException {
	Iterable<PageInfo> pages = PagesXmlReader
		.readPagesFromXml(getPageText("redirect"));
	for (PageInfo page : pages) {
	    assertEquals("Action film", page.getNextPage());
	    assertEquals(true, page.isRedirectedPage());
	}
    }

}
