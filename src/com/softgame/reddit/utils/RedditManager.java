package com.softgame.reddit.utils;

import java.net.ConnectException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.message.BasicNameValuePair;
import org.json.custom.JSONArray;
import org.json.custom.JSONException;
import org.json.custom.JSONObject;
import org.json.custom.JSONTokener;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.github.ignition.support.http.IgnitedHttpClient;
import com.github.ignition.support.http.IgnitedHttpPost;
import com.github.ignition.support.http.IgnitedHttpResponse;
import com.softgame.reddit.model.SubRedditModel;

public class RedditManager {

	public static IgnitedHttpClient getHttpClient(int type, Context context) {
		switch (type) {
		case Common.TYPE_BASE:
			return new IgnitedHttpClient(context);
			// authClient with sessionId
		case Common.TYPE_AUTH:
			// change init -> check the sessionid ->
			if (isUserAuth(context)) {
				String sessionId = RedditManager.getUserSessionId(context);
				if (sessionId == null && "".equals(sessionId)) {
					return null;
				}
				IgnitedHttpClient authClient = RedditManager.getHttpClient(
						Common.TYPE_BASE, context);
				authClient.getHttpClient().setCookieStore(getCookie(sessionId));
				return authClient;
			}

			break;
		case Common.TYPE_EITHER:
			IgnitedHttpClient client = RedditManager.getHttpClient(
					Common.TYPE_AUTH, context);
			if (client == null) {
				return getHttpClient(Common.TYPE_BASE, context);
			} else {
				return client;
			}
		}
		return new IgnitedHttpClient(context);
	}

	/**
	 * call this to set the cookie
	 * 
	 * @param usernane
	 * @param password
	 * @param sessionId
	 */
	public static CookieStore getCookie(String sessionId) {
		CookieStore cookieStore = new BasicCookieStore();
		BasicClientCookie cookie = new BasicClientCookie(Common.KEY_SESSION,
				sessionId);
		cookie.setPath("/");
		cookie.setDomain(".reddit.com");
		cookie.setVersion(1);
		cookieStore.addCookie(cookie);
		return cookieStore;
	}

	/**
	 * init the auth User from the SharedPreferences also check if the there is
	 * a user logined
	 * 
	 * @return
	 */
	public static boolean isUserAuth(Context context) {
		// change if there is a user and auth infomation is OK
		return LocalDataCenter.checkHaveAuthUser(context);
	}

	/**
	 * get the user modhash
	 * 
	 * @return null if not init or modhash
	 */
	public static String getUserModHash(Context context) {
		return LocalDataCenter.getLoginUserModHash(context);
	}

	/**
	 * get the user username
	 */
	public static String getUserName(Context context) {
		return LocalDataCenter.getLoginUsername(context);
	}

	/**
	 * get session id
	 */

	public static String getUserSessionId(Context context) {
		return LocalDataCenter.getLoginUserSessionId(context);
	}

	/**
	 * where the user profile is current user{
	 */
	public static boolean isLoginUser(String u, Context context) {
		String cu = RedditManager.getUserName(context);
		if (cu == null || u == null) {
			return false;
		} else {
			return cu.equalsIgnoreCase(u);
		}

	}

	/**
	 * check if the login result JSONObject contain error array
	 * 
	 * @param loginJSON
	 * @return
	 */
	public static boolean checkLoginOK(JSONObject loginJSON) {
		JSONArray errorsJSON;
		try {
			errorsJSON = loginJSON.getJSONObject(Common.KEY_JSON).getJSONArray(
					Common.KEY_ERRORS);
		} catch (JSONException e) {
			e.printStackTrace();
			return false;
		}
		return errorsJSON.isEmpty();
	}

	/**
	 * get the errors JSONArray from loginJSON
	 * 
	 * @return the error String
	 */
	public static String readErrorMessage(JSONObject loginJSON) {
		JSONArray errorsJSON;
		try {
			errorsJSON = loginJSON.getJSONObject(Common.KEY_JSON).getJSONArray(
					Common.KEY_ERRORS);
			String errorString = errorsJSON.getJSONArray(0).getString(1);
			return errorString;
		} catch (Exception e) {
			return Common.RESULT_UNKNOW;
		}
	}

	/**
	 * read the reddit session key
	 * 
	 * @param httpResponse
	 * @return session key String or null
	 */
	public static String readSession(HttpResponse httpResponse) {
		HeaderElementIterator it = new BasicHeaderElementIterator(
				httpResponse.headerIterator("Set-Cookie"));

		String sessionID = null;
		while (it.hasNext()) {
			HeaderElement elem = it.nextElement();
			if (elem.getName().trim().equalsIgnoreCase(Common.KEY_SESSION)) {
				sessionID = elem.getValue();
				break;
			}
		}
		return sessionID;
	}

	/**
	 * get the modhash from loginJSON
	 */
	public static String readModhash(JSONObject loginJSON) {
		try {
			String modhash = loginJSON.getJSONObject(Common.KEY_JSON)
					.getJSONObject(Common.KEY_DATA)
					.getString(Common.KEY_MODHASH);
			return modhash;
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * return login URI
	 * 
	 * @param username
	 * @param password
	 * @return
	 */
	private static URI getLoginUri(String username, String password) {
		List<NameValuePair> qparams = new ArrayList<NameValuePair>();
		qparams.add(new BasicNameValuePair(Common.KEY_USERNAME, username));
		qparams.add(new BasicNameValuePair(Common.KEY_PASSWORD, password));
		qparams.add(new BasicNameValuePair(Common.KEY_API_TYPE, "json"));
		URI uri = null;
		try {
			uri = URIUtils.createURI("http", "www.reddit.com/api/login", -1,
					username, URLEncodedUtils.format(qparams, "UTF-8"), null);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return uri;
	}

	public static String resetUserPassword(String oldPassword,
			String newPassword, Context context) {

		IgnitedHttpClient httpClient = RedditManager.getHttpClient(
				Common.TYPE_AUTH, context);
		if (httpClient == null) {
			return Common.RESULT_NO_DEFAULT_USER;
		}
		URI resetUri = getResetPasswordUri(oldPassword, newPassword);
		// HttpClient httpClient = RedditManager.getBaseHttpClient();
		IgnitedHttpPost httpPost = new IgnitedHttpPost(httpClient, resetUri);
		try {
			IgnitedHttpResponse httpResponse = httpPost.send();
			return httpResponse.getResponseBodyAsString();
		} catch (ConnectException e) {
			// retrive data errors
			return Common.RESULT_FETCHING_FAIL;
		} catch (Exception ex) {
			return Common.RESULT_UNKNOW;
		}

	}

	/**
	 * return login URI
	 * 
	 * @param username
	 * @param password
	 * @return
	 */
	private static URI getResetPasswordUri(String oldpassword,
			String newpassword) {
		List<NameValuePair> qparams = new ArrayList<NameValuePair>();
		qparams.add(new BasicNameValuePair(Common.KEY_RESET_PASSWORD_OLD,
				oldpassword));
		qparams.add(new BasicNameValuePair(Common.KEY_RESET_PASSWORD_NEW,
				newpassword));
		qparams.add(new BasicNameValuePair(Common.KEY_RESET_PASSWORD_RESET,
				"yes"));
		URI uri = null;
		try {
			uri = URIUtils.createURI("http", Common.URL_RESET_PASSWORD, -1,
					null, URLEncodedUtils.format(qparams, "UTF-8"), null);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return uri;
	}

	/**
	 * login using username and password
	 * 
	 * @param username
	 * @param password
	 * @return JSONObject of result
	 * @throws JSONException
	 */
	public static String login(Context context, String username, String pswd,JSONObject authJSON) {
		try {
			IgnitedHttpClient ignitedHttpClient = new IgnitedHttpClient(context);
			IgnitedHttpPost httpPost = new IgnitedHttpPost(ignitedHttpClient,
					getLoginUri(username, pswd));
			IgnitedHttpResponse httpResponse = httpPost.send();
			String sessionId = readSession(httpResponse.unwrap());
			String modhash = null;
			String loginResult = httpResponse.getResponseBodyAsString();
			JSONObject loginJSON = new JSONObject(new JSONTokener(loginResult));
			// check error
			boolean loginOK = checkLoginOK(loginJSON);
			if (loginOK) {
				// read modhash
				modhash = readModhash(loginJSON);
			} else {
				// read error message
				String errorMessage = readErrorMessage(loginJSON);
				return errorMessage;
			}

			if (sessionId != null && modhash != null && !sessionId.equals("")
					&& !modhash.equals("")) {
				// both values are OK
				authJSON.put(Common.KEY_SESSION, sessionId);
				authJSON.put(Common.KEY_MODHASH, modhash);
				authJSON.put(Common.KEY_USERNAME, username);
				authJSON.put(Common.KEY_PASSWORD, pswd);
				// -----------------set the data to the preferences------
				// set the init data frist

				return Common.RESULT_SUCCESS;
			}
		} catch (ConnectException e) {
			// retrive data errors
			return Common.RESULT_FETCHING_FAIL;
		} catch (Exception ex) {
			return Common.RESULT_UNKNOW;
		}
		return Common.RESULT_UNKNOW;
	}

	public static String getCurrentSubReddit(Context context) {
		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(context);
		return settings.getString(Common.PREF_KEY_SUBREDDIT,
				Common.DEFAULT_SUBREDDIT);
	}

	public static String getCurrentSubReddtiName(Context context) {
		// init subreddit from preference
		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(context);

		return settings.getString(Common.PREF_KEY_SUBREDDIT_NAME,
				Common.DEFAULT_SUBREDDIT_NAME);
	}

	public static String getCanvasSubReddit(Context context) {
		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(context);

		return settings.getString(Common.PREF_KEY_CANVAS_SUBREDDIT,
				Common.DEFAULT_SUBREDDIT);

	}

	public static String getCanvasSubReddtiName(Context context) {
		// init subreddit from preference
		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(context);
		return settings.getString(Common.PREF_KEY_CANVAS_SUBREDDIT_NAME,
				Common.DEFAULT_SUBREDDIT_NAME);
	}

	public static String getWidgetSubReddit(Context context) {
		SharedPreferences pf = PreferenceManager
				.getDefaultSharedPreferences(context);
		return pf.getString(Common.PREF_KEY_WIDGET_SUBREDDIT,
				Common.DEFAULT_SUBREDDIT);
	}

	public static String getWidgetSubReddtiName(Context context) {
		// init subreddit from preference
		SharedPreferences pf = PreferenceManager
				.getDefaultSharedPreferences(context);
		return pf.getString(Common.PREF_KEY_WIDGET_SUBREDDIT_NAME,
				Common.DEFAULT_SUBREDDIT_NAME);
	}

	public static SubRedditModel getWidgetOldData(Context context) {
		SubRedditModel model;
		SharedPreferences pf = PreferenceManager
				.getDefaultSharedPreferences(context);

		String dd = pf.getString(Common.PREF_KEY_WIDGET_SUBREDDIT_DATA, "");
		if ("".equals(dd)) {
			return null;
		}
		try {
			model = SubRedditModel.convertToModel(new JSONObject(dd));
		} catch (Exception e) {
			model = null;
		}
		return model;
	}

	public static boolean addWidgetData(Context context,
			JSONObject newJSONObject) {
		SharedPreferences pf = PreferenceManager
				.getDefaultSharedPreferences(context);

		String dd = pf.getString(Common.PREF_KEY_WIDGET_SUBREDDIT_DATA, "");
		// add old data to new data
		if (!"".equals(dd)) {
			try {
				// get the old data
				JSONObject oldData = new JSONObject(dd);

				JSONObject data = oldData.optJSONObject("data");
				JSONArray oldChildren = data.optJSONArray("children");
				if (oldChildren == null) {
					return false;
				}

				JSONObject newData = newJSONObject.optJSONObject("data");
				JSONArray newChildren = newData.optJSONArray("children");
				if (newChildren == null) {
					return false;
				}

				// if both are not null
				newChildren.addJSONArray(oldChildren, true);
				SharedPreferences.Editor editor = pf.edit();
				editor.putString(Common.PREF_KEY_WIDGET_SUBREDDIT_DATA,
						newJSONObject.toString());
				editor.commit();
				return true;
			} catch (Exception e) {
				return false;
			}
		}
		return false;
	}

	/**
	 * log out the current user
	 * 
	 * @param context
	 */
	public static void logout(Context context) {
		LocalDataCenter.removeDefaultUser(context);
	}
}
