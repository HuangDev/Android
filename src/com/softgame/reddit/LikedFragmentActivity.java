package com.softgame.reddit;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.OnNavigationListener;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.view.MenuItem;
import com.softgame.reddit.dialog.SubredditPopDialog;
import com.softgame.reddit.fragment.LikedListFragment;
import com.softgame.reddit.utils.Common;
import com.softgame.reddit.utils.RedditManager;

public class LikedFragmentActivity extends CacheFragmentActivity implements
		OnPageChangeListener, ActionBar.TabListener, OnNavigationListener {

	public static final int NUM_ITEMS = 4;
	ViewPager mViewPager;
	OverviewPagerAdapter mOverviewPagerAdapter;

	String mCurrentAuthor;
	public SubredditPopDialog mSubRedditPopDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mCurrentAuthor = RedditManager.getUserName(this);
		if (mCurrentAuthor == null || "".equals(mCurrentAuthor)) {
			//
			Toast.makeText(this, "Login Request!", Toast.LENGTH_SHORT).show();
			this.finish();
		}
		mImageWorker.setImageFadeIn(true);
		mImageWorker.setLoadFailImage(R.drawable.picture_not_found);
		
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		// getSupportActionBar().setDisplayShowHomeEnabled(false);
		getSupportActionBar().setDisplayUseLogoEnabled(true);
		this.getSupportActionBar().setTitle(mCurrentAuthor);

		this.setContentView(R.layout.activity_fragment_overview);
		mOverviewPagerAdapter = new OverviewPagerAdapter(
				this.getSupportFragmentManager());
		mViewPager = (ViewPager) this.findViewById(R.id.pager);
		mViewPager.setAdapter(mOverviewPagerAdapter);
		mViewPager.setOnPageChangeListener(this);
		if (super.isPortaitMode()) {
			getSupportActionBar().setNavigationMode(
					ActionBar.NAVIGATION_MODE_TABS);
			for (int i = 0; i < NUM_ITEMS; i++) {
				ActionBar.Tab tab = getSupportActionBar().newTab();
				tab.setText(Common.LIKED_TYPE_ARRAY_VALUE_TEXT[i]);
				tab.setTabListener(this);
				getSupportActionBar().addTab(tab);
			}
		} else {
			getSupportActionBar().setNavigationMode(
					ActionBar.NAVIGATION_MODE_LIST);
			ArrayAdapter<CharSequence> list = ArrayAdapter.createFromResource(
					this, R.array.liked_tab_text_array,
					com.actionbarsherlock.R.layout.sherlock_spinner_item);
			list.setDropDownViewResource(R.layout.sherlock_spinner_dropdown_item);
			getSupportActionBar().setListNavigationCallbacks(list, this);
		}
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

	public class OverviewPagerAdapter extends FragmentPagerAdapter {

		public OverviewPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public int getCount() {
			return NUM_ITEMS;
		}

		@Override
		public ListFragment getItem(int position) {
			return LikedListFragment
					.newInstance(Common.LIKED_TYPE_ARRAY_VALUE[position]);
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
	protected void onDestroy() {
		super.onDestroy();

		if (mSubRedditPopDialog != null) {
			mSubRedditPopDialog.dismiss();
		}

	}

	
}
