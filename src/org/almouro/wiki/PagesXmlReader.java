package org.almouro.wiki;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.almouro.wiki.utils.WikipediaPageUtils;
import org.apache.log4j.Logger;

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
				try {
					WikipediaPageUtils.writePageToFile("F:/Wiki/pages2/", currentPage.getTitle(),
							text, nbPagesProcessed + 1);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
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

	public static Iterable<PageInfo> readPagesFromXml(FileInputStream fis)
			throws XMLStreamException {
		return readPagesFromXml(xmlInputFactory.createXMLStreamReader(fis));
	}

	public static Iterable<PageInfo> readPagesFromXml(String document) throws XMLStreamException {
		return readPagesFromXml(xmlInputFactory.createXMLStreamReader(new ByteArrayInputStream(
				document.getBytes())));
	}

	private static final Logger logger = Logger.getLogger(PagesXmlReader.class);

	private static long nbPagesProcessed = 0;

	public static void main(String[] args) throws XMLStreamException, IOException {

		FileInputStream fis = new FileInputStream(args[0]);
		FileOutputStream fos = new FileOutputStream(args[1]);

		for (PageInfo page : PagesXmlReader.readPagesFromXml(fis)) {
			String result = page.getNextPage() + "\t" + page.getTitle()
					+ WikipediaPageUtils.LINE_BREAK;
			fos.write(result.getBytes());
			nbPagesProcessed++;
			if (nbPagesProcessed % 16000 == 0) {
				logger.info("" + nbPagesProcessed / 160000);
			}
		}

		fis.close();
		fos.close();

	}

}
