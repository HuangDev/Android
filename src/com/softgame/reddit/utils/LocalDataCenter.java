package com.softgame.reddit.utils;

import org.json.custom.JSONException;
import org.json.custom.JSONObject;
import org.json.custom.JSONTokener;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class LocalDataCenter {
	/**
	 * read the user login
	 * 
	 * @return JSONObject of user name or null if not exist or errors
	 */
	public static boolean readUserData(Context context, String username,
			JSONObject authJSON) {
		SharedPreferences sf = context
				.getSharedPreferences(Common.PREF_USER, 0);
		String authUser = sf.getString(username, "");
		if (authUser == null || authUser.equals("")) {
			return false;
		} else {
			try {
				authJSON.setJSONTokener(new JSONTokener(authUser));
				return true;
			} catch (JSONException e) {
				e.printStackTrace();
				return false;
			}
		}
	}

	public static boolean checkHaveAuthUser(Context context) {
		SharedPreferences df = PreferenceManager
				.getDefaultSharedPreferences(context);
		String defaultUser = df.getString(Common.PREF_DEFAULT_USER_KEY, "");
		if (defaultUser == null || "".equals(defaultUser)) {
			return false;
		}
		// from user to change if it is valid auth
		SharedPreferences sf = context
				.getSharedPreferences(Common.PREF_USER, 0);
		String authUser = sf.getString(defaultUser, "");
		if (authUser == null || "".equals(authUser)) {
			return false;
		} else {
			return true;
		}
	}

	public static String getLoginUsername(Context context) {
		SharedPreferences df = PreferenceManager
				.getDefaultSharedPreferences(context);
		String defaultUser = df.getString(Common.PREF_DEFAULT_USER_KEY, "");
		if (defaultUser == null || "".equals(defaultUser)) {
			return null;
		}
		// from user to change if it is valid auth
		SharedPreferences sf = context
				.getSharedPreferences(Common.PREF_USER, 0);
		String authUser = sf.getString(defaultUser, "");
		if (authUser == null || "".equals(authUser)) {
			return null;
		} else {
			try {
				JSONObject authJSON = new JSONObject(new JSONTokener(authUser));
				return authJSON.getString(Common.KEY_USERNAME);
			} catch (JSONException e) {
				e.printStackTrace();
				return null;
			}
		}
	}

	public static String getLoginUserModHash(Context context) {
		SharedPreferences df = PreferenceManager
				.getDefaultSharedPreferences(context);
		String defaultUser = df.getString(Common.PREF_DEFAULT_USER_KEY, "");
		if (defaultUser == null || "".equals(defaultUser)) {
			return null;
		}
		// from user to change if it is valid auth
		SharedPreferences sf = context
				.getSharedPreferences(Common.PREF_USER, 0);
		String authUser = sf.getString(defaultUser, "");
		if (authUser == null || "".equals(authUser)) {
			return null;
		} else {
			try {
				JSONObject authJSON = new JSONObject(new JSONTokener(authUser));
				return authJSON.getString(Common.KEY_MODHASH);
			} catch (JSONException e) {
				e.printStackTrace();
				return null;
			}
		}
	}

	public static String getLoginUserSessionId(Context context) {
		SharedPreferences df = PreferenceManager
				.getDefaultSharedPreferences(context);
		String defaultUser = df.getString(Common.PREF_DEFAULT_USER_KEY, "");
		if (defaultUser == null || "".equals(defaultUser)) {
			return null;
		}
		// from user to change if it is valid auth
		SharedPreferences sf = context
				.getSharedPreferences(Common.PREF_USER, 0);
		String authUser = sf.getString(defaultUser, "");
		if (authUser == null || "".equals(authUser)) {
			return null;
		} else {
			try {
				JSONObject authJSON = new JSONObject(new JSONTokener(authUser));
				return authJSON.getString(Common.KEY_SESSION);
			} catch (JSONException e) {
				e.printStackTrace();
				return null;
			}
		}
	}

	/**
	 * KEY_USERNAME => AuthJOSN(include the username,password,uh,session_id) use
	 * user name as the key and authString as the value
	 * 
	 * @param context
	 * @param authJSON
	 * @return
	 */
	public static boolean setUserData(Context context, JSONObject authJSON) {
		String authString = authJSON.toString();
		String username = authJSON.optString(Common.KEY_USERNAME, "");
		if (authString == null || username == null || authJSON.isEmpty()
				|| username.equals("")) {
			return false;
		}
		SharedPreferences sf = context
				.getSharedPreferences(Common.PREF_USER, 0);
		SharedPreferences.Editor ed = sf.edit();
		ed.putString(username, authString);
		return ed.commit();
	}

	/**
	 * put the username as the default login user; PREF_DEFAULT == > Username
	 * 
	 * Key: Common.PREF_DEFAULT_USER value:current username
	 * 
	 * @param context
	 * @param username
	 * @return
	 */
	public static boolean setDefaultUser(Context context, String username) {
		SharedPreferences df = PreferenceManager
				.getDefaultSharedPreferences(context);
		SharedPreferences.Editor ef = df.edit();
		ef.putString(Common.PREF_DEFAULT_USER_KEY, username);
		return ef.commit();
	}

	/**
	 * remove the default user from PREF_DEFAULT
	 * 
	 * @param context
	 * @return
	 */
	public static boolean removeDefaultUser(Context context) {
		SharedPreferences df = PreferenceManager
				.getDefaultSharedPreferences(context);
		SharedPreferences.Editor ef = df.edit();
		ef.remove(Common.PREF_DEFAULT_USER_KEY);
		return ef.commit();
	}

	/*
	 * get the defalut user
	 * 
	 * @return
	 */
	public static String getDefaultUser(Context context, JSONObject authJSON) {
		SharedPreferences df = PreferenceManager
				.getDefaultSharedPreferences(context);
		String defaultUser = df.getString(Common.PREF_DEFAULT_USER_KEY, "");
		if (defaultUser == null || defaultUser.equals("")) {
			return Common.RESULT_NO_DEFAULT_USER;
		}
		boolean result = readUserData(context, defaultUser, authJSON);
		if (result) {
			return Common.RESULT_SUCCESS;
		} else {
			return Common.RESULT_NO_DEFAULT_USER;
		}

	}

	/**
	 * load raw data of website
	 */

}
