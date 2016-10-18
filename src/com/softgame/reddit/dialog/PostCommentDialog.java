package com.softgame.reddit.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.softgame.reddit.R;
import com.softgame.reddit.model.Comment;
import com.softgame.reddit.model.SubRedditItem;
import com.softgame.reddit.utils.CommonUtil;

public class PostCommentDialog extends Dialog implements OnClickListener {
	public CustomDialogHandler mHandler;
	public ImageButton mPostBtn;
	public EditText mContentEtx;
	public String mThing_id;
	public int mPosition;
	public Comment mCommentItem;
	public SubRedditItem mSubRedditItem;
	public boolean mIsEdit = false;

	public PostCommentDialog(Context context) {
		this(context, R.style.TransparentDialogTheme_NoDim);
	}

	public PostCommentDialog(Context context, int theme) {
		super(context, theme);
		android.view.WindowManager.LayoutParams params = getWindow()
				.getAttributes();
		params.width = LayoutParams.MATCH_PARENT;
		params.height = LayoutParams.MATCH_PARENT;
		params.gravity = Gravity.BOTTOM;
		this.getWindow().setAttributes(params);
		this.setCanceledOnTouchOutside(true);
		initView();
	}

	public void setItem(Comment i) {
		mCommentItem = i;
		initCommentView();
	}

	public void setItem(SubRedditItem s) {
		mSubRedditItem = s;
		initSubRedditView();
	}
	
	public void setIsEditComment(boolean isEdit){
		mIsEdit = isEdit;
	}

	
	public void setInputText(String input){
		mContentEtx.setText(input);
		mContentEtx.requestFocus();
	}
	private void initSubRedditView() {
		TextView iauthor = (TextView) this.findViewById(R.id.comment_author);
		iauthor.setText(mSubRedditItem.author);

		TextView itime = (TextView) this.findViewById(R.id.comment_time);

		TextView iscore = (TextView) this.findViewById(R.id.comment_score);

		itime.setText(CommonUtil.getRelateTimeString(
				mSubRedditItem.created_utc * 1000, this.getContext()));

		if (mSubRedditItem.score > 0)
			iscore.setText("+" + mSubRedditItem.score);
		else {
			iscore.setText(mSubRedditItem.score + "");
		}
		TextView comment = (TextView) this.findViewById(R.id.comment);

		comment.setText(mSubRedditItem.title);
		comment.setFocusable(false);
		comment.setFocusableInTouchMode(false);
	}

	private void initCommentView() {

		TextView iauthor = (TextView) this.findViewById(R.id.comment_author);
		iauthor.setText(mCommentItem.author);

		TextView itime = (TextView) this.findViewById(R.id.comment_time);

		TextView iscore = (TextView) this.findViewById(R.id.comment_score);

		itime.setText(CommonUtil.getRelateTimeString(
				mCommentItem.created_utc * 1000, this.getContext()));
		long s = mCommentItem.ups - mCommentItem.downs;
		if (s > -1)
			iscore.setText("+" + s);
		else {
			iscore.setText("" + s);
		}

		TextView comment = (TextView) this.findViewById(R.id.comment);
        if(mIsEdit){
        	comment.setVisibility(View.GONE);
        	setInputText(mCommentItem.body);
        }else{
        	comment.setVisibility(View.VISIBLE);
    		comment.setText(mCommentItem.bodyMarkProcess);
        }

		comment.setFocusable(false);
		comment.setFocusableInTouchMode(false);

	}

	public PostCommentDialog(Activity context, boolean cancelable,
			OnCancelListener cancelListener) {
		super(context, cancelable, cancelListener);
		initView();
	}

	public void setCustomDialogHandler(CustomDialogHandler r, String thing_id,
			int p) {
		mThing_id = thing_id;
		mHandler = r;
		mPosition = p;
	}

	/**
	 * init Data
	 */
	private void initView() {
		super.setContentView(R.layout.dialog_post_comment);
		mContentEtx = (EditText) this.findViewById(R.id.comment_input);
		mPostBtn = (ImageButton) this.findViewById(R.id.comment_send);
		mPostBtn.setOnClickListener(this);
		mContentEtx.requestFocus();
	}

	public String getInputText() {
		if (mContentEtx.getEditableText() != null) {
			return mContentEtx.getEditableText().toString().trim();
		} else {
			return "";
		}

	}

	@Override
	public void onClick(View v) {
		if(getInputText() == null || "".equals(getInputText())){
			Toast.makeText(this.getContext(),"Input request!", Toast.LENGTH_SHORT).show();
			return;
		}
		if(mIsEdit && mCommentItem != null){
			if(getInputText().equals(mCommentItem.body)){
				//nothing change
				Toast.makeText(this.getContext(),"No edit text change!", Toast.LENGTH_SHORT).show();
				return;
			}
		}
		mHandler.onViewClick(this, v, mPosition);
	}

}
