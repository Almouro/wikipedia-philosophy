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
    
    private static class PageInfoIterator implements Iterator<PageInfo>{
        
        private XMLStreamReader xmlReader;
        private PageInfo currentPage = null;
        private String currentEltName = null;
        
        public PageInfoIterator(String document) throws XMLStreamException, FactoryConfigurationError{
            this.xmlReader = XMLInputFactory.newInstance().createXMLStreamReader(
                    new ByteArrayInputStream(document.getBytes()));;
        }
        
        private void processStartXmlElement() throws XMLStreamException{
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
            
            // TODO getElementText can return a huge text -> process text chunk by chunk with getText instead
            else if (ElementNames.TEXT_ELEMENT_NAME.equals(currentEltName)
                    && currentPage.getNextPage() == null) {
                currentPage.setNextPage(WikipediaPageUtils.getNextPage(xmlReader
                        .getElementText()));
            }
        }
        
        //Easier to read
        private boolean hasNextThrowing() throws XMLStreamException{
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

    private static Iterator<PageInfo> getPagesIterator(String document) throws XMLStreamException,
            FactoryConfigurationError {
        
        return new PageInfoIterator(document);
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

}
