package com.softgame.reddit;

import java.io.File;
import java.lang.ref.WeakReference;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;
import com.softgame.reddit.cache.DiskLruCache;
import com.softgame.reddit.cache.ImageCache.ImageCacheParams;
import com.softgame.reddit.fragment.SubscribeFragmentList.SubscribeSubRedditTask;
import com.softgame.reddit.model.Subscribe;
import com.softgame.reddit.utils.Common;
import com.softgame.reddit.utils.CommonUtil;
import com.softgame.reddit.utils.RedditManager;

public class RedditPreferenceActivity extends SherlockPreferenceActivity
		implements OnPreferenceChangeListener, OnPreferenceClickListener,
		OnSharedPreferenceChangeListener {

	DiskLruCache mDiskCache;
	WeakReference<ClearCacheTask> mClearCachTaskWF;
	WeakReference<SubscribeSubRedditTask> mSubscribeTaskWF;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.getSupportActionBar().setTitle("Settings");
		this.getSupportActionBar().setDisplayUseLogoEnabled(true);
		this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		this.getSupportActionBar().setDisplayShowHomeEnabled(true);
		PreferenceManager.setDefaultValues(this, R.xml.preference, false);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			this.finish();
			break;
		}
		return true;
	}

	protected void onResume() {
		super.onResume();
		refresh();
		getPreferenceScreen().getSharedPreferences()
				.registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		getPreferenceScreen().getSharedPreferences()
				.unregisterOnSharedPreferenceChangeListener(this);
	}

	private void refresh() {
		ListPreference themePref = (ListPreference) this.findPreference(this
				.getString(R.string.pref_theme_key));
		themePref.setSummary(themePref.getEntry());

		CheckBoxPreference embedViewPref = (CheckBoxPreference) this
				.findPreference(this.getString(R.string.pref_key_embed_view));
		embedViewPref.setEnabled(CommonUtil.isHoneycomb() ? true : false);

		ListPreference checkratePref = (ListPreference) this
				.findPreference(this.getString(R.string.pref_check_rate_key));
		checkratePref.setSummary(checkratePref.getEntry());

		ListPreference commentPref = (ListPreference) this.findPreference(this
				.getString(R.string.pref_key_comment_count));
		commentPref.setSummary(commentPref.getEntry());

		ListPreference clearCachePref = (ListPreference) this
				.findPreference(this
						.getString(R.string.pref_key_clear_cache_rate));
		clearCachePref.setSummary(clearCachePref.getEntry());

		try {
			RingtonePreference rightstonePref = (RingtonePreference) this
					.findPreference(this
							.getString(R.string.pref_notification_ringtone_key));

			String uri = PreferenceManager
					.getDefaultSharedPreferences(this)
					.getString(
							this.getString(R.string.pref_notification_ringtone_key),
							"");
			if (uri == null || "".equals(uri)) {
				rightstonePref.setSummary("silent");
			} else {
				Uri ringtoneUri = Uri.parse(uri);
				Ringtone ringtone = RingtoneManager.getRingtone(this,
						ringtoneUri);
				String name = ringtone.getTitle(this);
				rightstonePref.setSummary(name);
			}
		} catch (Exception t) {
			// do noting
		}
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		// Let's do something a preference value changes
		if (key.equals(this.getString(R.string.pref_theme_key))) {
			ListPreference themePref = (ListPreference) this
					.findPreference(this.getString(R.string.pref_theme_key));
			themePref.setSummary(themePref.getEntry());
		} else if (key.equals(this.getString(R.string.pref_check_rate_key))) {
			ListPreference checkratePref = (ListPreference) this
					.findPreference(this
							.getString(R.string.pref_check_rate_key));
			checkratePref.setSummary(checkratePref.getEntry());
		} else if (key.equals(this.getString(R.string.pref_key_comment_count))) {
			ListPreference commentPref = (ListPreference) this
					.findPreference(this
							.getString(R.string.pref_key_comment_count));
			commentPref.setSummary(commentPref.getEntry());
		} else if (key.equals(this
				.getString(R.string.pref_notification_ringtone_key))) {
			try {
				RingtonePreference rightstonePref = (RingtonePreference) this
						.findPreference(this
								.getString(R.string.pref_notification_ringtone_key));

				String uri = PreferenceManager
						.getDefaultSharedPreferences(this)
						.getString(
								this.getString(R.string.pref_notification_ringtone_key),
								"");
				if (uri == null || "".equals(uri)) {
					rightstonePref.setSummary("silent");
				} else {
					Uri ringtoneUri = Uri.parse(uri);
					Ringtone ringtone = RingtoneManager.getRingtone(this,
							ringtoneUri);
					String name = ringtone.getTitle(this);
					rightstonePref.setSummary(name);
				}
			} catch (Exception t) {
				// do noting
			}
		} else if (key.equals(this
				.getString(R.string.pref_key_clear_cache_rate))) {
			ListPreference clearCachePref = (ListPreference) this
					.findPreference(this
							.getString(R.string.pref_key_clear_cache_rate));
			clearCachePref.setSummary(clearCachePref.getEntry());
		}
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		if (preference.getKey().equals(this.getString(R.string.pref_theme_key))) {
			Intent t = this.getIntent();
			t.putExtra(Common.EXTRA_RESTART_APP, true);
			this.setResult(Activity.RESULT_OK, t);
			this.finish();
		}

		if (preference.getKey().equals(
				this.getString(R.string.pref_key_clear_cache_rate))) {
			CommonUtil.turnOnOrOffClearDiskRate(this.getApplicationContext(),
					newValue.toString());
		}

		if (preference.getKey().equals(
				this.getString(R.string.pref_check_message_key))) {
			Boolean check = false;
			try {
				check = (Boolean) newValue;
			} catch (Exception e) {
				check = false;
			}
			if (check) {
				CommonUtil.turnOnMessageCheck(this.getApplicationContext());
			} else {
				CommonUtil.turnOffMessageCheck(this.getApplicationContext());
			}

			return true;
		}

		if (preference.getKey().equals(
				this.getString(R.string.pref_check_rate_key))) {
			CommonUtil.updateMessageCheck(this.getApplicationContext(),
					newValue.toString());
		}

		if (preference.getKey().equals(
				this.getString(R.string.pref_key_subscribe_redditet))) {
 
			if(!RedditManager.isUserAuth(this)){
				return false;
			}
			Boolean check = false;
			try {
				check = (Boolean) newValue;
			} catch (Exception e) {
				check = false;
			}

			if (mSubscribeTaskWF != null && mSubscribeTaskWF.get() != null) {
				mSubscribeTaskWF.get().cancel(true);
			}
			SubscribeSubRedditTask st = new SubscribeSubRedditTask(this,
					Common.REDDIT_ET_ID, check);
			mSubscribeTaskWF = new WeakReference<SubscribeSubRedditTask>(st);
			st.execute();
		}

		return true;
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		if (preference.getKey().equals(
				this.getString(R.string.pref_contact_developer_key))) {

			Intent t = new Intent(this, ComposeMessageActivity.class);
			t.putExtra(Common.KEY_REDDITOR_NAME, Common.REDDIT_ET_USERNAME);
			this.startActivity(t);
			return true;
		} else if (preference.getKey().equals(
				this.getString(R.string.pref_report_bug_key))) {
			Intent t = new Intent(this, SubmitLinkTextActivity.class);
			t.putExtra(Common.EXTRA_SUBREDDIT_NAME, Common.REDDIT_ET_SUBREDDIT_NAME);
			this.startActivity(t);
			return true;

		} else if (preference.getKey().equals(
				this.getString(R.string.pref_share_app_key))) {
			// show share app dialog
			this.startActivity(Intent.createChooser(createShareIntent(),
					"Share via"));

		} else if (preference.getKey().equals(
				this.getString(R.string.pref_key_clear_cache))) {
			// check running
			if (mClearCachTaskWF != null && mClearCachTaskWF.get() != null) {
				if (!mClearCachTaskWF.get().getStatus()
						.equals(AsyncTask.Status.FINISHED)) {
					Toast.makeText(this, "clear cacheFiles is running",
							Toast.LENGTH_SHORT).show();
					return true;
				}
			}

			if (mDiskCache == null) {
				ImageCacheParams cacheParams = new ImageCacheParams(
						Common.IMAGE_CACHE_DIR);
				final File diskCacheDir = DiskLruCache.getDiskCacheDir(this,
						cacheParams.uniqueName);
				mDiskCache = DiskLruCache.openCache(this, diskCacheDir,
						cacheParams.diskCacheSize);
			}

			ClearCacheTask clearTask = new ClearCacheTask(mDiskCache, this);
			mClearCachTaskWF = new WeakReference<RedditPreferenceActivity.ClearCacheTask>(
					clearTask);
			clearTask.execute();
		} else if (preference.getKey().equals(
				this.getString(R.string.pref_key_rate_redditet))) {
			// show go pro
			Uri uri = Uri.parse(Common.REDDIT_ET_GOOGLE_PLAY_PAID);
			Intent netIntent = new Intent(Intent.ACTION_VIEW, uri);
			this.startActivity(netIntent);
		} else if (preference.getKey().equals(
				this.getString(R.string.pref_key_go_to_redditet))) {
			Intent t = this.getIntent();
			t.putExtra(Common.EXTRA_GO_TO_REDDIT_ET, true);
			t.putExtra(Common.EXTRA_SUBREDDIT, Common.REDDIT_ET_SUBREDDIT);
			t.putExtra(Common.EXTRA_SUBREDDIT_NAME,
					Common.REDDIT_ET_SUBREDDIT_NAME);
			this.setResult(Activity.RESULT_OK, t);
			this.finish();
		}
		return false;
	}

	public Intent createShareIntent() {
		Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
		sharingIntent.setType("text/plain");
		String shareBody = "RedditET App on Google play: "
				+ Common.SHARE_APP_LINK;
		String subject = "A sleek new Reddit android client";
		sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, subject
				+ "\n" + shareBody);
		return sharingIntent;
	}

	public static class ClearCacheTask extends AsyncTask<Void, Void, Integer> {
		WeakReference<DiskLruCache> mDiskCacheWF;
		WeakReference<Context> mContextWF;

		public ClearCacheTask(DiskLruCache d, Context context) {
			mDiskCacheWF = new WeakReference<DiskLruCache>(d);
			mContextWF = new WeakReference<Context>(context);
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (mContextWF == null || mDiskCacheWF == null
					|| mContextWF.get() == null || mDiskCacheWF.get() == null) {
				this.cancel(true);
			} else {
				Toast.makeText(mContextWF.get(), "Clearing Dish Cache",
						Toast.LENGTH_SHORT).show();

			}

		}

		@Override
		protected Integer doInBackground(Void... params) {
			if (mContextWF == null || mDiskCacheWF == null
					|| mContextWF.get() == null || mDiskCacheWF.get() == null) {
				this.cancel(true);
				return -1;
			} else {
				return mDiskCacheWF.get().clearCache();
			}
		}

		@Override
		protected void onPostExecute(Integer result) {
			super.onPostExecute(result);

			if (mContextWF == null || mDiskCacheWF == null
					|| mContextWF.get() == null || mDiskCacheWF.get() == null) {
				return;
			}

			if (result > 0) {
				Toast.makeText(mContextWF.get(), result + " cache files clear",
						Toast.LENGTH_SHORT).show();
				return;
			}
			if (result == 0) {
				Toast.makeText(mContextWF.get(), "No Cache Files to clear",
						Toast.LENGTH_SHORT).show();
				return;
			}

			if (result < 0) {
				Toast.makeText(mContextWF.get(),
						"clear Cache files fail, try again latter",
						Toast.LENGTH_SHORT).show();
				return;
			}

		}

	}

}
