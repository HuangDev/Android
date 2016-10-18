package com.softgame.reddit;

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.actionbarsherlock.view.MenuItem;
import com.softgame.reddit.dialog.DraftDialog;
import com.softgame.reddit.dialog.MineSubredditDialog;
import com.softgame.reddit.fragment.SubmitLinkTextFragment;
import com.softgame.reddit.fragment.SubscribeFragmentList;
import com.softgame.reddit.impl.OnDraftDialogClick;
import com.softgame.reddit.impl.OnSubscribeItemClickListener;
import com.softgame.reddit.model.Subscribe;
import com.softgame.reddit.utils.Common;

public class SubmitLinkTextActivity extends CacheFragmentActivity implements
		OnDraftDialogClick, OnClickListener, OnSubscribeItemClickListener {

	SubmitLinkTextFragment mSubmitLinkTextFragment;
	String mSharedText;
	String mSubjectText;
	WeakReference<DraftDialog> mDraftDialogWF;
	public MineSubredditDialog mMineSubscribeDialog;
	boolean mFromShare;

	String subredditName;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Get intent, action and MIME type
		Intent intent = getIntent();
		String action = intent.getAction();
		String type = intent.getType();

		if (Intent.ACTION_SEND.equals(action) && type != null) {
			if ("text/plain".equals(type)) {
				mFromShare = true;
				mSharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
				mSubjectText = intent.getStringExtra(Intent.EXTRA_SUBJECT);
			}
		}

		subredditName = intent.getStringExtra(Common.EXTRA_SUBREDDIT_NAME);
		this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		this.getSupportActionBar().setDisplayUseLogoEnabled(true);
		this.getSupportActionBar().setTitle("Submit");

		mMineSubscribeDialog = new MineSubredditDialog(
				SubmitLinkTextActivity.this);

		mMineSubscribeDialog.setPickSubredditMode();

		mSubmitLinkTextFragment = SubmitLinkTextFragment
				.findOrCreateComposeMessageFragment(
						getSupportFragmentManager(), subredditName, mFromShare,
						mSharedText, mSubjectText);
	}

	@Override
	public View findViewById(int id) {
		View v = super.findViewById(id);
		if (v == null && mMineSubscribeDialog != null) {
			return mMineSubscribeDialog.findViewById(id);
		} else {
			return v;
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			if (mSubmitLinkTextFragment.needSaveDraft()) {
				popDraftDialog();
			} else {
				this.finish();
			}

			break;

		}
		return super.onOptionsItemSelected(item);
	}

	private void popDraftDialog() {
		if (mDraftDialogWF != null && mDraftDialogWF.get() != null) {
			mDraftDialogWF.get().dismiss();
		}
		DraftDialog dd = new DraftDialog(this);
		dd.setOwnerActivity(this);
		mDraftDialogWF = new WeakReference<DraftDialog>(dd);
		dd.show();
	}

	@Override
	public void onDraftClick(boolean isCheck, View v) {
		switch (v.getId()) {
		case R.id.button_save:
			if (!mFromShare) {
				mSubmitLinkTextFragment.saveToDraft();
				Toast.makeText(this, "Text Saved!", Toast.LENGTH_SHORT).show();
			}
			this.finish();
			break;
		case R.id.button_discard:
			if (!mFromShare) {
				mSubmitLinkTextFragment.setNeedToSave(false);
				mSubmitLinkTextFragment.clearnDraft();
			}
			Toast.makeText(this, "Text discard!", Toast.LENGTH_SHORT).show();
			this.finish();
			break;
		}
	}

	@Override
	public void onBackPressed() {
		if (mSubmitLinkTextFragment.needSaveDraft()) {
			popDraftDialog();
		} else {
			this.finish();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mDraftDialogWF != null && mDraftDialogWF.get() != null) {
			mDraftDialogWF.get().dismiss();
		}
		if (mMineSubscribeDialog != null) {
			mMineSubscribeDialog.dismiss();
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.pick_subreddit:
			mMineSubscribeDialog.show();
			break;
		}

	}

	// deal with Subscribe Pop Dialog Call back
	@Override
	public void onSubscribeItemClick(Subscribe s, int action) {
		switch (action) {
		case MineSubredditDialog.ACTION_SUBREDDIT_DONE:
			// do nothing
			break;
		case MineSubredditDialog.ACTION_SUBREDDIT_FRONT:
			setSubredditInfo(Common.DEFAULT_SUBREDDIT_NAME,
					Common.DEFAULT_SUBREDDIT);
			break;
		case MineSubredditDialog.ACTION_SUBREDDIT_ALL:
			setSubredditInfo(Common.SUBREDDIT_ALL_NAME, Common.SUBREDDIT_ALL);
			break;
		case MineSubredditDialog.ACTION_SUBREDDIT_FRIENDS:
			setSubredditInfo(Common.SUBREDDIT_FRIEND_NAME,
					Common.SUBREDDIT_FRIEND);
			break;
		case MineSubredditDialog.ACTION_SUBREDDIT_MOD:
			setSubredditInfo(Common.SUBREDDIT_MOD_NAME, Common.SUBREDDIT_MOD);
			break;
		case MineSubredditDialog.ACTION_SUBREDDIT_SEARCH:
			// do the search
			Intent search = new Intent(this, SearchSubscribeActivity.class);
			this.startActivityForResult(search, Common.REQUEST_SEARCH_SUBSCRIBE);
			break;
		case SubscribeFragmentList.ACTION_SUBREDDIT_ITEM:
			setSubredditInfo(s.display_name, s.url);
			break;
		}

		mMineSubscribeDialog.dismiss();
	}

	private void setSubredditInfo(String subredditName, String subredditUrl) {
		mSubmitLinkTextFragment.setSubreddit(subredditName);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case Common.REQUEST_SEARCH_SUBSCRIBE:
			if (resultCode == Activity.RESULT_OK && data != null) {
				String s = data.getStringExtra(Common.EXTRA_SUBREDDIT);
				String sn = data.getStringExtra(Common.EXTRA_SUBREDDIT_NAME);
				if (s != null && !"".equals(s) && sn != null && !"".equals(sn)) {
					setSubredditInfo(sn, s);
				}
			}
			break;
		}
	}

}
