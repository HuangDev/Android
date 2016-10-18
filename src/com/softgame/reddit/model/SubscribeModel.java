package com.softgame.reddit.model;

import java.util.ArrayList;

import org.json.custom.JSONArray;
import org.json.custom.JSONObject;

public class SubscribeModel {
	public String kind;
	// public String modhash;
	public String before;
	public String after;
	public ArrayList<Subscribe> subscribeList;

	public SubscribeModel() {
		subscribeList = new ArrayList<Subscribe>();
	}

	public static void convertToList(JSONObject infoJSON, SubscribeModel model) {
		model.kind = infoJSON.optString("kind");
		JSONObject dataJSONObject = infoJSON.optJSONObject("data");
		if (dataJSONObject != null)
			// model.modhash = dataJSONObject.optString("modhash");
			model.after = dataJSONObject.optString("after");
		// model.before = dataJSONObject.optString("before");

		JSONArray dataJSONArray = infoJSON.optJSONObject("data").optJSONArray(
				"children");
		if (dataJSONArray == null) {
			return;
		}
		for (int i = 0; i < dataJSONArray.length(); i++) {
			try {
				JSONObject dataItem = dataJSONArray.getJSONObject(i);
				Subscribe item = new Subscribe(dataItem);
				model.subscribeList.add(item);
			} catch (Exception e) {

			}
		}

	}
}
