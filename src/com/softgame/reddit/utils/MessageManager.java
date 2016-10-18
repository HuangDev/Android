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

public class MessageManager {

	/**
	 * get the raw json subreddit data
	 * 
	 * @param after
	 *            if not "", mean should add after = "";
	 */
	public static String getMessage(String path, String after,
			JSONObject dataJSON, Context context) {
		// check if there is a login user
		IgnitedHttpClient httpClient = RedditManager.getHttpClient(
				Common.TYPE_AUTH, context);
		if (httpClient == null) {
			return Common.RESULT_NO_DEFAULT_USER;
		}
		try {
			URI unreadMessageUri = MessageManager.getMessageUri(path, after);

			IgnitedHttpGet httpGet = new IgnitedHttpGet(httpClient,
					unreadMessageUri);
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

	public static URI getMessageUri(String path, String after) {
		String formatparams = null;
		List<NameValuePair> qparams = new ArrayList<NameValuePair>();
		if (after != null && !"".equals(after)) {
			qparams.add(new BasicNameValuePair(Common.KEY_AFTER, after));
		}

		if (!qparams.isEmpty()) {
			formatparams = URLEncodedUtils.format(qparams, "UTF-8");
		}

		String pathJ = path + "/" + Common.TYPE_JSON_PATH;
		URI url = null;
		try {
			url = URIUtils.createURI("http", "www.reddit.com/message", -1,
					pathJ, formatparams, null);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return url;
	}

	public static String markReadMessage(Context context) {
		// check if there is a login user
		if (!RedditManager.isUserAuth(context)) {
			return Common.RESULT_NO_DEFAULT_USER;
		}
		try {
			// http://www.reddit.com/message/unread/
			IgnitedHttpClient httpClient = RedditManager.getHttpClient(
					Common.TYPE_AUTH, context);
			URI uri = URI.create(Common.MESSAGE_UNREAD_URL);
			IgnitedHttpGet httpGet = new IgnitedHttpGet(httpClient, uri);
			IgnitedHttpResponse httpResponse = httpGet.send();
		} catch (ConnectException e) {
			// retrive data errors
			return Common.RESULT_FETCHING_FAIL;
		} catch (Exception ex) {
			return Common.RESULT_UNKNOW;
		}
		return Common.RESULT_SUCCESS;
	}

	/**
	 * read the Single Message
	 * 
	 * @return
	 */
	public static String readMessage(Context context, JSONObject dataJSON,
			String id) {
		// check if there is a login user
		if (!RedditManager.isUserAuth(context)) {
			return Common.RESULT_NO_DEFAULT_USER;
		}
		try {

			URI subRedditUri = MessageManager.getReadMessageUri(context, id);
			IgnitedHttpClient httpClient = RedditManager.getHttpClient(
					Common.TYPE_AUTH, context);

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

	public static URI getReadMessageUri(Context context, String id) {

		String path = id + ".json";
		URI url = null;
		try {

			url = URIUtils.createURI("http", "www.reddit.com", 0,
					"/message/messages/" + path, null, null);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return url;
	}

	/**
	 * get the raw json subreddit data
	 * 
	 * @param after
	 *            if not "", mean should add after = "";
	 */
	public static String SendNewMessage(String to, String subject, String text,
			JSONObject dataJSON, Context context) {
		// check if there is a login user
		if (!RedditManager.isUserAuth(context)) {
			return Common.RESULT_NO_DEFAULT_USER;
		}
		try {

			URI messageUri = MessageManager
					.getSendMessageUri(to, subject, text);
			IgnitedHttpClient httpClient = RedditManager.getHttpClient(
					Common.TYPE_AUTH, context);

			IgnitedHttpPost httpPost = new IgnitedHttpPost(httpClient,
					messageUri);
			IgnitedHttpResponse httpResponse = httpPost.send();
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

	public static URI getSendMessageUri(String to, String subject, String text) {
		String formatparams = null;
		List<NameValuePair> qparams = new ArrayList<NameValuePair>();
		if (to != null && !"".equals(to)) {
			qparams.add(new BasicNameValuePair(Common.KEY_TO, to));
		}
		if (subject != null && !"".equals(subject)) {
			qparams.add(new BasicNameValuePair(Common.KEY_SUBJECT, subject));
		}

		if (text != null && !"".equals(text)) {
			qparams.add(new BasicNameValuePair(Common.KEY_TEXT, text));
		}

		if (!qparams.isEmpty()) {
			formatparams = URLEncodedUtils.format(qparams, "UTF-8");
		}

		URI url = null;
		try {
			url = URIUtils.createURI("http", "www.reddit.com/api/compose", -1,
					null, formatparams, null);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return url;
	}
}
