package com.softgame.reddit.model;

import java.util.ArrayList;

import org.json.custom.JSONArray;
import org.json.custom.JSONObject;

import android.util.Log;

public class OverviewModel {
	public ArrayList<OverviewItem> mItemList;
	String kind;
	public String after;

	public OverviewModel() {
		mItemList = new ArrayList<OverviewItem>();
	}

	public static OverviewModel newInstance(JSONObject srJSObject) {
		OverviewModel tt = new OverviewModel();
		tt.kind = srJSObject.optString("kind", "");
		JSONObject data = srJSObject.optJSONObject("data");
		tt.after = data.optString("after", "");
		JSONArray children = data.optJSONArray("children");
		if (children == null) {
			return tt;
		}
		for (int i = 0; i < children.length(); i++) {
			JSONObject childData = children.optJSONObject(i);
			if (childData != null) {
				OverviewItem s = new OverviewItem(childData);
				
				//NOTE: Subreddit position is the position in mItemList, not the Adapter position;
				if (s.subRedditItem != null) {
					s.subRedditItem.position = tt.mItemList.size();
				}
				tt.mItemList.add(s);
			}
		}
		return tt;

	}

	public void addOverviewModel(OverviewModel m) {
		kind = m.kind;
		after = m.after;
		mItemList.addAll(m.mItemList);
	}

	public int getItemsSize() {
		if (mItemList == null)
			return 0;
		return mItemList.size();
	}

	public OverviewItem getItemByIndex(int index) {
		return mItemList.get(index);
	}

}
