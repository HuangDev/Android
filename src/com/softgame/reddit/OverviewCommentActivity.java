package com.softgame.reddit;

import java.net.URL;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.softgame.reddit.dialog.CustomDialogHandler;
import com.softgame.reddit.dialog.PostCommentDialog;
import com.softgame.reddit.fragment.OverviewCommentFragment;
import com.softgame.reddit.impl.OnPopCommentListener;
import com.softgame.reddit.model.CommentIndex;
import com.softgame.reddit.model.SubRedditItem;
import com.softgame.reddit.utils.Common;
import com.softgame.reddit.utils.RedditManager;

public class OverviewCommentActivity extends CacheFragmentActivity implements
		CustomDialogHandler, OnClickListener, OnPopCommentListener {

	public static final int LOAD_TYPE_SHARE_COMMENT = 0x0;
	public static final int LOAD_TYPE_LINK_ID = 0x1;
	public static final int LOAD_TYPE_SHARE_COMMENT_CONTEXT = 0x2;

	public static final String TAG = "OverviewCommentActivity";

	public static final int TYPE_SUBREDDIT = 0;
	public static final int TYPE_COMMENT = 1;
	public static final int TYPE_HIDE = 2;
	public static final int TYPE_LOADING = 3;
	public static final int TYPE_NO_COMMENT = 4;
	public static final int TYPE_COMMENT_SORT = 5;
	public static final int TYPE_LOAD_FAIL = 6;

	int mLoadType;
	String mCommentUrl;
	// http://www.reddit.com/comments/s0oyx.json
	OverviewCommentFragment mCommentRetainFragment;

	PostCommentDialog mPostCommentDialog;

	ActionMode mActionMode;
	long mFragmentId = 0L;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// this.setContentView(R.layout.activity_comment);

		Intent intent = getIntent();
		String action = intent.getAction();
		String data = intent.getDataString();
		if (Intent.ACTION_VIEW.equals(action)) {

			if (data != null && !"".equals(data.trim())) {
				// check if it is context or not
				mCommentUrl = checkCommentType(data.trim());
			}

		} else {
			if (intent != null) {
				mLoadType = intent.getIntExtra(Common.EXTRA_OVERVIEW_LOAD_TYPE,
						-1);
			}

			if (mLoadType == -1) {
				// missing
				Toast.makeText(this, "fail to decode link!", Toast.LENGTH_SHORT)
						.show();
				this.finish();
				return;
			}

			if (this.getIntent() != null) {
				mCommentUrl = this.getIntent().getStringExtra(
						Common.EXTRA_OVERVIEW_COMMENT_URL);
			}

		}

		if (mCommentUrl == null || "".equals(mCommentUrl)) {
			// missing
			Toast.makeText(this, "fail to decode link!", Toast.LENGTH_SHORT)
					.show();
			this.finish();
			return;
		}
		this.getImageWorker().setLoadingImage(R.drawable.empty_pic);
		this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		this.getSupportActionBar().setDisplayShowHomeEnabled(false);
		this.getSupportActionBar().setTitle("Comments");
		// this.getSupportActionBar().setSubtitle(s.subreddit);
		// this.getSupportActionBar().setDisplayShowHomeEnabled(false);
		if (savedInstanceState != null)
			mFragmentId = savedInstanceState.getLong("fragment_id");
		if (mFragmentId == 0L) {
			mFragmentId = System.currentTimeMillis();
		}

		mCommentRetainFragment = OverviewCommentFragment
				.findOrCreateOveriviewCommentFragment(
						this.getSupportFragmentManager(), mCommentUrl,
						mLoadType, mFragmentId);

		showPostCommentDialogIfNeeded();

	}

	private void showPostCommentDialogIfNeeded() {

		if (mCommentRetainFragment.mIsPostingComment
				&& mCommentRetainFragment.mCommentAdapter != null) {
			int position = mCommentRetainFragment.mPostingPosition;
			Object ob = mCommentRetainFragment.mCommentAdapter
					.getItem(position);

			if (ob != null
					&& position == 0
					&& mCommentRetainFragment.mCommentAdapter
							.getItemViewType(position) == OverviewCommentFragment.TYPE_SUBREDDIT) {
				SubRedditItem sub = (SubRedditItem) ob;

				if (mPostCommentDialog != null) {
					mPostCommentDialog.dismiss();
				}
				mPostCommentDialog = new PostCommentDialog(
						OverviewCommentActivity.this);
				mPostCommentDialog.setCustomDialogHandler(
						OverviewCommentActivity.this, sub.name, position);
				mPostCommentDialog.setItem(sub);
				mPostCommentDialog
						.setInputText(mCommentRetainFragment.mInputPosting);
				mPostCommentDialog.show();
			} else if (ob != null
					&& position > 0
					&& mCommentRetainFragment.mCommentAdapter
							.getItemViewType(position) == OverviewCommentFragment.TYPE_COMMENT) {
				CommentIndex commentItemIndex = (CommentIndex) ob;
				// subreddit
				if (mPostCommentDialog != null) {
					mPostCommentDialog.dismiss();
				}
				mPostCommentDialog = new PostCommentDialog(
						OverviewCommentActivity.this);

				mPostCommentDialog.setCustomDialogHandler(
						OverviewCommentActivity.this,
						commentItemIndex.redditComment.name, position);
				mPostCommentDialog
						.setIsEditComment(mCommentRetainFragment.mIsEditComment);
				mPostCommentDialog.setItem(commentItemIndex.redditComment);
				mPostCommentDialog
						.setInputText(mCommentRetainFragment.mInputPosting);
				mPostCommentDialog.show();
			}

		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	}

	public String checkCommentType(String link) {
		try {
			URL linkURL = new URL(link);
			if (linkURL.getPath() != null) {

				if (linkURL.getPath().matches(Common.COMMENT_PATTEN)) {
					mLoadType = LOAD_TYPE_SHARE_COMMENT;
					return link;
				} else if (linkURL.getPath().matches(
						Common.COMMENT_CONTEXT_PATENT)) {
					mLoadType = LOAD_TYPE_SHARE_COMMENT_CONTEXT;
					return link;
				}
			}
			return null;
		} catch (Exception e) {
			return null;
		}

	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putLong("fragment_id", mFragmentId);

		if (mPostCommentDialog != null && mPostCommentDialog.isShowing()
				&& mCommentRetainFragment != null) {
			mCommentRetainFragment.mIsPostingComment = true;
			mCommentRetainFragment.mPostingPosition = mPostCommentDialog.mPosition;
			mCommentRetainFragment.mInputPosting = mPostCommentDialog
					.getInputText();
			mCommentRetainFragment.mIsEditComment = mPostCommentDialog.mIsEdit;
		} else {
			if (mCommentRetainFragment != null) {
				mCommentRetainFragment.mIsPostingComment = false;
			}
		}

		super.onSaveInstanceState(outState);
	}

	// TODO NullPointException
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			if (mCommentRetainFragment != null
					&& mCommentRetainFragment.mSubRedditItem != null) {
				Intent backIntent = new Intent();
				backIntent.putExtra(Common.INTENT_EXTRA_SUBREDDIT,
						mCommentRetainFragment.mSubRedditItem);
				this.setResult(Activity.RESULT_OK, backIntent);
			}
			this.finish();
			break;
		}
		return super.onOptionsItemSelected(item);
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
			menu.add("Post a comment")
					.setIcon(R.drawable.icon_actionbar_comment_add)
					.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

			if (mCommentRetainFragment.getSubRedditItem().likes == null) {
				menu.add("Upvote")
						.setIcon(R.drawable.icon_actionbar_vote_up_grey)
						.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

				menu.add("Downvote")
						.setIcon(R.drawable.icon_actionbar_vote_down_grey)
						.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
			} else {
				menu.add("Upvote")
						.setIcon(
								mCommentRetainFragment.getSubRedditItem().likes ? R.drawable.vote_up_selected
										: R.drawable.icon_actionbar_vote_up_grey)
						.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

				menu.add("Downvote")
						.setIcon(
								mCommentRetainFragment.getSubRedditItem().likes ? R.drawable.icon_actionbar_vote_down_grey
										: R.drawable.vote_down_selected)
						.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

			}

			if (mCommentRetainFragment.getSubRedditItem().saved) {
				menu.add("UnSave post")
						.setIcon(R.drawable.icon_actionbar_unsaved)
						.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

			} else {
				menu.add("Save post").setIcon(R.drawable.icon_actionbar_saved)
						.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

			}

			if (mCommentRetainFragment.getSubRedditItem().hidden) {
				menu.add("UnHide").setIcon(R.drawable.icon_hide_selected)
						.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
			} else {
				menu.add("Hide").setIcon(R.drawable.icon_hide_unselected)
						.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
			}

			menu.add("Profile").setIcon(R.drawable.icon_profile)
					.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

			menu.add("Full screen")
					.setIcon(R.drawable.icon_actionbar_fullscreen)
					.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
			return true;
		}

		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

			if (item.getItemId() == R.id.abs__action_mode_close_button) {
				OverviewCommentActivity.this.dismissActionMode();
			}

			if (item.getTitle().equals("Full screen")) {
				OverviewCommentActivity.this.dismissActionMode();
				OverviewCommentActivity.this.getSupportActionBar().hide();
				if (OverviewCommentActivity.this.getRetainFragment() != null) {
					OverviewCommentActivity.this.getRetainFragment()
							.setFullScreen(true);
				}
			}

			if (item.getTitle().equals("Upvote")) {
				if (RedditManager.isUserAuth(OverviewCommentActivity.this)) {
					mCommentRetainFragment.updateSubRedditVote(
							mCommentRetainFragment.mSubRedditItem, true);
					OverviewCommentActivity.this.invalidateActionMode();
				} else {
					Toast.makeText(
							OverviewCommentActivity.this,
							OverviewCommentActivity.this
									.getString(R.string.login_request),
							Toast.LENGTH_SHORT).show();
				}

			}
			if (item.getTitle().equals("Downvote")) {
				if (RedditManager.isUserAuth(OverviewCommentActivity.this)) {
					mCommentRetainFragment.updateSubRedditVote(
							mCommentRetainFragment.mSubRedditItem, false);
					OverviewCommentActivity.this.invalidateActionMode();
				} else {
					Toast.makeText(
							OverviewCommentActivity.this,
							OverviewCommentActivity.this
									.getString(R.string.login_request),
							Toast.LENGTH_SHORT).show();
				}

			}
			if (item.getTitle().equals("Post a comment")) {
				if (RedditManager.isUserAuth(OverviewCommentActivity.this)) {
					if (mPostCommentDialog != null) {
						mPostCommentDialog.dismiss();
					}
					mPostCommentDialog = new PostCommentDialog(
							OverviewCommentActivity.this);
					mPostCommentDialog.setCustomDialogHandler(
							OverviewCommentActivity.this,
							mCommentRetainFragment.mSubRedditItem.name, 0);
					mPostCommentDialog
							.setItem(mCommentRetainFragment.mSubRedditItem);
					mPostCommentDialog.show();
					OverviewCommentActivity.this.invalidateActionMode();
				} else {
					Toast.makeText(
							OverviewCommentActivity.this,
							OverviewCommentActivity.this
									.getString(R.string.login_request),
							Toast.LENGTH_SHORT).show();
				}
			}

			if (item.getTitle().equals("Save post")
					|| item.getTitle().equals("UnSave post")) {
				if (RedditManager.isUserAuth(OverviewCommentActivity.this)) {
					new OverviewCommentFragment.SaveTask(
							mCommentRetainFragment,
							mCommentRetainFragment.getSubRedditItem().name,
							!mCommentRetainFragment.getSubRedditItem().saved)
							.execute();
				} else {
					Toast.makeText(
							OverviewCommentActivity.this,
							OverviewCommentActivity.this
									.getString(R.string.login_request),
							Toast.LENGTH_SHORT).show();
				}
			}

			if (item.getTitle().equals("Hide")
					|| item.getTitle().equals("UnHide")) {
				if (RedditManager.isUserAuth(OverviewCommentActivity.this)) {
					new OverviewCommentFragment.HideTask(
							mCommentRetainFragment,
							mCommentRetainFragment.getSubRedditItem().name,
							!mCommentRetainFragment.getSubRedditItem().hidden)
							.execute();
					OverviewCommentActivity.this.invalidateActionMode();
				} else {
					Toast.makeText(
							OverviewCommentActivity.this,
							OverviewCommentActivity.this
									.getString(R.string.login_request),
							Toast.LENGTH_SHORT).show();
				}
			}

			if (item.getTitle().equals("Profile")) {
				String profileName = mCommentRetainFragment.getSubRedditItem().author;
				Intent profileIntent = new Intent(OverviewCommentActivity.this,
						OverviewFragmentActivity.class);
				profileIntent.putExtra(Common.EXTRA_USERNAME, profileName);
				OverviewCommentActivity.this.startActivity(profileIntent);
			}
			return true;
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
			mActionMode = null;
		}
	}

	// POST COMMENT
	@Override
	public void onViewClick(Dialog customDialog, View v, int position) {
		switch (v.getId()) {

		case R.id.comment_send:
			PostCommentDialog cd = (PostCommentDialog) customDialog;
			String text = cd.mContentEtx.getEditableText().toString();
			String things_id = cd.mThing_id;
			if (cd.mIsEdit) {
				mCommentRetainFragment.editComment(position, text);
			} else {
				new OverviewCommentFragment.PostCommentTask(
						mCommentRetainFragment, things_id, text, position)
						.execute();
			}
			cd.dismiss();
			break;
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.subreddit_image:

			Intent imageIntent = new Intent(this, ImageDetailActivity.class);
			imageIntent.putExtra(Common.EXTRA_SUBREDDIT,
					mCommentRetainFragment.mSubRedditItem);
			startActivity(imageIntent);
			break;
		case R.id.subreddit_link:
			startActivity(new Intent(Intent.ACTION_VIEW,
					Uri.parse(mCommentRetainFragment.mSubRedditItem.url)));
			break;

		case R.id.comment_post_comment:
			// Post Comment
			// Post Comment
			if (mPostCommentDialog != null) {
				mPostCommentDialog.dismiss();
			}

			mPostCommentDialog = new PostCommentDialog(
					OverviewCommentActivity.this);
			mPostCommentDialog.setCustomDialogHandler(
					OverviewCommentActivity.this,
					mCommentRetainFragment.mSubRedditItem.name, 0);
			mPostCommentDialog.setItem(mCommentRetainFragment.mSubRedditItem);
			mPostCommentDialog.show();
			break;

		case R.id.vote_up_wraper:
			if (RedditManager.isUserAuth(this)) {
				mCommentRetainFragment.updateSubRedditVote(
						mCommentRetainFragment.mSubRedditItem, true);
			} else {
				Toast.makeText(this, this.getString(R.string.login_request),
						Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.vote_down_wraper:
			if (RedditManager.isUserAuth(this)) {
				mCommentRetainFragment.updateSubRedditVote(
						mCommentRetainFragment.mSubRedditItem, false);
			} else {
				Toast.makeText(this, this.getString(R.string.login_request),
						Toast.LENGTH_SHORT).show();
			}
			break;
		}

	}

	@Override
	public void onBackPressed() {
		Intent t = new Intent();
		t.putExtra(Common.INTENT_EXTRA_SUBREDDIT,
				mCommentRetainFragment.mSubRedditItem);
		this.setResult(Activity.RESULT_OK, t);
		finish();
	}

	@Override
	public void onPopCommentClick(View v, SubRedditItem subRedditItem,
			CommentIndex commentItemIndex, int position) {
		switch (v.getId()) {
		case R.id.comment_post_comment:
			// subreddit
			if (position == 0 && subRedditItem != null) {
				PostCommentDialog pd = new PostCommentDialog(
						OverviewCommentActivity.this);
				pd.setCustomDialogHandler(OverviewCommentActivity.this,
						subRedditItem.name, position);
				pd.setItem(subRedditItem);
				pd.show();
			}
			if (position != 0 && commentItemIndex != null) {
				PostCommentDialog pd = new PostCommentDialog(
						OverviewCommentActivity.this);
				pd.setCustomDialogHandler(OverviewCommentActivity.this,
						commentItemIndex.redditComment.name, position);
				pd.setItem(commentItemIndex.redditComment);
				pd.show();
			}

			break;

		case R.id.comment_delete:
			if (position == 0 && subRedditItem != null) {

			}
			if (position != 0 && commentItemIndex != null) {
				mCommentRetainFragment
						.deleteComment(commentItemIndex, position);
			}
			break;
		case R.id.comment_edit:

			if (position == 0 && subRedditItem != null) {

			}
			if (position != 0 && commentItemIndex != null) {
				if (mPostCommentDialog != null) {
					mPostCommentDialog.dismiss();
				}
				mPostCommentDialog = new PostCommentDialog(
						OverviewCommentActivity.this);
				mPostCommentDialog.setCustomDialogHandler(
						OverviewCommentActivity.this,
						commentItemIndex.redditComment.name, position);
				mPostCommentDialog.setIsEditComment(true);
				mPostCommentDialog.setItem(commentItemIndex.redditComment);
				mPostCommentDialog.show();
			}

			break;

		case R.id.comment_vote_up:
			if (position == 0 && subRedditItem != null) {
				mCommentRetainFragment.updateSubRedditVote(subRedditItem, true);
			}
			if (position != 0 && commentItemIndex != null) {
				mCommentRetainFragment.updateCommentVote(
						commentItemIndex.redditComment, true);
			}
			break;
		case R.id.comment_vote_down:
			if (position == 0 && subRedditItem != null) {
				mCommentRetainFragment
						.updateSubRedditVote(subRedditItem, false);
			}
			if (position != 0 && commentItemIndex != null) {
				mCommentRetainFragment.updateCommentVote(
						commentItemIndex.redditComment, false);
			}

			break;
		case R.id.comment_save:
			if (position == 0 && subRedditItem != null) {
				new OverviewCommentFragment.SaveTask(mCommentRetainFragment,
						subRedditItem.name, !subRedditItem.saved).execute();
			}

			break;
		case R.id.comment_hide:
			if (position == 0 && subRedditItem != null) {
				new OverviewCommentFragment.HideTask(mCommentRetainFragment,
						subRedditItem.name, !subRedditItem.hidden).execute();
			}

			break;
		case R.id.comment_profile:
			String profileName = "redditet";
			if (position == 0 && subRedditItem != null) {
				profileName = subRedditItem.author;
			}
			if (position != 0 && commentItemIndex != null) {
				profileName = commentItemIndex.redditComment.author;
			}

			Intent profileIntent = new Intent(OverviewCommentActivity.this,
					OverviewFragmentActivity.class);
			profileIntent.putExtra(Common.EXTRA_USERNAME, profileName);
			OverviewCommentActivity.this.startActivity(profileIntent);

			break;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mPostCommentDialog != null) {
			mPostCommentDialog.dismiss();
		}
	}
}