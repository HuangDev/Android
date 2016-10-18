package com.softgame.reddit.model;

import org.json.custom.JSONObject;

public class Subscribe {
	public String kind; // "t5"
	public String display_name; // Pics
	public String name; // t5_2qh62
	public String title; // " Pics, share for all";
	public String url; // "/r/pics"
	public String header_pic;
	public long created;
	public long created_utc;
	public boolean over18;
	public long subscribers; // 163454 number of subscribers
	public String id;
	public boolean requestLogin;

	public Subscribe() {
		kind = "";
		display_name = "";
		name = "";
		title = "";
		url = "";
		header_pic = "";
		created_utc = 0;
		over18 = false;
		subscribers = 0;
		id = "";
		requestLogin = false;
	}

	public Subscribe(JSONObject o) {
		kind = o.optString("kind");
		JSONObject data = o.optJSONObject("data");
		display_name = data.optString("display_name", "");
		name = data.optString("name", "");
		title = data.optString("title", "");
		url = data.optString("url", "");
		header_pic = data.optString("header_img","");
		created = data.optLong("created", 0);
		created_utc = data.optLong("created_utc", 0);
		over18 = data.optBoolean("over18", false);
		subscribers = data.optLong("subscribers", 0);
		id = data.optString("id", "0");
	}

}
