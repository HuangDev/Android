package com.softgame.reddit.impl;

import android.view.View;

import com.softgame.reddit.model.Comment;
import com.softgame.reddit.model.CommentIndex;
import com.softgame.reddit.model.SubRedditItem;

public interface OnPopCommentListener {
	public void onPopCommentClick(View v, SubRedditItem subRedditItem,
			CommentIndex commentIndexItem, int position);
}
