package com.softgame.reddit.model;

import java.util.ArrayList;

import org.json.custom.JSONArray;
import org.json.custom.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

public class MessageModel implements Parcelable {
	public ArrayList<MessageItem> mItemList;
	// String kind;
	public String after;
	public MessageItemIndex mIndexList;

	public MessageModel() {
		mItemList = new ArrayList<MessageItem>();
		mIndexList = new MessageItemIndex();
	}

	public static void convertToList(JSONObject srJSObject, MessageModel model) {
		// model.kind = srJSObject.optString("kind", "");
		JSONObject data = srJSObject.optJSONObject("data");
		model.after = data.optString("after", "");
		JSONArray children = data.optJSONArray("children");
		if (children == null) {
			return;
		}
		for (int i = 0; i < children.length(); i++) {
			JSONObject childData = children.optJSONObject(i);
			if (childData != null) {
				MessageItem s = new MessageItem(childData);
				model.mItemList.add(s);
			}
		}

	}

	public String getSender(String loginUser) {
		for (int i = 0; i < mIndexList.getCount(); i++) {
			MessageItem item = mIndexList.getItem(i);
			if (!loginUser.equalsIgnoreCase(item.author)) {
				return item.author;
			}
		}
		return loginUser;

	}

	public void clear() {
		mItemList.clear();
		mIndexList.clear();
	}

	// convert and add to index list
	public void convertIndexList(MessageItem item) {
		mIndexList.convertAndAddMessageItem(item);
	}

	public void addToIndexList(MessageItem item) {
		mIndexList.addMessageItem(item);
	}

	public int getIndexListSize() {
		return mIndexList.getCount();
	}

	public int getItemsSize() {
		if (mItemList == null)
			return 0;
		return mItemList.size();
	}

	public MessageItem getItemByIndex(int index) {
		return mItemList.get(index);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeTypedList(mItemList);
		// dest.writeList(mItemList);
		// dest.writeString(modhash);
		// dest.writeString(kind);
		dest.writeString(after);
		// dest.writeString(before);
	}

	public static final Parcelable.Creator<MessageModel> CREATOR = new Parcelable.Creator<MessageModel>() {
		public MessageModel createFromParcel(Parcel in) {
			return new MessageModel(in);
		}

		public MessageModel[] newArray(int size) {
			return new MessageModel[size];
		}
	};

	private MessageModel(Parcel in) {
		in.readTypedList(mItemList, MessageItem.CREATOR);
		// in.readList(mItemList, SubRedditItem.class.getClassLoader());
		// modhash = in.readString();
		// kind = in.readString();
		after = in.readString();
		// before = in.readString();
	}

}
