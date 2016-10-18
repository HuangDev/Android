package com.softgame.reddit.model;

import org.json.custom.JSONObject;

import com.softgame.reddit.utils.CommonUtil;

import android.text.Html;
import android.text.Spanned;
import android.util.Log;

public class OverviewItem {

	public String kind;
	public String body_original;
	public Spanned body;
	public String link_title;
	public String subreddit_id;
	public long created;
	public long downs;
	public String author;
	public long created_utc;
	public String link_id;
	public long parent_id;
	public Boolean likes;
	public String replies;
	public String subreddit;
	public long ups;
	public String id;
	public String name;
	public Boolean old_like;

	public SubRedditItem subRedditItem;

	public OverviewItem(JSONObject jsonObject) {
		kind = jsonObject.optString("kind", "t1");
		if (kind.equals("t3")) {
			subRedditItem = new SubRedditItem(jsonObject);
			return;
		}
		JSONObject data = jsonObject.optJSONObject("data");
		setValueFromJSONObject(data);
	}

	public void setValueFromJSONObject(JSONObject jsonObject) {
		body_original = jsonObject.optString("body", "");
		body = Html.fromHtml(CommonUtil.getRedditETMarkDown().markdownToHtml(
				body_original.trim()));
		subreddit = jsonObject.optString("subreddit", "");
		link_title = jsonObject.optString("link_title", "");
		link_id = jsonObject.optString("link_id", "");
		likes = jsonObject.optBooleanIncludeNull("likes");
		id = jsonObject.optString("id", "iojf9");
		author = jsonObject.optString("author", "unknow");
		subreddit_id = jsonObject.optString("subreddit_id", "0");
		downs = jsonObject.optLong("downs", 0);
		name = jsonObject.optString("name", "");
		created = jsonObject.optLong("created", 0);
		created_utc = jsonObject.optLong("created_utc", 0);
		ups = jsonObject.optLong("ups", 0);
	}

}
