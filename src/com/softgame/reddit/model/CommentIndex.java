package com.softgame.reddit.model;

public class CommentIndex {
	public Comment redditComment;
	public int deep;

	public CommentIndex(Comment r, int d) {
		redditComment = r;
		deep = d;
	}
	
	
}
