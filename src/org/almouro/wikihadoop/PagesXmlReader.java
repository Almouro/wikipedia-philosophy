package org.almouro.wikihadoop;

import java.io.ByteArrayInputStream;
import java.util.Iterator;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.almouro.wiki.PageInfo;
import org.almouro.wiki.utils.WikipediaPageUtils;

class PagesXmlReader {

    private static class ElementNames {
	public static final String PAGE_ELEMENT_NAME = "page";
	public static final String TITLE_ELEMENT_NAME = "title";
	public static final String TEXT_ELEMENT_NAME = "text";
	public static final String REDIRECT_ELEMENT_NAME = "redirect";
    }

    public static Iterable<PageInfo> readPagesFromXml(final String document) {
	return new Iterable<PageInfo>() {

	    @Override
	    public Iterator<PageInfo> iterator() {
		try {
		    return getPagesIterator(document);
		} catch (XMLStreamException | FactoryConfigurationError e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		}
		return null;
	    }
	};
    }

    private static Iterator<PageInfo> getPagesIterator(String document)
	    throws XMLStreamException, FactoryConfigurationError {
	final XMLStreamReader xmlReader = XMLInputFactory.newInstance()
		.createXMLStreamReader(
			new ByteArrayInputStream(document.getBytes()));

	return new Iterator<PageInfo>() {

	    private PageInfo currentPage = null;

	    @Override
	    public boolean hasNext() {
		try {
		    while (xmlReader.hasNext()) {
			int code = xmlReader.next();

			switch (code) {

			case XMLStreamConstants.START_ELEMENT:
			    String currentEltName = xmlReader.getLocalName();

			    if (ElementNames.PAGE_ELEMENT_NAME
				    .equals(currentEltName))
				currentPage = new PageInfo();
			    else if (ElementNames.REDIRECT_ELEMENT_NAME
				    .equals(currentEltName)) {
				currentPage.setRedirectedPage(true);
				currentPage.setNextPage(xmlReader
					.getAttributeValue(null, "title"));
			    } else if (ElementNames.TITLE_ELEMENT_NAME
				    .equals(currentEltName)) {
				currentPage
					.setTitle(xmlReader.getElementText());
			    } else if (ElementNames.TEXT_ELEMENT_NAME
				    .equals(currentEltName)
				    && currentPage.getNextPage() == null) {
				currentPage
					.setNextPage(WikipediaPageUtils
						.getNextPage(xmlReader
							.getElementText()));
			    }

			    break;

			case XMLStreamConstants.END_ELEMENT:
			    currentEltName = null;
			    if (xmlReader.getLocalName().equals(
				    ElementNames.PAGE_ELEMENT_NAME)
				    && currentPage != null) {
				return true;
			    }

			    break;
			}
		    }
		} catch (XMLStreamException e) {
		    e.printStackTrace();
		}
		return false;
	    }

	    @Override
	    public void remove() {
		throw new UnsupportedOperationException();
	    }

	    @Override
	    public PageInfo next() {
		try {
		    return currentPage;
		} finally {
		    // currentPage = null;
		}
	    }
	};
    }

    /*
     * public static void readPagesFromXml(String document, Context context) {
     * 
     * PageInfo currentPage = null; String currentEltName = null;
     * 
     * try { XMLStreamReader xmlReader =
     * XMLInputFactory.newInstance().createXMLStreamReader(new
     * ByteArrayInputStream(document.getBytes()));
     * 
     * while(xmlReader.hasNext()){ int code = xmlReader.next();
     * 
     * switch (code) { case XMLStreamConstants.CHARACTERS:
     * if(ElementNames.TITLE_ELEMENT_NAME.equals(currentEltName))
     * currentPage.setTitle(xmlReader.getText()); else
     * if(ElementNames.TEXT_ELEMENT_NAME.equals(currentEltName) &&
     * currentPage.getNextPage() == null)
     * currentPage.setNextPage(WikipediaPageUtils
     * .getNextPage(xmlReader.getTextCharacters()));
     * 
     * break;
     * 
     * case XMLStreamConstants.START_ELEMENT: currentEltName =
     * xmlReader.getLocalName();
     * if(ElementNames.PAGE_ELEMENT_NAME.equals(currentEltName)) currentPage =
     * new PageInfo(); else
     * if(ElementNames.REDIRECT_ELEMENT_NAME.equals(currentEltName)){
     * currentPage.setRedirectedPage(true);
     * currentPage.setNextPage(xmlReader.getAttributeValue(null, "title")); }
     * 
     * break;
     * 
     * case XMLStreamConstants.END_ELEMENT:
     * if(xmlReader.getLocalName().equals(ElementNames.PAGE_ELEMENT_NAME) &&
     * currentPage != null){ //Do stuff currentPage = null; }
     * 
     * break; } }
     * 
     * xmlReader.close();
     * 
     * } catch (XMLStreamException | FactoryConfigurationError e) { // TODO
     * Auto-generated catch block e.printStackTrace(); } }
     */
}
