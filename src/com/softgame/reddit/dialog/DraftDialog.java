package com.softgame.reddit.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.CheckBox;

import com.softgame.reddit.R;
import com.softgame.reddit.impl.OnDraftDialogClick;

public class DraftDialog extends Dialog implements OnClickListener {
	Button mSaveButton;
	Button mDiscardButton;
	Button mCancelButton;

	public DraftDialog(Context context) {
		this(context, R.style.TransparentDialogTheme);
	}

	public DraftDialog(Context context, int theme) {
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
		this.setContentView(R.layout.dialog_draft);
		mSaveButton = (Button) this.findViewById(R.id.button_save);
		mDiscardButton = (Button) this.findViewById(R.id.button_discard);
		mCancelButton = (Button)this.findViewById(R.id.button_cancel);
		mSaveButton.setOnClickListener(this);
		mDiscardButton.setOnClickListener(this);
		mCancelButton.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.button_save:
			this.dismiss();
			((OnDraftDialogClick) this.getOwnerActivity()).onDraftClick(
					true, v);
			break;
		case R.id.button_discard:
			this.dismiss();
			((OnDraftDialogClick) this.getOwnerActivity()).onDraftClick(
					false, v);
			break;
		case R.id.button_cancel:
			this.dismiss();
			break;
		}

	}
}
