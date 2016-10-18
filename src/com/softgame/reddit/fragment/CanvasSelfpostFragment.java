package com.softgame.reddit.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.softgame.reddit.CommentFragmentActivity;
import com.softgame.reddit.R;
import com.softgame.reddit.model.SubRedditItem;
import com.softgame.reddit.utils.Common;
import com.softgame.reddit.utils.CommonUtil;

public class CanvasSelfpostFragment extends CanvasFragment {

	TextView mTitle;
	TextView mBody;

	public CanvasSelfpostFragment() {
	}

	public static CanvasSelfpostFragment newInstance(SubRedditItem s) {
		CanvasSelfpostFragment f = new CanvasSelfpostFragment();
		Bundle args = new Bundle();
		args.putParcelable("key_subreddit_item", s);
		f.setArguments(args);
		return f;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setRetainInstance(true);
		if (this.getArguments() != null) {
			mSubRedditItem = this.getArguments().getParcelable(
					"key_subreddit_item");
		}
		if (mSubRedditItem == null) {
			Toast.makeText(getActivity(), "Post missing!", Toast.LENGTH_SHORT)
					.show();
			return;
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.item_canvas_selfpost_fragment,
				container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		super.setUpContentView(this.getView(), mSubRedditItem);
		mTitle = (TextView) this.getView().findViewById(R.id.title);
		mBody = (TextView) this.getView().findViewById(R.id.body);
		mBody.setText(mSubRedditItem.getSelftext());
		mTitle.setText(mSubRedditItem.title);

	}

}
