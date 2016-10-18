package com.softgame.reddit.utils;

import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.json.custom.JSONArray;
import org.json.custom.JSONObject;
import org.json.custom.JSONTokener;

import android.content.Context;

import com.github.ignition.support.http.IgnitedHttpClient;
import com.github.ignition.support.http.IgnitedHttpGet;
import com.github.ignition.support.http.IgnitedHttpPost;
import com.github.ignition.support.http.IgnitedHttpResponse;

public class RedditorManager {

	public static String getMyProfile() {
		return null;
	}

	public static String getRedditorProfile(String username,
			JSONObject infoJSON, Context context) {
		try {
			URI profileUri = RedditorManager.getUserProfileUri(username);
			IgnitedHttpClient httpClient = RedditManager.getHttpClient(
					Common.TYPE_EITHER, context);
			IgnitedHttpGet httpGet = new IgnitedHttpGet(httpClient, profileUri);

			IgnitedHttpResponse httpResponse = httpGet.send();
			String infoResult = httpResponse.getResponseBodyAsString();
			infoJSON.setJSONTokener(new JSONTokener(infoResult));
			if (infoJSON.isEmpty() || !infoJSON.optString("error").equals("")) {
				return Common.RESULT_NO_REDDITOR;
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

	// http://www.reddit.com/user/{username}/about.json

	public static URI getUserProfileUri(String username) {

		URI url = null;
		try {
			url = URIUtils.createURI("http", "www.reddit.com", -1, "user/"
					+ username + "/about.json", null, null);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return url;
	}

	/**
	 * add friend
	 * 
	 * name:twannerson container:t2_3pcp6 type:friend
	 * uh:2hc8f1pz39eb79e6978408dc5fd401fa76fdc776040ef3c2d1 renderstyle:html
	 * 
	 * http://www.reddit.com/api/friend?
	 */

	public static String addFriendRedditor(String name, String container,
			Context context) {

		IgnitedHttpClient httpClient = RedditManager.getHttpClient(
				Common.TYPE_AUTH, context);
		if (httpClient == null) {
			return Common.RESULT_NO_DEFAULT_USER;
		}

		String uh = RedditManager.getUserModHash(context);
		URI voteURI = getAddFriendURI(name, container, uh, "friend");
		try {
			UrlEncodedFormEntity e = RedditorManager.getAddFriendEntity(name,
					container, uh, "friend");
			IgnitedHttpPost httpPost = new IgnitedHttpPost(httpClient, voteURI,
					e);
			IgnitedHttpResponse httpResponse = httpPost.send();

			String infoResult = httpResponse.getResponseBodyAsString();
			if (infoResult.toLowerCase().contains(
					Common.EXAMPLE_PLEASE_LOGIN_IN)) {
				return Common.RESULT_NO_DEFAULT_USER;
			}
			if (infoResult.toLowerCase().contains(Common.EXAMPLE_TOO_MUCH)) {
				return Common.RESULT_TRY_TOO_MUCH;
			}
			return Common.RESULT_SUCCESS;

		} catch (ConnectException e) {
			// retrive data errors
			return Common.RESULT_FETCHING_FAIL;
		} catch (Exception ex) {
			return Common.RESULT_UNKNOW;
		}
	}

	/**
	 * * name:twannerson container:t2_3pcp6 type:friend
	 * uh:2hc8f1pz39eb79e6978408dc5fd401fa76fdc776040ef3c2d1 renderstyle:html
	 * 
	 * @param type
	 *            friend moderator.contributor.banned
	 * @return
	 */
	public static URI getAddFriendURI(String name, String container, String uh,
			String type) {

		List<NameValuePair> qparams = new ArrayList<NameValuePair>();
		/**
		 * qparams.add(new BasicNameValuePair(Common.KEY_NAME, name));
		 * qparams.add(new BasicNameValuePair(Common.KEY_CONTAINER, "t2_" +
		 * container)); qparams.add(new BasicNameValuePair(Common.KEY_TYPE,
		 * "friend")); qparams.add(new BasicNameValuePair(Common.KEY_USER, uh));
		 * qparams.add(new BasicNameValuePair("note", "friend add"));
		 */
		qparams.add(new BasicNameValuePair(Common.KEY_API_TYPE, Common.KEY_JSON));
		// http://www.reddit.com/api/friend URLEncodedUtils.format(qparams,
		// "UTF-8")
		URI uri = null;
		try {
			uri = URIUtils.createURI("http", "www.reddit.com", -1,
					"api/friend", URLEncodedUtils.format(qparams, "UTF-8"),
					null);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return uri;

	}

	public static UrlEncodedFormEntity getAddFriendEntity(String name,
			String container, String uh, String type) {
		List<NameValuePair> qparams = new ArrayList<NameValuePair>();
		qparams.add(new BasicNameValuePair(Common.KEY_NAME, name));
		qparams.add(new BasicNameValuePair(Common.KEY_CONTAINER, "t2_"
				+ container));
		qparams.add(new BasicNameValuePair(Common.KEY_TYPE, "friend"));
		qparams.add(new BasicNameValuePair(Common.KEY_USER, uh));
		try {
			return new UrlEncodedFormEntity(qparams);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * add friend
	 * 
	 * name:twannerson container:t2_3pcp6 type:friend
	 * uh:2hc8f1pz39eb79e6978408dc5fd401fa76fdc776040ef3c2d1 renderstyle:html
	 * 
	 * http://www.reddit.com/api/friend?
	 */

	public static String getFriendList(Context context, JSONArray dataJSON) {

		IgnitedHttpClient httpClient = RedditManager.getHttpClient(
				Common.TYPE_AUTH, context);
		if (httpClient == null) {
			return Common.RESULT_NO_DEFAULT_USER;
		}

		URI voteURI = getFriendListUri();
		try {
			IgnitedHttpGet httpGet = new IgnitedHttpGet(httpClient, voteURI);
			IgnitedHttpResponse httpResponse = httpGet.send();
			String infoResult = httpResponse.getResponseBodyAsString();
			if (infoResult.toLowerCase().contains(
					Common.EXAMPLE_PLEASE_LOGIN_IN)) {
				return Common.RESULT_NO_DEFAULT_USER;
			}
			if (infoResult.toLowerCase().contains(Common.EXAMPLE_TOO_MUCH)) {
				return Common.RESULT_TRY_TOO_MUCH;
			}
			dataJSON.setJSONTokener(new JSONTokener(infoResult));
			return Common.RESULT_SUCCESS;

		} catch (ConnectException e) {
			// retrive data errors
			return Common.RESULT_FETCHING_FAIL;
		} catch (Exception ex) {
			return Common.RESULT_UNKNOW;
		}
	}

	/**
	 * * name:twannerson container:t2_3pcp6 type:friend
	 * uh:2hc8f1pz39eb79e6978408dc5fd401fa76fdc776040ef3c2d1 renderstyle:html
	 * 
	 * @param type
	 *            friend moderator.contributor.banned
	 * @return
	 */
	public static URI getFriendListUri() {

		// http://www.reddit.com/api/friend URLEncodedUtils.format(qparams,
		// "UTF-8")
		URI uri = null;
		try {
			uri = URIUtils.createURI("https", "ssl.reddit.com", -1,
					"prefs/friends/.json", null, null);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return uri;

	}

}
