package org.almouro.wiki.utils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

//TODO Clean code
public class NextPageGetter {

	private class TagOccurences {
		private String tagName = null;
		private int n = 0;

		public void add(String tag) {
			if (isEmpty()) {
				this.tagName = tag;
				n++;
			} else if (this.tagName.equals(tag))
				n++;
		}

		public void remove() {
			n--;
			if (n <= 0) {
				tagName = null;
				n = 0;
			}
		}

		public boolean isEmpty() {
			return tagName == null;
		}

		public boolean isResultTag() {
			return "]]".equals(tagName) && n == 1;
		}
	}

	private static final List<String> tagsList = new ArrayList<>();
	private static final LinkedHashMap<String, String> tagsToStrip = new LinkedHashMap<>();
	static {
		tagsToStrip.put("{{", "}}");
		tagsToStrip.put("{", "}");
		tagsToStrip.put("((", "))");
		tagsToStrip.put("(", ")");
		tagsToStrip.put("[[", "]]");
		tagsToStrip.put("<br", ">");
		tagsToStrip.put("<!--", "-->");
		tagsToStrip.put("<", "</");
		
		for (String tag : tagsToStrip.keySet()) {
			tagsList.add(tag);
		}
		for (String tag : tagsToStrip.values()) {
			tagsList.add(tag);
		}
	}

	private int cursorIndex = 0;
	private String currentTag = null;
	private char[] chars;

	private TagOccurences closingTagsToFind;
	
	private void setCurrentTag(){
		for (String tag : tagsList) {
			if(isCursorAtTag(tag)){
				currentTag = tag;
				return;
			}
		}
		currentTag = null;
	}
	
	private void moveCursor(){
		cursorIndex += Math.max(1, currentTag != null ? currentTag.length() : 0);
		setCurrentTag();
	}
	
	private boolean isCurrentTagThisTag(String tag){
		return tag.equals(currentTag);
	}

	private boolean isCursorAtTag(String tag) {

		if (cursorIndex + tag.length() > chars.length)
			return false;

		for (int i = 0; i < tag.length(); i++) {
			if (chars[cursorIndex + i] != tag.charAt(i))
				return false;
		}

		// Fix to prevent </ matched with <
		if (tag.equals("<") && cursorIndex + 1 < chars.length && chars[cursorIndex + 1] == '/')
			return false;

		return true;
	}

	private String readLink() {
		String result = "";
		moveCursor();
		for (; cursorIndex < chars.length - 1; moveCursor()) {
			if (chars[cursorIndex] == '|') {
				if (!WikipediaPageUtils.isSpecialPage(result))
					return result;
				break;
			}
			else if (isCurrentTagThisTag("[[")){
				return readLink();
			}
			else if (isCurrentTagThisTag("]]")) {
				if (!WikipediaPageUtils.isSpecialPage(result))
					return result;
				else {
					closingTagsToFind.remove();
				}
				break;
			} else
				result += chars[cursorIndex];
		}
		return null;
	}

	public String readAndGetLink(String pageText) {
		chars = pageText.toCharArray();
		cursorIndex = 0;
		setCurrentTag();
		
		closingTagsToFind = new TagOccurences();

		for (; cursorIndex < chars.length - 1; moveCursor()) {
			String potentialClosingTagToFind = tagsToStrip.get(currentTag);

			if (potentialClosingTagToFind != null) {
				closingTagsToFind.add(potentialClosingTagToFind);

				if (closingTagsToFind.isResultTag() && isCurrentTagThisTag("[[")) {

					String potentialResult = readLink();
					if (potentialResult != null)
						return potentialResult;
				}
			}

			else if (!closingTagsToFind.isEmpty() && isCurrentTagThisTag(closingTagsToFind.tagName)) {
				closingTagsToFind.remove();
			}
		}

		return "";
	}
}
