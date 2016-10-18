package com.softgame.reddit;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.softgame.reddit.dialog.FriendDialog;
import com.softgame.reddit.fragment.ComposeMessageFragment;
import com.softgame.reddit.impl.OnFriendItemClick;
import com.softgame.reddit.utils.Common;

public class ComposeMessageActivity extends CacheFragmentActivity implements
		OnFriendItemClick {

	ComposeMessageFragment mComposeMessageFragment;
	FriendDialog mFriendDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		String name = null;
		Intent t = this.getIntent();
		if (t != null) {
			name = t.getStringExtra(Common.KEY_REDDITOR_NAME);
		}

		this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		this.getSupportActionBar().setDisplayShowHomeEnabled(false);
		this.getSupportActionBar().setDisplayUseLogoEnabled(false);
		this.getSupportActionBar().setTitle("Compose");
		// this.getSupportActionBar().setSubtitle(s.subreddit);
		// this.getSupportActionBar().setDisplayShowHomeEnabled(false);
		mFriendDialog = new FriendDialog(this);
		if (name != null && !"".equals(name)) {
			mComposeMessageFragment = ComposeMessageFragment
					.findOrCreateComposeMessageFragment(
							this.getSupportFragmentManager(), name);

		} else {
			mComposeMessageFragment = ComposeMessageFragment
					.findOrCreateComposeMessageFragment(this
							.getSupportFragmentManager());
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			this.finish();
			return true;
		}
		return false;
	}

	@Override
	public View findViewById(int id) {
		View v = super.findViewById(id);
		if (v == null && mFriendDialog != null) {
			return mFriendDialog.findViewById(id);
		} else {
			return v;
		}
	}

	public void showFriendsDilaog() {
		mFriendDialog.show();
	}

	public void dismissFriendsDialog() {
		mFriendDialog.dismiss();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mFriendDialog != null) {
			mFriendDialog.dismiss();
		}
	}

	@Override
	public void onFriendNameClick(String name) {
		mComposeMessageFragment.onFriendNameClick(name);
		mFriendDialog.dismiss();
	}

}
