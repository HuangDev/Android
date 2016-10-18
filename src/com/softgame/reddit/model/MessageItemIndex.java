package com.softgame.reddit.model;

import java.util.ArrayList;

public class MessageItemIndex {

	public ArrayList<MessageItem> mMessageList;

	public MessageItemIndex() {
		mMessageList = new ArrayList<MessageItem>();
	}

	public void convertAndAddMessageItem(MessageItem messageItem) {
		messageItem.convertToIndexList(mMessageList);
	}
	
	public void addMessageItem(MessageItem messageItem){
		mMessageList.add(messageItem);
	}

	public int getCount() {
		return mMessageList.size();
	}

	public MessageItem getItem(int position) {
		return mMessageList.get(position);
	}

	public void clear() {
		mMessageList.clear();
	}
}
