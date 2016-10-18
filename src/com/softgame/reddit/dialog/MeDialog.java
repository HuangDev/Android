package com.softgame.reddit.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.softgame.reddit.R;
import com.softgame.reddit.utils.RedditManager;

/**
 * login in
 * 
 * @author xinyunxixi
 * 
 */
public class MeDialog extends Dialog implements OnClickListener {

	TextView mNameTxt;
	LinearLayout mProfile;
	LinearLayout mLiked;
	LinearLayout mEmail;
	LinearLayout mMessage;
	LinearLayout mSubmit;
	LinearLayout mLogout;
	Context mContext;
	android.view.View.OnClickListener mListener;

	public MeDialog(Context context) {
		this(context, R.style.TransparentDialogTheme);
	}

	public MeDialog(Context context, int theme) {
		super(context, theme);
		mContext = context;
		mListener = (android.view.View.OnClickListener) context;
		initView();
		// set params
		android.view.WindowManager.LayoutParams params = getWindow()
				.getAttributes();
		params.width = LayoutParams.MATCH_PARENT;
		params.height = LayoutParams.WRAP_CONTENT;
		this.getWindow().setAttributes(params);
		this.setCanceledOnTouchOutside(true);
	}

	private void initView() {
		this.setContentView(R.layout.dialog_me);
		mNameTxt = (TextView) this.findViewById(R.id.me_name);
		mProfile = (LinearLayout) this.findViewById(R.id.me_profile);
		mLiked = (LinearLayout) this.findViewById(R.id.me_liked);
		
		mMessage = (LinearLayout) this.findViewById(R.id.me_message);
		mSubmit = (LinearLayout)this.findViewById(R.id.me_commit);
		mLogout = (LinearLayout) this.findViewById(R.id.me_logout);

		mLiked.setOnClickListener(this);
		mMessage.setOnClickListener(this);
		mLogout.setOnClickListener(this);

		mSubmit.setOnClickListener(this);
		mProfile.setOnClickListener(this);
		mLiked.setOnClickListener(this);
		mMessage.setOnClickListener(this);
		mLogout.setOnClickListener(this);
	}

	@Override
	protected void onStart() {
		super.onStart();
		mNameTxt.setText(RedditManager.getUserName(mContext));
	}

	@Override
	public void onClick(View v) {
		if (mListener != null) {
			mListener.onClick(v);
		}
		this.dismiss();
	}
}
