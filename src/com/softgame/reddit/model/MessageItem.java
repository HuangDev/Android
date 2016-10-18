package com.softgame.reddit.model;

import java.util.ArrayList;

import org.json.custom.JSONArray;
import org.json.custom.JSONObject;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.softgame.reddit.utils.RedditManager;

public class MessageItem implements Parcelable {
	public String kind;
	public String body;
	public boolean was_comment;
	public long first_message;
	public String name;
	public String dest;
	public String author;
	public long created_utc;
	public String subreddit;
	public String parent_id;
	public String context;
	public ArrayList<MessageItem> repliesList;
	public boolean isNew;
	public String id;
	public String subject;

	public MessageItem(JSONObject jsonObject) {

		repliesList = new ArrayList<MessageItem>();
		kind = jsonObject.optString("kind", "t1");
		JSONObject data = jsonObject.optJSONObject("data");
		setValueFromJSONObject(data);
	}

	public MessageItem() {
		repliesList = new ArrayList<MessageItem>();
	}

	public void addMessageItem(MessageItem messageItem) {
		repliesList.add(messageItem);
	}

	public String getMessageSender(Context context) {
		String loginUser = RedditManager.getUserName(context);
		if (loginUser != null && !"".equals(loginUser)) {
			if (author.equalsIgnoreCase(loginUser)) {
				return dest;
			} else {
				return author;
			}
		}
		return dest;
	}

	public String getParentId() {
		if (parent_id != null && !"".equals(parent_id)) {
			return parent_id;
		} else {
			return id;
		}

	}

	public int getCount() {
		int t = 1;

		if (repliesList != null && !repliesList.isEmpty()) {
			for (MessageItem reply : repliesList) {
				t = t + reply.getCount();
			}
		}
		return t;
	}

	public void setValueFromJSONObject(JSONObject jsonObject) {
		body = jsonObject.optString("body", "");
		was_comment = jsonObject.optBoolean("was_comment", true);
		first_message = jsonObject.optLong("first_message");
		name = jsonObject.optString("name");
		dest = jsonObject.optString("dest");
		isNew = jsonObject.optBoolean("new");
		author = jsonObject.optString("author");
		created_utc = jsonObject.optLong("created_utc");
		subreddit = jsonObject.optString("subreddit");
		parent_id = jsonObject.optString("parent_id");
		context = jsonObject.optString("context");
		id = jsonObject.optString("id");
		subject = jsonObject.optString("subject");

		JSONObject replies = jsonObject.optJSONObject("replies");
		if (replies != null && !replies.isEmpty()) {
			JSONArray jd = replies.optJSONObject("data").optJSONArray(
					"children");
			if (jd != null && !jd.isEmpty()) {
				for (int i = 0; i < jd.length(); i++) {
					JSONObject jobject = jd.optJSONObject(i);
					if (jobject != null) {
						MessageItem m = new MessageItem(jobject);
						repliesList.add(m);
					}
				}
			}
		}

	}

	public void convertToIndexList(ArrayList<MessageItem> indexList) {
		indexList.add(this);
		if (repliesList != null && !repliesList.isEmpty()) {
			for (MessageItem reply : repliesList) {
				reply.convertToIndexList(indexList);
			}
		}

	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int frag) {
		dest.writeString(kind);
		dest.writeString(body);
		dest.writeInt(was_comment ? 1 : -1);
		dest.writeLong(first_message);
		dest.writeString(name);
		dest.writeString(this.dest);
		dest.writeString(author);
		dest.writeLong(created_utc);
		dest.writeString(subreddit);
		dest.writeString(parent_id);
		dest.writeString(context);
		dest.writeInt(isNew ? 1 : -1);
		dest.writeString(id);
		dest.writeString(subject);
		dest.writeList(repliesList);
	}

	public static final Parcelable.Creator<MessageItem> CREATOR = new Parcelable.Creator<MessageItem>() {
		public MessageItem createFromParcel(Parcel in) {
			return new MessageItem(in);
		}

		public MessageItem[] newArray(int size) {
			return new MessageItem[size];
		}
	};

	private MessageItem(Parcel in) {
		repliesList = new ArrayList<MessageItem>();
		kind = in.readString();
		body = in.readString();
		was_comment = in.readInt() == 1 ? true : false;
		first_message = in.readLong();
		name = in.readString();
		dest = in.readString();
		author = in.readString();
		created_utc = in.readLong();
		subreddit = in.readString();
		parent_id = in.readString();
		context = in.readString();
		isNew = in.readInt() == 1 ? true : false;
		id = in.readString();
		subject = in.readString();
		in.readList(repliesList, this.getClass().getClassLoader());
	}
}
