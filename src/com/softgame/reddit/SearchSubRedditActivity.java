package com.softgame.reddit;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.softgame.reddit.dialog.SubredditPopDialog;
import com.softgame.reddit.fragment.SearchSubRedditListFragment;
import com.softgame.reddit.fragment.SearchSubscribeFragmentList;
import com.softgame.reddit.utils.Common;

public class SearchSubRedditActivity extends CacheFragmentActivity implements
		OnCheckedChangeListener, OnClickListener {

	private static final String TAG = "SubRedditFragmentActivity";
	public static final int ITEM_REDDIT = 0x0;
	public static final int ITEM_SUBSCRIBE = 0x1;
	public static final int NUM_ITEMS = 2;

	String mCurrentSubReddit;
	String mCurrentSubRedditName;

	SearchSubRedditListFragment mSeachRedditFragment;
	EditText mEditText;
	long mFragmentId = 0L;

	public SubredditPopDialog mSubRedditPopDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.activity_search_subreddit);

		if (savedInstanceState != null)
			mFragmentId = savedInstanceState.getLong("fragment_id");
		if (mFragmentId == 0L) {
			mFragmentId = System.currentTimeMillis();
		}

		this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		this.getSupportActionBar().setTitle("Search Posts");
		this.getSupportActionBar().setDisplayShowHomeEnabled(false);

		SharedPreferences df = PreferenceManager
				.getDefaultSharedPreferences(this);
		mCurrentSubReddit = df.getString(Common.PREF_KEY_SUBREDDIT,
				Common.DEFAULT_SUBREDDIT);
		mCurrentSubRedditName = df.getString(Common.PREF_KEY_SUBREDDIT_NAME,
				Common.DEFAULT_SUBREDDIT_NAME);

		mSeachRedditFragment = SearchSubRedditListFragment
				.findOrCreateSubRedditRetainFragment(R.id.fragment_container,
						getSupportFragmentManager(), mCurrentSubReddit,
						mFragmentId);

		// Inflate the custom view
		View customBar = LayoutInflater.from(this).inflate(
				R.layout.item_limit_subscribe, null);

		TextView subreddit = (TextView) customBar
				.findViewById(R.id.subreddit_name);
		subreddit.setText(mCurrentSubRedditName);

		CheckBox checkBox = (CheckBox) customBar.findViewById(R.id.limit_check);
		checkBox.setChecked(mSeachRedditFragment.mRestrict_ON);
		checkBox.setOnCheckedChangeListener(this);

		// Attach to the action bar
		getSupportActionBar().setCustomView(customBar);
		getSupportActionBar().setDisplayShowCustomEnabled(true);

		mEditText = (EditText) this.findViewById(R.id.search_input);
		mEditText.setHint("search reddit");
		if (mSeachRedditFragment.mSearchItem != null
				&& !"".equals(mSeachRedditFragment.mSearchItem)) {
			mEditText.setText(mSeachRedditFragment.mSearchItem);
		}

		this.findViewById(R.id.search).setOnClickListener(this);

	};

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putLong("fragment_id", mFragmentId);
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		mSeachRedditFragment.setRestrictOn(isChecked);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			this.finish();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.search:
			if (mEditText.getEditableText() == null
					|| "".equals(mEditText.getEditableText().toString().trim())) {
				Toast.makeText(this, "Input request!", Toast.LENGTH_SHORT)
						.show();
			} else {
				mSeachRedditFragment.setSearchItem(mEditText.getEditableText()
						.toString());
			}
			break;
		}

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mSubRedditPopDialog != null) {
			mSubRedditPopDialog.dismiss();
		}
	}

}
