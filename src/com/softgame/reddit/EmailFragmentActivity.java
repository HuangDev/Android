package com.softgame.reddit;

import android.os.Bundle;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.softgame.reddit.fragment.EmailListFragment;

public class EmailFragmentActivity extends SherlockFragmentActivity {

	// http://www.reddit.com/comments/s0oyx.json
	EmailListFragment mEmailListFragment;
	long mFragmentId = 0L;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null)
			mFragmentId = savedInstanceState.getLong("fragment_id");
		if (mFragmentId == 0L) {
			mFragmentId = System.currentTimeMillis();
		}
		mEmailListFragment = EmailListFragment.findOrCreateEmailListFragment(this.getSupportFragmentManager(), mFragmentId);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putLong("fragment_id", mFragmentId);
		super.onSaveInstanceState(outState);
	}
}
