package com.softgame.reddit.utils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
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
import android.util.Log;
import android.widget.Toast;

import com.github.ignition.support.http.IgnitedHttpClient;
import com.github.ignition.support.http.IgnitedHttpGet;
import com.github.ignition.support.http.IgnitedHttpPost;
import com.github.ignition.support.http.IgnitedHttpResponse;
import com.softgame.reddit.OverviewCommentActivity;

public class RedditCommentManager {
	public static String getRedditComment(Context context, String link,
			int loadType, String sort, String limit, JSONArray infoArray) {

		URI commentURI = RedditCommentManager.getCommmentURI(link, loadType,
				sort, limit);
		if (commentURI == null) {
			return Common.ERROR_URL;
		}
		IgnitedHttpClient httpClient = RedditManager.getHttpClient(
				Common.TYPE_EITHER, context);
		IgnitedHttpGet httpGet = new IgnitedHttpGet(httpClient, commentURI);
		try {
			Log.d("Time", "start to load comment");
			IgnitedHttpResponse httpResponse = httpGet.send();
			String infoResult = httpResponse.getResponseBodyAsString();
			Log.d("Time", "comment string loaded");
			infoArray.setJSONTokener(new JSONTokener(infoResult));
			Log.d("Time", "comment string decode");
			if (infoArray.isEmpty()) {
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

	public static String getRedditETComment(Context context, String link,
			String sort, String limit, JSONArray infoArray) {

		URI commentURI = RedditCommentManager.getRedditETCommmentURI(link,
				sort, limit);
		if (commentURI == null) {
			return Common.ERROR_URL;
		}
		HttpURLConnection urlConnection = null;
		InputStream in = null;
		try {
			Log.d("Time", "start to load comment");
			// urlConnection = (HttpURLConnection) commentURI.toURL()
			// .openConnection();
			// in = new BufferedInputStream(urlConnection.getInputStream());
			// infoArray.setJSONTokener(new JSONTokener(in));
			IgnitedHttpClient httpClient = RedditManager.getHttpClient(
					Common.TYPE_EITHER, context);
			IgnitedHttpGet httpGet = new IgnitedHttpGet(httpClient, commentURI);

			Log.d("Time", "comment string loaded");
			IgnitedHttpResponse httpResponse = httpGet.send();
			String infoResult = httpResponse.getResponseBodyAsString();
			Log.d("Time", "comment string loaded");
			infoArray.setJSONTokener(new JSONTokener(infoResult));
			Log.d("Time", "comment string decode");

			if (infoArray.isEmpty()) {
				return Common.RESULT_PAGE_NOTFOUND;
			} else {
				return Common.RESULT_SUCCESS;
			}
		} catch (Exception e) {
			return Common.RESULT_FETCHING_FAIL;
		} finally {
			if (urlConnection != null)
				urlConnection.disconnect();

			if (in != null) {
				try {
					in.close();
				} catch (final IOException e) {
				}
			}

		}
	}

	/**
	 * get Context Comment
	 * 
	 */
	public static String getContextComment(Context context, URI linkURI,
			JSONArray infoArray) {

		IgnitedHttpClient httpClient = RedditManager.getHttpClient(
				Common.TYPE_EITHER, context);
		IgnitedHttpGet httpGet = new IgnitedHttpGet(httpClient, linkURI);
		try {
			Log.d("Time", "start to load comment");
			IgnitedHttpResponse httpResponse = httpGet.send();
			String infoResult = httpResponse.getResponseBodyAsString();
			Log.d("Time", "comment string loaded");
			infoArray.setJSONTokener(new JSONTokener(infoResult));
			Log.d("Time", "comment string decode");
			if (infoArray.isEmpty()) {
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

	/**
	 * Post Comment
	 * 
	 * @param context
	 * @param text
	 * @param thing_id
	 * @param infoJSON
	 * @return
	 */
	public static String postComment(Context context, String text,
			String thing_id, JSONObject infoJSON) {

		IgnitedHttpClient httpClient = RedditManager.getHttpClient(
				Common.TYPE_AUTH, context);
		if (httpClient == null) {
			return Common.RESULT_NO_DEFAULT_USER;
		}
		String uh = RedditManager.getUserModHash(context);
		URI voteURI = getPostCommentURI(text, thing_id, uh);
		try {
			IgnitedHttpPost httpPost = new IgnitedHttpPost(httpClient, voteURI);
			IgnitedHttpResponse httpResponse = httpPost.send();

			String infoResult = httpResponse.getResponseBodyAsString();
			if (infoResult.toLowerCase().contains(
					Common.EXAMPLE_PLEASE_LOGIN_IN)) {
				return Common.RESULT_NO_DEFAULT_USER;
			}
			if (infoResult.toLowerCase().contains(Common.EXAMPLE_TOO_MUCH)) {
				return Common.RESULT_TRY_TOO_MUCH;
			}
			if (infoResult.toLowerCase().contains(Common.EXAMPLE_TOO_LATE)) {
				return Common.RESULT_TOO_LATE;
			} else {
				infoJSON.setJSONTokener(new JSONTokener(infoResult));
			}
			return Common.RESULT_SUCCESS;

		} catch (ConnectException e) {
			// retrive data errors
			return Common.RESULT_FETCHING_FAIL;
		} catch (Exception ex) {
			return Common.RESULT_UNKNOW;
		}
	}

	public static URI getPostCommentURI(String content, String thing_id,
			String uh) {
		URI url = null;
		List<NameValuePair> qparams = new ArrayList<NameValuePair>();
		qparams.add(new BasicNameValuePair(Common.KEY_USER, uh));
		qparams.add(new BasicNameValuePair(Common.KEY_TEXT, content));
		qparams.add(new BasicNameValuePair(Common.KEY_THING_ID, thing_id));

		try {
			url = URIUtils.createURI("http", "www.reddit.com/api/comment", -1,
					null, URLEncodedUtils.format(qparams, "UTF-8"), null);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return url;
	}

	// for CommentRetain
	public static URI getRedditETCommmentURI(String link, String sort,
			String limit) {

		URI url = null;
		List<NameValuePair> qparams = new ArrayList<NameValuePair>();
		qparams.add(new BasicNameValuePair(Common.KEY_SORT, sort));
		qparams.add(new BasicNameValuePair(Common.KEY_LIMIT, limit));

		// http://www.reddit.com/comments/6nw57.json
		try {
			url = URIUtils.createURI("http", "www.reddit.com", -1, link
					+ ".json", URLEncodedUtils.format(qparams, "UTF-8"), null);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}

		Log.d("CommentActivity", url.toString());
		return url;

	}

	// comment link: r/\w*/comments/\w*/\w*/?$
	// context comment link : r/\w*/comments/\w*/\w*/\w+

	// http://www.reddit.com/r/AskReddit/comments/wgdbs/reddit_one_of_my_old_coworkers_never_drank_water/c5d9btc.json
	// http://www.reddit.com/r/AskReddit/comments/wgdbs/reddit_one_of_my_old_coworkers_never_drank_water/.json
	//

	// for OverviewComment
	public static URI getCommmentURI(String link, int loadType, String sort,
			String limit) {
		URI url = null;
		switch (loadType) {
		case OverviewCommentActivity.LOAD_TYPE_LINK_ID:
			List<NameValuePair> qparams = new ArrayList<NameValuePair>();
			qparams.add(new BasicNameValuePair(Common.KEY_SORT, sort));
			qparams.add(new BasicNameValuePair(Common.KEY_LIMIT, limit));

			// http://www.reddit.com/comments/6nw57.json
			try {
				url = URIUtils.createURI("http", "www.reddit.com", -1, link
						+ ".json", URLEncodedUtils.format(qparams, "UTF-8"),
						null);
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}

			Log.d("CommentActivity", url.toString());
			return url;
		case OverviewCommentActivity.LOAD_TYPE_SHARE_COMMENT:
		case OverviewCommentActivity.LOAD_TYPE_SHARE_COMMENT_CONTEXT:

			List<NameValuePair> sqparams = new ArrayList<NameValuePair>();
			sqparams.add(new BasicNameValuePair(Common.KEY_SORT, sort));
			sqparams.add(new BasicNameValuePair(Common.KEY_LIMIT, limit));

			try {
				url = URI.create(link);
				url = URIUtils.createURI("http", "www.reddit.com", -1,
						url.getPath() + ".json",
						URLEncodedUtils.format(sqparams, "UTF-8"), null);
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}

			Log.d("CommentActivity", url.toString());

			break;
		}
		return url;
	}

	public static String deleteRedditComment(Context context, String thing_id,
			String subreddit) {

		IgnitedHttpClient httpClient = RedditManager.getHttpClient(
				Common.TYPE_AUTH, context);
		if (httpClient == null) {
			return Common.RESULT_NO_DEFAULT_USER;
		}

		String uh = RedditManager.getUserModHash(context);
		try {
			IgnitedHttpPost httpPost = new IgnitedHttpPost(httpClient,
					getDelCommentURI(), getDelCommentEntity(thing_id,
							subreddit, uh));
			IgnitedHttpResponse httpResponse = httpPost.send();

			String infoResult = httpResponse.getResponseBodyAsString();

			// <title>reddit broke!</title>
			if (infoResult.toLowerCase().contains(
					Common.EXAMPLE_PLEASE_LOGIN_IN)) {
				return Common.RESULT_NO_DEFAULT_USER;
			}
			if (infoResult.toLowerCase().contains(Common.EXAMPLE_TOO_MUCH)) {
				return Common.RESULT_TRY_TOO_MUCH;
			}

			if (infoResult.toLowerCase().contains(Common.EXAMPLE_REDDIT_BROKE)) {
				return Common.RESULT_REDDIT_BROKE;
			}
			if (infoResult.toLowerCase().contains(
					Common.EXAMPLE_SUBMIT_TOO_FAST)) {
				return Common.RESULT_SUBMIT_TOO_FAST;
			}

			if (infoResult.toLowerCase().contains(
					Common.EXAMPLE_SUBREDDIT_NOEXIST)) {
				return Common.RESULT_SUBREDDIT_NOEXIST;
			}
			if (infoResult.toLowerCase().contains(
					Common.EXAMPLE_SUBREDDIT_NOTALLOWED)) {
				return Common.RESULT_SUBREDDIT_NOTALLLOW;
			} else {
				// can not set JSONTokener
				return Common.RESULT_SUCCESS;
			}

		} catch (ConnectException e) {
			// retrive data errors
			return Common.RESULT_FETCHING_FAIL;
		} catch (Exception ex) {
			return Common.RESULT_UNKNOW;
		}
	}

	public static URI getDelCommentURI() {
		// http://www.reddit.com/api/submit
		URI uri = null;
		try {
			uri = URIUtils.createURI("http", "www.reddit.com", -1, "/api/del",
					null, null);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		Log.d("CommentActivity", uri.toString());
		return uri;
	}

	public static UrlEncodedFormEntity getDelCommentEntity(String thing_id,
			String subreddit, String uh) {

		// Construct data
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("id", thing_id));
		nvps.add(new BasicNameValuePair("executed", "deleted"));
		nvps.add(new BasicNameValuePair("r", subreddit));
		nvps.add(new BasicNameValuePair("uh", uh));

		try {
			return new UrlEncodedFormEntity(nvps);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}

	}

	public static String editRedditComment(Context context, String thing_id,
			String text) {

		IgnitedHttpClient httpClient = RedditManager.getHttpClient(
				Common.TYPE_AUTH, context);
		if (httpClient == null) {
			return Common.RESULT_NO_DEFAULT_USER;
		}

		String uh = RedditManager.getUserModHash(context);
		try {
			IgnitedHttpPost httpPost = new IgnitedHttpPost(httpClient,
					getEditCommentURI(), getEditCommentEntity(thing_id, text,
							uh));
			IgnitedHttpResponse httpResponse = httpPost.send();

			String infoResult = httpResponse.getResponseBodyAsString();

			// <title>reddit broke!</title>
			if (infoResult.toLowerCase().contains(
					Common.EXAMPLE_PLEASE_LOGIN_IN)) {
				return Common.RESULT_NO_DEFAULT_USER;
			}
			if (infoResult.toLowerCase().contains(Common.EXAMPLE_TOO_MUCH)) {
				return Common.RESULT_TRY_TOO_MUCH;
			}

			if (infoResult.toLowerCase().contains(Common.EXAMPLE_REDDIT_BROKE)) {
				return Common.RESULT_REDDIT_BROKE;
			}
			if (infoResult.toLowerCase().contains(
					Common.EXAMPLE_SUBMIT_TOO_FAST)) {
				return Common.RESULT_SUBMIT_TOO_FAST;
			}

			if (infoResult.toLowerCase().contains(
					Common.EXAMPLE_SUBREDDIT_NOEXIST)) {
				return Common.RESULT_SUBREDDIT_NOEXIST;
			}
			if (infoResult.toLowerCase().contains(
					Common.EXAMPLE_SUBREDDIT_NOTALLOWED)) {
				return Common.RESULT_SUBREDDIT_NOTALLLOW;
			} else {
				// can not set JSONTokener
				return Common.RESULT_SUCCESS;
			}

		} catch (ConnectException e) {
			// retrive data errors
			return Common.RESULT_FETCHING_FAIL;
		} catch (Exception ex) {
			return Common.RESULT_UNKNOW;
		}
	}

	public static URI getEditCommentURI() {
		// http://www.reddit.com/api/submit
		URI uri = null;
		try {
			uri = URIUtils.createURI("http", "www.reddit.com", -1,
					"/api/editusertext", null, null);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		Log.d("CommentActivity", uri.toString());
		return uri;
	}

	public static UrlEncodedFormEntity getEditCommentEntity(String thing_id,
			String text, String uh) {

		// Construct data
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("thing_id", thing_id));
		nvps.add(new BasicNameValuePair("text", text));
		nvps.add(new BasicNameValuePair("uh", uh));

		try {
			return new UrlEncodedFormEntity(nvps);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}

	}

}
