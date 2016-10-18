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

/**
 * for all the subreddit related
 * 
 * @author xinyunxixi
 * 
 */
public class SubRedditManager {

	public static final String TAG = "SubReddtiManager";

	/**
	 * get the raw json subreddit data
	 * 
	 * @param after
	 *            if not "", mean should add after = "";
	 */
	public static String getSubReddit(String subreddit, int kindIndex,
			String kind, String sort, String type, String after,
			JSONObject dataJSON, Context context) {
		// check if there is a login user
		if (kindIndex == Common.KIND_SAVED) {
			if (!RedditManager.isUserAuth(context)) {
				return Common.RESULT_NO_DEFAULT_USER;
			}
		}
		try {
			URI subRedditUri = SubRedditManager.getSubRedditUri(subreddit,
					kindIndex, kind, sort, type, after);
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

	public static URI getSubRedditUri(String subReddit, int kindIndex,
			String kind, String sort, String type, String after) {
		String formatparams = null;

		List<NameValuePair> qparams = new ArrayList<NameValuePair>();
		if (after != null && after != "") {
			qparams.add(new BasicNameValuePair(Common.KEY_AFTER, after));
		}

		if (Common.KIND_CONTROVERSIAL == kindIndex
				|| Common.KIND_TOP == kindIndex) {
			// add the sort
			qparams.add(new BasicNameValuePair(Common.KEY_T, type));
		}

		if (Common.KIND_NEW == kindIndex) {
			qparams.add(new BasicNameValuePair(Common.KEY_SORT, sort));
		}

		if (!qparams.isEmpty()) {
			formatparams = URLEncodedUtils.format(qparams, "UTF-8");
		}
		String path = subReddit + kind + Common.TYPE_JSON_PATH;
		URI url = null;
		try {
			url = URIUtils.createURI("http", "www.reddit.com", -1, path,
					formatparams, null);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return url;
	}

	/**
	 * url:http://www.reddit.com/api/vote vote for post, user login needed. POST
	 * 
	 * @param dir
	 *            1 vote up, -1 vote down 0 to clear a vote fullname:kind_id;
	 * @return
	 */
	public static String voteSubRedditPost(String id, String dir,
			Context context) {

		if (!RedditManager.isUserAuth(context)) {
			return Common.RESULT_NO_DEFAULT_USER;
		}
		try {
			String uh = RedditManager.getUserModHash(context);
			URI voteURI = getVoteSubRedditPostUri(id, dir, uh);

			IgnitedHttpClient ignitedHttpClient = RedditManager.getHttpClient(
					Common.TYPE_AUTH, context);
			IgnitedHttpPost httpPost = new IgnitedHttpPost(ignitedHttpClient,
					voteURI);
			IgnitedHttpResponse httpResponse = httpPost.send();
			JSONObject dataJSON = new JSONObject();
			String infoResult = httpResponse.getResponseBodyAsString();
			dataJSON.setJSONTokener(new JSONTokener(infoResult));
			// dataJSON is empty, mean is OK
			if (dataJSON.isEmpty()) {
				return Common.RESULT_SUCCESS;
			}
			return Common.ERROR_IGNORE;

		} catch (ConnectException e) {
			// retrive data errors
			return Common.RESULT_FETCHING_FAIL;
		} catch (Exception ex) {
			return Common.RESULT_UNKNOW;
		}

	}

	public static URI getVoteSubRedditPostUri(String id, String dir, String uh) {
		List<NameValuePair> qparams = new ArrayList<NameValuePair>();
		qparams.add(new BasicNameValuePair(Common.KEY_FULLNAME, id));
		qparams.add(new BasicNameValuePair(Common.KEY_VOTE, dir));
		qparams.add(new BasicNameValuePair(Common.KEY_USER, uh));
		URI url = null;
		try {
			url = URIUtils.createURI("http", "www.reddit.com", -1, "api/vote",
					URLEncodedUtils.format(qparams, "UTF-8"), null);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return url;
	}

	// ---------------hide -------------------
	public static String hideSubReddit(String id, boolean hide, Context context) {
		try {
			String uh = RedditManager.getUserModHash(context);
			URI hideURI = getHideSubRedditPostUri(id, uh, hide);

			IgnitedHttpClient ignitedHttpClient = RedditManager.getHttpClient(
					Common.TYPE_AUTH, context);
			if (ignitedHttpClient == null) {
				return Common.RESULT_NO_DEFAULT_USER;
			}
			IgnitedHttpPost httpPost = new IgnitedHttpPost(ignitedHttpClient,
					hideURI);
			IgnitedHttpResponse httpResponse = httpPost.send();

			JSONObject dataJSON = new JSONObject();
			String infoResult = httpResponse.getResponseBodyAsString();
			dataJSON.setJSONTokener(new JSONTokener(infoResult));
			// dataJSON is empty, mean is OK
			if (dataJSON.isEmpty()) {
				return Common.RESULT_SUCCESS;
			}
			return Common.ERROR_IGNORE;

		} catch (ConnectException e) {
			// retrive data errors
			return Common.RESULT_FETCHING_FAIL;
		} catch (Exception ex) {
			return Common.RESULT_UNKNOW;
		}
	}

	public static URI getHideSubRedditPostUri(String id, String uh, boolean hide) {
		List<NameValuePair> qparams = new ArrayList<NameValuePair>();
		qparams.add(new BasicNameValuePair(Common.KEY_FULLNAME, id));
		qparams.add(new BasicNameValuePair(Common.KEY_USER, uh));
		URI url = null;

		try {
			if (hide)
				url = URIUtils.createURI("http", "www.reddit.com", -1,
						"api/hide", URLEncodedUtils.format(qparams, "UTF-8"),
						null);
			else {
				url = URIUtils.createURI("http", "www.reddit.com", -1,
						"api/unhide", URLEncodedUtils.format(qparams, "UTF-8"),
						null);

			}
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return url;
	}

	// ------------------ save ----------------------

	public static String saveSubRedditPost(String id, boolean save,
			Context context) {

		if (!RedditManager.isUserAuth(context)) {
			return Common.RESULT_NO_DEFAULT_USER;
		}

		try {
			String uh = RedditManager.getUserModHash(context);
			URI saveURI = getSaveSubRedditPostUri(id, uh, save);
			IgnitedHttpClient httpClient = RedditManager.getHttpClient(
					Common.TYPE_AUTH, context);
			JSONObject dataJSON = new JSONObject();
			IgnitedHttpPost httpPost = new IgnitedHttpPost(httpClient, saveURI);
			IgnitedHttpResponse httpResponse = httpPost.send();
			JSONObject dataJSONSave = new JSONObject();
			String infoResult = httpResponse.getResponseBodyAsString();
			dataJSONSave.setJSONTokener(new JSONTokener(infoResult));
			// dataJSON is empty, mean is OK
			if (dataJSON.isEmpty()) {
				return Common.RESULT_SUCCESS;
			}
			return Common.ERROR_IGNORE;
		} catch (ConnectException e) {
			// retrive data errors
			return Common.RESULT_FETCHING_FAIL;
		} catch (Exception ex) {
			return Common.RESULT_UNKNOW;
		}

	}

	public static URI getSaveSubRedditPostUri(String id, String uh, boolean save) {
		List<NameValuePair> qparams = new ArrayList<NameValuePair>();
		qparams.add(new BasicNameValuePair(Common.KEY_FULLNAME, id));
		qparams.add(new BasicNameValuePair(Common.KEY_USER, uh));
		URI url = null;

		try {
			if (save)
				url = URIUtils.createURI("http", "www.reddit.com", -1,
						"api/save", URLEncodedUtils.format(qparams, "UTF-8"),
						null);
			else {
				url = URIUtils.createURI("http", "www.reddit.com", -1,
						"api/unsave", URLEncodedUtils.format(qparams, "UTF-8"),
						null);

			}
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return url;
	}

	/**
	 * search http://www.reddit.com/r/all/search?q=tt
	 * 
	 * http://www.reddit.com/r/all/search?q=tt&sort=relevance&restrict_sr=on
	 */

	public static String getRedditSearch(String subreddit, String searchString,
			String sort, boolean restrict_on, String after,
			JSONObject dataJSON, Context context) {
		try {
			URI subRedditUri = SubRedditManager.getSearchRedditUri(subreddit,
					searchString, sort, restrict_on, after);
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

	/*
	 * http://www.reddit.com/r/funny/search?q=fun+picture&sort=top&after=t3_m87j7
	 * &count=25
	 */
	public static URI getSearchRedditUri(String subReddit, String searchString,
			String sort, boolean restrict_on, String after) {
		String formatparams = null;

		List<NameValuePair> qparams = new ArrayList<NameValuePair>();
		if (after != null && after != "") {
			qparams.add(new BasicNameValuePair(Common.KEY_AFTER, after));
		}

		if (restrict_on) {
			qparams.add(new BasicNameValuePair(Common.KEY_RESTRICT_SR, "on"));
		}

		qparams.add(new BasicNameValuePair(Common.KEY_SEARCH, searchString));
		qparams.add(new BasicNameValuePair(Common.KEY_SORT, sort));

		if (!qparams.isEmpty()) {
			formatparams = URLEncodedUtils.format(qparams, "UTF-8");
		}
		String path = subReddit + "search" + Common.TYPE_JSON_PATH;
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
