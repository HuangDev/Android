package com.softgame.reddit.model;

import java.util.ArrayList;

import org.json.custom.JSONArray;
import org.json.custom.JSONException;
import org.json.custom.JSONObject;

/**
 * this is SubRedditItem with CommentList
 * 
 * @author xinyunxixi
 * 
 */
public class CommentModel {
	public ArrayList<Comment> mCommentList;
	public SubRedditItem mSubRedditItem;

	public CommentModel() {
		mCommentList = new ArrayList<Comment>();
	}

	/**
	 * init the CommentList (unsort)
	 * 
	 * @param subRedditItem
	 */
	public CommentModel(SubRedditItem subRedditItem) {
		mSubRedditItem = subRedditItem;
		mCommentList = new ArrayList<Comment>();
	}

	public void clearList() {
		mCommentList.clear();
	}

	public void setCommentList(JSONArray infoJSON) {
		try {

			if (mSubRedditItem == null) {
				JSONObject subredditItem = infoJSON.optJSONObject(0)
						.optJSONObject("data").optJSONArray("children")
						.optJSONObject(0);
				mSubRedditItem = new SubRedditItem(subredditItem);
			}
			mCommentList.clear();
			JSONObject childComment = infoJSON.optJSONObject(1);
			if (childComment == null) {
				return;
			}
			JSONArray commentJSONArray = childComment.getJSONObject("data")
					.getJSONArray("children");
			setData(commentJSONArray);

		} catch (JSONException e) {
			e.printStackTrace();
			mCommentList = null;
		}

	}

	public void addComment(int index, Comment c) {
		mCommentList.add(index, c);
	}

	public void removeComment(int index) {
		mCommentList.remove(index);
	}

	private void setData(JSONArray commentJSONArray) throws JSONException {
		for (int i = 0; i < commentJSONArray.length(); i++) {
			JSONObject jo = commentJSONArray.getJSONObject(i);
			Comment rc = new Comment(jo);
			mCommentList.add(rc);
		}

	}

}
