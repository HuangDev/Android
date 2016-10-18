package com.softgame.reddit.utils;

import android.app.AlarmManager;
import android.webkit.URLUtil;

import com.softgame.reddit.R;

public class Common {

	public static final boolean SHOW_LOG = true;

	/**
	 * Fragment onCreatedActivity check preference value change and registe
	 * preference onDetach unregiste preference Android:CacheColorHint can not
	 * be transprant(#00000000).
	 */

	// friend
	// https://ssl.reddit.com/api/friend

    public static final String REDDIT_ET_USERNAME = "redditet";	
    public static final String REDDIT_ET_SUBREDDIT_NAME = "redditet";
    public static final String REDDIT_ET_SUBREDDIT = "/r/redditet";
	public static final String REDDIT_ET_ID = "t5_2ukxd";
	public static final String REDDIT_ET_GOOGLE_PLAY_FREE= "http://market.android.com/details?id=com.softgame.redditopen";
	public static final String REDDIT_ET_GOOGLE_PLAY_PAID = "http://market.android.com/details?id=com.softgame.reddit";
	
	public static final String SHARE_APP_LINK = "http://market.android.com/details?id=com.softgame.reddit";

	public static final String COMMENT_PATTEN = "/?r/\\w+/comments/\\w+/\\w+/?$";
	public static final String COMMENT_CONTEXT_PATENT = "/?r/\\w+/comments/\\w+/\\w+/\\w+/?$";
	public static final String LABEL_DELETE = "[deleted]";

	public static final String PREFERENCE_TAG = "preference change";

	public static final String DOMAIN_IMGUR = "imgur.com";
	public static final String DOMAIN_IMGUR_I = "i.imgur.com";
	public static final String DOMAIN_IMGUR_GROUP = "http://imgur.com/a/";
	public static final String DOMAIN_IMGUR_GROUP_I = "http://i.imgur.com/a/";
	public static final String DOMIAN_IMAGE_GROUP_S = "https://imgur.com/a/";
	public static final String DOMIAN_IMAGE_GROUP_S_I = "https://i.imgur.com/a/";

	public static final String SUBREDDIT_GIFS = "gif";
	public static final String SUBREDDIT_GIFS_CAP = "GIF";
	public static final String FROM_INSIDE_APP = "from_inside_of_app";

	public static final String SUBREDDIT_PATTEN = "[a-zA-Z0-9]*";

	public static final String DOMAIN_YOUTUBE = "youtube.com";
	public static final String DOMAIN_REDDIT = "reddit.com";

	public static final String RESULT_NO_TO_USER = "result_no_to_user";

	/**
	 * Common
	 */

	public static final String APP_VERSION = "Reddit ET 1.0";
	public static final String IMAGE_CACHE_DIR = "cacheimages";

	public static final String URL_SEPERATOR = "/";
	public static final String TYPE_JSON_PATH = ".json";

	public static final String KEY_USERNAME = "user";
	public static final String KEY_PASSWORD = "passwd";
	public static final String KEY_API_TYPE = "api_type";

	public static final String KEY_TITLE = "title";
	public static final String KEY_URL = "url";
	public static final String KEY_SR = "sr";
	public static final String KEY_R = "r";
	public static final String KEY_KIND = "kind"; // link text

	public static final String KEY_EXTRA_IS_COMMENT = "key_start_comment";

	public static final String KEY_SESSION = "reddit_session";
	public static final String KEY_ERRORS = "errors";
	public static final String KEY_JSON = "json";
	public static final String KEY_DATA = "data";
	public static final String KEY_MODHASH = "modhash";

	public static final String KEY_WIDGET_POSITION = "key_widget_position";
	public static final String KEY_WIDGET_REFRESH = "key_widget_refresh";
	public static final String KEY_EXTRA_WIDGET_DATA = "key_widget_data_extra";
	public static final String ACTION_WIDGET_NAVIGATION_LEFT = "com.softgame.reddit.NAVIGATION_LEFT";
	public static final String ACTION_WIDGET_NAVIGATION_RIGHT = "com.softgame.reddit.NAVIGATION_RIGHT";

	public static final String KEY_USER = "uh";
	public static final String KEY_ID = "id";

	public static final String KEY_FULLNAME = "id";
	public static final String KEY_Q = "q";

	public static final String KEY_ACTION = "action";
	public static final String KEY_RENDERSTYLE = "renderstyle";

	public static final String KEY_AFTER = "after";
	public static final String KEY_SORT = "sort";
	public static final String KEY_T = "t";
	public static final String KEY_SEARCH = "q";

	public static final String KEY_LIMIT = "limit";
	public static final String KEY_TYPE = "type";

	public static final String SORT_NEW = "";
	public static final String SORT_HOT = "hot";
	public static final String SORT_TOP = "top";
	public static final String SORT_CONTROVERSIAL = "controversial";

	public static final String KEY_TEXT = "text";
	public static final String KEY_THING_ID = "thing_id";
	public static final String KEY_NAME = "name";
	public static final String KEY_CONTAINER = "container";
	public static final String KEY_RESTRICT_SR = "restrict_sr";

	public static final String KEY_TO = "to";
	public static final String KEY_SUBJECT = "subject";

	public static final String KEY_FROM_SHARE = "from_share";
	public static final String KEY_BODY_TEXT = "body_text";
	public static final String KEY_SUBJECT_TEXT = "subject_text";
	public static final String KEY_SUBREDDIT_NAME = "subreddit_name";
	

	public static final String LOGIN_SUCCESS = "login_success";
	public static final String KEY_MESSAGE_ID = "key_message_id";

	public static final String EXTRA_SUBSCRIBE_LIST = "subscribe_list";

	public static final String EXTRA_SUBREDDIT_MODEL = "subreddit_model";

	public static final String EXTRA_SUBSCRIBE_NAME = "subscribe_name";
	public static final String EXTRA_SUBSCRIBE = "subscribe";

	public static final String EXTRA_MESSAGE_ID = "extra_message_id";
	public static final String EXTRA_MESSAGE_SENDER = "extra_message_sender";

	public static final String DEFAULT_SUBREDDIT_NAME = "FRONT";
	public static final String DEFAULT_SUBREDDIT = "/";

	public static final String EXTRA_OVERVIEW_COMMENT_URL = "overview_commment_url";
	public static final String EXTRA_OVERVIEW_LOAD_TYPE = "overview_comment_load_type";

	public static final String KEY_DRAFT_TITLE = "draft_title";
	public static final String KEY_DRAFT_IS_LINK = "draft_is_link";
	public static final String KEY_DRAFT_LINK_URL = "draft_is_url";
	public static final String KEY_DRAFT_OPTION_TEXT = "draft_option_text";
	public static final String KEY_DRAFT_SUBREDDIT = "draft_subreddit";

	public static final String[] COMMENT_SORT_VALUE_ARRAY = new String[] {
			"confidence", "hot", "new", "controversial", "top", "old" };

	public static final int REQUEST_SETTING = 0x853;
	public static final int[] THEME_VALUE_ARRAY = new int[] {
			R.style.Theme_Blue, R.style.Theme_Balck };

	public static final String KEY_REDDITOR_NAME = "key_redditor_name";

	/**
	 * Fragment
	 */
	public static final String KEY_SUBSCRIBE_TYPE = "subscribe_type";
	public static final String KEY_MESSAGE_TYPE = "message_type";
	public static final String KEY_MESSAGE_MODEL = "message_model";
	public static final String KEY_MESSAGE_KIND = "message_kind";
	public static final String KEY_MODERATOR_TYPE = "moderator_type";

	public static final String KEY_FROM_NOTIFICATION = "from_notification";

	public static final long[] VIBRATE_PATTENT = new long[] { 3000, 12000 };

	/**
	 * Result
	 */
	public static final String RESULT_SUCCESS = "success";
	public static final String RESULT_WRONG_PASSWORD = "invalid password";
	public static final String RESULT_UNKNOW = "seems like there is an error,try again latter!\nNote:Reddit servers may cause this ==!";
	public static final String RESULT_WRONG_ME = "errors while getting user infomation";
	public static final String RESULT_FETCHING_FAIL = "fail to fetch the data";
	public static final String RESULT_NO_DEFAULT_USER = "no_default_user";
	public static final String RESULT_TRY_TOO_MUCH = "try to much";
	public static final String RESULT_TOO_LATE = "too late to reply";
	public static final String RESULT_PAGE_NOTFOUND = "error404";

	public static final String RESULT_TASK_CANCLE = "task cancel";

	public static final String RESULT_NO_REDDITOR = "can not find the user!";
	public static final String RESULT_REDDITOR_PROFILE = "redditor profile";
	public static final String RESULT_MY_PROFILE = "my profile";
	public static final String RESULT_NEED_CAPTCHA = "need captcha";

	public static final String RESULT_REDDIT_BROKE = "reddit broke";

	// if suit, means error
	public static final String EXAMPLE_TOO_MUCH = "you are doing that too much";
	public static final String EXAMPLE_PLEASE_LOGIN_IN = "please login to do that";
	public static final String EXAMPLE_TOO_LATE = "it's too late to reply to it";
	public static final String EXAMPLE_REDDIT_BROKE = "reddit broke!";
	public static final String EXAMPLE_SUBMIT_TOO_FAST = "you are trying to submit too fast";
	public static final String EXAMPLE_SUBREDDIT_NOEXIST = "subreddit_noexist";
	public static final String EXAMPLE_SUBREDDIT_NOTALLOWED = "subreddit_notallowed";

	public static final String RESULT_SUBREDDIT_NOEXIST = "subreddint no exist";
	public static final String RESULT_SUBREDDIT_NOTALLLOW = "subreddit_not allow";
	public static final String RESULT_SUBMIT_TOO_FAST = "submit too fast";

	/**
	 * Error
	 */
	public static final String ERROR_IGNORE = "error_ignore";
	public static final String ERROR_HTTP = "access internet error";
	public static final String ERROR_IO = "read data error,try again latter";
	public static final String ERROR_URL = "invalite url";

	/**
	 * User Infomation
	 */
	public static final String URL_USER_INFO = "http://www.reddit.com/api/me.json";
	// reset password post parameter: passwd parameter: password2 reset
	public static final String URL_RESET_PASSWORD = "reddit.com/api/resetpassword";
	public static final String KEY_RESET_PASSWORD_OLD = "passwd";
	public static final String KEY_RESET_PASSWORD_NEW = "passwd2";
	public static final String KEY_RESET_PASSWORD_RESET = "reset";

	/**
	 * Pref
	 */
	public static final String PREF_USER = "pref_user";
	// public static final String PREF_DEFALUT = "pref_default";
	public static final String PREF_DEFAULT_USER_KEY = "pref_default_username_key";

	// how key make: filename + key name
	public static final String PREF_DATE_RELOAD = "pref_date_reload";

	/**
	 * Activity Requst
	 */
	public static final int REQUST_LOGIN = 0x1234;
	public static final int REQUEST_ACTIVITY_COMMENT = 0x4321;
	public static final int REQUEST_ACTIVITY_WEBVIEW = 0x3342;

	/**
	 * Subreddit
	 */
	public static final int ID_DATE_SPINNER = 0x234;
	public static final int ID_NEW_SPINNER = 0x433;

	public static final String[] SUBREDDIT_VALUE_ARRAY = new String[] { "",
			"/new", "/mine" };

	public static final String[] SEARCH_SORT_VALUE_ARRAY = new String[] {
			"relevance", "new", "top" };

	public static final String SUBREDDIT_FRIEND_NAME = "FRIENDS";
	public static final String SUBREDDIT_FRIEND = "r/friends/";

	public static final String SUBREDDIT_ALL_NAME = "ALL";
	public static final String SUBREDDIT_ALL = "r/all/";

	public static final String SUBREDDIT_MOD_NAME = "MOD";
	public static final String SUBREDDIT_MOD = "r/mod/";

	public static final int SUBREDDIT_TYPE_LAODING = 0;
	public static final int SUBREDDIT_TYPE_NO_ITEM = 1;
	public static final int SUBREDDIT_TYPE_MORE = 2;
	public static final int SUBREDDIT_TYPE_ITEM_LINK = 3;
	public static final int SUBREDDIT_TYPE_ITEM_LINK_NOPIC = 4;
	public static final int SUBREDDIT_TYPE_ITEM_SELFPOST = 5;
	public static final int SUBREDDIT_TYPE_NO_MORE = 6;
	public static final int SUBREDDIT_TYPE_NEW_HEADER = 7;
	public static final int SUBREDDIT_TYPE_DATE_HEADER = 8;
	public static final int SUBREDDIT_TYPE_EMPTY = 9;
	public static final int SUBREDDIT_TYPE_LOAD_FAIL = 10;

	public static final int SEARCH_TYPE_LAODING = 0;
	public static final int SEARCH_TYPE_NO_ITEM = 1;
	public static final int SEARCH_TYPE_MORE = 2;
	public static final int SEARCH_TYPE_ITEM_LINK = 3;
	public static final int SEARCH_TYPE_ITEM_LINK_NOPIC = 4;
	public static final int SEARCH_TYPE_ITEM_SELFPOST = 5;
	public static final int SEARCH_TYPE_NO_MORE = 6;
	public static final int SEARCH_TYPE_SORT = 7;
	public static final int SEARCH_TYPE_EMPTY = 8;
	public static final int SEARCH_TYPE_LOAD_FAIL = 9;

	// public static final String SUBREDDIT_PRE = "http://www.reddit.com/r/";
	public static final String SUBREDDIT_JSON = "/.json";
	public static final int KIND_HOT = 0;
	public static final int KIND_NEW = 1;
	public static final int KIND_CONTROVERSIAL = 2;
	public static final int KIND_TOP = 3;
	public static final int KIND_SAVED = 4;

	public static final String KIND_OVERVIEW = "";
	public static final String KIND_COMMENTS = "comments/";
	public static final String KIND_SUBMITTED = "submitted/";

	public static final String KIND_LIKED = "liked/";
	public static final String KIND_DISLIKED = "disliked/";
	public static final String KIND_HIDDEN = "hidden/";

	public static final int REQUEST_SEARCH_SUBSCRIBE = 0x3521;

	// --------------------- Vote------------------
	public static final String KEY_VOTE = "dir";
	public static final int VOTE_UP = 1;
	public static final int VOTE_DOWN = -1;
	public static final int VOTE_RESCIND = 0;

	// -------------- Subscribe --------
	public static final String URL_MINE_SUBSCRIBE = "http://www.reddit.com/reddits/mine.json";

	// -------------------Intent -------------
	public static final String INTENT_EXTRA_ID = "subreddit_id_intent_extra";
	public static final String INTENT_EXTRA_LINK = "subreddit_post_link";
	public static final String INTENT_EXTRA_SUBREDDIT = "com.softgame.reddit.SUBREDDIT_ITEM";
	public static final String INTENT_EXTRA_URL = "intent_extra_url";

	public static final String EXTRA_MESSAGE = "extra_message";
	public static final String EXTRA_MESSAGE_NAME = "extra_message_name";

	public static final int TYPE_BASE = 0x11;
	public static final int TYPE_AUTH = 0x22;
	public static final int TYPE_EITHER = 0x33;

	public static final int TYPE_MINE = 0x1111;
	public static final int TYPE_PUBLIC = 0x2222;

	public static final int TYPE_LOADING = 0;
	public static final int TYPE_ITEM_REMOTE = 1;
	public static final int TYPE_ITEM_LOCAL = 2;
	public static final int TYPE_MORE = 3;
	public static final int TYPE_NO_MORE = 4;
	public static final int TYPE_NO_ITEM = 5;
	public static final int TYPE_EMPTY = 6;

	public static final String EXTRA_USERNAME = "intent_username";
	public static final String EXTRA_SUBREDDIT = "intent_subreddit";
	public static final String EXTRA_RESTART_APP = "intent_restart_app";
	public static final String EXTRA_FROM_CANVAS = "intent_from_canvas";
	public static final String EXTRA_SUBREDDIT_NAME = "intent_subreddit_name";
	public static final String EXTRA_GO_TO_REDDIT_ET = "intent_go_to_redditet";

	public static final String PREF_KEY_PICTURE_SAVE_PATH = "key_picture_save_path";

	// public static final String PREF_DATA_STATE = "datastate";
	// for front page subreddit list
	public static final String PREF_KEY_SUBREDDIT = "pref_key_subreddit";
	public static final String PREF_KEY_SUBREDDIT_NAME = "pref_key_subreddit_name";

	// for canvas subreddit list
	public static final String PREF_KEY_CANVAS_SUBREDDIT = "pref_key_canvas_subreddit";
	public static final String PREF_KEY_CANVAS_SUBREDDIT_NAME = "pref_key_canvas_subreddit_name";

	// for widget subreddit
	public static final String PREF_KEY_WIDGET_SUBREDDIT = "pref_key_widget_subreddit";
	public static final String PREF_KEY_WIDGET_SUBREDDIT_NAME = "currentsubredditname_widget";

	public static final String PREF_KEY_WIDGET_SUBREDDIT_DATA = "key_widget_subreddit_data";

	public static final String TYPE_MESSAGE_UNREAD = "unread";

	public static final String[] TYPE_ARRAY = new String[] { "", "/new/",
			"/controversial/", "/top/", "/saved/" };

	public static final String[] TYPE_ARRAY_TEXT = new String[] { "hot", "new",
			"controversial", "top", "saved" };

	public static final String[] DATA_ARRAY = new String[] { "hour", "day",
			"week", "month", "year", "all" };
	public static final String[] NEW_ARRAY = new String[] { "new", "rising" };

	public static boolean isVaildUrl(String url) {
		if (url == null || url.trim().equals("")) {
			return false;
		}
		return URLUtil.isValidUrl(url.trim());
	}

	public static final int MSG_SAVE_CACHE = 0x555;
	public static final String BUNDLE_URL = "bundle_url";
	public static final String BUNDLE_BYTE_IMAGE = "bundle_byte_image";

	public static final int MSG_SHOW_LOADING = 0x666;

	public static final String[] REDDITOR_TEXT_ARRAY = new String[] { "ABOUT",
			"Account Age", "Karma", "Link Karma", "Comment Karma" };

	public static final String[] OVERVIEW_TYPE_ARRAY_TEXT = new String[] {
			"Profile", "Overview", "Comments", "Submitted" };

	public static final String[] LIKED_TYPE_ARRAY_VALUE = new String[] {
			"/liked/", "/disliked/", "/hidden/", "/saved/" };

	public static final String[] LIKED_TYPE_ARRAY_VALUE_TEXT = new String[] {
			"liked", "disliked", "hidden", "saved" };

	public static final long[] CHECK_RATE_VALUE = new long[] { 5 * 60 * 1000,
			AlarmManager.INTERVAL_FIFTEEN_MINUTES,
			AlarmManager.INTERVAL_HALF_HOUR, AlarmManager.INTERVAL_HOUR,
			AlarmManager.INTERVAL_HALF_DAY, AlarmManager.INTERVAL_DAY };

	public static final String MESSAGE_UNREAD_PATH = "unread";
	public static final String MESSAGE_UNREAD_URL = "http://www.reddit.com/message/unread/";

	public static final int COMMENT_INDICATE_COLOR[] = new int[] {
			R.color.blue_indicate_color, R.color.black_indicate_color };

}
