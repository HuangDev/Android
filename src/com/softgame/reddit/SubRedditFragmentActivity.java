package com.softgame.reddit;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.softgame.reddit.dialog.MeDialog;
import com.softgame.reddit.dialog.MineSubredditDialog;
import com.softgame.reddit.dialog.QuitConfirmDialog;
import com.softgame.reddit.dialog.SubredditPopDialog;
import com.softgame.reddit.fragment.SubRedditListFragment;
import com.softgame.reddit.fragment.SubscribeFragmentList;
import com.softgame.reddit.impl.OnSubscribeItemClickListener;
import com.softgame.reddit.model.Subscribe;
import com.softgame.reddit.utils.Common;
import com.softgame.reddit.utils.CommonUtil;
import com.softgame.reddit.utils.RedditManager;

public class SubRedditFragmentActivity extends CacheFragmentActivity implements
		OnSubscribeItemClickListener, OnClickListener, OnPageChangeListener,
		ActionBar.TabListener, ActionBar.OnNavigationListener,
		OnSharedPreferenceChangeListener {

	private static final String TAG = "SubRedditFragmentActivity";
	public static final int NUM_ITEMS = 4;

	TextView mSubRedditText;

	ViewPager mViewPager;
	SubRedditPagerAdapter mSubRedditPagerAdapter;
	// Pop
	public MineSubredditDialog mMineSubscribeDialog;
	public SubredditPopDialog mSubRedditPopDialog;
	public MeDialog mMeDialog;

	public QuitConfirmDialog mQuitDialog;

	// r/pics
	public String mCurrentSubReddit;
	public String mCurrentSubRedditName;
	public int mCurrentKind;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		System.setProperty("http.keepAlive", "false");
		this.setContentView(R.layout.activity_subreddit);
		mSubRedditPagerAdapter = new SubRedditPagerAdapter(
				this.getSupportFragmentManager());
		mViewPager = (ViewPager) this.findViewById(R.id.pager);
		mViewPager.setAdapter(mSubRedditPagerAdapter);
		mViewPager.setOnPageChangeListener(this);
		// set fadding to true
		mImageWorker.setImageFadeIn(true);
		mImageWorker.setLoadFailImage(R.drawable.picture_not_found);
		// mImageWorker.setLoadingImage(R.drawable.loading_picture);

		mMineSubscribeDialog = new MineSubredditDialog(
				SubRedditFragmentActivity.this);

		mMeDialog = new MeDialog(SubRedditFragmentActivity.this);
		mMeDialog.setOwnerActivity(this);

		// init subreddit from preference
		mCurrentSubReddit = RedditManager.getCurrentSubReddit(this);
		mCurrentSubRedditName = RedditManager.getCurrentSubReddtiName(this);

		// is portrait mode

		if (super.isPortaitMode()) {
			getSupportActionBar().setNavigationMode(
					ActionBar.NAVIGATION_MODE_TABS);
			for (int i = 0; i < NUM_ITEMS; i++) {
				ActionBar.Tab tab = getSupportActionBar().newTab();
				tab.setText(Common.TYPE_ARRAY_TEXT[i]);
				tab.setTabListener(this);
				getSupportActionBar().addTab(tab);
			}
		} else {
			getSupportActionBar().setNavigationMode(
					ActionBar.NAVIGATION_MODE_LIST);
			ArrayAdapter<CharSequence> list = ArrayAdapter.createFromResource(
					this, R.array.subreddit_type_array_text,
					com.actionbarsherlock.R.layout.sherlock_spinner_item);
			list.setDropDownViewResource(R.layout.sherlock_spinner_dropdown_item);
			getSupportActionBar().setListNavigationCallbacks(list, this);
		}

		// DO NOT SHOW TITLE
		this.getSupportActionBar().setDisplayShowTitleEnabled(false);
		this.getSupportActionBar().setDisplayHomeAsUpEnabled(false);
		this.getSupportActionBar().setDisplayUseLogoEnabled(true);
		// Show the Custom View for selecting the Subscribe
		// Inflate the custom view
		View customNav = LayoutInflater.from(this).inflate(
				R.layout.subreddit_subscribe_button, null);

		customNav.findViewById(R.id.subreddit_subscribe).setOnClickListener(
				this);
		mSubRedditText = (TextView) customNav.findViewById(R.id.subreddit_name);
		mSubRedditText.setText(mCurrentSubRedditName.toUpperCase());
		// Attach to the action bar
		getSupportActionBar().setCustomView(customNav);
		getSupportActionBar().setDisplayShowCustomEnabled(true);

		// hide the actionBar if need
		if (this.getRetainFragment() != null) {
			if (this.getRetainFragment().isFullScreen()) {
				this.getSupportActionBar().hide();
			}
		}

		SharedPreferences df = PreferenceManager
				.getDefaultSharedPreferences(this);
		df.registerOnSharedPreferenceChangeListener(this);

	}

	@Override
	public void onResume() {
		super.onResume();
		Log.d(TAG, mCurrentKind + "onResume called!");
		this.getImageWorker().setExitTasksEarly(false);
	}

	@Override
	public void onPause() {
		super.onPause();
		Log.d(TAG, mCurrentKind + "onPause called!");
		this.getImageWorker().setExitTasksEarly(true);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Used to put dark icons on light action bar
		// boolean isLight = SampleList.THEME == R.style.Theme_Sherlock_Light;

		if (RedditManager.isUserAuth(SubRedditFragmentActivity.this)) {

			menu.add("Me").setShowAsAction(
					MenuItem.SHOW_AS_ACTION_ALWAYS
							| MenuItem.SHOW_AS_ACTION_WITH_TEXT);

		} else {

			menu.add("Login").setShowAsAction(
					MenuItem.SHOW_AS_ACTION_ALWAYS
							| MenuItem.SHOW_AS_ACTION_WITH_TEXT);

		}

		menu.add("Canvas mode").setIcon(R.drawable.icon_actionbar_canvas_mode)
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

		menu.add("Search").setIcon(R.drawable.icon_actionbar_search)
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

		menu.add("Full Screen").setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

		menu.add("Reload All").setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

		menu.add("Setting").setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getTitle().equals("Login")) {
			if (!RedditManager.isUserAuth(SubRedditFragmentActivity.this)) {
				Intent loginIntent = new Intent(SubRedditFragmentActivity.this,
						LoginActivity.class);
				SubRedditFragmentActivity.this.startActivityForResult(
						loginIntent, Common.REQUST_LOGIN);
			}
		} else if (item.getTitle().equals("Me")) {
			mMeDialog.show();
		} else if (item.getTitle().equals("Canvas mode")) {
			Intent portraitIntent = new Intent(SubRedditFragmentActivity.this,
					SubRedditCanvasActivity.class);
			this.startActivity(portraitIntent);
		} else if (item.getTitle().equals("Search")) {
			Intent searchItem = new Intent(this, SearchSubRedditActivity.class);
			this.startActivity(searchItem);
		} else if (item.getTitle().equals("Full Screen")) {
			this.getSupportActionBar().hide();
			if (this.getRetainFragment() != null) {
				this.getRetainFragment().setFullScreen(true);
			}
		} else if (item.getTitle().equals("Reload All")) {
			SharedPreferences df = PreferenceManager
					.getDefaultSharedPreferences(this);
			SharedPreferences.Editor editor = df.edit();
			editor.putBoolean(Common.PREF_DATE_RELOAD,
					!df.getBoolean(Common.PREF_DATE_RELOAD, false));
			editor.commit();
			Toast.makeText(getApplicationContext(), "Reload All!",
					Toast.LENGTH_SHORT).show();
		} else if (item.getTitle().equals("Setting")) {
			startActivityForResult(new Intent(this, SettingActivity.class),
					Common.REQUEST_SETTING);

		}
		return true;
	}

	// capture the mune button click;
	@Override
	public boolean onKeyDown(int keycode, KeyEvent e) {
		switch (keycode) {
		case KeyEvent.KEYCODE_MENU:
			if (!this.getSupportActionBar().isShowing()) {
				this.getSupportActionBar().show();
				if (this.getRetainFragment() != null) {
					this.getRetainFragment().setFullScreen(false);
				}
				return true;
			}
			break;
		case KeyEvent.KEYCODE_BACK:
			if (!this.getSupportActionBar().isShowing()) {
				this.getSupportActionBar().show();
				if (this.getRetainFragment() != null) {
					this.getRetainFragment().setFullScreen(false);
				}
				return true;
			}
			if (CommonUtil.needToComfirmExit(getApplicationContext())) {
				if (mQuitDialog == null)
					mQuitDialog = new QuitConfirmDialog(this);
				mQuitDialog.setOwnerActivity(this);
				mQuitDialog.show();
				return true;
			}
			break;
		}
		return super.onKeyDown(keycode, e);
	}

	@Override
	public View findViewById(int id) {
		View v = super.findViewById(id);
		if (v == null && mMineSubscribeDialog != null) {
			return mMineSubscribeDialog.findViewById(id);
		} else {
			return v;
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.subreddit_subscribe:
			mMineSubscribeDialog.show();
			break;
		case R.id.me_profile:
			String username = RedditManager
					.getUserName(SubRedditFragmentActivity.this);
			Intent accountIntent = new Intent(SubRedditFragmentActivity.this,
					OverviewFragmentActivity.class);
			accountIntent.putExtra(Common.EXTRA_USERNAME, username);
			SubRedditFragmentActivity.this.startActivity(accountIntent);
			break;

		case R.id.me_liked:
			Intent likedIntent = new Intent(SubRedditFragmentActivity.this,
					LikedFragmentActivity.class);
			this.startActivity(likedIntent);
			break;
		case R.id.me_message:
			Intent messageIntent = new Intent(SubRedditFragmentActivity.this,
					MessageActivity.class);
			SubRedditFragmentActivity.this.startActivity(messageIntent);
			break;
		case R.id.me_commit:
			Intent commitIntent = new Intent(SubRedditFragmentActivity.this,
					SubmitLinkTextActivity.class);
			SubRedditFragmentActivity.this.startActivity(commitIntent);
			break;
		case R.id.me_logout:
			// remove from default preference
			RedditManager.logout(SubRedditFragmentActivity.this);
			onUserLogChange();
			break;

		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case Common.REQUST_LOGIN:
			if (resultCode == Activity.RESULT_OK) {
				onUserLogChange();
			}
			break;
		case Common.REQUEST_SEARCH_SUBSCRIBE:
			if (resultCode == Activity.RESULT_OK && data != null) {
				String s = data.getStringExtra(Common.EXTRA_SUBREDDIT);
				String sn = data.getStringExtra(Common.EXTRA_SUBREDDIT_NAME);
				if (s != null && !"".equals(s) && sn != null && !"".equals(sn)) {
					setSubredditInfo(sn, s);
				}
			}
			break;
		case Common.REQUEST_SETTING:
			if (resultCode == Activity.RESULT_OK && data != null) {
				boolean restartApp = data.getBooleanExtra(
						Common.EXTRA_RESTART_APP, false);
				if (restartApp) {
					Intent ttt = new Intent(this,
							SubRedditFragmentActivity.class);
					ttt.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
					this.startActivity(ttt);
					this.finish();
					return;
				}

				boolean goToRedditET = data.getBooleanExtra(
						Common.EXTRA_GO_TO_REDDIT_ET, false);
				if (goToRedditET) {
					String s = data.getStringExtra(Common.EXTRA_SUBREDDIT);
					String sn = data
							.getStringExtra(Common.EXTRA_SUBREDDIT_NAME);
					if (s != null && !"".equals(s) && sn != null
							&& !"".equals(sn)) {
						setSubredditInfo(sn, s);
					}
				}
			}
			break;
		}
	}

	public static class SubRedditPagerAdapter extends FragmentPagerAdapter {

		public SubRedditPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public int getCount() {
			return NUM_ITEMS;
		}

		@Override
		public SubRedditListFragment getItem(int position) {
			return SubRedditListFragment.newInstance(position);
		}
	}

	// deal with Subscribe Pop Dialog Call back
	@Override
	public void onSubscribeItemClick(Subscribe s, int action) {
		switch (action) {
		case MineSubredditDialog.ACTION_SUBREDDIT_DONE:
			// do nothing
			break;
		case MineSubredditDialog.ACTION_SUBREDDIT_FRONT:
			setSubredditInfo(Common.DEFAULT_SUBREDDIT_NAME,
					Common.DEFAULT_SUBREDDIT);
			break;
		case MineSubredditDialog.ACTION_SUBREDDIT_ALL:
			setSubredditInfo(Common.SUBREDDIT_ALL_NAME, Common.SUBREDDIT_ALL);
			break;
		case MineSubredditDialog.ACTION_SUBREDDIT_FRIENDS:
			setSubredditInfo(Common.SUBREDDIT_FRIEND_NAME,
					Common.SUBREDDIT_FRIEND);
			break;
		case MineSubredditDialog.ACTION_SUBREDDIT_MOD:
			setSubredditInfo(Common.SUBREDDIT_MOD_NAME, Common.SUBREDDIT_MOD);
			break;
		case MineSubredditDialog.ACTION_SUBREDDIT_SEARCH:
			// do the search
			Intent search = new Intent(this, SearchSubscribeActivity.class);
			this.startActivityForResult(search, Common.REQUEST_SEARCH_SUBSCRIBE);
			break;
		case SubscribeFragmentList.ACTION_SUBREDDIT_ITEM:
			setSubredditInfo(s.display_name, s.url);
			break;
		}

		mMineSubscribeDialog.dismiss();
	}

	private void setSubredditInfo(String subredditName, String subredditUrl) {
		if (mCurrentSubReddit != null && mCurrentSubReddit.equals(subredditUrl)) {
			return;
		}
		mCurrentSubReddit = subredditUrl;
		mCurrentSubRedditName = subredditName;
		SharedPreferences df = PreferenceManager
				.getDefaultSharedPreferences(this);
		SharedPreferences.Editor editor = df.edit();
		editor.putString(Common.PREF_KEY_SUBREDDIT, mCurrentSubReddit);
		editor.putString(Common.PREF_KEY_SUBREDDIT_NAME, mCurrentSubRedditName);
		editor.commit();
		mSubRedditText.setText(mCurrentSubRedditName.toUpperCase());
	}

	private void onUserLogChange() {
		this.invalidateOptionsMenu();
		if (!RedditManager.isUserAuth(this)) {
			if (Common.SUBREDDIT_FRIEND.equals(mCurrentSubReddit)
					|| Common.SUBREDDIT_MOD.equals(mCurrentSubReddit)) {
				mCurrentSubRedditName = Common.DEFAULT_SUBREDDIT_NAME;
				mCurrentSubReddit = Common.DEFAULT_SUBREDDIT;
				SharedPreferences df = PreferenceManager
						.getDefaultSharedPreferences(this);
				SharedPreferences.Editor editor = df.edit();
				editor.putString(Common.PREF_KEY_SUBREDDIT, mCurrentSubReddit);
				editor.putString(Common.PREF_KEY_SUBREDDIT_NAME,
						mCurrentSubRedditName.toUpperCase());
				editor.commit();
			}
		}
		mMineSubscribeDialog.updateDialog(true);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		SharedPreferences df = PreferenceManager
				.getDefaultSharedPreferences(this);
		df.unregisterOnSharedPreferenceChangeListener(this);

		if (mSubRedditPopDialog != null) {
			mSubRedditPopDialog.dismiss();
		}

		if (mQuitDialog != null) {
			mQuitDialog.dismiss();
		}
		if (mMineSubscribeDialog != null) {
			mMineSubscribeDialog.dismiss();
		}

	}

	@Override
	public void onPageScrolled(int position, float positionOffset,
			int positionOffsetPixels) {
		// do nothing;
	}

	@Override
	public void onPageSelected(int position) {
		// this.getSupportActionBar().selectTab(
		// this.getSupportActionBar().getTabAt(position));
		this.getSupportActionBar().setSelectedNavigationItem(position);
	}

	@Override
	public void onPageScrollStateChanged(int state) {

	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		mViewPager.setCurrentItem(tab.getPosition(), true);
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {

	}

	@Override
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
		mViewPager.setCurrentItem(itemPosition, false);
		return true;
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if (key.equals(this.getString(R.string.pref_key_picture_quality_level))) {
			// GET LOAD PICTURE
			mImageWorker.setPictureQualityLevel(CommonUtil
					.getPictureQualityLevel(this));
		}

	}

}
