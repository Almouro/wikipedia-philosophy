package org.almouro.wiki.utils;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class WikipediaUtilsTest {

    @Test
    public void testGetNextPageSimple() {
	String nextPageShouldBeSuccess = "The next page is [[Success]]. [[Fail]]";
	assertEquals("Success",
		WikipediaPageUtils.getNextPage(nextPageShouldBeSuccess));
    }

    @Test
    public void testGetNextPageFormat() {
	String nextPageShouldBeSuccess = "The next page is [[success]]. ";
	assertEquals("Success",
		WikipediaPageUtils.getNextPage(nextPageShouldBeSuccess));
    }

    @Test
    public void testGetNextPageWithMultipleTagsToStrip() {
	String nextPageShouldBeSuccess = "<div fail>{{[[[[Fail1]]]]{{This is a [[Fail2]].}}[[Fail3]]}} This is a [[Fail4]] . </div> [[Success]]";
	assertEquals("Success",
		WikipediaPageUtils.getNextPage(nextPageShouldBeSuccess));
    }

    @Test
    public void testGetNextPageWithDivs() {
	String nextPageShouldBeSuccess = "<hello> <div fail> [[Fail1]]</div> [[Fail2]]</hello>[[Success]]";
	assertEquals("Success",
		WikipediaPageUtils.getNextPage(nextPageShouldBeSuccess));
    }

    @Test
    public void testGetNextPageWithVerticalBar() {
	String nextPageShouldBeSuccess = "The next page is [[Success|Fail]]";
	assertEquals("Success",
		WikipediaPageUtils.getNextPage(nextPageShouldBeSuccess));
    }

    @Test
    public void testGetNextPageWithSimpleMediaLink() {
	String nextPageShouldBeSuccess = "This is a [[Image:Fail.html|Fail]] but this is [[Success]]";
	assertEquals("Success",
		WikipediaPageUtils.getNextPage(nextPageShouldBeSuccess));
    }

    @Test
    public void testGetNextPageWithComplexNonArticleLink() {
	String nextPageShouldBeSuccess = "[[File:f|([[Fail]]) ]][[Success]]";
	assertEquals("Success",
		WikipediaPageUtils.getNextPage(nextPageShouldBeSuccess));
    }

}
