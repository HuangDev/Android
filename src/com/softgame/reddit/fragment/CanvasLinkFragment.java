package com.softgame.reddit.fragment;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;
import android.widget.Toast;

import com.softgame.reddit.CacheFragmentActivity;
import com.softgame.reddit.R;
import com.softgame.reddit.SubRedditCanvasActivity;
import com.softgame.reddit.WebviewActivity;
import com.softgame.reddit.model.SubRedditItem;
import com.softgame.reddit.utils.Common;

public class CanvasLinkFragment extends CanvasFragment {

	TextView mTitle;
	boolean mSafeForWork;

	public static CanvasLinkFragment newInstance(SubRedditItem s) {
		CanvasLinkFragment f = new CanvasLinkFragment();
		Bundle args = new Bundle();
		args.putParcelable("key_subreddit_item", s);
		f.setArguments(args);
		return f;
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
		mSafeForWork = ((SubRedditCanvasActivity) this.getActivity()).mSafeForWork;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.item_canvas_link_fragment, container,
				false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		if (mSubRedditItem != null) {

			this.getView().findViewById(R.id.view_on_web)
					.setOnClickListener(this);

			this.getView().findViewById(R.id.button_view_link)
					.setOnClickListener(this);

			mTitle = (TextView) this.getView().findViewById(R.id.title);
			mTitle.setText(mSubRedditItem.title);

			ImageView thumb = (ImageView) this.getView().findViewById(
					R.id.subreddit_thumb);

			this.getView().findViewById(R.id.view_on_web)
					.setVisibility(View.VISIBLE);
			thumb.setVisibility(View.VISIBLE);

			if (mSubRedditItem.over_18) {
				thumb.setImageResource(R.drawable.pic_nsfw);
				thumb.setScaleType(ScaleType.CENTER);
			} else if (mSubRedditItem.thumbnail == null
					|| mSubRedditItem.thumbnail.equals("")
					|| mSubRedditItem.thumbnail.equals("default")
					|| mSubRedditItem.thumbnail.equals("nsfw")) {
				thumb.setImageResource(R.drawable.icon_reddit_link);
				thumb.setScaleType(ScaleType.CENTER);
			} else {
				((CacheFragmentActivity) this.getActivity()).getImageWorker()
						.loadImage(mSubRedditItem.thumbnail, thumb);
			}

			TextView link = (TextView) this.getView().findViewById(
					R.id.subreddit_link);
			link.setText(mSubRedditItem.domain);

			setUpContentView(this.getView(), mSubRedditItem);
		} else {
			Toast.makeText(getActivity(), "Post missing!", Toast.LENGTH_SHORT)
					.show();
		}

	}

	@Override
	public void onClick(View v) {
		super.onClick(v);
		switch (v.getId()) {
		case R.id.view_on_web:
			startActivity(new Intent(Intent.ACTION_VIEW,
					Uri.parse(mSubRedditItem.url)));
			break;

		case R.id.button_view_link:
			Intent ttt = new Intent(this.getActivity(), WebviewActivity.class);
			ttt.putExtra(Common.EXTRA_SUBREDDIT, mSubRedditItem);
			this.startActivity(ttt);
			break;
		}

	}
}
