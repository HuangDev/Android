package com.softgame.reddit;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.ShareActionProvider;
import com.softgame.reddit.dialog.CustomDialogHandler;
import com.softgame.reddit.dialog.PostCommentDialog;
import com.softgame.reddit.fragment.CommentRetainFragment;
import com.softgame.reddit.impl.OnPopCommentListener;
import com.softgame.reddit.model.CommentIndex;
import com.softgame.reddit.model.SubRedditItem;
import com.softgame.reddit.utils.Common;
import com.softgame.reddit.utils.RedditManager;

public class CommentFragmentActivity extends CacheFragmentActivity implements
		CustomDialogHandler, OnClickListener, OnPopCommentListener {

	public static final String TAG = "CommentActivity";

	public static final int TYPE_SUBREDDIT = 0;
	public static final int TYPE_COMMENT = 1;
	public static final int TYPE_HIDE = 2;
	public static final int TYPE_LOADING = 3;
	public static final int TYPE_NO_COMMENT = 4;
	public static final int TYPE_COMMENT_SORT = 5;

	ActionMode mActionMode;
	// http://www.reddit.com/comments/s0oyx.json
	CommentRetainFragment mCommentRetainFragment;
	PostCommentDialog mPostCommentDialog;
	long mFragmentId = 0L;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// this.setContentView(R.layout.activity_comment);
		// this.getImageWorker().setLoadingImage(R.drawable.empty_pic);
		SubRedditItem s = null;
		if (this.getIntent() != null) {
			s = this.getIntent().getParcelableExtra(
					Common.INTENT_EXTRA_SUBREDDIT);
		}

		if (s == null) {
			Toast.makeText(this, "Post missing!", Toast.LENGTH_SHORT).show();
			this.finish();
			return;
		}
		this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		this.getSupportActionBar().setDisplayShowHomeEnabled(false);
		this.getSupportActionBar().setTitle("Comments");
		// this.getSupportActionBar().setSubtitle(s.subreddit);
		// this.getSupportActionBar().setDisplayShowHomeEnabled(false);

		// set loading image

		this.getImageWorker().setLoadingImage(R.drawable.empty_pic);
	    this.getImageWorker().setLoadFailImage(R.drawable.picture_not_found);
	    this.getImageWorker().setImageFadeIn(true);
		if (savedInstanceState != null)
			mFragmentId = savedInstanceState.getLong("fragment_id");
		if (mFragmentId == 0L) {
			mFragmentId = System.currentTimeMillis();
		}

		// hide the actionBar if need
		if (this.getRetainFragment() != null) {
			if (this.getRetainFragment().isFullScreen()) {
				this.getSupportActionBar().hide();
			}
		}

		// show comment dialog if need;
		mCommentRetainFragment = CommentRetainFragment
				.findOrCreateCommentRetainFragment(
						this.getSupportFragmentManager(), s, mFragmentId);

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
							.getItemViewType(position) == CommentRetainFragment.TYPE_SUBREDDIT) {
				SubRedditItem sub = (SubRedditItem) ob;

				if (mPostCommentDialog != null) {
					mPostCommentDialog.dismiss();
				}
				mPostCommentDialog = new PostCommentDialog(
						CommentFragmentActivity.this);
				mPostCommentDialog.setCustomDialogHandler(
						CommentFragmentActivity.this, sub.name, position);
				mPostCommentDialog.setItem(sub);
				mPostCommentDialog
						.setInputText(mCommentRetainFragment.mInputPosting);
				mPostCommentDialog.show();
			} else if (ob != null
					&& position > 0
					&& mCommentRetainFragment.mCommentAdapter
							.getItemViewType(position) == CommentRetainFragment.TYPE_COMMENT) {
				CommentIndex commentItemIndex = (CommentIndex) ob;
				// subreddit
				if (mPostCommentDialog != null) {
					mPostCommentDialog.dismiss();
				}
				mPostCommentDialog = new PostCommentDialog(
						CommentFragmentActivity.this);

				mPostCommentDialog.setCustomDialogHandler(
						CommentFragmentActivity.this,
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
	protected void onSaveInstanceState(Bundle outState) {
		outState.putLong("fragment_id", mFragmentId);
		if (mPostCommentDialog != null && mPostCommentDialog.isShowing()
				&& mCommentRetainFragment != null) {
			mCommentRetainFragment.mIsPostingComment = true;
			mCommentRetainFragment.mIsEditComment = mPostCommentDialog.mIsEdit;
			mCommentRetainFragment.mPostingPosition = mPostCommentDialog.mPosition;
			mCommentRetainFragment.mInputPosting = mPostCommentDialog
					.getInputText();
		} else {
			if (mCommentRetainFragment != null) {
				mCommentRetainFragment.mIsPostingComment = false;
			}
		}
		super.onSaveInstanceState(outState);
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
				CommentFragmentActivity.this.dismissActionMode();
			}

			if (item.getTitle().equals("Full screen")) {
				CommentFragmentActivity.this.dismissActionMode();
				CommentFragmentActivity.this.getSupportActionBar().hide();
				if (CommentFragmentActivity.this.getRetainFragment() != null) {
					CommentFragmentActivity.this.getRetainFragment()
							.setFullScreen(true);
				}
			}

			if (item.getTitle().equals("Upvote")) {
				if (RedditManager.isUserAuth(CommentFragmentActivity.this)) {
					mCommentRetainFragment.updateSubRedditVote(
							mCommentRetainFragment.mSubRedditItem, true);
					CommentFragmentActivity.this.invalidateActionMode();
				} else {
					Toast.makeText(
							CommentFragmentActivity.this,
							CommentFragmentActivity.this
									.getString(R.string.login_request),
							Toast.LENGTH_SHORT).show();
				}

			}
			if (item.getTitle().equals("Downvote")) {
				if (RedditManager.isUserAuth(CommentFragmentActivity.this)) {
					mCommentRetainFragment.updateSubRedditVote(
							mCommentRetainFragment.mSubRedditItem, false);
					CommentFragmentActivity.this.invalidateActionMode();
				} else {
					Toast.makeText(
							CommentFragmentActivity.this,
							CommentFragmentActivity.this
									.getString(R.string.login_request),
							Toast.LENGTH_SHORT).show();
				}

			}
			if (item.getTitle().equals("Post a comment")) {
				if (RedditManager.isUserAuth(CommentFragmentActivity.this)) {
					if (mPostCommentDialog != null) {
						mPostCommentDialog.dismiss();
					}
					mPostCommentDialog = new PostCommentDialog(
							CommentFragmentActivity.this);
					mPostCommentDialog.setCustomDialogHandler(
							CommentFragmentActivity.this,
							mCommentRetainFragment.mSubRedditItem.name, 0);
					mPostCommentDialog
							.setItem(mCommentRetainFragment.mSubRedditItem);
					mPostCommentDialog.show();
					CommentFragmentActivity.this.invalidateActionMode();
				} else {
					Toast.makeText(
							CommentFragmentActivity.this,
							CommentFragmentActivity.this
									.getString(R.string.login_request),
							Toast.LENGTH_SHORT).show();
				}
			}

			if (item.getTitle().equals("Save post")
					|| item.getTitle().equals("UnSave post")) {
				if (RedditManager.isUserAuth(CommentFragmentActivity.this)) {
					new CommentRetainFragment.SaveTask(mCommentRetainFragment,
							mCommentRetainFragment.getSubRedditItem().name,
							!mCommentRetainFragment.getSubRedditItem().saved)
							.execute();
				} else {
					Toast.makeText(
							CommentFragmentActivity.this,
							CommentFragmentActivity.this
									.getString(R.string.login_request),
							Toast.LENGTH_SHORT).show();
				}
			}

			if (item.getTitle().equals("Hide")
					|| item.getTitle().equals("UnHide")) {
				if (RedditManager.isUserAuth(CommentFragmentActivity.this)) {
					new CommentRetainFragment.HideTask(mCommentRetainFragment,
							mCommentRetainFragment.getSubRedditItem().name,
							!mCommentRetainFragment.getSubRedditItem().hidden)
							.execute();
					CommentFragmentActivity.this.invalidateActionMode();
				} else {
					Toast.makeText(
							CommentFragmentActivity.this,
							CommentFragmentActivity.this
									.getString(R.string.login_request),
							Toast.LENGTH_SHORT).show();
				}
			}

			if (item.getTitle().equals("Profile")) {
				String profileName = mCommentRetainFragment.getSubRedditItem().author;
				Intent profileIntent = new Intent(CommentFragmentActivity.this,
						OverviewFragmentActivity.class);
				profileIntent.putExtra(Common.EXTRA_USERNAME, profileName);
				CommentFragmentActivity.this.startActivity(profileIntent);
			}
			return true;
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
			mActionMode = null;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Used to put dark icons on light action bar
		// boolean isLight = SampleList.THEME == R.style.Theme_Sherlock_Light;
		getSupportMenuInflater().inflate(R.menu.comment_activity_menu, menu);
		// Set file with share history to the provider and set the share intent.
		MenuItem actionItem = menu
				.findItem(R.id.menu_item_share_action_provider_action_bar);
		ShareActionProvider actionProvider = (ShareActionProvider) actionItem
				.getActionProvider();
		actionProvider
				.setShareHistoryFileName(ShareActionProvider.DEFAULT_SHARE_HISTORY_FILE_NAME);
		// Note that you can set/change the intent any time,
		// say when the user has selected an image.
		actionProvider.setShareIntent(createShareIntent());

		// Note that you can set/change the intent any time,
		// say when the user has selected an image.
		// actionProvider.setShareIntent(createShareIntent());
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			Intent backIntent = new Intent();
			backIntent.putExtra(Common.INTENT_EXTRA_SUBREDDIT,
					mCommentRetainFragment.mSubRedditItem);
			this.setResult(Activity.RESULT_OK, backIntent);
			this.finish();
			break;
		case R.id.menu_action_mode:
			showActionMode();
			break;

		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onKeyDown(int keycode, KeyEvent e) {
		switch (keycode) {
		case KeyEvent.KEYCODE_MENU:
			if (!this.getSupportActionBar().isShowing()) {
				this.getSupportActionBar().show();
				if (this.getRetainFragment() != null) {
					this.getRetainFragment().setFullScreen(false);
				}
				return true;
			}
			break;

		case KeyEvent.KEYCODE_BACK:
			if (!this.getSupportActionBar().isShowing()) {
				this.getSupportActionBar().show();
				return true;
			}
			if (mActionMode != null) {
				 this.dismissActionMode();
				 return true;
			} else {
				Intent t = new Intent();
				t.putExtra(Common.INTENT_EXTRA_SUBREDDIT,
						mCommentRetainFragment.mSubRedditItem);
				this.setResult(Activity.RESULT_OK, t);
				this.finish();
				return true;
			}
		}
		return super.onKeyDown(keycode, e);
	}

	public Intent createShareIntent() {
		Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
		sharingIntent.setType("text/plain");
		String shareBody = "http://www.reddit.com"
				+ mCommentRetainFragment.mSubRedditItem.permalink;
		String subject = "Reddit this! "
				+ mCommentRetainFragment.mSubRedditItem.title;
		sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, subject
				+ "\n" + shareBody);
		return sharingIntent;
	}

	// POST COMMENT
	@Override
	public void onViewClick(Dialog customDialog, View v, int position) {
		switch (v.getId()) {

		// edit or post a new comment
		case R.id.comment_send:
			PostCommentDialog cd = (PostCommentDialog) customDialog;
			String text = cd.mContentEtx.getEditableText().toString();
			String things_id = cd.mThing_id;
			if (cd.mIsEdit) {
				mCommentRetainFragment.editComment(position, text);
			} else {
				new CommentRetainFragment.PostCommentTask(
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
			if (mPostCommentDialog != null) {
				mPostCommentDialog.dismiss();
			}
			mPostCommentDialog = new PostCommentDialog(
					CommentFragmentActivity.this);
			mPostCommentDialog.setCustomDialogHandler(
					CommentFragmentActivity.this,
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
	public void onPopCommentClick(View v, SubRedditItem subRedditItem,
			CommentIndex commentItemIndex, int position) {
		switch (v.getId()) {
		case R.id.comment_post_comment:
			// subreddit
			if (position == 0 && subRedditItem != null) {
				if (mPostCommentDialog != null) {
					mPostCommentDialog.dismiss();
				}
				mPostCommentDialog = new PostCommentDialog(
						CommentFragmentActivity.this);
				mPostCommentDialog.setCustomDialogHandler(
						CommentFragmentActivity.this, subRedditItem.name,
						position);
				mPostCommentDialog.setItem(subRedditItem);
				mPostCommentDialog.show();
			}
			if (position != 0 && commentItemIndex != null) {
				if (mPostCommentDialog != null) {
					mPostCommentDialog.dismiss();
				}
				mPostCommentDialog = new PostCommentDialog(
						CommentFragmentActivity.this);
				mPostCommentDialog.setIsEditComment(false);
				mPostCommentDialog.setCustomDialogHandler(
						CommentFragmentActivity.this,
						commentItemIndex.redditComment.name, position);
				mPostCommentDialog.setItem(commentItemIndex.redditComment);
				mPostCommentDialog.show();
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
						CommentFragmentActivity.this);
				mPostCommentDialog.setCustomDialogHandler(
						CommentFragmentActivity.this,
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
				new CommentRetainFragment.SaveTask(mCommentRetainFragment,
						subRedditItem.name, !subRedditItem.saved).execute();
			}

			break;
		case R.id.comment_hide:
			if (position == 0 && subRedditItem != null) {
				new CommentRetainFragment.HideTask(mCommentRetainFragment,
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

			Intent profileIntent = new Intent(CommentFragmentActivity.this,
					OverviewFragmentActivity.class);
			profileIntent.putExtra(Common.EXTRA_USERNAME, profileName);
			CommentFragmentActivity.this.startActivity(profileIntent);

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