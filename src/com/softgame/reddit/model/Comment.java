package com.softgame.reddit.model;

import java.util.ArrayList;

import org.json.custom.JSONArray;
import org.json.custom.JSONException;
import org.json.custom.JSONObject;

import com.softgame.reddit.utils.CommonUtil;

import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.util.Linkify;
import android.util.Log;

public class Comment {
	public String kind; // t1 mean comment
	// more only have id and name
	public String body;
	public String subreddit_id;
	public long created_utc;
	public String author;
	// public String body_html;
	public Boolean likes; // true false null
	public String link_id;
	public String parent_id;
	public long downs;
	public long ups;
	public String name;// t1_c3skarx
	public String id; // c3skarx;
	private Spanned bodyMark;
	public CharSequence bodyMarkProcess;

	// the likes to scroll back the original value
	public Boolean old_likes;
	public String old_body;
	public String old_author;

	// show this in the list view
	public boolean show = true;

	// do show the children
	public void hideItSelf() {
		show = false;
	}

	public int getChildSize() {
		int size = 0;
		if (repliesList != null) {
			size = repliesList.size();
			for (int i = 0; i < repliesList.size(); i++) {
				size = size + repliesList.get(i).getChildSize();
			}
		}
		return size;

	}

	public String subreddit; // pics;

	public ArrayList<Comment> repliesList; // null means no replies;

	// "" mean no replies or use get children to get the data;

	public Comment() {
		kind = "";
		body = "";
		subreddit_id = "";
		created_utc = System.currentTimeMillis();
	}

	public Comment(JSONObject joo) {

		kind = joo.optString("kind");
		// ----------get the really data of information
		JSONObject jo = joo.optJSONObject("data");
		if (jo != null) {
			if (kind.equals("t1")) {
				body = jo.optString("body", "");
				// bodyMark = Html.fromHtml(CommonUtil.getMarkDown()
				// .markdown(body));

				bodyMark = Html.fromHtml(CommonUtil.getRedditETMarkDown()
						.markdownToHtml(body));

				
				bodyMarkProcess = processMark(bodyMark);
				if(bodyMarkProcess == null){
					bodyMarkProcess = bodyMark.toString();	
				}
				
				subreddit_id = jo.optString("subreddit_id", "");
				created_utc = jo.optLong("created_utc",
						System.currentTimeMillis());
				downs = jo.optLong("downs");
				author = jo.optString("author");
				// body_html = jo.optString("body_html");
				link_id = jo.optString("link_id");
				parent_id = jo.optString("parent_id");
				likes = jo.optBooleanIncludeNull("likes");
				id = jo.optString("id");
				subreddit = jo.optString("subreddit");
				ups = jo.optLong("ups");
				name = jo.optString("name");

				JSONObject replies = jo.optJSONObject("replies");
				if (replies == null || replies.isEmpty()
						|| replies.optJSONObject("data") == null) {
					repliesList = null; // mean no replies;
				} else {
					repliesList = new ArrayList<Comment>();
					JSONArray jd = replies.optJSONObject("data").optJSONArray(
							"children");
					for (int i = 0; i < jd.length(); i++) {
						JSONObject jobject = jd.optJSONObject(i);
						if (jobject != null) {
							Comment rc = new Comment(jobject);
							repliesList.add(rc);
						}
					}
				}
			}

			if (kind.equals("more")) {
				id = jo.optString("id");
				name = jo.optString("name");
				repliesList = null;
			}
		}
	}

	public static SpannableString processMark(Spanned st) {
		try {
			CharSequence target = "";
			if (st.length() > 2) {
				target = st.subSequence(0, st.length() - 2);
			} else {
				return null;
			}
			SpannableString ss = SpannableString.valueOf(target);
			Linkify.addLinks(ss, Linkify.WEB_URLS);
			return ss;
		} catch (Exception e) {
			Log.d("CommentDecode", "comment decode string fail");
			return null;
		}
	}

	public void countDeep(ArrayList<CommentIndex> redditCommentIndexList,
			int deep) {
		if (this.kind.equals("more")) {
			return;
		}
		// add it self to the list first.
		CommentIndex ri = new CommentIndex(this, deep);
		redditCommentIndexList.add(ri);
		// check has the replies, if yes, add it and trace it is replies again;
		if (repliesList != null && show) {
			for (int i = 0; i < repliesList.size(); i++) {
				// get the first one and add it to the list
				Comment r = repliesList.get(i);
				r.countDeep(redditCommentIndexList, deep + 1);
			}
		}
	}

}
