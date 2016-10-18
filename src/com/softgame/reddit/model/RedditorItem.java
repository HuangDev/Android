package com.softgame.reddit.model;

import org.json.custom.JSONObject;

import android.util.Log;

/**
 * @author xinyunxixi
 * 
 */
public class RedditorItem {
	public String kind;
	public String id;
	public String name; // MusicOcean
	public long age; // 2years old , 32d old
	public long comment_karma;
	public long link_karma;
	public boolean is_gold; // false
	public boolean is_mod; // false;
	public boolean has_mod_mail; // false;
	public boolean has_mail; // false;

	public RedditorItem(JSONObject infoObject) {
		kind = infoObject.optString("kind");
		JSONObject dataJSON = infoObject.optJSONObject("data");
		name = dataJSON.optString("name", "no name");
		age = dataJSON.optLong("created_utc", 0);
		link_karma = dataJSON.optLong("link_karma");
		comment_karma = dataJSON.optLong("comment_karma");

		id = dataJSON.optString("id");
		is_gold = dataJSON.optBoolean("is_gold");
		is_mod = dataJSON.optBoolean("is_mod");
		has_mod_mail = dataJSON.optBoolean("has_mod_mail");
		has_mail = dataJSON.optBoolean("has_mail");
		Log.d("Redditor Infomation", infoObject.toString());
	}
}
