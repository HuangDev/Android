package com.softgame.reddit;

import android.content.Intent;
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
import com.softgame.reddit.fragment.OverviewListFragment;
import com.softgame.reddit.fragment.RedditorFragment;
import com.softgame.reddit.utils.Common;

public class OverviewFragmentActivity extends CacheFragmentActivity implements
		OnPageChangeListener, ActionBar.TabListener, OnNavigationListener {

	public static final int NUM_ITEMS = 4;
	ViewPager mViewPager;
	OverviewPagerAdapter mOverviewPagerAdapter;
	String mCurrentAuthor;
	String[] mKindTypeArray;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent startIntent = this.getIntent();
		if (startIntent != null) {
			mCurrentAuthor = startIntent.getStringExtra(Common.EXTRA_USERNAME);
			if (mCurrentAuthor == null) {
				Toast.makeText(OverviewFragmentActivity.this,
						"Profile missing!", Toast.LENGTH_LONG).show();
				this.finish();
			}
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

		mKindTypeArray = this.getResources().getStringArray(
				R.array.overview_tab_value_array);

		if (super.isPortaitMode()) {
			getSupportActionBar().setNavigationMode(
					ActionBar.NAVIGATION_MODE_TABS);
			for (int i = 0; i < NUM_ITEMS; i++) {
				ActionBar.Tab tab = getSupportActionBar().newTab();
				tab.setText(Common.OVERVIEW_TYPE_ARRAY_TEXT[i]);
				tab.setTabListener(this);
				getSupportActionBar().addTab(tab);
			}
		} else {
			getSupportActionBar().setNavigationMode(
					ActionBar.NAVIGATION_MODE_LIST);
			ArrayAdapter<CharSequence> list = ArrayAdapter.createFromResource(
					this, R.array.overview_tab_text_array,
					R.layout.sherlock_spinner_item);
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
			if (position == 0) {
				return RedditorFragment.newInstance(mCurrentAuthor);
			} else {
				return OverviewListFragment.newInstance(mCurrentAuthor,
						mKindTypeArray[position - 1]);
			}
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

}
