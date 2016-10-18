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
import com.github.ignition.support.http.IgnitedHttpPost;
import com.github.ignition.support.http.IgnitedHttpResponse;

public class SubscribeManager {

	public static String getSubscribeList(String type, String after,
			JSONObject infoJSON, Context context) {
		try {

			URI mineSubscribeURI = getSubscribeUri(type, after);
			IgnitedHttpClient httpClient = RedditManager.getHttpClient(
					Common.TYPE_EITHER, context);

			IgnitedHttpGet httpGet = new IgnitedHttpGet(httpClient,
					mineSubscribeURI);
			IgnitedHttpResponse httpResponse = httpGet.send();
			String infoResult = httpResponse.getResponseBodyAsString();

			if (infoResult == null || infoResult.equals("")) {
				// error
				return Common.RESULT_UNKNOW;
			}
			infoJSON.setJSONTokener(new JSONTokener(infoResult));
			return Common.RESULT_SUCCESS;
		} catch (ConnectException e) {
			// retrive data errors
			return Common.RESULT_FETCHING_FAIL;
		} catch (Exception ex) {
			return Common.RESULT_UNKNOW;
		}
	}

	private static URI getSubscribeUri(String type, String after) {

		String formatparams = null;

		List<NameValuePair> qparams = new ArrayList<NameValuePair>();
		if (after != null && !"".equals(after)) {
			qparams.add(new BasicNameValuePair(Common.KEY_AFTER, after));
		}
		if (!qparams.isEmpty()) {
			formatparams = URLEncodedUtils.format(qparams, "UTF-8");
		}
		URI url = null;
		try {
			url = URIUtils.createURI("http", "www.reddit.com", -1, "reddits"
					+ type + Common.SUBREDDIT_JSON, formatparams, null);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return url;
	}

	public static String getSearchSubscribeList(String search, String after,
			JSONObject infoJSON, Context context) {
		try {

			URI mineSubscribeURI = getSearchSubscribeUri(search, after);
			IgnitedHttpClient httpClient = RedditManager.getHttpClient(
					Common.TYPE_EITHER, context);

			IgnitedHttpGet httpGet = new IgnitedHttpGet(httpClient,
					mineSubscribeURI);
			IgnitedHttpResponse httpResponse = httpGet.send();
			String infoResult = httpResponse.getResponseBodyAsString();

			if (infoResult == null || infoResult.equals("")) {
				// error
				return Common.RESULT_UNKNOW;
			}
			infoJSON.setJSONTokener(new JSONTokener(infoResult));
			return Common.RESULT_SUCCESS;
		} catch (ConnectException e) {
			// retrive data errors
			return Common.RESULT_FETCHING_FAIL;
		} catch (Exception ex) {
			return Common.RESULT_UNKNOW;
		}
	}

	private static URI getSearchSubscribeUri(String searchItem, String after) {

		String formatparams = null;

		List<NameValuePair> qparams = new ArrayList<NameValuePair>();
		if (searchItem != null && !"".equals(searchItem)) {
			qparams.add(new BasicNameValuePair(Common.KEY_Q, searchItem));
		}
		if (after != null && !"".equals(after)) {
			qparams.add(new BasicNameValuePair(Common.KEY_AFTER, after));
		}
		if (!qparams.isEmpty()) {
			formatparams = URLEncodedUtils.format(qparams, "UTF-8");
		}
		URI url = null;
		try {
			url = URIUtils.createURI("http", "www.reddit.com", -1,
					"reddits/search.json", formatparams, null);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return url;
	}

	/**
	 * subscribe subreddit
	 */

	public static String subscribeSubReddit(String sr, boolean sub,
			Context context) {

		if (!RedditManager.isUserAuth(context)) {
			return Common.RESULT_NO_DEFAULT_USER;
		}
		try {
			String uh = RedditManager.getUserModHash(context);
			URI subscribeUri = getSubscribeSubRedditUri(sr, sub, uh);

			IgnitedHttpClient httpClient = RedditManager.getHttpClient(
					Common.TYPE_AUTH, context);

			IgnitedHttpPost httpPost = new IgnitedHttpPost(httpClient,
					subscribeUri);
			IgnitedHttpResponse httpResponse = httpPost.send();
			String infoResult = httpResponse.getResponseBodyAsString();

			if (infoResult == null || infoResult.equals("")) {
				// error
				return Common.RESULT_UNKNOW;
			}

			JSONObject j = new JSONObject(infoResult);
			if (j.isEmpty()) {
				return Common.RESULT_SUCCESS;
			} else {
				return Common.RESULT_UNKNOW;
			}
		} catch (ConnectException e) {
			// retrive data errors
			return Common.RESULT_FETCHING_FAIL;
		} catch (Exception ex) {
			return Common.RESULT_UNKNOW;
		}
	}

	private static URI getSubscribeSubRedditUri(String sr, boolean sub,
			String uh) {

		String formatparams = null;
		List<NameValuePair> qparams = new ArrayList<NameValuePair>();
		qparams.add(new BasicNameValuePair(Common.KEY_SR, sr));
		qparams.add(new BasicNameValuePair(Common.KEY_USER, uh));
		if (sub) {
			qparams.add(new BasicNameValuePair(Common.KEY_ACTION, "sub"));
		} else {
			qparams.add(new BasicNameValuePair(Common.KEY_ACTION, "unsub"));
		}
		qparams.add(new BasicNameValuePair(Common.KEY_API_TYPE,Common.KEY_JSON));
		//qparams.add(new BasicNameValuePair(Common.KEY_RENDERSTYLE, "html"));
		formatparams = URLEncodedUtils.format(qparams, "UTF-8");
		URI url = null;
		try {
			url = URIUtils.createURI("http", "www.reddit.com", -1,
					"api/subscribe", formatparams, null);

		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return url;
	}

}
