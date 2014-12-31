package org.almouro.wiki;

public class PageInfo {

    private String title;
    private String nextPage;
    private boolean isRedirectedPage;

    public String getTitle() {
	return title;
    }

    public void setTitle(String title) {
	this.title = title;
    }

    public String getNextPage() {
	return nextPage;
    }

    public void setNextPage(String nextPage) {
	this.nextPage = nextPage;
    }

    public boolean isRedirectedPage() {
	return isRedirectedPage;
    }

    public void setRedirectedPage(boolean isRedirectedPage) {
	this.isRedirectedPage = isRedirectedPage;
    }
}