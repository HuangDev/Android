package com.softgame.reddit.impl;

import android.view.View;

import com.softgame.reddit.dialog.PostCommentDialog;

public interface OnPostCommentListener {
	public void onPostCommentListener(PostCommentDialog dialog, View v,
			int index);
}
