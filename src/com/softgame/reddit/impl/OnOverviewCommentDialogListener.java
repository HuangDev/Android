package com.softgame.reddit.impl;

import android.view.View;

import com.softgame.reddit.model.OverviewItem;

public interface OnOverviewCommentDialogListener {

	public void onOverviewCommentDialogSelected(View v,
			OverviewItem overviewItem, int position);
}
