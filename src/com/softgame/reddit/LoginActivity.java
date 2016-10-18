package com.softgame.reddit;

import android.os.Bundle;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.softgame.reddit.fragment.LoginFragment;

public class LoginActivity extends SherlockFragmentActivity {

	long mFragmentId = 0L;
	LoginFragment mLoginFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.getSupportActionBar().hide();
		if (savedInstanceState != null)
			mFragmentId = savedInstanceState.getLong("fragment_id");
		if (mFragmentId == 0L) {
			mFragmentId = System.currentTimeMillis();
		}
		mLoginFragment = LoginFragment.findOrCreateLoginFragment(
				this.getSupportFragmentManager(), mFragmentId);
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putLong("fragment_id", mFragmentId);
		super.onSaveInstanceState(outState);
	}


}
