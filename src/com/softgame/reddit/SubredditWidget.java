/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.softgame.reddit;

import java.io.File;

import org.json.custom.JSONObject;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.softgame.reddit.cache.DiskLruCache;
import com.softgame.reddit.cache.ImageCache;
import com.softgame.reddit.cache.ImageFetcher;
import com.softgame.reddit.cache.ImageCache.ImageCacheParams;
import com.softgame.reddit.cache.ImageResizer;
import com.softgame.reddit.model.SubRedditItem;
import com.softgame.reddit.model.SubRedditModel;
import com.softgame.reddit.service.CustomService;
import com.softgame.reddit.service.WidgetService;
import com.softgame.reddit.utils.Common;
import com.softgame.reddit.utils.CommonUtil;
import com.softgame.reddit.utils.RedditManager;
import com.softgame.reddit.utils.SubRedditManager;

/**
 * Define a simple widget that shows the Wiktionary "Word of the day." To build
 * an update we spawn a background {@link Service} to perform the API queries.
 */
public class SubredditWidget extends AppWidgetProvider {
	public static final String TAG = "SubredditWidget";
	public static final int MASSAGE_WIDGET_PICTURE = 0x2341;

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		// To prevent any ANR timeouts, we perform the update in a service
		context.startService(new Intent(context, SubRedditWidgetService.class));
	}

	public static class SubRedditWidgetService extends WidgetService {

		public String mCurrentSubReddit = Common.DEFAULT_SUBREDDIT;
		public String mCurrentSubRedditName = Common.DEFAULT_SUBREDDIT_NAME;
		// hot new ..
		public int mCurrentKind = 0;
		// day week all time...
		public int mCurrentControversalSort = 2;
		public int mCurrentTopSort = 2;
		public int mCurrentNewType = 1;
		public boolean mIsRefresh = false;
		public PictureHandler mPictureHandler;
		private volatile Looper mPictureLooper;

		private ImageCache mImageCache = null;
		WidgetAdapter mWidgetAdapter;

		ImageResizer mImageResizer;

		private final class PictureHandler extends Handler {
			public PictureHandler(Looper looper) {
				super(looper);
			}

			@Override
			public void handleMessage(Message msg) {
				// get pic
				SubRedditItem item = (SubRedditItem) msg.obj;
				int startId = msg.arg1;
				int position = msg.arg2;
				// load picture
				// create Discache
				if (mImageCache == null) {
					ImageCacheParams cacheParams = new ImageCacheParams(
							Common.IMAGE_CACHE_DIR);
					cacheParams.memoryCacheEnabled = false;
					mImageCache = new ImageCache(
							SubRedditWidgetService.this.getApplicationContext(),
							cacheParams);
				}
				if (mImageResizer == null) {
					mImageResizer = new ImageFetcher(
							SubRedditWidgetService.this.getApplicationContext(),
							160, 160);
					mImageResizer.setImageCache(mImageCache);
				}

				String data = item.thumbnail;
				Bitmap bitmap = null;

				// If the image cache is available and this task has not been
				// cancelled by another
				// thread and the ImageView that was originally bound to this
				// task
				// is still bound back
				// to this task and our "exit early" flag is not set then try
				// and
				// fetch the bitmap from
				// the cache
				if (mImageCache != null) {
					String fileName = mImageCache.getBitmapFromDiskCache(data);
					if (fileName != null && !"".equals(fileName)) {
						bitmap = mImageResizer
								.processBitmap(new File(fileName));
					}
				}

				// If the bitmap was not found in the cache and this task has
				// not
				// been cancelled by
				// another thread and the ImageView that was originally bound to
				// this task is still
				// bound back to this task and our "exit early" flag is not set,
				// then call the main
				// process method (as implemented by a subclass)

				// download
				if (bitmap == null) {
					bitmap = mImageResizer.processBitmap(data);
				}

				// If the bitmap was processed and the image cache is available,
				// then add the processed
				// bitmap to the cache for future use. Note we don't check if
				// the
				// task was cancelled
				// here, if it was, and the thread is still running, we may as
				// well
				// add the processed
				// bitmap to our cache as it might be used again in the future
				if (bitmap != null && mImageCache != null) {
					mImageCache.addBitmapToCache(data, bitmap);
				}

				if (startId == mMostRecentStartId && bitmap == null) {
					bitmap = BitmapFactory.decodeResource(
							SubRedditWidgetService.this.getResources(),
							R.drawable.thumbnail_default);
				}
				// apply to widget
				if (startId == mMostRecentStartId && bitmap != null) {
					mWidgetAdapter.updateWidgetView(bitmap, position, startId);
				}
				if (startId == mMostRecentStartId) {
					Log.d(TAG, "Picture Handler stop the service");
					stopSelf(startId);
				}

			}
		}

		@Override
		public void onCreate() {
			super.onCreate();
			HandlerThread thread = new HandlerThread(
					"IntentService[PictureThead]");
			thread.start();
			mPictureLooper = thread.getLooper();
			mPictureHandler = new PictureHandler(mPictureLooper);
		}

		@Override
		public void onDestroy() {
			super.onDestroy();
			if (mPictureLooper != null)
				mPictureLooper.quit();
		}

		@Override
		protected void onHandleIntent(Intent intent, int startId) {
			// intent must have (position)
			int wigetPosition = 0;
			SubRedditModel subredditModel = null;
			if (intent.getExtras() != null) {
				wigetPosition = intent.getExtras().getInt(
						Common.KEY_WIDGET_POSITION, 0);
				mIsRefresh = intent.getExtras().getBoolean(
						Common.KEY_WIDGET_REFRESH, false);
				subredditModel = intent.getExtras().getParcelable(
						Common.KEY_EXTRA_WIDGET_DATA);
			}
			if (wigetPosition < 0) {
				wigetPosition = 0;
			}

			SharedPreferences pf = PreferenceManager
					.getDefaultSharedPreferences(this.getApplicationContext());
			try {
				mCurrentSubReddit = pf.getString(
						Common.PREF_KEY_WIDGET_SUBREDDIT,
						Common.DEFAULT_SUBREDDIT);
				mCurrentSubRedditName = pf.getString(
						Common.PREF_KEY_WIDGET_SUBREDDIT_NAME,
						Common.DEFAULT_SUBREDDIT_NAME);
				mCurrentKind = pf.getInt(
						this.getString(R.string.key_widget_subreddit_type), 0);
				mCurrentNewType = pf.getInt(
						this.getString(R.string.key_widget_new_sort), 1);
				mCurrentControversalSort = pf.getInt(
						this.getString(R.string.key_widget_controversal_sort),
						2);
				mCurrentTopSort = pf.getInt(
						this.getString(R.string.key_widget_top_sort), 2);
				if (mCurrentNewType < 0 || mCurrentNewType > 1) {
					mCurrentNewType = 0;
				}
				if (mCurrentKind < 0 || mCurrentKind > 3) {
					mCurrentKind = 0;
				}
				if (mCurrentControversalSort < 0
						|| mCurrentControversalSort > 5) {
					mCurrentControversalSort = 2;
				}
				if (mCurrentTopSort < 0 || mCurrentTopSort > 5) {
					mCurrentTopSort = 2;
				}
			} catch (Exception e) {
				// do nothing, use default
			}
			mWidgetAdapter = new WidgetAdapter(getApplicationContext());

			// if old data is not presented.
			if (mIsRefresh || subredditModel == null
					|| subredditModel.mItemList.isEmpty()) {
				mWidgetAdapter.setWidgetPosition(0);
				// update widget to loading
				mWidgetAdapter.setLoading(true, true);
				// read load the data
				Log.d(TAG, "Position:" + wigetPosition
						+ "is out of listsize or  data is null");
				int sortType = mCurrentKind == 2 ? mCurrentControversalSort
						: mCurrentTopSort;
				JSONObject dataJSON = new JSONObject();
				String result = SubRedditManager.getSubReddit(
						mCurrentSubReddit, mCurrentKind,
						Common.TYPE_ARRAY[mCurrentKind],
						Common.NEW_ARRAY[mCurrentNewType],
						Common.DATA_ARRAY[sortType], null, dataJSON,
						getApplicationContext());
				mWidgetAdapter.setLoading(false, false);
				// result
				if (Common.RESULT_SUCCESS.equals(result)) {
					// decode
					SubRedditModel model = SubRedditModel
							.convertToModel(dataJSON);
					mWidgetAdapter.addModel(model);
					// save data to preference
					// saveToPreference(dataJSON);
				} else {
					// load fail
					mWidgetAdapter.setFail(true, true);
					if (Common.RESULT_FETCHING_FAIL.equals(result)) {
						Toast.makeText(this.getApplicationContext(),
								"Internet error!", Toast.LENGTH_SHORT).show();
						return;
					}
				}
				// old data exists
			} else {
				mWidgetAdapter.addModel(subredditModel);
				if (wigetPosition > subredditModel.mItemList.size()) {
					wigetPosition = subredditModel.mItemList.size();
				}

				mWidgetAdapter.setWidgetPosition(wigetPosition);
				// load more
				if (wigetPosition == subredditModel.mItemList.size()
						&& subredditModel.after != null
						&& !"".equals(subredditModel.after)) {
					mWidgetAdapter.setLoading(true, true);
					Log.d(TAG, "Position:" + wigetPosition + "load more!");
					int sortType = mCurrentKind == 2 ? mCurrentControversalSort
							: mCurrentTopSort;
					JSONObject dataJSON = new JSONObject();
					String result = SubRedditManager.getSubReddit(
							mCurrentSubReddit, mCurrentKind,
							Common.TYPE_ARRAY[mCurrentKind],
							Common.NEW_ARRAY[mCurrentNewType],
							Common.DATA_ARRAY[sortType], subredditModel.after,
							dataJSON, getApplicationContext());
					mWidgetAdapter.setLoading(false, false);
					// result
					if (Common.RESULT_SUCCESS.equals(result)) {
						// decode
						SubRedditModel model = SubRedditModel
								.convertToModel(dataJSON);
						mWidgetAdapter.addModel(model);
						// addToPreference(dataJSON);
					} else {
						// load fail
						mWidgetAdapter.setFail(true, true);
						if (Common.RESULT_FETCHING_FAIL.equals(result)) {
							Toast.makeText(this.getApplicationContext(),
									"Load failed!", Toast.LENGTH_SHORT).show();
							return;
						}
					}
				}

			}

			// update the view after caculate
			mWidgetAdapter.updateWidgetView();
		}

		public void saveToPreference(JSONObject data) {
			SharedPreferences pf = PreferenceManager
					.getDefaultSharedPreferences(getApplicationContext());
			SharedPreferences.Editor editor = pf.edit();
			editor.putString(Common.PREF_KEY_WIDGET_SUBREDDIT_DATA,
					data.toString());
			editor.commit();
		}

		public void addToPreference(JSONObject data) {
			if (!RedditManager.addWidgetData(getApplicationContext(), data)) {
				// did not add successful
			}
		}

		public class WidgetAdapter {

			public static final int TYPE_ITEM_NO_PICTURE = 0x6;
			public static final int TYPE_ITEM_PICTURE = 0x7;
			// load from reddit and no item
			public static final int TYPE_NO_ITEM = 0x2;
			public static final int TYPE_MORE = 0x3;
			public static final int TYPE_NO_MORE = 0x4;
			public static final int TYPE_LOADING = 0x0;
			public static final int TYPE_FAIL = 0x5;

			public boolean isLoading = false;
			public int mWidgetPosition = 0;
			public boolean mLoadFail = false;
			public SubRedditModel mSubRedditModel;
			LayoutInflater mLayoutInflater;
			Context mContext;

			boolean mNeedToKill = true;

			public WidgetAdapter(Context context) {
				mContext = context;
				mSubRedditModel = new SubRedditModel();
			}

			public WidgetAdapter(Context context, SubRedditModel model) {
				mContext = context;
				mSubRedditModel = model;
			}

			public void addModel(SubRedditModel model) {
				mSubRedditModel.addData(model);
			}

			public void setWidgetPosition(int p) {
				mWidgetPosition = p;
			}

			public void setLoading(boolean loading, boolean update) {
				isLoading = loading;
				if (update) {
					updateWidgetView();
				}
			}

			public void setFail(boolean fail, boolean update) {
				mLoadFail = fail;
				if (update) {
					updateWidgetView();
				}
			}

			public void updateWidgetView() {
				RemoteViews updateViews = this.getView(mWidgetPosition, null);

				// Push update for this widget to the home screen
				ComponentName thisWidget = new ComponentName(mContext,
						SubredditWidget.class);
				AppWidgetManager manager = AppWidgetManager
						.getInstance(mContext);
				manager.updateAppWidget(thisWidget, updateViews);
				// clear all the pending intents
				clearPendingIntent(mNeedToKill);
			}

			public void updateWidgetView(Bitmap bitmap, int position,
					int startId) {
				RemoteViews updateViews = this.getView(position, bitmap);

				// Push update for this widget to the home screen
				ComponentName thisWidget = new ComponentName(mContext,
						SubredditWidget.class);
				AppWidgetManager manager = AppWidgetManager
						.getInstance(mContext);

				if (startId == mMostRecentStartId) {
					manager.updateAppWidget(thisWidget, updateViews);
				}
				if (startId == mMostRecentStartId) {
					// clear all the pending intents
					clearPendingIntent(mNeedToKill);
				}
			}

			public int getItemViewType(int position) {
				if (mLoadFail) {
					return TYPE_FAIL;
				}
				if (position >= 0
						&& position < mSubRedditModel.mItemList.size()) {
					SubRedditItem item = (SubRedditItem) getItem(position);
					if (item.thumbnail == null || "".equals(item.thumbnail)
							|| "default".equals(item.thumbnail) || item.over_18
							|| "self".equals(item.thumbnail)) {
						return WidgetAdapter.TYPE_ITEM_NO_PICTURE;
					}
					return WidgetAdapter.TYPE_ITEM_PICTURE;
				} else if (isLoading) {
					return WidgetAdapter.TYPE_LOADING;
				} else {
					if (mSubRedditModel.mItemList.size() == 0) {
						return WidgetAdapter.TYPE_NO_ITEM;
					}
					if (mSubRedditModel.after == null
							|| "".equals(mSubRedditModel.after)) {
						return WidgetAdapter.TYPE_NO_MORE;
					} else {
						return WidgetAdapter.TYPE_MORE;
					}

				}
			}

			public int getCount() {
				return mSubRedditModel.mItemList.size() + 1;
			}

			public SubRedditItem getItem(int position) {
				if (position >= 0
						&& position < mSubRedditModel.mItemList.size()) {
					return mSubRedditModel.mItemList.get(position);
				} else {
					return null;
				}
			}

			public RemoteViews getView(int position, Bitmap bitmap) {
				int type = this.getItemViewType(position);
				SubRedditItem item = this.getItem(position);
				RemoteViews remoteViews = null;
				switch (type) {
				case WidgetAdapter.TYPE_FAIL:
					remoteViews = new RemoteViews(mContext.getPackageName(),
							R.layout.widget_fail);
					break;
				case WidgetAdapter.TYPE_LOADING:
					remoteViews = new RemoteViews(mContext.getPackageName(),
							R.layout.widget_loading);
					break;
				case WidgetAdapter.TYPE_ITEM_PICTURE:
					remoteViews = new RemoteViews(mContext.getPackageName(),
							R.layout.widget_subreddit_picture);
					setRemoteViews(remoteViews, item, position);
					if (bitmap == null) {
						mNeedToKill = false;
						// start to load pic and update
						mPictureHandler
								.removeMessages(SubredditWidget.MASSAGE_WIDGET_PICTURE);
						Message msg = mPictureHandler
								.obtainMessage(SubredditWidget.MASSAGE_WIDGET_PICTURE);
						msg.what = SubredditWidget.MASSAGE_WIDGET_PICTURE;
						msg.obj = item;
						msg.arg1 = mMostRecentStartId;
						msg.arg2 = position;
						mPictureHandler.sendMessage(msg);
						remoteViews.setImageViewResource(R.id.remote_pic,
								R.drawable.transparent);
					} else {
						mNeedToKill = true;
						remoteViews.setImageViewBitmap(R.id.remote_pic, bitmap);
					}
					break;

				case WidgetAdapter.TYPE_ITEM_NO_PICTURE:
					remoteViews = new RemoteViews(mContext.getPackageName(),
							R.layout.widget_subreddit_no_pic);
					setRemoteViews(remoteViews, item, position);
					break;
				case WidgetAdapter.TYPE_MORE:
					// do nothing
					remoteViews = new RemoteViews(mContext.getPackageName(),
							R.layout.widget_loading);
					break;
				case WidgetAdapter.TYPE_NO_MORE:
					remoteViews = new RemoteViews(mContext.getPackageName(),
							R.layout.widget_no_more);
					break;
				case WidgetAdapter.TYPE_NO_ITEM:
					remoteViews = new RemoteViews(mContext.getPackageName(),
							R.layout.widget_no_item);
					break;
				}
				setBasicSubReddit(remoteViews);
				return remoteViews;
			}

			public void setRemoteViews(RemoteViews remoteViews,
					SubRedditItem item, int position) {
				remoteViews.setTextViewText(R.id.title, item.title);
				Intent t = new Intent(mContext, CommentFragmentActivity.class);
				t.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
						| Intent.FLAG_ACTIVITY_CLEAR_TOP);
				if (item != null) {
					t.putExtra(Common.INTENT_EXTRA_SUBREDDIT, item);
				}
				PendingIntent pendingIntent = PendingIntent.getActivity(
						mContext, 0 /* no requestCode */, t,
						PendingIntent.FLAG_UPDATE_CURRENT);
				remoteViews.setOnClickPendingIntent(R.id.subreddit_comment,
						pendingIntent);

				// widget count
				String cout = "" + (position + 1) + "/"
						+ mSubRedditModel.mItemList.size();
				remoteViews.setTextViewText(R.id.widget_count, cout);

				remoteViews.setTextViewText(R.id.subreddit_comment_count,
						item.num_comments + "");
				// info time
				remoteViews.setTextViewText(R.id.subreddit_info_time,
						CommonUtil.getRelateTimeString(item.created_utc * 1000,
								mContext));
				// info author
				remoteViews.setTextViewText(R.id.subreddit_info_author, "by "
						+ item.author);
				// nsfw
				remoteViews.setViewVisibility(R.id.subreddit_info_nsfw,
						item.over_18 ? View.VISIBLE : View.GONE);

				// points
				remoteViews.setTextViewText(R.id.subreddit_info_score,
						item.score + " points");

				// subreddit
				remoteViews.setTextViewText(R.id.subreddit_info_subreddit,
						item.subreddit);

				// title bar subreddit
				remoteViews.setTextViewText(R.id.widget_subreddit_name,
						mCurrentSubRedditName);

				// forward
				PendingIntent leftPending = null;
				if (position <= 0) {
					remoteViews.setImageViewResource(R.id.navigation_left,
							R.drawable.icon_navigation_left_unclickable);

				} else {
					remoteViews.setImageViewResource(R.id.navigation_left,
							R.drawable.icon_navigation_left_state);
					remoteViews.setViewVisibility(R.id.navigation_left,
							View.VISIBLE);
					Intent left = new Intent(mContext,
							SubRedditWidgetService.class);
					left.putExtra(Common.KEY_WIDGET_POSITION, position - 1);
					left.putExtra(Common.KEY_EXTRA_WIDGET_DATA, mSubRedditModel);
					left.setAction(Common.ACTION_WIDGET_NAVIGATION_LEFT);
					leftPending = PendingIntent.getService(mContext, 0, left,
							PendingIntent.FLAG_UPDATE_CURRENT);
					remoteViews.setOnClickPendingIntent(R.id.navigation_left,
							leftPending);
				}
				Intent right = new Intent(mContext,
						SubRedditWidgetService.class);
				right.putExtra(Common.KEY_WIDGET_POSITION, position + 1);
				right.putExtra(Common.KEY_EXTRA_WIDGET_DATA, mSubRedditModel);
				right.setAction(Common.ACTION_WIDGET_NAVIGATION_RIGHT);
				PendingIntent rightPending = PendingIntent.getService(mContext,
						0, right, PendingIntent.FLAG_UPDATE_CURRENT);
				remoteViews.setOnClickPendingIntent(R.id.navigation_right,
						rightPending);
			}

			// subreddit sorttype setting refresh
			public void setBasicSubReddit(RemoteViews remoteViews) {

				remoteViews.setTextViewText(R.id.widget_subreddit_name,
						mCurrentSubRedditName);

				remoteViews.setTextViewText(R.id.widget_subreddit_kind,
						Common.TYPE_ARRAY_TEXT[mCurrentKind]);

				// refresh
				Intent refresh = new Intent(mContext,
						SubRedditWidgetService.class);
				refresh.putExtra(Common.KEY_WIDGET_REFRESH, true);
				PendingIntent refreshPending = PendingIntent
						.getService(mContext, 0 /* no requestCode */, refresh,
								0 /* no flags */);
				remoteViews.setOnClickPendingIntent(R.id.widget_refresh,
						refreshPending);

				// setting
				Intent setting = new Intent(mContext,
						WidgetConfigureActivity.class);
				PendingIntent settingPending = PendingIntent
						.getActivity(mContext, 0, setting,
								PendingIntent.FLAG_UPDATE_CURRENT);
				remoteViews.setOnClickPendingIntent(R.id.widget_setting,
						settingPending);
			}
		}

	}

}