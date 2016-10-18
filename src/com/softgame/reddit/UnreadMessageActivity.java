package com.softgame.reddit;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.softgame.reddit.fragment.UnreadMessageFragmentList;
import com.softgame.reddit.utils.Common;
import com.softgame.reddit.utils.RedditManager;

public class UnreadMessageActivity extends CacheFragmentActivity{

	String mUserName;
	UnreadMessageFragmentList mFragment;
	boolean fromInside = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// check login
		if (!RedditManager.isUserAuth(this)) {
			Toast.makeText(this, "User log out!", Toast.LENGTH_LONG).show();
			this.finish();
		}

		if (this.getIntent() != null && this.getIntent().getExtras()!=null) {
			fromInside = this.getIntent().getExtras()
					.getBoolean(Common.FROM_INSIDE_APP);
		}
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.cancel(R.drawable.icon);
		mUserName = RedditManager.getUserName(this);
		getSupportActionBar().setDisplayShowHomeEnabled(false);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setTitle(mUserName + " 's unread messages!");
		mFragment = UnreadMessageFragmentList
				.findOrCreateMessageFragmentList(getSupportFragmentManager());
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			if (!fromInside) {
				Intent t = new Intent(this, SubRedditFragmentActivity.class);
				t.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				this.startActivity(t);
			}
			this.finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
