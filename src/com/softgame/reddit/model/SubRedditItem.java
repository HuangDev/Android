package com.softgame.reddit.model;

import org.json.custom.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.Html;
import android.text.Spanned;
import android.text.util.Linkify;
import android.util.Log;

import com.softgame.reddit.utils.CommonUtil;

public class SubRedditItem implements Parcelable {

	public String kind;
	public String domain;
	public String subreddit;
	public Boolean likes;
	public boolean saved;
	public String id;
	public String author;
	public long score;
	public boolean over_18;
	public boolean hidden;
	// "" means no reddit_session and mark as NSFW
	public String thumbnail;
	public String subreddit_id;
	public long downs;
	public boolean is_self;
	// url for comment
	public String permalink;
	public String name;
	// public long created;
	// url for refer page
	public String url;
	public String title;
	public long created_utc;
	public long num_comments;
	public long ups;
	private CharSequence selftext;
	public String selftext_original;
	public String selftxt_makedown_html = "";
	public Boolean old_like;

	// only use for record the CommentActivity position so that can update when
	// back
	public int position;

	public SubRedditItem(JSONObject jsonObject) {
		kind = jsonObject.optString("kind", "t1");
		JSONObject data = jsonObject.optJSONObject("data");
		setValueFromJSONObject(data);
	}

	public void setValueFromJSONObject(JSONObject jsonObject) {
		domain = jsonObject.optString("domain", "reddit.com");
		subreddit = jsonObject.optString("subreddit", "all");
		likes = jsonObject.optBooleanIncludeNull("likes");
		saved = jsonObject.optBoolean("saved", false);
		id = jsonObject.optString("id", "iojf9");
		author = jsonObject.optString("author", "unknow");
		score = jsonObject.optLong("score", 0);
		over_18 = jsonObject.optBoolean("over_18", false);
		hidden = jsonObject.optBoolean("hidden", false);
		thumbnail = jsonObject.optString("thumbnail", "");
		subreddit_id = jsonObject.optString("subreddit_id", "0");
		downs = jsonObject.optLong("downs", 0);
		is_self = jsonObject.optBoolean("is_self", false);
		// link to the post
		permalink = jsonObject.optString("permalink", "http://www.reddit.com");
		name = jsonObject.optString("name", "");
		// created = jsonObject.optLong("created", 0);
		// link to the image
		url = jsonObject.optString("url", "http://www.reddit.com");
		title = jsonObject.optString("title", "").trim();
		created_utc = jsonObject.optLong("created_utc", 0);
		num_comments = jsonObject.optLong("num_comments", 0);
		ups = jsonObject.optLong("ups", 0);
		selftext_original = jsonObject.optString("selftext");
		if (selftext_original != null && !"".equals(selftext_original)) {
			selftxt_makedown_html = CommonUtil.getRedditETMarkDown()
					.markdownToHtml(selftext_original.trim());
		} else {
			selftxt_makedown_html = "";
		}
		selftext = processSelfTextItem(selftxt_makedown_html);
	}

	public CharSequence processSelfTextItem(String html_source) {
		Spanned html_spand = Html.fromHtml(html_source);
		try {
			if (html_spand.length() > 2) {
				return html_spand.subSequence(0, html_spand.length() - 2);
			} else {
				return html_spand.toString();
			}
		} catch (Exception e) {
			Log.d("CommentDecode", "decode s: error!");
			return html_spand.toString();
		}
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(kind);
		dest.writeString(domain);
		dest.writeString(subreddit);
		if (likes == null) {
			dest.writeInt(0);
		} else {
			dest.writeInt(likes ? 1 : -1);
		}

		dest.writeInt(saved ? 1 : -1);
		dest.writeString(id);
		dest.writeString(author);
		dest.writeLong(score);
		dest.writeInt(over_18 ? 1 : -1);
		dest.writeInt(hidden ? 1 : -1);
		// "" means no reddit_session and mark as NSFW
		dest.writeString(thumbnail);
		dest.writeString(subreddit_id);

		dest.writeLong(downs);
		dest.writeInt(is_self ? 1 : -1);
		// url for comment
		dest.writeString(permalink);
		dest.writeString(name);
		// dest.writeLong(created);
		// url for refer page
		dest.writeString(url);
		dest.writeString(title);
		dest.writeLong(created_utc);
		dest.writeLong(num_comments);
		dest.writeLong(ups);
		dest.writeString(selftxt_makedown_html);
		dest.writeInt(position);
	}

	public static final Parcelable.Creator<SubRedditItem> CREATOR = new Parcelable.Creator<SubRedditItem>() {
		public SubRedditItem createFromParcel(Parcel in) {
			return new SubRedditItem(in);
		}

		public SubRedditItem[] newArray(int size) {
			return new SubRedditItem[size];
		}
	};

	private SubRedditItem(Parcel in) {
		kind = in.readString();
		domain = in.readString();
		subreddit = in.readString();

		int i = in.readInt();
		if (i == 0) {
			likes = null;
		} else {
			likes = i == 1 ? Boolean.TRUE : Boolean.FALSE;
		}

		saved = in.readInt() == 1 ? Boolean.TRUE : Boolean.FALSE;
		id = in.readString();
		author = in.readString();
		score = in.readLong();
		over_18 = in.readInt() == 1 ? Boolean.TRUE : Boolean.FALSE;
		hidden = in.readInt() == 1 ? Boolean.TRUE : Boolean.FALSE;
		// "" means no reddit_session and mark as NSFW
		thumbnail = in.readString();
		subreddit_id = in.readString();

		downs = in.readLong();
		is_self = in.readInt() == 1 ? Boolean.TRUE : Boolean.FALSE;
		// url for comment
		permalink = in.readString();
		name = in.readString();
		// created = in.readLong();
		// url for refer page
		url = in.readString();
		title = in.readString();
		created_utc = in.readLong();
		num_comments = in.readLong();
		ups = in.readLong();
		selftxt_makedown_html = in.readString();
		position = in.readInt();
		// set the spanded
		// TODO: Disable the selftext from Pracel
		// selftext = Html.fromHtml(selftxt_makedown_html);
	}

	public CharSequence getSelftext() {
		if (selftext == null) {
			if (selftxt_makedown_html != null
					&& !"".equals(selftxt_makedown_html)) {
				selftext = processSelfTextItem(selftxt_makedown_html);
			} else {
				selftext = Html.fromHtml("");
			}
		}
		return selftext;
	}

	public void setSubRedditItem(SubRedditItem s) {
		this.likes = s.likes;
		this.score = s.score;
		this.saved = s.saved;
		this.ups = s.ups;
		this.downs = s.downs;
		this.num_comments = s.num_comments;
	}
}
