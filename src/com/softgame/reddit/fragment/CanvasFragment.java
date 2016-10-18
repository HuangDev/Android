package com.softgame.reddit.fragment;

import com.softgame.reddit.CommentFragmentActivity;
import com.softgame.reddit.R;
import com.softgame.reddit.model.SubRedditItem;
import com.softgame.reddit.utils.Common;
import com.softgame.reddit.utils.CommonUtil;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public abstract class CanvasFragment extends Fragment implements
		OnClickListener {

	protected SubRedditItem mSubRedditItem;

	protected void setUpContentView(View convertView, SubRedditItem item) {

		TextView subreddit_info_time = (TextView) convertView
				.findViewById(R.id.comment_info_date);

		subreddit_info_time.setText(CommonUtil.getRelateTimeString(
				item.created_utc * 1000, this.getActivity()));

		TextView cis = (TextView) convertView
				.findViewById(R.id.comment_info_subreddit);
		cis.setText(item.subreddit);

		TextView subreddit_info_nsfw = (TextView) convertView
				.findViewById(R.id.comment_info_nsfw);
		if (item.over_18) {
			subreddit_info_nsfw.setVisibility(View.VISIBLE);
		} else {
			subreddit_info_nsfw.setVisibility(View.GONE);
		}

		// comments counts

		TextView info_count = (TextView) convertView
				.findViewById(R.id.comment_info_comments_count);
		info_count.setText(item.num_comments + " "
				+ this.getString(R.string.label_comments));

		TextView score = (TextView) convertView.findViewById(R.id.vote_score);
		if (item.score >= 0) {
			score.setText("+" + item.score);
		} else {
			score.setText(item.score + "");
		}

		convertView.findViewById(R.id.button_comments).setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.button_comments:
			Intent t = new Intent(this.getActivity(),
					CommentFragmentActivity.class);
			t.putExtra(Common.INTENT_EXTRA_SUBREDDIT, mSubRedditItem);
			this.getActivity().startActivity(t);
			break;
		}
	}

}
