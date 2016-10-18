package com.softgame.reddit.model;

import java.util.ArrayList;

import org.json.custom.JSONArray;
import org.json.custom.JSONException;
import org.json.custom.JSONObject;

public class FriendModel {
	public ArrayList<FriendItem> mFriendList;

	public FriendModel() {
		mFriendList = new ArrayList<FriendItem>();
	}

	public void addFriendItem(FriendItem friendItem) {
		mFriendList.add(friendItem);
	}

	public void addFriendItem(String name, String id) {
		mFriendList.add(new FriendItem(name, id));
	}

	public static void ConvertToList(FriendModel fm, JSONArray infoJSON) {
		try {
			if (infoJSON.length() >= 1) {
				JSONObject firstDataJSON = infoJSON.getJSONObject(0);
				if (firstDataJSON != null) {
					JSONObject dataJSON = firstDataJSON.optJSONObject("data");
					if (dataJSON != null) {
						JSONArray friendJSONArray = dataJSON
								.optJSONArray("children");
						if (friendJSONArray != null
								&& !friendJSONArray.isEmpty()) {
							for (int i = 0; i < friendJSONArray.length(); i++) {

								JSONObject friendJSONObject = friendJSONArray
										.getJSONObject(i);
								fm.addFriendItem(
										friendJSONObject.optString("name"),
										friendJSONObject.optString("id"));
							}

						}
					}
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
}
