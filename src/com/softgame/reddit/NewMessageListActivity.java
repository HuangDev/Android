package com.softgame.reddit;

import android.content.Intent;
import android.os.Bundle;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.softgame.reddit.fragment.NewMessageFragmentList;
import com.softgame.reddit.model.MessageModel;
import com.softgame.reddit.utils.Common;

/*
 * This class is to show the user when it has more than 2 message.
 */
public class NewMessageListActivity extends SherlockFragmentActivity {
	MessageModel mMessageModel;
	NewMessageFragmentList mFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayUseLogoEnabled(true);

		if (this.getIntent() != null) {
			mMessageModel = this.getIntent().getParcelableExtra(
					Common.KEY_MESSAGE_MODEL);
		}

		if (mMessageModel != null) {
			getSupportActionBar().setTitle(
					mMessageModel.getItemsSize() + " New Message");
		}

		mFragment = NewMessageFragmentList.findOrCreateMessageFragmentList(
				this.getSupportFragmentManager(), mMessageModel);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			Intent t = new Intent(this, SubRedditFragmentActivity.class);
			this.startActivity(t);
			this.finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
