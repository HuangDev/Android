package com.softgame.reddit.utils;

import java.net.URL;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.util.Log;
import android.widget.Toast;

import com.commonsware.cwac.anddown.AndDown;
import com.softgame.reddit.R;
import com.softgame.reddit.service.ClearDiskService;
import com.softgame.reddit.service.MessageCheckService;

public class CommonUtil {

	public static final String TAG = "AlarmManager";

	public static String[] IMAGE_END = new String[] { ".png", ".jpg", ".jpeg" };

	public static boolean isHoneycomb() {
		// Can use static final constants like HONEYCOMB, declared in later
		// versions
		// of the OS since they are inlined at compile time. This is guaranteed
		// behavior.
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
	}

	public static boolean isLowThanFroyo() {
		return Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO;
	}

	public static String getShortUrl(String link) {
		try {
			URL linkURL = new URL(link);
			String path = linkURL.getPath();
			String shortUrl = linkURL.getHost() + (path == null ? "" : path);
			if (shortUrl != null) {
				return shortUrl;
			} else {
				return link;
			}
		} catch (Exception e) {
			return link;
		}
	}

	
	public static boolean checkIsComment(String domain, String url) {
		try {
			URL linkURL = new URL(url);
			if (domain.equalsIgnoreCase(Common.DOMAIN_REDDIT)
					&& linkURL.getPath() != null) {

				return linkURL.getPath().matches(Common.COMMENT_PATTEN);
			} else {
				return false;
			}
		} catch (Exception e) {
			return false;
		}
	}

	public static boolean checkIsContextComment(String domain, String url) {
		try {
			URL linkURL = new URL(url);
			if (linkURL.getPath() != null) {

				if (linkURL.getPath().matches(Common.COMMENT_CONTEXT_PATENT)) {
					return true;
				} else {
					return false;
				}
			}
			return false;
		} catch (Exception e) {
			return false;
		}

	}

	
	public static boolean isTablet(Context context) {
		return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
	}

	public static boolean isHoneycombTablet(Context context) {
		return isHoneycomb() && isTablet(context);
	}

	public static boolean needToMessageCheck(Context context) {

		SharedPreferences pf = PreferenceManager
				.getDefaultSharedPreferences(context);
		return pf.getBoolean(
				context.getString(R.string.pref_check_message_key), false);
	}

	public static boolean turnOnMessageCheck(Context context) {
		try {
			AlarmManager mAlarmManager = (AlarmManager) context
					.getSystemService(Context.ALARM_SERVICE);
			// get the time
			SharedPreferences pf = PreferenceManager
					.getDefaultSharedPreferences(context);
			String index = pf.getString(
					context.getString(R.string.pref_check_rate_key), "0");
			int i = 2;
			try {
				i = Integer.parseInt(index);
			} catch (Exception e) {
				i = 2;
			}
			if (i < 0 || i > Common.CHECK_RATE_VALUE.length) {
				i = 2;
			}

			long t = Common.CHECK_RATE_VALUE[i];

			Intent check = new Intent(context, MessageCheckService.class);
			check.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			PendingIntent checkIntent = PendingIntent.getService(context, 0,
					check, PendingIntent.FLAG_UPDATE_CURRENT);

			mAlarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP,
					0, t, checkIntent);
			Toast.makeText(context, "Check message is on!", Toast.LENGTH_SHORT)
					.show();
			return true;

		} catch (Exception e) {
			Toast.makeText(context, "error during turn on message check!",
					Toast.LENGTH_SHORT).show();
			return false;
		}
	}

	public static int getPictureQualityLevel(Context context) {
		try {
			// get the time
			SharedPreferences pf = PreferenceManager
					.getDefaultSharedPreferences(context);
			String index = pf.getString(
					context.getString(R.string.pref_key_picture_quality_level),
					"1");
			int i = Integer.parseInt(index);
			if (i < 0 || i > 3) {
				i = 1;
			}
			return i;
		} catch (Exception e) {
			return 1;
		}
	}

	public static boolean turnOnOrOffClearDiskRate(Context context, String index) {
		try {
			AlarmManager mAlarmManager = (AlarmManager) context
					.getSystemService(Context.ALARM_SERVICE);

			int i = 0;
			try {
				i = Integer.parseInt(index);
			} catch (Exception e) {
				i = 0;
			}
			if (i < 0 || i > 48) {
				i = 2;
			}

			Intent check = new Intent(context, ClearDiskService.class);
			check.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			PendingIntent checkIntent = PendingIntent.getService(context, 0,
					check, PendingIntent.FLAG_UPDATE_CURRENT);

			if (i == 0) {
				mAlarmManager.cancel(checkIntent);
			} else {
				long time = i * 60 * 60 * 1000;
				mAlarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP,
						0, time, checkIntent);
			}
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public static int getWidgetCurrentType(Context context, int keyStringId,
			int defaultIndex, int maxLength) {
		SharedPreferences pf = PreferenceManager
				.getDefaultSharedPreferences(context);
		int typeIndex = 0;
		try {
			typeIndex = pf.getInt(context.getString(keyStringId), defaultIndex);
			if (typeIndex < 0 || typeIndex > maxLength) {
				typeIndex = defaultIndex;
			}
		} catch (Exception e) {
			return defaultIndex;
		}
		return typeIndex;
	}

	public static boolean updateMessageCheck(Context context, String index) {
		AlarmManager mAlarmManager = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		int i = 2;
		try {
			i = Integer.parseInt(index);
		} catch (Exception e) {
			i = 2;
		}
		if (i < 0 || i > Common.CHECK_RATE_VALUE.length) {
			i = 2;
		}

		long t = Common.CHECK_RATE_VALUE[i];

		Intent check = new Intent(context, MessageCheckService.class);
		check.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		PendingIntent checkIntent = PendingIntent.getService(context, 0, check,
				Intent.FLAG_ACTIVITY_NEW_TASK);

		mAlarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP,
				0, t, checkIntent);
		return true;
	}

	public static boolean isNeedToVibrate(Context context) {
		try {
			SharedPreferences pf = PreferenceManager
					.getDefaultSharedPreferences(context);
			return pf.getBoolean(
					context.getString(R.string.pref_notification_vibrate_key),
					false);

		} catch (Exception e) {
			return false;
		}
	}

	public static Uri getRingStone(Context context) {
		try {
			Uri ringstone = null;
			SharedPreferences pf = PreferenceManager
					.getDefaultSharedPreferences(context);
			String r = pf.getString(
					context.getString(R.string.pref_notification_ringtone_key),
					null);

			if (r != null) {
				ringstone = Uri.parse(r);
			}
			return ringstone;
		} catch (Exception e) {
			return null;
		}

	}

	public static boolean turnOffMessageCheck(Context context) {
		try {
			AlarmManager mAlarmManager = (AlarmManager) context
					.getSystemService(Context.ALARM_SERVICE);

			Intent check = new Intent(context, MessageCheckService.class);
			check.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			PendingIntent pd = PendingIntent.getService(context, 0, check,
					PendingIntent.FLAG_UPDATE_CURRENT);
			mAlarmManager.cancel(pd);
			Toast.makeText(context, "Check message is off!", Toast.LENGTH_SHORT)
					.show();
			return true;

		} catch (Exception e) {
			Toast.makeText(context, "error during turn on message check!",
					Toast.LENGTH_SHORT).show();
			return false;
		}
	}

	public static boolean isPictureUrl(String url, String domain,
			String subreddit) {
		try {
			if (url == null || "".equals(url.trim())) {
				return false;
			}

			for (String m : IMAGE_END) {
				if (url.toLowerCase().endsWith(m)) {
					return true;
				}
			}

			if (subreddit.contains("gif")) {
				return false;
			}

			// to be save
			if (subreddit.toLowerCase().contains(Common.SUBREDDIT_GIFS)
					|| url.toLowerCase().endsWith(".gif")) {
				return false;
			}

			if (domain.equalsIgnoreCase(Common.DOMAIN_IMGUR)
					&& !url.toLowerCase().contains(Common.DOMAIN_IMGUR_GROUP)
					&& !url.toLowerCase().contains(Common.DOMIAN_IMAGE_GROUP_S)) {
				return true;
			}
			if (domain.equalsIgnoreCase(Common.DOMAIN_IMGUR_I)
					&& !url.toLowerCase().contains(Common.DOMAIN_IMGUR_GROUP_I)
					&& !url.toLowerCase().contains(
							Common.DOMIAN_IMAGE_GROUP_S_I)) {
				return true;
			}

		} catch (Exception e) {
			return false;
		}
		return false;
	}

	public static String appendJPG(String url) {
		try {
			for (String m : IMAGE_END) {
				if (url.endsWith(m)) {
					return url;
				}
			}

			url = url + ".jpg";
			return url;
		} catch (Exception e) {
			return url;
		}

	}

	public static int getCurrentTheme(Context context, String key) {
		SharedPreferences defaultPref = PreferenceManager
				.getDefaultSharedPreferences(context);
		int index = 0;
		try {
			index = Integer.parseInt(defaultPref.getString(key, "0"));
			Log.d("CommonUtil", "index:" + index);
		} catch (Exception e) {
			// do nothing
		}
		if (index >= 0 && index < Common.THEME_VALUE_ARRAY.length) {
			return Common.THEME_VALUE_ARRAY[index];
		} else {
			return Common.THEME_VALUE_ARRAY[0];
		}
	}

	public static int getIndicateColor(Context context, int resId) {
		SharedPreferences defaultPref = PreferenceManager
				.getDefaultSharedPreferences(context);
		int index = 0;
		try {
			index = Integer.parseInt(defaultPref.getString(
					context.getString(resId), "0"));
			Log.d("CommonUtil", "index:" + index);
		} catch (Exception e) {
			// do nothing
		}
		if (index >= 0 && index < Common.COMMENT_INDICATE_COLOR.length) {
			return Common.COMMENT_INDICATE_COLOR[index];
		} else {
			return Common.COMMENT_INDICATE_COLOR[0];
		}
	}

	public static int getCurrentTheme(Context context, int resId) {
		return getCurrentTheme(context, context.getString(resId));
	}

	public static int getCommentCount(Context context) {
		SharedPreferences defaultPref = PreferenceManager
				.getDefaultSharedPreferences(context);
		int index = 1;
		try {
			index = Integer.parseInt(defaultPref.getString(
					context.getString(R.string.pref_key_comment_count), "1"));
		} catch (Exception e) {
			// do nothing
		}
		if (index >= 0 && index < 5) {
			return index;
		} else {
			return 1;
		}

	}

	public static boolean needToComfirmExit(Context context) {
		try {
			SharedPreferences pf = PreferenceManager
					.getDefaultSharedPreferences(context);
			return pf.getBoolean(
					context.getString(R.string.pref_confirm_exit_key), true);
		} catch (Exception e) {
			return false;
		}
	}


	public static boolean safeForWork(Context context) {
		try {
			SharedPreferences pf = PreferenceManager
					.getDefaultSharedPreferences(context);
			return pf.getBoolean(
					context.getString(R.string.pref_safe_for_work), true);
		} catch (Exception e) {
			return true;
		}
	}

	public static boolean needToShowPictureInfo(Context context) {
		try {
			SharedPreferences pf = PreferenceManager
					.getDefaultSharedPreferences(context);
			return pf.getBoolean(context
					.getString(R.string.pref_key_show_text_viewing_picture),
					true);
		} catch (Exception e) {
			return true;
		}
	}

	public static boolean needToLoadPicture(Context context) {
		try {
			SharedPreferences pf = PreferenceManager
					.getDefaultSharedPreferences(context);
			return pf.getBoolean(
					context.getString(R.string.pref_load_picture_key), true);
		} catch (Exception e) {
			return true;
		}
	}

	public static boolean needToLoadWebPage(Context context) {
		try {
			SharedPreferences pf = PreferenceManager
					.getDefaultSharedPreferences(context);
			return pf.getBoolean(
					context.getString(R.string.pref_load_webpage_key), true);
		} catch (Exception e) {
			return true;
		}
	}

	private static AndDown mMarkDown;

	public static AndDown getRedditETMarkDown() {
		if (mMarkDown == null) {
			mMarkDown = new AndDown();
		}
		return mMarkDown;
	}

	/**
	 * get the relative time (remember to *1000 if it is in second)
	 * 
	 * @param then
	 * @param context
	 * @return
	 */
	public static CharSequence getRelateTimeString(long then, Context context) {
		CharSequence result = "";
		long now = System.currentTimeMillis();
		long relate = Math.abs(now - then);

		// Seconds
		if (relate < DateUtils.MINUTE_IN_MILLIS) {
			result = DateUtils.getRelativeTimeSpanString(then, now,
					DateUtils.SECOND_IN_MILLIS,
					DateUtils.FORMAT_ABBREV_RELATIVE);
		}

		// Minutes
		else if (relate < DateUtils.HOUR_IN_MILLIS) {
			result = DateUtils.getRelativeTimeSpanString(then, now,
					DateUtils.MINUTE_IN_MILLIS,
					DateUtils.FORMAT_ABBREV_RELATIVE);
		}
		// Hour
		else if (relate < DateUtils.DAY_IN_MILLIS) {
			result = DateUtils.getRelativeTimeSpanString(then, now,
					DateUtils.HOUR_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE);
		}
		// Day
		else if (relate < DateUtils.WEEK_IN_MILLIS) {
			result = DateUtils.getRelativeTimeSpanString(then, now,
					DateUtils.DAY_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE);
		}

		// from here , need to caculate by ourself.
		else if (relate < DateUtils.YEAR_IN_MILLIS) {
			long dayc = CommonUtil.getNumberOfDaysPassed(now, then);
			return dayc + " " + context.getString(R.string.days_ago);
		} else {
			long dayc = CommonUtil.getNumberOfDaysPassed(now, then);
			long yearc = (dayc + 1) / 364;
			if (yearc == 1)
				return yearc + " " + context.getString(R.string.year_ago);
			else {
				return yearc + " " + context.getString(R.string.years_ago);
			}
		}
		return result;
	}

	public static String getSavePicturePath(Context context) {
		try {
			SharedPreferences pf = PreferenceManager
					.getDefaultSharedPreferences(context);
			return pf.getString(
					context.getString(R.string.pref_key_picture_save_path),
					Environment.DIRECTORY_PICTURES);
		} catch (Exception e) {
			return Environment.DIRECTORY_PICTURES;
		}
	}

	public static long getNumberOfDaysPassed(long date1, long date2) {
		Time sThenTime = new Time();
		sThenTime.set(date1);
		int day1 = Time.getJulianDay(date1, sThenTime.gmtoff);
		sThenTime.set(date2);
		int day2 = Time.getJulianDay(date2, sThenTime.gmtoff);
		return Math.abs(day2 - day1);
	}

}
