package org.almouro.wiki;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.logging.Logger;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.almouro.util.FileUtils;
import org.almouro.wiki.utils.WikipediaPageUtils;

public class PagesXmlReader {

	private final static XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();

	private static class ElementNames {
		public static final String PAGE_ELEMENT_NAME = "page";
		public static final String TITLE_ELEMENT_NAME = "title";
		public static final String TEXT_ELEMENT_NAME = "text";
		public static final String REDIRECT_ELEMENT_NAME = "redirect";
	}

	private static class PageInfoIterator implements Iterator<PageInfo> {

		private XMLStreamReader xmlReader;
		private PageInfo currentPage = null;
		private String currentEltName = null;

		public PageInfoIterator(XMLStreamReader xmlReader) {
			this.xmlReader = xmlReader;
		}

		private void processStartXmlElement() throws XMLStreamException {
			currentEltName = xmlReader.getLocalName();

			if (ElementNames.PAGE_ELEMENT_NAME.equals(currentEltName))
				currentPage = new PageInfo();

			else if (ElementNames.REDIRECT_ELEMENT_NAME.equals(currentEltName)) {
				currentPage.setRedirectedPage(true);
				currentPage.setNextPage(xmlReader.getAttributeValue(null, "title"));
			}

			else if (ElementNames.TITLE_ELEMENT_NAME.equals(currentEltName)) {
				currentPage.setTitle(xmlReader.getElementText());
			}

			// TODO getElementText can return a huge text -> process text chunk
			// by chunk with getText instead
			else if (ElementNames.TEXT_ELEMENT_NAME.equals(currentEltName)
					&& currentPage.getNextPage() == null) {
				String text = xmlReader.getElementText();
				currentPage.setNextPage(WikipediaPageUtils.getNextPage(text));

			}
		}

		// Easier to read
		private boolean hasNextThrowing() throws XMLStreamException {
			while (xmlReader.hasNext()) {
				int code = xmlReader.next();

				switch (code) {

				case XMLStreamConstants.START_ELEMENT:
					processStartXmlElement();
					break;

				case XMLStreamConstants.END_ELEMENT:
					currentEltName = null;
					if (xmlReader.getLocalName().equals(ElementNames.PAGE_ELEMENT_NAME)
							&& currentPage != null) {
						return true;
					}

					break;
				}
			}
			xmlReader.close();
			return false;
		}

		@Override
		public boolean hasNext() {
			try {
				return hasNextThrowing();
			} catch (XMLStreamException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return false;
		}

		@Override
		public PageInfo next() {
			try {
				return currentPage;
			} finally {
				// currentPage = null;
			}
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}

	}

	private static Iterable<PageInfo> readPagesFromXml(final XMLStreamReader xmlStreamReader) {
		return new Iterable<PageInfo>() {

			@Override
			public Iterator<PageInfo> iterator() {
				try {
					return new PageInfoIterator(xmlStreamReader);
				} catch (FactoryConfigurationError e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return null;
			}
		};
	}

	public static Iterable<PageInfo> readPagesFromXml(InputStream is) throws XMLStreamException {
		return readPagesFromXml(xmlInputFactory.createXMLStreamReader(is));
	}

	public static Iterable<PageInfo> readPagesFromXml(Reader reader) throws XMLStreamException {
		return readPagesFromXml(xmlInputFactory.createXMLStreamReader(reader));
	}

	public static Iterable<PageInfo> readPagesFromXml(String document) throws XMLStreamException,
			UnsupportedEncodingException {
		return readPagesFromXml(xmlInputFactory.createXMLStreamReader(new ByteArrayInputStream(
				document.getBytes("UTF-8"))));
	}

	private static final Logger logger = Logger.getLogger(PagesXmlReader.class.getName());

	private static double nbPagesProcessed = 0;

	public static void main(String[] args) throws Exception {

		BufferedReader reader = FileUtils.getBufferedReader(args[0]);
		BufferedWriter writer = FileUtils.getBufferedWriter(args[1]);

		BufferedWriter specialPages = FileUtils.getBufferedWriter("specialPages.txt");
		BufferedWriter specialPagesPointing = FileUtils
				.getBufferedWriter("templates.txt");

		for (PageInfo page : PagesXmlReader.readPagesFromXml(reader)) {
			String result = page.getNextPage() + FileUtils.TAB + page.getTitle()
					+ (page.isRedirectedPage() ? FileUtils.TAB + 1 : "") + FileUtils.LINE_BREAK;

			if (page.getTitle().length() > 0 && page.getNextPage().length() > 0
					&& !WikipediaPageUtils.isSpecialPage(page.getTitle()))
				writer.write(result);
			
			nbPagesProcessed++;
			if (nbPagesProcessed % 16000 == 0) {
				logger.info("" + nbPagesProcessed / 160000);
			}

/*			if (page.getTitle().contains(":") && !WikipediaPageUtils.isSpecialPage(page.getTitle())) {
				specialPages.write(page.getTitle() + FileUtils.LINE_BREAK);
			}*/
			if (!WikipediaPageUtils.isSpecialPage(page.getTitle())
					&& page.getNextPage().startsWith("Template:")) {
				specialPagesPointing.write(result);
			}
		}

		reader.close();
		writer.flush();
		writer.close();
		specialPages.flush();
		specialPages.close();
		specialPagesPointing.flush();
		specialPagesPointing.close();

	}

}
