package com.softgame.reddit.dialog;

import java.lang.ref.WeakReference;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.softgame.reddit.R;
import com.softgame.reddit.impl.OnPopCommentListener;
import com.softgame.reddit.model.Comment;
import com.softgame.reddit.model.CommentIndex;
import com.softgame.reddit.model.SubRedditItem;
import com.softgame.reddit.utils.RedditManager;

/**
 * login in
 * 
 * @author xinyunxixi
 * 
 */
public class CommentPopDialog extends Dialog implements OnClickListener {

	LinearLayout mComment;
	LinearLayout mVoteUp;
	LinearLayout mVoteDown;
	ImageView mVoteUpIcon;
	ImageView mVoteDownIcon;
	LinearLayout mSave;
	TextView mSaveText;
	LinearLayout mHide;
	TextView mHideText;
	LinearLayout mProfile;
	LinearLayout mDeleteComment;
	LinearLayout mEditComment;

	WeakReference<CommentIndex> commentItemIndexWF;
	WeakReference<SubRedditItem> subRedditItemWF;
	OnPopCommentListener popCommentListener;
	int position;

	boolean mFromLoginUser = false;

	public CommentPopDialog(Context context) {
		this(context, R.style.TransparentDialogTheme);
	}

	public CommentPopDialog(Context context, int theme) {
		super(context, theme);
		// set params
		android.view.WindowManager.LayoutParams params = getWindow()
				.getAttributes();
		params.width = LayoutParams.MATCH_PARENT;
		params.height = LayoutParams.WRAP_CONTENT;
		this.getWindow().setAttributes(params);
		this.setCanceledOnTouchOutside(true);
		popCommentListener = (OnPopCommentListener) context;
		initView();
	}

	private void initView() {

		this.setContentView(R.layout.dialog_pop_comment);

		mComment = (LinearLayout) this.findViewById(R.id.comment_post_comment);
		mVoteUp = (LinearLayout) this.findViewById(R.id.comment_vote_up);
		mVoteDown = (LinearLayout) this.findViewById(R.id.comment_vote_down);
		mVoteUpIcon = (ImageView) this.findViewById(R.id.comment_vote_up_icon);
		mVoteDownIcon = (ImageView) this
				.findViewById(R.id.comment_vote_down_icon);

		mDeleteComment = (LinearLayout) this.findViewById(R.id.comment_delete);
		mEditComment = (LinearLayout) this.findViewById(R.id.comment_edit);
		mSave = (LinearLayout) this.findViewById(R.id.comment_save);
		mSaveText = (TextView) this.findViewById(R.id.comment_save_text);
		mHide = (LinearLayout) this.findViewById(R.id.comment_hide);
		mHideText = (TextView) this.findViewById(R.id.comment_hide_text);
		mProfile = (LinearLayout) this.findViewById(R.id.comment_profile);

		mComment.setOnClickListener(this);
		mEditComment.setOnClickListener(this);
		mDeleteComment.setOnClickListener(this);
		mVoteUp.setOnClickListener(this);
		mVoteDown.setOnClickListener(this);
		mSave.setOnClickListener(this);
		mHide.setOnClickListener(this);
		mProfile.setOnClickListener(this);
	}

	public void setIsFromUser(boolean fromLoginUser) {
		mFromLoginUser = fromLoginUser;
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	// for position == 0
	public void setSubRedditItem(SubRedditItem subRedditItem, int p) {
		mDeleteComment.setVisibility(View.GONE);
		this.findViewById(R.id.comment_delete_divider).setVisibility(View.GONE);
		mEditComment.setVisibility(View.GONE);
		this.findViewById(R.id.comment_edit_divider).setVisibility(View.GONE);

		position = p;
		subRedditItemWF = new WeakReference<SubRedditItem>(subRedditItem);
		if (RedditManager.isUserAuth(this.getContext())) {
			mComment.setVisibility(View.VISIBLE);
			mVoteUp.setVisibility(View.VISIBLE);
			mVoteDown.setVisibility(View.VISIBLE);
			if (subRedditItem.likes == null) {
				mVoteUpIcon.setImageResource(R.drawable.vote_up_grey);
				mVoteDownIcon.setImageResource(R.drawable.vote_down_grey);
			} else {
				mVoteUpIcon
						.setImageResource(subRedditItem.likes ? R.drawable.vote_up_selected
								: R.drawable.vote_up_grey);
				mVoteDownIcon
						.setImageResource(subRedditItem.likes ? R.drawable.vote_down_grey
								: R.drawable.vote_down_selected);
			}
			mSave.setVisibility(View.VISIBLE);
			mSaveText.setText(this.getContext().getString(
					subRedditItem.saved ? R.string.comment_pop_unsave
							: R.string.comment_pop_save));
			mHide.setVisibility(View.VISIBLE);
			mHideText.setText(this.getContext().getString(
					subRedditItem.hidden ? R.string.comment_pop_unhide
							: R.string.comment_pop_hide));

		} else {
			mComment.setVisibility(View.GONE);
			this.findViewById(R.id.comment_post_comment_divider).setVisibility(
					View.GONE);

			mVoteUp.setVisibility(View.GONE);
			this.findViewById(R.id.comment_vote_up_divider).setVisibility(
					View.GONE);
			mVoteDown.setVisibility(View.GONE);
			this.findViewById(R.id.comment_vote_down_divider).setVisibility(
					View.GONE);

			if (subRedditItem.likes == null) {
				mVoteUpIcon.setImageResource(R.drawable.vote_up_grey);
				mVoteDownIcon.setImageResource(R.drawable.vote_down_grey);
			} else {
				mVoteUpIcon
						.setImageResource(subRedditItem.likes ? R.drawable.vote_up_selected
								: R.drawable.vote_up_grey);
				mVoteDownIcon
						.setImageResource(subRedditItem.likes ? R.drawable.vote_down_grey
								: R.drawable.vote_down_selected);
			}
			mSave.setVisibility(View.GONE);
			this.findViewById(R.id.comment_save_divider).setVisibility(
					View.GONE);
			mHide.setVisibility(View.GONE);
			this.findViewById(R.id.comment_hide_divider).setVisibility(
					View.GONE);
		}

	}

	public void setCommentItem(CommentIndex commentItemIndex, int p) {

		Comment commentItem = commentItemIndex.redditComment;
		position = p;
		commentItemIndexWF = new WeakReference<CommentIndex>(commentItemIndex);
		mSave.setVisibility(View.GONE);
		this.findViewById(R.id.comment_save_divider).setVisibility(View.GONE);
		mHide.setVisibility(View.GONE);
		this.findViewById(R.id.comment_hide_divider).setVisibility(View.GONE);

		if (RedditManager.isUserAuth(this.getContext())) {
			mComment.setVisibility(View.VISIBLE);
			mVoteUp.setVisibility(View.VISIBLE);
			mVoteDown.setVisibility(View.VISIBLE);
			mDeleteComment.setVisibility(mFromLoginUser ? View.VISIBLE
					: View.GONE);

			this.findViewById(R.id.comment_delete_divider).setVisibility(
					mFromLoginUser ? View.VISIBLE : View.GONE);

			mDeleteComment.setVisibility(mFromLoginUser ? View.VISIBLE
					: View.GONE);
			this.findViewById(R.id.comment_delete_divider).setVisibility(
					mFromLoginUser ? View.VISIBLE : View.GONE);

			mEditComment.setVisibility(mFromLoginUser ? View.VISIBLE
					: View.GONE);
			this.findViewById(R.id.comment_edit_divider).setVisibility(
					mFromLoginUser ? View.VISIBLE : View.GONE);

			if (commentItem.likes == null) {
				mVoteUpIcon.setImageResource(R.drawable.vote_up_grey);
				mVoteDownIcon.setImageResource(R.drawable.vote_down_grey);
			} else {
				mVoteUpIcon
						.setImageResource(commentItem.likes ? R.drawable.vote_up_selected
								: R.drawable.vote_up_grey);
				mVoteDownIcon
						.setImageResource(commentItem.likes ? R.drawable.vote_down_grey
								: R.drawable.vote_down_selected);
			}

		} else {

			mDeleteComment.setVisibility(View.GONE);
			this.findViewById(R.id.comment_delete_divider).setVisibility(
					View.GONE);
			mEditComment.setVisibility(View.GONE);
			this.findViewById(R.id.comment_edit_divider).setVisibility(
					View.GONE);

			mComment.setVisibility(View.GONE);
			this.findViewById(R.id.comment_post_comment_divider).setVisibility(
					View.GONE);

			mVoteUp.setVisibility(View.GONE);

			this.findViewById(R.id.comment_vote_up_divider).setVisibility(
					View.GONE);

			mVoteDown.setVisibility(View.GONE);
			this.findViewById(R.id.comment_vote_down_divider).setVisibility(
					View.GONE);

		}

	}

	@Override
	public void onClick(View v) {

		if ((commentItemIndexWF == null || commentItemIndexWF.get() == null)
				&& (subRedditItemWF == null || subRedditItemWF.get() == null)) {
			// data is missing.
			this.dismiss();
			return;
		} else {
			popCommentListener.onPopCommentClick(
					v,
					subRedditItemWF == null ? null : subRedditItemWF.get(),
					commentItemIndexWF == null ? null : commentItemIndexWF
							.get(), position);
			this.dismiss();
		}
	}
}
