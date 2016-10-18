package com.softgame.reddit;

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.softgame.reddit.fragment.WebViewFragment;
import com.softgame.reddit.model.SubRedditItem;
import com.softgame.reddit.utils.Common;
import com.softgame.reddit.utils.CommonUtil;
import com.softgame.reddit.utils.SubRedditManager;

public class WebviewActivity extends SherlockFragmentActivity {

	ActionMode mActionMode;
	WebViewFragment mWebViewFragment;
	long mFragmentId = 0L;
	boolean mFullScreen = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		this.setTheme(R.style.Theme_Blue);
		super.onCreate(savedInstanceState);

		this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		this.getSupportActionBar().setDisplayShowHomeEnabled(false);
		this.getSupportActionBar().setTitle("Link");

		if (savedInstanceState != null) {
			mFragmentId = savedInstanceState.getLong("fragment_id");
			mFullScreen = savedInstanceState.getBoolean("is_fullscreen");
		}
		if (mFragmentId == 0L) {
			mFragmentId = System.currentTimeMillis();
		}

		// hide the actionBar if need
		if (mFullScreen) {
			this.getSupportActionBar().hide();
		}

		SubRedditItem subredditItem = null;
		if (this.getIntent() != null) {
			subredditItem = (SubRedditItem) this.getIntent()
					.getParcelableExtra(Common.EXTRA_SUBREDDIT);
		}

		if (subredditItem == null) {
			Toast.makeText(this, "link missing!", Toast.LENGTH_LONG).show();
			finish();
		}
		mWebViewFragment = WebViewFragment.findOrCreateRedditorFragment(
				this.getSupportFragmentManager(), subredditItem, mFragmentId);

	}

	public ActionMode getImageActionMode() {
		return mActionMode;
	}

	public void showActionMode() {
		mActionMode = startActionMode(new AnActionModeOfEpicProportions());
	}

	public boolean dismissActionMode() {
		if (mActionMode != null) {
			mActionMode.finish();
			return true;
		} else {
			return false;
		}
	}

	public void invalidateActionMode() {
		if (mActionMode != null) {
			mActionMode.invalidate();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		if (!mWebViewFragment.getSubredditItem().saved) {
			menu.add("UnSaved").setIcon(R.drawable.icon_actionbar_unsaved)
					.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

		} else {
			menu.add("Saved").setIcon(R.drawable.icon_actionbar_saved)
					.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

		}

		menu.add("Comments").setIcon(R.drawable.icon_actionbar_comment)
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

		menu.add("Share the Link").setIcon(R.drawable.icon_actionbar_share)
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

		menu.add("Action Mode").setIcon(R.drawable.icon_actionbar_context)
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			setResultData(false);
			this.finish();
			break;
		}
		if (item.getTitle().equals("UnSaved")) {
			// save
			mWebViewFragment.getSubredditItem().saved = true;
			new SaveTask(this, mWebViewFragment.getSubredditItem(), true)
					.execute();
			this.invalidateOptionsMenu();
		}
		if (item.getTitle().equals("Saved")) {
			mWebViewFragment.getSubredditItem().saved = false;
			new SaveTask(this, mWebViewFragment.getSubredditItem(), false)
					.execute();
			this.invalidateOptionsMenu();
		}

		if (item.getTitle().equals("Open in Browser")) {
			Intent i = new Intent(Intent.ACTION_VIEW,
					Uri.parse(mWebViewFragment.getSubredditItem().url));
			startActivity(i);
		}

		if (item.getTitle().equals("Share the Link")) {
			createLinkShareIntent();
		}

		if (item.getTitle().equals("Comments")) {
			Intent dd = new Intent();
			dd.putExtra(Common.EXTRA_SUBREDDIT,
					mWebViewFragment.getSubredditItem());
			dd.putExtra(Common.KEY_EXTRA_IS_COMMENT, true);
			this.setResult(Activity.RESULT_OK, dd);
			this.finish();
			return true;
		}
		if (item.getTitle().equals("Action Mode")) {
			mActionMode = startActionMode(new AnActionModeOfEpicProportions());

		}
		return super.onOptionsItemSelected(item);
	}

	private final class AnActionModeOfEpicProportions implements
			ActionMode.Callback {
		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			return true;
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			menu.clear();
			// Used to put dark icons on light action bar

			if (mWebViewFragment.getSubredditItem().likes == null) {
				menu.add("Upvote")
						.setIcon(R.drawable.icon_actionbar_vote_up_grey)
						.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

				menu.add("Downvote")
						.setIcon(R.drawable.icon_actionbar_vote_down_grey)
						.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
			} else {
				menu.add("Upvote")
						.setIcon(
								mWebViewFragment.getSubredditItem().likes ? R.drawable.vote_up_selected
										: R.drawable.icon_actionbar_vote_up_grey)
						.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

				menu.add("Downvote")
						.setIcon(
								mWebViewFragment.getSubredditItem().likes ? R.drawable.icon_actionbar_vote_down_grey
										: R.drawable.vote_down_selected)
						.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

			}

			menu.add("Open in Browser")
					.setIcon(R.drawable.icon_actionbar_browser)
					.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
			menu.add("Full Screen")
					.setIcon(R.drawable.icon_actionbar_fullscreen)
					.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

			return true;
		}

		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

			if (item.getItemId() == R.id.abs__action_mode_close_button) {
				WebviewActivity.this.dismissActionMode();
			}

			if (item.getTitle().equals("Open in Browser")) {
				Intent i = new Intent(Intent.ACTION_VIEW,
						Uri.parse(mWebViewFragment.getSubredditItem().url));
				startActivity(i);
			}

			if (item.getTitle().equals("Full Screen")) {
				WebviewActivity.this.dismissActionMode();
				WebviewActivity.this.getSupportActionBar().hide();
				mFullScreen = true;
			}

			if (item.getTitle().equals("Upvote")) {
				String dir = mWebViewFragment.updateVoteAndGetResult(true);
				new VoteTask(WebviewActivity.this,
						mWebViewFragment.getSubredditItem(), dir, true)
						.execute();

				WebviewActivity.this.invalidateActionMode();
			}
			if (item.getTitle().equals("Downvote")) {
				String dir = mWebViewFragment.updateVoteAndGetResult(false);
				new VoteTask(WebviewActivity.this,
						mWebViewFragment.getSubredditItem(), dir, false)
						.execute();
				WebviewActivity.this.invalidateActionMode();
			}

			return true;
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
			mActionMode = null;
		}

	}

	@Override
	public boolean onKeyDown(int keycode, KeyEvent e) {
		switch (keycode) {
		case KeyEvent.KEYCODE_MENU:
			if (!this.getSupportActionBar().isShowing()) {
				this.getSupportActionBar().show();
				mFullScreen = false;
				return true;
			}
			break;

		case KeyEvent.KEYCODE_BACK:
			if (mActionMode != null) {
				return this.dismissActionMode();
			} else if (mWebViewFragment.onKeyBackClick(keycode, e)) {
				// return super.onKeyDown(keycode, e);
				return true;
			} else {
				// return;
				setResultData(false);
				this.finish();
				return true;
			}
		}
		return super.onKeyDown(keycode, e);
	}

	private void setResultData(boolean isComment) {
		Intent dd = new Intent();
		dd.putExtra(Common.EXTRA_SUBREDDIT, mWebViewFragment.getSubredditItem());
		dd.putExtra(Common.KEY_EXTRA_IS_COMMENT, isComment);
		this.setResult(Activity.RESULT_OK, dd);
	}

	public void createLinkShareIntent() {
		Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
		sharingIntent.setType("text/plain");
		String shareBody = "http://www.reddit.com"
				+ mWebViewFragment.getSubredditItem().permalink;
		String subject = "Reddit this! "
				+ mWebViewFragment.getSubredditItem().title;
		sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, subject
				+ "\n" + shareBody);
		startActivity(Intent.createChooser(sharingIntent, "Share link via"));
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putLong("fragment_id", mFragmentId);
		outState.putBoolean("is_fullscreen", mFullScreen);
		super.onSaveInstanceState(outState);
	}

	// -------------------vote ------------------
	public static class VoteTask extends AsyncTask<Void, Void, String> {
		String name;
		boolean isUp;
		String dir = "0";
		WeakReference<WebviewActivity> WebviewActivityWF;
		WeakReference<SubRedditItem> subRedditItemWF;

		public VoteTask(WebviewActivity activity, SubRedditItem s, String d,
				boolean up) {
			WebviewActivityWF = new WeakReference<WebviewActivity>(activity);
			subRedditItemWF = new WeakReference<SubRedditItem>(s);
			isUp = up;
			dir = d;
			name = s.name;
		}

		@Override
		protected String doInBackground(Void... params) {
			if (WebviewActivityWF != null && WebviewActivityWF.get() != null) {
				return SubRedditManager.voteSubRedditPost(name, dir,
						WebviewActivityWF.get());
			} else {
				this.cancel(true);
				return Common.RESULT_TASK_CANCLE;
			}
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			if (result.equals(Common.RESULT_TASK_CANCLE)
					|| WebviewActivityWF == null
					|| WebviewActivityWF.get() == null
					|| subRedditItemWF == null || subRedditItemWF.get() == null
					|| WebviewActivityWF == null) {
				return;
			}
			if (result == Common.RESULT_SUCCESS) {
				Toast.makeText(WebviewActivityWF.get(), "Vote succeeded!",
						Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(WebviewActivityWF.get(), "Vote failed!",
						Toast.LENGTH_SHORT).show();

				// roll back
				subRedditItemWF.get().likes = subRedditItemWF.get().old_like;

				if (WebviewActivityWF.get().getImageActionMode() != null)
					WebviewActivityWF.get().getImageActionMode().invalidate();
			}
		}

	}

	// ---------------------- SAVE --------------------
	public static class SaveTask extends AsyncTask<Void, Void, String> {
		String name;
		boolean save;
		WeakReference<SubRedditItem> subRedditItemWF;
		WeakReference<WebviewActivity> WebviewActivityWF;

		public SaveTask(WebviewActivity activity, SubRedditItem item, boolean s) {
			save = s;
			name = item.name;
			subRedditItemWF = new WeakReference<SubRedditItem>(item);
			WebviewActivityWF = new WeakReference<WebviewActivity>(activity);
		}

		@Override
		protected String doInBackground(Void... params) {
			if (WebviewActivityWF != null && WebviewActivityWF.get() != null
					&& subRedditItemWF != null && subRedditItemWF.get() != null) {
				return SubRedditManager.saveSubRedditPost(name, save,
						WebviewActivityWF.get());
			} else {
				this.cancel(true);
				return Common.RESULT_TASK_CANCLE;
			}
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			if (result.equals(Common.RESULT_TASK_CANCLE)
					|| WebviewActivityWF == null
					|| WebviewActivityWF.get() == null
					|| subRedditItemWF == null || subRedditItemWF.get() == null) {
				return;
			}

			if (Common.RESULT_SUCCESS.equals(result)) {
				Toast.makeText(WebviewActivityWF.get(),
						save ? "Save succeeded!" : "Unsave succeeded!",
						Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(WebviewActivityWF.get(),
						"Save failed, try again latter!", Toast.LENGTH_SHORT)
						.show();
				subRedditItemWF.get().saved = !save;
				WebviewActivityWF.get().invalidateOptionsMenu();
			}

		}
	}

}
