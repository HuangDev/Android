package com.softgame.reddit;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.softgame.reddit.fragment.MessageFragmentList;
import com.softgame.reddit.utils.Common;

public class MessageActivity extends CacheFragmentActivity implements
		OnPageChangeListener, OnClickListener, ActionBar.TabListener,
		ActionBar.OnNavigationListener

{
	public static final int NUM_COUNT = 7;

	int mCurrentKind = -1;

	ViewPager mViewPager;
	MessagePagerAdapter mPagerAdapter;
	String[] mMessageTypeArray;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_messages);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayUseLogoEnabled(true);

		mMessageTypeArray = this.getResources().getStringArray(
				R.array.message_value_array);

		mPagerAdapter = new MessagePagerAdapter(
				this.getSupportFragmentManager());
		mViewPager = (ViewPager) this.findViewById(R.id.message_pager);
		mViewPager.setAdapter(mPagerAdapter);

		getSupportActionBar().setDisplayShowTitleEnabled(false);
		getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

		mViewPager.setOnPageChangeListener(this);

		ArrayAdapter<CharSequence> list = ArrayAdapter.createFromResource(this,
				R.array.label_message_text_array,
				R.layout.sherlock_spinner_item);
		list.setDropDownViewResource(R.layout.sherlock_spinner_dropdown_item);
		// getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		getSupportActionBar().setListNavigationCallbacks(list, this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add("Unread")
				.setIcon(R.drawable.icon_actionbar_email_unread)
				.setShowAsAction(
						MenuItem.SHOW_AS_ACTION_IF_ROOM
								| MenuItem.SHOW_AS_ACTION_WITH_TEXT);

		menu.add("Compose").setIcon(R.drawable.icon_actionbar_email_add)
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		return super.onCreateOptionsMenu(menu);
	}

	// ----------new ------------
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			this.finish();
			return true;
		}

		if (item.getTitle().equals("Unread")) {
			Intent t = new Intent(this, UnreadMessageActivity.class);
			t.putExtra(Common.FROM_INSIDE_APP, true);
			this.startActivity(t);
		}
		if (item.getTitle().equals("Compose")) {
			Intent t = new Intent(this, ComposeMessageActivity.class);
			this.startActivity(t);
		}

		return super.onOptionsItemSelected(item);
	}

	public class MessagePagerAdapter extends FragmentPagerAdapter {
		public MessagePagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public int getCount() {
			return NUM_COUNT;
		}

		@Override
		public Fragment getItem(int position) {
			return MessageFragmentList.newInstance(mMessageTypeArray[position]);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case android.R.id.home:
			this.finish();
			break;
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
