package com.softgame.reddit.service;

import java.io.File;

import org.json.custom.JSONObject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.softgame.reddit.ConversationActivity;
import com.softgame.reddit.R;
import com.softgame.reddit.UnreadMessageActivity;
import com.softgame.reddit.cache.DiskLruCache;
import com.softgame.reddit.cache.ImageCache.ImageCacheParams;
import com.softgame.reddit.fragment.NewMessageFragmentList;
import com.softgame.reddit.model.MessageItem;
import com.softgame.reddit.model.MessageModel;
import com.softgame.reddit.utils.Common;
import com.softgame.reddit.utils.CommonUtil;
import com.softgame.reddit.utils.MessageManager;
import com.softgame.reddit.utils.RedditManager;

public class ClearDiskService extends CustomService {

	public static final String TAG = "MessageCheckService";
	@Override
	protected void onHandleIntent(Intent intent) {
		Log.d(TAG, "start to clear disk using sheddule");
		ImageCacheParams cacheParams = new ImageCacheParams(
				Common.IMAGE_CACHE_DIR);
		final File diskCacheDir = DiskLruCache.getDiskCacheDir(this,
				cacheParams.uniqueName);
		DiskLruCache mDiskCache = DiskLruCache.openCache(this, diskCacheDir,
				cacheParams.diskCacheSize);
		mDiskCache.clearCache();
		Toast.makeText(this.getApplicationContext(),"clear disk from intent",Toast.LENGTH_LONG).show();
	}
}
