package org.almouro.wiki.utils;

import java.util.LinkedHashMap;

public class NextPageGetter{
	
	private class TagOccurences{
		private String tagName = null;
		private int n = 0;
		
		public void add(String tag){
			if(isEmpty()){
				this.tagName = tag;
				n++;
			}
			else if(this.tagName.equals(tag)) n++;
		}
		
		public void remove(){
			n--;
			if(n <= 0) {
				tagName = null;
				n = 0;
			}
		}
		
		public boolean isEmpty(){
			return tagName == null;
		}
		
		public boolean isResultTag(){
			return "]]".equals(tagName) && n == 1;
		}
	}
	
	private static final LinkedHashMap<String, String> tagsToStrip = new LinkedHashMap<>();
	static {
		tagsToStrip.put("{{{", "}}}");
		tagsToStrip.put("{{", "}}");
		tagsToStrip.put("{", "}");
		tagsToStrip.put("((", "))");
		tagsToStrip.put("(", ")");
		tagsToStrip.put("[[", "]]");
		tagsToStrip.put("<!--", "-->");
		tagsToStrip.put("<", "</");
	}
	
	private int cursorIndex = 0;
	private char[] chars;
	
	private TagOccurences closingTagsToFind;
	
	private boolean isCursorAtTag(String tag){
		
		if(cursorIndex + tag.length() > chars.length)
			return false;
		
		for(int i = 0; i < tag.length(); i++){
			if(chars[cursorIndex + i] != tag.charAt(i))
				return false;
		}
		
		//Fix to prevent </ matched with <
		if(tag.equals("<") && cursorIndex + 1 < chars.length && chars[cursorIndex + 1] == '/')
			return false;
		
		//Move cursor after tag for next iteration of cursorIndex
		cursorIndex += tag.length() - 1;
		
		return true;
	}
	
	private String getClosingTagNeededIfCursorAtOpeningTag(){
		for (String tag : tagsToStrip.keySet()) {
			if(isCursorAtTag(tag)){
				return tagsToStrip.get(tag);
			}
		}
		
		return null;
	}
	
	private String readLink(){
		String result = "";
		for (cursorIndex = cursorIndex + 1; cursorIndex < chars.length-1; cursorIndex++) {
			if(chars[cursorIndex] == '|'){
				if(!WikipediaPageUtils.isMediaPage(result))
					return result;
				break;
			}
			else if(isCursorAtTag("]]")){
				if(!WikipediaPageUtils.isMediaPage(result))
					return result;
				else{
					closingTagsToFind.remove();
				}
				break;
			}
			else
				result += chars[cursorIndex];
		}
		return null;
	}
	
	public String readAndGetLink(String pageText){		
		chars = pageText.toCharArray();
		cursorIndex = 0;
		
		closingTagsToFind = new TagOccurences();
		
		for (cursorIndex = 0; cursorIndex < chars.length - 1; cursorIndex++) {	
			String potentialClosingTagToFind = getClosingTagNeededIfCursorAtOpeningTag();
			
			if(potentialClosingTagToFind != null){
				closingTagsToFind.add(potentialClosingTagToFind);
				
				if(closingTagsToFind.isResultTag() && potentialClosingTagToFind.equals("]]")){

					String potentialResult = readLink();
					if(potentialResult != null)
						return potentialResult;
				}
			}
			
			else if(!closingTagsToFind.isEmpty() && isCursorAtTag(closingTagsToFind.tagName)){
				closingTagsToFind.remove();
			}
		}
		
		return "";
	}
}
