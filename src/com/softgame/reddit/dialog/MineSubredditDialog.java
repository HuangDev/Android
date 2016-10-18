package com.softgame.reddit.dialog;

import android.app.Dialog;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.softgame.reddit.R;
import com.softgame.reddit.fragment.SubscribeFragmentList;
import com.softgame.reddit.impl.OnSubscribeItemClickListener;
import com.softgame.reddit.utils.CommonUtil;
import com.softgame.reddit.utils.RedditManager;
import com.softgame.reddit.view.CustomTabWidget;

// a dialog to show the user the pop, can only be used to SubRedditActivity
public class MineSubredditDialog extends Dialog implements
		OnPageChangeListener, CustomTabWidget.OnTabSelectionChanged,
		OnClickListener {

	public static final int KIND_POPULAR = 0x0;
	public static final int KIND_NEW = 0x1;
	public static final int KIND_MY_REDDITS = 0x2;

	public static final int ACTION_SUBREDDIT_SEARCH = 0x101;
	public static final int ACTION_SUBREDDIT_DONE = 0x102;
	public static final int ACTION_SUBREDDIT_ALL = 0x103;
	public static final int ACTION_SUBREDDIT_MOD = 0x104;
	public static final int ACTION_SUBREDDIT_FRIENDS = 0x105;
	public static final int ACTION_SUBREDDIT_FRONT = 0x106;

	int mCurrentKind = -1;

	FragmentActivity mOwnActivity;
	OnSubscribeItemClickListener mListener;

	int NUM_ITEMS;

	CustomTabWidget mCustomTabWidget;

	ViewPager mPager;
	CustomPagerAdapter mAdapter;

	View mMyRedditsView;

	ImageButton mSearch;
	ImageButton mDone;

	LinearLayout mFriend;
	LinearLayout mAll;
	LinearLayout mMod;
	LinearLayout mFront;
	
	boolean mSaveForWork;

	public MineSubredditDialog(Context context) {
		this(context, R.style.WhiteDialogTheme);
	}

	public MineSubredditDialog(Context context, int theme) {
		super(context, R.style.WhiteDialogTheme);
		// set params
		android.view.WindowManager.LayoutParams params = getWindow()
				.getAttributes();
		params.width = LayoutParams.MATCH_PARENT;
		params.height = LayoutParams.MATCH_PARENT;
		params.gravity = Gravity.BOTTOM;
		this.getWindow().setAttributes(params);
		this.setCanceledOnTouchOutside(true);

		this.setContentView(R.layout.dialog_subscribelist_pop);
		
		mOwnActivity = (FragmentActivity) context;
		mListener = (OnSubscribeItemClickListener) context;

		mSaveForWork = CommonUtil.safeForWork(this.getContext());
		mCustomTabWidget = (CustomTabWidget) this
				.findViewById(R.id.subscribe_tab);

		mCustomTabWidget.setChildView();
		mMyRedditsView = mCustomTabWidget
				.findViewById(R.id.subscribe_type_myreddits);
		mAdapter = new CustomPagerAdapter(
				mOwnActivity.getSupportFragmentManager());

		mPager = (ViewPager) this.findViewById(R.id.dialog_pager);
		mPager.setAdapter(mAdapter);
		mPager.setOnPageChangeListener(this);
		mCustomTabWidget.setTabSelectionListener(this);
		mCustomTabWidget.focusCurrentTab(0);

		mAll = (LinearLayout) this.findViewById(R.id.subreddit_all);
		mFriend = (LinearLayout) this.findViewById(R.id.subreddit_friend);
		mMod = (LinearLayout) this.findViewById(R.id.subreddit_mod);
		mFront = (LinearLayout) this.findViewById(R.id.subreddit_front);

		mSearch = (ImageButton) this.findViewById(R.id.subreddit_search);
		mDone = (ImageButton) this.findViewById(R.id.subreddit_done);

		mAll.setOnClickListener(this);
		mFriend.setOnClickListener(this);
		mMod.setOnClickListener(this);
		mSearch.setOnClickListener(this);
		mDone.setOnClickListener(this);
		mFront.setOnClickListener(this);
		updateDialog(false);
	}

	public void updateDialog(boolean changed) {
		if (RedditManager.isUserAuth(mOwnActivity)) {
			mFriend.setVisibility(View.VISIBLE);
			mMod.setVisibility(View.VISIBLE);
			NUM_ITEMS = 3;
			mMyRedditsView.setVisibility(View.VISIBLE);
			mPager.setCurrentItem(2, false);
		} else {
			mFriend.setVisibility(View.GONE);
			mMod.setVisibility(View.GONE);
			NUM_ITEMS = 2;
			mMyRedditsView.setVisibility(View.GONE);
			mPager.setCurrentItem(0, false);
		}
		if (changed) {
			mAdapter.notifyDataSetChanged();
		//	mPager.setCurrentItem(0, false);
		}
	}

	// ----------new ------------
	public class CustomPagerAdapter extends FragmentPagerAdapter {
		public CustomPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public int getCount() {
			return NUM_ITEMS;
		}

		@Override
		public Fragment getItem(int position) {
			return SubscribeFragmentList.newInstance(position);
		}

	}

	@Override
	public void onPageScrolled(int position, float positionOffset,
			int positionOffsetPixels) {
		// do nothing;
	}

	@Override
	public void onPageSelected(int position) {
		mCustomTabWidget.focusCurrentTab(position);
	}

	@Override
	public void onPageScrollStateChanged(int state) {

	}

	@Override
	public void onTabSelectionChanged(int tabIndex, boolean clicked) {
		Log.d("FragmentPagerAdapter", "selected " + tabIndex + "is clicked?"
				+ clicked);
		mCustomTabWidget.setCurrentTab(tabIndex);
		mPager.setCurrentItem(tabIndex);
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		int action = 0;
		switch (id) {
		case R.id.subreddit_front:
			action = ACTION_SUBREDDIT_FRONT;
			break;
		case R.id.subreddit_all:
			action = ACTION_SUBREDDIT_ALL;
			break;
		case R.id.subreddit_friend:
			action = ACTION_SUBREDDIT_FRIENDS;
			break;
		case R.id.subreddit_mod:
			action = ACTION_SUBREDDIT_MOD;
			break;
		case R.id.subreddit_search:
			action = ACTION_SUBREDDIT_SEARCH;
			break;
		case R.id.subreddit_done:
			action = ACTION_SUBREDDIT_DONE;
			break;
		}
		mListener.onSubscribeItemClick(null, action);
	}

	public void setPickSubredditMode() {
		mFriend.setVisibility(View.GONE);
		mAll.setVisibility(View.GONE);
		mMod.setVisibility(View.GONE);
		mFront.setVisibility(View.GONE);
	}
}
