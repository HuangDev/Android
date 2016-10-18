package com.softgame.reddit.dialog;

import java.lang.ref.WeakReference;

import android.app.Dialog;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.softgame.reddit.R;
import com.softgame.reddit.impl.OnOverviewCommentDialogListener;
import com.softgame.reddit.model.OverviewItem;
import com.softgame.reddit.utils.RedditManager;

/**
 * login in
 * 
 * @author xinyunxixi
 * 
 */
public class OverviewCommentPopDialog extends Dialog implements OnClickListener {

	LinearLayout mAllComment;
	LinearLayout mVoteUp;
	LinearLayout mVoteDown;
	ImageView mVoteUpIcon;
	ImageView mVoteDownIcon;

	WeakReference<OverviewItem> commentItemWF;
	OnOverviewCommentDialogListener popCommentListener;
	int position;

	public OverviewCommentPopDialog(Fragment fragment) {
		this(fragment, R.style.TransparentDialogTheme);
	}

	public OverviewCommentPopDialog(Fragment fragment, int theme) {
		super(fragment.getActivity(), theme);
		// set params
		android.view.WindowManager.LayoutParams params = getWindow()
				.getAttributes();
		params.width = LayoutParams.MATCH_PARENT;
		params.height = LayoutParams.WRAP_CONTENT;
		this.getWindow().setAttributes(params);
		this.setCanceledOnTouchOutside(true);

		try {
			popCommentListener = (OnOverviewCommentDialogListener) fragment;
		} catch (ClassCastException e) {
			e.printStackTrace();
			this.dismiss();
		}
		initView();

	}

	private void initView() {

		this.setContentView(R.layout.dialog_overview_pop_comment);

		mAllComment = (LinearLayout) this
				.findViewById(R.id.overview_all_comment);
		mVoteUp = (LinearLayout) this.findViewById(R.id.comment_vote_up);
		mVoteDown = (LinearLayout) this.findViewById(R.id.comment_vote_down);
		mVoteUpIcon = (ImageView) this.findViewById(R.id.comment_vote_up_icon);
		mVoteDownIcon = (ImageView) this
				.findViewById(R.id.comment_vote_down_icon);

		mAllComment.setOnClickListener(this);
		mVoteUp.setOnClickListener(this);
		mVoteDown.setOnClickListener(this);
	}

	public OverviewCommentPopDialog setOverviewItemItem(
			OverviewItem overviewItem, int p) {
		position = p;
		commentItemWF = new WeakReference<OverviewItem>(overviewItem);

		if (RedditManager.isUserAuth(this.getContext())) {
			mVoteUp.setVisibility(View.VISIBLE);
			mVoteDown.setVisibility(View.VISIBLE);
			if (overviewItem.likes == null) {
				mVoteUpIcon.setImageResource(R.drawable.vote_up_grey);
				mVoteDownIcon.setImageResource(R.drawable.vote_down_grey);
			} else {
				mVoteUpIcon
						.setImageResource(overviewItem.likes ? R.drawable.vote_up_selected
								: R.drawable.vote_up_grey);
				mVoteDownIcon
						.setImageResource(overviewItem.likes ? R.drawable.vote_down_grey
								: R.drawable.vote_down_selected);
			}

		} else {
			mVoteUp.setVisibility(View.GONE);
			this.findViewById(R.id.comment_vote_up_divider).setVisibility(
					View.GONE);

			mVoteDown.setVisibility(View.GONE);
			this.findViewById(R.id.comment_vote_down_divider).setVisibility(
					View.GONE);

		}

		return this;
	}

	@Override
	public void onClick(View v) {

		if (commentItemWF != null && commentItemWF.get() != null) {
			popCommentListener.onOverviewCommentDialogSelected(v,
					commentItemWF.get(), position);
			this.dismiss();
		}
	}
}
