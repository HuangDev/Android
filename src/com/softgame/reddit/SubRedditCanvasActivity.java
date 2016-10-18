package com.softgame.reddit;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.OnNavigationListener;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.softgame.reddit.dialog.MineSubredditDialog;
import com.softgame.reddit.dialog.SubredditPopDialog;
import com.softgame.reddit.fragment.CanvasFragment;
import com.softgame.reddit.fragment.CanvasLinkFragment;
import com.softgame.reddit.fragment.CanvasLoadFailFragment;
import com.softgame.reddit.fragment.CanvasLoadingFragment;
import com.softgame.reddit.fragment.CanvasNoMoreFragment;
import com.softgame.reddit.fragment.CanvasPictureFragment;
import com.softgame.reddit.fragment.CanvasSelfpostFragment;
import com.softgame.reddit.fragment.SubRedditLoadFragment;
import com.softgame.reddit.fragment.SubscribeFragmentList;
import com.softgame.reddit.impl.OnDataRefreshListener;
import com.softgame.reddit.impl.OnSubscribeItemClickListener;
import com.softgame.reddit.model.SubRedditItem;
import com.softgame.reddit.model.Subscribe;
import com.softgame.reddit.utils.Common;
import com.softgame.reddit.utils.CommonUtil;
import com.softgame.reddit.utils.RedditManager;
import com.softgame.reddit.view.EnhanceViewPager;

public class SubRedditCanvasActivity extends CacheFragmentActivity implements
		OnSubscribeItemClickListener, OnClickListener, OnNavigationListener,
		OnDataRefreshListener {

	TextView mSubRedditText;

	EnhanceViewPager mViewPager;
	SubRedditPagerAdapter mSubRedditPagerAdapter;
	// Pop
	public SubredditPopDialog mSubRedditPopDialog;

	// r/pics
	public String mCurrentSubReddit;
	public String mCurrentSubRedditName;

	Dialog mSortDialog;
	Dialog mTypeDialog;

	SubRedditLoadFragment mSubRedditLoadFragment;
	MineSubredditDialog mMineSubscribeDialog;

	public boolean mSafeForWork = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (!super.isPortaitMode()) {
			getWindow().addFlags(LayoutParams.FLAG_FULLSCREEN);
		}
		this.setContentView(R.layout.activity_subreddit_canvas);
		// this.getImageWorker().setLoadingImage(R.drawable.empty_pic);
		this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		this.getSupportActionBar().setDisplayShowTitleEnabled(false);
		this.getSupportActionBar().setDisplayUseLogoEnabled(true);

		mSafeForWork = CommonUtil.safeForWork(this);
		// create loading fragment
		mSubRedditLoadFragment = SubRedditLoadFragment
				.findOrCreateSubRedditLoadFragment(getSupportFragmentManager(),
						Common.KIND_HOT, 1, 2);

		mSubRedditLoadFragment.setDataRefreshListener(this);

		mSubRedditPagerAdapter = new SubRedditPagerAdapter(
				this.getSupportFragmentManager());
		mViewPager = (EnhanceViewPager) this.findViewById(R.id.pager);
		mViewPager.setAdapter(mSubRedditPagerAdapter);

		mMineSubscribeDialog = new MineSubredditDialog(this);
		mMineSubscribeDialog.setOwnerActivity(this);

		ArrayAdapter<CharSequence> typeList = ArrayAdapter.createFromResource(
				this, R.array.subreddit_type_array_text,
				R.layout.sherlock_spinner_item);
		typeList.setDropDownViewResource(R.layout.sherlock_spinner_dropdown_item);
		getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		getSupportActionBar().setListNavigationCallbacks(typeList, this);

		this.getSupportActionBar().setSelectedNavigationItem(
				mSubRedditLoadFragment.mCurrentKind);

		mCurrentSubReddit = RedditManager.getCanvasSubReddit(this);
		mCurrentSubRedditName = RedditManager.getCanvasSubReddtiName(this);

		// Show the Custom View for selecting the Subscribe
		// Inflate the custom view
		View customNav = LayoutInflater.from(this).inflate(
				R.layout.subreddit_canvas_subscribe_button, null);

		customNav.setOnClickListener(this);
		mSubRedditText = (TextView) customNav.findViewById(R.id.subreddit_name);
		mSubRedditText.setText(mCurrentSubRedditName.toUpperCase());
		// Attach to the action bar
		getSupportActionBar().setCustomView(customNav);
		getSupportActionBar().setDisplayShowCustomEnabled(true);

		if (mSubRedditLoadFragment != null
				&& !mSubRedditLoadFragment.mShowActionBar) {
			getSupportActionBar().hide();
		}
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
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.activity_canvas_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			this.finish();
			break;
		case R.id.menu_item_lock:
			mViewPager.setPagingEnabled(!mViewPager.isPagingEnable());
			item.setIcon(mViewPager.isPagingEnable() ? R.drawable.icon_actionbar_unlock
					: R.drawable.icon_actionbar_lock);
			break;
		case R.id.menu_item_sort:

			if (mSubRedditLoadFragment.mCurrentKind == Common.KIND_CONTROVERSIAL
					|| mSubRedditLoadFragment.mCurrentKind == Common.KIND_TOP) {
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setSingleChoiceItems(R.array.date_array,
						mSubRedditLoadFragment.mCurrentType,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int item) {
								mSubRedditLoadFragment.setType(item);
								dialog.dismiss();
							}
						});
				if (mTypeDialog != null) {
					mTypeDialog.dismiss();
				}
				mTypeDialog = builder.create();
				mTypeDialog.show();
			} else if (mSubRedditLoadFragment.mCurrentKind == Common.KIND_NEW) {
				AlertDialog.Builder builderNew = new AlertDialog.Builder(this);
				builderNew.setSingleChoiceItems(R.array.new_array,
						mSubRedditLoadFragment.mCurrentSort,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int item) {
								mSubRedditLoadFragment.setCurrentSort(item);
								dialog.dismiss();
							}
						});
				if (mSortDialog != null) {
					mSortDialog.dismiss();
				}
				mSortDialog = builderNew.create();
				mSortDialog.show();
			} else {
				Toast.makeText(getApplicationContext(),
						"no sort available for 'hot'!", Toast.LENGTH_SHORT)
						.show();
			}
			break;
		case R.id.menu_item_search:
			Intent searchItem = new Intent(this, SearchSubRedditActivity.class);
			this.startActivity(searchItem);
			break;
		case R.id.menu_item_full_screen:
			this.getSupportActionBar().hide();
			mSubRedditLoadFragment.mShowActionBar = false;
			break;
		}

		if (item.getItemId() == android.R.id.home) {
			this.finish();
		}
		if (item.getTitle().equals("Search")) {

		} else if (item.getTitle().equals("Full Screen")) {
			this.getSupportActionBar().hide();
		} else if (item.getTitle().equals("Reload")) {
			SharedPreferences df = PreferenceManager
					.getDefaultSharedPreferences(this);
			SharedPreferences.Editor editor = df.edit();
			editor.putBoolean(Common.PREF_DATE_RELOAD,
					!df.getBoolean(Common.PREF_DATE_RELOAD, false));
			editor.commit();
			Toast.makeText(getApplicationContext(), "Reload!",
					Toast.LENGTH_SHORT).show();
		}
		return true;
	}

	/*
	 * @Override protected void onStart() { super.onStart(); if
	 * (mSubRedditLoadFragment != null) {
	 * mSubRedditLoadFragment.registSharedPreference(this); }
	 * 
	 * }
	 * 
	 * @Override protected void onStop() { super.onStop(); if
	 * (mSubRedditLoadFragment != null) {
	 * mSubRedditLoadFragment.unRegistSharedPreference(this); } }
	 */
	// capture the mune button click;
	@Override
	public boolean onKeyDown(int keycode, KeyEvent e) {
		switch (keycode) {
		case KeyEvent.KEYCODE_MENU:
		case KeyEvent.KEYCODE_BACK:
			if (!this.getSupportActionBar().isShowing()) {
				this.getSupportActionBar().show();
				mSubRedditLoadFragment.mShowActionBar = true;
				return true;
			}
		}
		return super.onKeyDown(keycode, e);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
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
			if (resultCode == Activity.RESULT_OK) {
				Intent ttt = new Intent(this, SubRedditCanvasActivity.class);
				ttt.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
				this.startActivity(ttt);
				this.finish();
			}
			break;
		}
	}

	public class SubRedditPagerAdapter extends FragmentStatePagerAdapter {
		public boolean clear = false;

		public SubRedditPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public int getItemPosition(Object object) {
			if (clear) {
				return PagerAdapter.POSITION_NONE;
			}

			if (object instanceof CanvasLoadingFragment
					|| object instanceof CanvasLoadFailFragment) {
				return PagerAdapter.POSITION_NONE;
			} else {
				return PagerAdapter.POSITION_UNCHANGED;
			}
		}

		@Override
		public int getCount() {
			return mSubRedditLoadFragment.getCount();
		}

		@Override
		public CanvasFragment getItem(int position) {

			CanvasFragment fragment = null;
			int type = mSubRedditLoadFragment.getItemViewType(position);
			SubRedditItem item = mSubRedditLoadFragment.getItem(position);
			switch (type) {
			case SubRedditLoadFragment.TYPE_LINK_NOPIC:
				fragment = CanvasLinkFragment.newInstance(item);
				break;
			case SubRedditLoadFragment.TYPE_LINK_PIC:
				fragment = CanvasPictureFragment.newInstance(item);
				break;
			case SubRedditLoadFragment.TYPE_SELFPOST:
				fragment = CanvasSelfpostFragment.newInstance(item);
				break;
			case SubRedditLoadFragment.TYPE_NO_MORE:
				fragment = new CanvasNoMoreFragment();
				break;
			case SubRedditLoadFragment.TYPE_LOAD_MORE:
				fragment = new CanvasLoadingFragment();
				clear = false;
				// load more without update
				mSubRedditLoadFragment.loadMore();
				break;
			case SubRedditLoadFragment.TYPE_LOADING:
				fragment = new CanvasLoadingFragment();
				break;
			case SubRedditLoadFragment.TYPE_LOAD_FAIL:
				fragment = new CanvasLoadFailFragment();
				break;
			}
			return fragment;
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
		editor.putString(Common.PREF_KEY_CANVAS_SUBREDDIT, subredditUrl);
		editor.putString(Common.PREF_KEY_CANVAS_SUBREDDIT_NAME, subredditName);
		editor.commit();
		mSubRedditText.setText(mCurrentSubRedditName.toUpperCase());
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		if (mSubRedditPopDialog != null) {
			mSubRedditPopDialog.dismiss();
		}

		if (mMineSubscribeDialog != null) {
			mMineSubscribeDialog.dismiss();
		}

		if (mSortDialog != null) {
			mSortDialog.dismiss();
		}
		if (mTypeDialog != null) {
			mTypeDialog.dismiss();
		}
	}

	@Override
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
		// mViewPager.setCurrentItem(itemPosition, true);
		mSubRedditLoadFragment.setCurrentKind(itemPosition);
		// updateViewPager();
		return true;
	}

	@Override
	public void onDataRefresh(boolean clear) {
		mSubRedditPagerAdapter.clear = clear;
		mSubRedditPagerAdapter.notifyDataSetChanged();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.subreddit_subscribe:
			mMineSubscribeDialog.show();
			break;
		case R.id.reload:
			mSubRedditLoadFragment.loadMoreIfNeeed();
			break;
		}
	}

}
