package com.softgame.reddit.utils;

import java.net.ConnectException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.json.custom.JSONObject;
import org.json.custom.JSONTokener;

import android.content.Context;

import com.github.ignition.support.http.IgnitedHttpClient;
import com.github.ignition.support.http.IgnitedHttpGet;
import com.github.ignition.support.http.IgnitedHttpResponse;

public class OverviewManager {

	public static String getOverview(String username, String kind,
			String after, String sort, String date, JSONObject dataJSON,
			Context context) {

		try {
			URI subRedditUri = OverviewManager.getOverviewUri(username, kind,
					after, sort, date);
			IgnitedHttpClient httpClient = RedditManager.getHttpClient(
					Common.TYPE_EITHER, context);

			IgnitedHttpGet httpGet = new IgnitedHttpGet(httpClient,
					subRedditUri);
			IgnitedHttpResponse httpResponse = httpGet.send();
			String infoResult = httpResponse.getResponseBodyAsString();

			dataJSON.setJSONTokener(new JSONTokener(infoResult));
			if (dataJSON.isEmpty()) {
				return Common.RESULT_FETCHING_FAIL;
			}
			if (dataJSON.optString("error") != null
					&& dataJSON.optString("error").equals("404")) {
				return Common.RESULT_PAGE_NOTFOUND;
			} else {
				return Common.RESULT_SUCCESS;
			}
		} catch (ConnectException e) {
			// retrive data errors
			return Common.RESULT_FETCHING_FAIL;
		} catch (Exception ex) {
			return Common.RESULT_UNKNOW;
		}
	}

	public static URI getOverviewUri(String username, String kind,
			String after, String sort, String date) {
		String formatparams = null;
		List<NameValuePair> qparams = new ArrayList<NameValuePair>();
		if (after != null && !"".equals(after)) {
			qparams.add(new BasicNameValuePair(Common.KEY_AFTER, after));
		}
		if (sort != null && !"".equals(sort)) {
			qparams.add(new BasicNameValuePair(Common.KEY_SORT, sort));
		}
		if (date != null && !"".equals(date)) {
			qparams.add(new BasicNameValuePair(Common.KEY_T, date));
		}
		formatparams = URLEncodedUtils.format(qparams, "UTF-8");
		String path = "user" + Common.URL_SEPERATOR + username
				+ Common.URL_SEPERATOR + kind + Common.TYPE_JSON_PATH;
		URI url = null;
		try {
			url = URIUtils.createURI("http", "www.reddit.com", -1, path,
					formatparams, null);

		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return url;
	}
}
