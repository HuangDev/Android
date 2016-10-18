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
import org.json.custom.JSONObject;
import org.json.custom.JSONTokener;

import android.content.Context;

import com.github.ignition.support.http.IgnitedHttpClient;
import com.github.ignition.support.http.IgnitedHttpPost;
import com.github.ignition.support.http.IgnitedHttpResponse;

public class SubmitManager {

	// --------------------Compose message----------------
	public static String composeMessage(Context context, String subject,
			String text, String to, String iden, String captcha,
			JSONObject infoJSON) {

		IgnitedHttpClient httpClient = RedditManager.getHttpClient(
				Common.TYPE_AUTH, context);
		if (httpClient == null) {
			return Common.RESULT_NO_DEFAULT_USER;
		}

		String uh = RedditManager.getUserModHash(context);
		URI messageURI = getComposeMessageURI(subject, text, to, uh, iden,
				captcha);
		try {
			IgnitedHttpPost httpPost = new IgnitedHttpPost(httpClient,
					messageURI);
			IgnitedHttpResponse httpResponse = httpPost.send();
			String infoResult = httpResponse.getResponseBodyAsString();
			if (infoResult.toLowerCase().contains(
					Common.EXAMPLE_PLEASE_LOGIN_IN)) {
				return Common.RESULT_NO_DEFAULT_USER;
			}
			if (infoResult.toLowerCase().contains(Common.EXAMPLE_TOO_MUCH)) {
				return Common.RESULT_TRY_TOO_MUCH;
			}

			if (infoResult.contains("NO_USER")) {
				return Common.RESULT_NO_TO_USER;
			}

			else {
				infoJSON.setJSONTokener(new JSONTokener(infoResult));
				String ca = null;
				if (infoJSON.optJSONObject("json") != null) {
					ca = infoJSON.optJSONObject("json").optString("captcha");
				}
				if (ca != null && !"".equals(ca)) {
					return Common.RESULT_NEED_CAPTCHA;
				} else {
					return Common.RESULT_SUCCESS;
				}
			}

		} catch (ConnectException e) {
			// retrive data errors
			return Common.RESULT_FETCHING_FAIL;
		} catch (Exception ex) {
			return Common.RESULT_UNKNOW;
		}
	}

	public static URI getComposeMessageURI(String subject, String text,
			String to, String uh, String iden, String captcha) {
		List<NameValuePair> qparams = new ArrayList<NameValuePair>();
		qparams.add(new BasicNameValuePair(Common.KEY_SUBJECT, subject));
		qparams.add(new BasicNameValuePair(Common.KEY_TEXT, text));
		qparams.add(new BasicNameValuePair(Common.KEY_TO, to));
		qparams.add(new BasicNameValuePair(Common.KEY_USER, uh));
		qparams.add(new BasicNameValuePair(Common.KEY_API_TYPE, Common.KEY_JSON));

		if (iden != null && !iden.equals(""))
			qparams.add(new BasicNameValuePair("iden", iden));
		if (captcha != null && !captcha.equals("")) {
			qparams.add(new BasicNameValuePair("captcha", captcha));
		}

		// http://www.reddit.com/api/submit
		URI uri = null;
		try {
			uri = URIUtils.createURI("http", "www.reddit.com", -1,
					"api/compose", URLEncodedUtils.format(qparams, "UTF-8"),
					null);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return uri;

	}

	// --------------------link post ----------------
	public static String commitLinkPost(Context context, String title,
			String url, String sr, String iden, String captcha,
			JSONObject infoJSON) {

		IgnitedHttpClient httpClient = RedditManager.getHttpClient(
				Common.TYPE_AUTH, context);
		if (httpClient == null) {
			return Common.RESULT_NO_DEFAULT_USER;
		}

		String uh = RedditManager.getUserModHash(context);
		try {
			IgnitedHttpPost httpPost = new IgnitedHttpPost(httpClient,
					getSubmitLinkTextURI(), getSubmitLinkEntity(title, url, sr,
							uh, iden, captcha));
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
				infoJSON.setJSONTokener(new JSONTokener(infoResult));
				if (infoJSON.optJSONObject("json") != null) {
					String ca = infoJSON.optJSONObject("json").optString(
							"captcha");
					if (ca != null && !"".equals(ca)) {
						return Common.RESULT_NEED_CAPTCHA;
					}
				}
				return Common.RESULT_SUCCESS;
			}

		} catch (ConnectException e) {
			// retrive data errors
			return Common.RESULT_FETCHING_FAIL;
		} catch (Exception ex) {
			return Common.RESULT_UNKNOW;
		}
	}

	public static URI getSubmitLinkTextURI() {
		String formatparams = null;
		List<NameValuePair> qparams = new ArrayList<NameValuePair>();
		qparams.add(new BasicNameValuePair(Common.KEY_API_TYPE, Common.KEY_JSON));
		// http://www.reddit.com/api/submit
		URI uri = null;
		try {
			uri = URIUtils.createURI("http", "www.reddit.com", -1,
					"api/submit", formatparams, null);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return uri;
	}

	public static UrlEncodedFormEntity getSubmitLinkEntity(String title,
			String url, String sr, String uh, String iden, String captcha) {
		List<NameValuePair> qparams = new ArrayList<NameValuePair>();
		qparams.add(new BasicNameValuePair(Common.KEY_TITLE, title));
		qparams.add(new BasicNameValuePair(Common.KEY_URL, url));
		qparams.add(new BasicNameValuePair(Common.KEY_SR, sr));
		qparams.add(new BasicNameValuePair(Common.KEY_R, sr));
		qparams.add(new BasicNameValuePair(Common.KEY_KIND, "link"));
		qparams.add(new BasicNameValuePair(Common.KEY_USER, uh));
		qparams.add(new BasicNameValuePair(Common.KEY_API_TYPE, Common.KEY_JSON));
		if (iden != null && !iden.equals(""))
			qparams.add(new BasicNameValuePair("iden", iden));
		if (captcha != null && !captcha.equals("")) {
			qparams.add(new BasicNameValuePair("captcha", captcha));
		}
		try {
			return new UrlEncodedFormEntity(qparams);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
	}

	// -----------------Text Post ----------

	public static String commitTextPost(Context context, String title,
			String text, String sr, String iden, String captcha,
			JSONObject infoJSON) {

		IgnitedHttpClient httpClient = RedditManager.getHttpClient(
				Common.TYPE_AUTH, context);
		if (httpClient == null) {
			return Common.RESULT_NO_DEFAULT_USER;
		}
		String uh = RedditManager.getUserModHash(context);
		try {
			IgnitedHttpPost httpPost = new IgnitedHttpPost(httpClient,
					getSubmitLinkTextURI(), getSubmitTextEntity(title, text,
							sr, uh, iden, captcha));
			IgnitedHttpResponse httpResponse = httpPost.send();

			String infoResult = httpResponse.getResponseBodyAsString();
			if (infoResult == null || "".equals(infoResult.trim())) {
				return Common.RESULT_FETCHING_FAIL;
			}
			if (infoResult.toLowerCase().contains(
					Common.EXAMPLE_PLEASE_LOGIN_IN)) {
				return Common.RESULT_NO_DEFAULT_USER;
			}
			if (infoResult.toLowerCase().contains(Common.EXAMPLE_TOO_MUCH)) {
				return Common.RESULT_TRY_TOO_MUCH;
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
			}

			else {
				infoJSON.setJSONTokener(new JSONTokener(infoResult));
				if (infoJSON.optJSONObject("json") != null) {
					String ca = infoJSON.optJSONObject("json").optString(
							"captcha");
					if (ca != null && !"".equals(ca)) {
						return Common.RESULT_NEED_CAPTCHA;
					} else {
						return Common.RESULT_SUCCESS;
					}
				} else {
					return Common.RESULT_SUCCESS;
				}
			}

		} catch (ConnectException e) {
			// retrive data errors
			return Common.RESULT_FETCHING_FAIL;
		} catch (Exception ex) {
			return Common.RESULT_UNKNOW;
		}
	}

	public static UrlEncodedFormEntity getSubmitTextEntity(String title,
			String text, String sr, String uh, String iden, String captcha) {
		List<NameValuePair> qparams = new ArrayList<NameValuePair>();
		qparams.add(new BasicNameValuePair(Common.KEY_TITLE, title));
		if (text != null && !"".equals(text.trim()))
			qparams.add(new BasicNameValuePair(Common.KEY_TEXT, text));
		qparams.add(new BasicNameValuePair(Common.KEY_SR, sr));
		qparams.add(new BasicNameValuePair(Common.KEY_R, sr));
		qparams.add(new BasicNameValuePair(Common.KEY_KIND, "self"));
		qparams.add(new BasicNameValuePair(Common.KEY_USER, uh));
		qparams.add(new BasicNameValuePair(Common.KEY_API_TYPE, Common.KEY_JSON));
		if (iden != null && !iden.equals(""))
			qparams.add(new BasicNameValuePair("iden", iden));
		if (captcha != null && !captcha.equals("")) {
			qparams.add(new BasicNameValuePair("captcha", captcha));
		}
		try {
			return new UrlEncodedFormEntity(qparams);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}

	}
}
