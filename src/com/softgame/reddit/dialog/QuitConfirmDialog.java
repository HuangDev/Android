package com.softgame.reddit.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
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
public class QuitConfirmDialog extends Dialog implements OnClickListener {
	Button mYesButton;
	Button mNoButton;

	public QuitConfirmDialog(Context context) {
		this(context, R.style.TransparentDialogTheme);
	}

	public QuitConfirmDialog(Context context, int theme) {
		super(context, theme);
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
		this.setContentView(R.layout.dialog_quit);

		mYesButton = (Button) this.findViewById(R.id.button_yes);
		mNoButton = (Button) this.findViewById(R.id.button_no);
		mYesButton.setOnClickListener(this);
		mNoButton.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.button_yes:
			this.dismiss();
			this.getOwnerActivity().finish();
			break;
		case R.id.button_no:
			this.dismiss();
			break;
		}

	}
}
