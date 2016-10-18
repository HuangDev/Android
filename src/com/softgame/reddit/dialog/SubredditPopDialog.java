package com.softgame.reddit.dialog;

import java.lang.ref.WeakReference;

import org.json.custom.JSONObject;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.softgame.reddit.R;
import com.softgame.reddit.impl.OnSubredditPopSelectedListener;
import com.softgame.reddit.model.SubRedditItem;

// a dialog to show the user the pop, can only be used to SubRedditActivity
public class SubredditPopDialog extends Dialog implements OnClickListener {

	public static final int ACTION_VOTE_UP = 0x111;
	public static final int ACTION_VOTE_DOWN = 0x222;
	public static final int ACTION_SAVE = 0x333;
	public static final int ACTION_UNSAVE = 0x444;
	public static final int ACTION_UNHIDE = 0x555;
	public static final int ACTION_HIDE = 0x666;
	public static final int ACTION_PROFILE = 0x777;

	LinearLayout mVoteUp;
	LinearLayout mVoteDown;
	LinearLayout mSave;
	LinearLayout mHide;
	LinearLayout mProfile;
	ImageView mVoteUpImage;
	ImageView mVoteDownImage;

	TextView mSaveText;
	TextView mHideText;

	boolean save;
	boolean hide;

	int position;

	WeakReference<OnSubredditPopSelectedListener> mListenerWF;

	public SubredditPopDialog(Context context) {
		this(context, R.style.TransparentDialogTheme);
	}

	public SubredditPopDialog(Context context, int theme) {
		super(context, theme);
		// set params
		android.view.WindowManager.LayoutParams params = getWindow()
				.getAttributes();
		params.width = LayoutParams.MATCH_PARENT;
		params.height = LayoutParams.WRAP_CONTENT;
		this.getWindow().setAttributes(params);
		this.setCanceledOnTouchOutside(true);
		initView();
	}

	public void setOnSubredditPopSelectedListener(
			OnSubredditPopSelectedListener listener) {
		mListenerWF = new WeakReference<OnSubredditPopSelectedListener>(
				listener);
	}

	/**
	 * init Data
	 */
	private void initView() {
		super.setContentView(R.layout.dialog_subreddit_pop);
		mVoteUp = (LinearLayout) this.findViewById(R.id.subreddit_vote_up);
		mVoteDown = (LinearLayout) this.findViewById(R.id.subreddit_vote_down);
		mSave = (LinearLayout) this.findViewById(R.id.subreddit_save);
		mHide = (LinearLayout) this.findViewById(R.id.subreddit_hide);
		mProfile = (LinearLayout) this.findViewById(R.id.subreddit_profile);

		mVoteUpImage = (ImageView) this.findViewById(R.id.vote_up_image);
		mVoteDownImage = (ImageView) this.findViewById(R.id.vote_down_image);

		mSaveText = (TextView) this.findViewById(R.id.subreddit_save_text);
		mHideText = (TextView) this.findViewById(R.id.subreddit_hide_text);

		mVoteUp.setOnClickListener(this);
		mVoteDown.setOnClickListener(this);
		mSave.setOnClickListener(this);
		mHide.setOnClickListener(this);
		mProfile.setOnClickListener(this);

	}

	public void setSubRedditItem(SubRedditItem item, int p) {
		// set position
		position = p;
		if (item.likes == null) {
			mVoteUpImage.setImageResource(R.drawable.vote_up_grey);
			mVoteDownImage.setImageResource(R.drawable.vote_down_grey);
		} else if (item.likes) {
			mVoteUpImage.setImageResource(R.drawable.vote_up_selected);
			mVoteDownImage.setImageResource(R.drawable.vote_down_grey);
		} else {
			mVoteUpImage.setImageResource(R.drawable.vote_up_grey);
			mVoteDownImage.setImageResource(R.drawable.vote_down_selected);
		}

		save = item.saved;
		mSaveText.setText(item.saved ? getContext().getString(
				R.string.label_unsave) : getContext().getString(
				R.string.label_save));

		hide = item.hidden;
		mHideText.setText(item.hidden ? getContext().getString(
				R.string.label_unhide) : getContext().getString(
				R.string.label_hide));
	}

	@Override
	public void onClick(View v) {
		int action = ACTION_VOTE_UP;
		switch (v.getId()) {
		case R.id.subreddit_vote_up:
			action = ACTION_VOTE_UP;
			break;
		case R.id.subreddit_vote_down:
			action = ACTION_VOTE_DOWN;
			break;
		case R.id.subreddit_save:
			action = save ? ACTION_UNSAVE : ACTION_SAVE;
			break;
		case R.id.subreddit_hide:
			action = hide ? ACTION_UNHIDE : ACTION_HIDE;
			break;
		case R.id.subreddit_profile:
			action = ACTION_PROFILE;
			break;
		}

		if (mListenerWF != null && mListenerWF.get() != null) {
			mListenerWF.get().onSubredditPopSelected(position, action);
		}
		this.dismiss();
	}

}
