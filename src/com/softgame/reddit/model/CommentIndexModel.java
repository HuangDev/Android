package com.softgame.reddit.model;

import java.util.ArrayList;

public class CommentIndexModel {
	// position O : subreddit
	// position 1 : sort type
	// position 2: CommentIndex

	public SubRedditItem mSubRedditItem;
	public ArrayList<CommentIndex> mCommentIndexList;

	public CommentIndexModel() {
		mCommentIndexList = new ArrayList<CommentIndex>();
	}

	public CommentIndexModel(SubRedditItem subRedditItem) {
		mSubRedditItem = subRedditItem;
		mCommentIndexList = new ArrayList<CommentIndex>();
	}

	public void convertToCommentIndex(CommentModel commentModel) {
		mCommentIndexList.clear();
		int deep = 0;
		for (int i = 0; i < commentModel.mCommentList.size(); i++) {
			deep = 0;
			Comment r = commentModel.mCommentList.get(i);
			r.countDeep(mCommentIndexList, deep);
		}
	}

	public void setSubRedditItem(SubRedditItem subRedditItem) {
		mSubRedditItem = subRedditItem;
		mCommentIndexList = new ArrayList<CommentIndex>();
	}

	public int getSize() {

		if (mSubRedditItem == null) {
			return 0;
		}

		// size + 1 subreddit
		return mCommentIndexList.size() + 1;
	}

	public int getCommentSize() {
		return mCommentIndexList.size();
	}

	public void clearList() {
		mCommentIndexList.clear();
	}

	public Object getItem(int position) {
		if (position == 0) {
			return mSubRedditItem;
		} else {
			if (position > 1 && position < getCommentSize() + 2) {
				return mCommentIndexList.get(position - 2);
			} else {
				return null;
			}
		}

	}
	/**
	 * I NOTE:only for CommentRetainFragment
	 * @param item
	 * @param position
	 */
	public void deleteItem(int position) {
		if (position == 0) {
		} else {
			if (position > 1 && position < getCommentSize() + 2) {
				mCommentIndexList.remove(position - 2);
			}
		}
	}
/**
 * I NOTE:only for CommentRetainFragment
 * @param item
 * @param position
 */
	public void unDeleteItem(CommentIndex item,int position){
		if (position == 0) {
		} else {
			//add 1 more, if it is last, sometimes it is last
			if (position > 1 && position <= getCommentSize() + 2) {
				mCommentIndexList.add(position - 2, item);
			}
		}
	}
	
	/**
	 * NOTE: only for OverviewCommentFragment 
	 * @param position in Adapter
	 */
	public void deleteOverviewItem(int position) {
		if (position == 0) {
		} else {
			if (position > 2 && position < getCommentSize() + 3) {
				mCommentIndexList.remove(position - 3);
			}
		}
	}
	/**
	 * I NOTE:only for OverviewCommentFragment
	 * @param item
	 * @param position
	 */
	public void unDeleteOverviewItem(CommentIndex item,int position){
		if (position == 0) {
		} else {
			//add 1 more, if it is last, sometimes it is last
			if (position > 2 && position <= getCommentSize() + 3) {
				mCommentIndexList.add(position - 3, item);
			}
		}
	}
	
	/*
	 * only for Overview Comment  LOAD_TYPE_CONTEXT_COMMENT
	 */
	public Object getOverviewItem(int position) {
		if (position == 0) {
			return mSubRedditItem;
		} else {
			if (position > 2 && position < getCommentSize() + 3) {
				return mCommentIndexList.get(position - 3);
			} else {
				return null;
			}
		}
	}

	
	public Comment getCommentItem(int position) {
		if (position > 1 && position < getCommentSize() + 2) {
			return mCommentIndexList.get(position - 2).redditComment;
		}
		return null;
	}
}
