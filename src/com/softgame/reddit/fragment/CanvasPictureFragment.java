package com.softgame.reddit.fragment;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.softgame.reddit.CacheFragmentActivity;
import com.softgame.reddit.CommentFragmentActivity;
import com.softgame.reddit.ImageViewActivity;
import com.softgame.reddit.R;
import com.softgame.reddit.SubRedditCanvasActivity;
import com.softgame.reddit.WebviewActivity;
import com.softgame.reddit.cache.ImageWorker;
import com.softgame.reddit.impl.OnScaleImageClick;
import com.softgame.reddit.model.SubRedditItem;
import com.softgame.reddit.utils.Common;
import com.softgame.reddit.utils.CommonUtil;
import com.softgame.reddit.view.CustomScaleImageView;
import com.softgame.reddit.view.ScaleImageView;

public class CanvasPictureFragment extends CanvasFragment implements
		OnScaleImageClick {
	ScaleImageView mScaleImage;
	TextView mTitle;
	LinearLayout mPictureInfo;
	boolean mSafeForWork;

	public CanvasPictureFragment() {
	}

	public static CanvasPictureFragment newInstance(SubRedditItem s) {
		CanvasPictureFragment f = new CanvasPictureFragment();
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
		mSafeForWork = ((SubRedditCanvasActivity) this.getActivity()).mSafeForWork;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.item_canvas_picture_fragment,
				container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		this.getView().findViewById(R.id.button_view_picture)
				.setOnClickListener(this);

		mScaleImage = (ScaleImageView) this.getView()
				.findViewById(R.id.picture);
		// mScaleImage.setScaleImageClickListener(this);
		mPictureInfo = (LinearLayout) this.getView().findViewById(
				R.id.picture_info);
		mTitle = (TextView) this.getView().findViewById(R.id.title);

		if (mSubRedditItem.over_18 && mSafeForWork) {
			// show nsfw pic
			mScaleImage.setImageResource(R.drawable.pic_nsfw);
			mScaleImage.reset();
		} else if (mSubRedditItem != null && mSubRedditItem.url != null
				&& !"".equals(mSubRedditItem.url)) {
			((CacheFragmentActivity) this.getActivity()).getImageWorker()
					.loadImage(CommonUtil.appendJPG(mSubRedditItem.url),
							mScaleImage);
			this.getView().findViewById(R.id.button_browser)
					.setVisibility(View.VISIBLE);
		}

		mTitle.setText(mSubRedditItem.title);

		this.getView().findViewById(R.id.button_browser)
				.setOnClickListener(this);

		super.setUpContentView(this.getView(), mSubRedditItem);

	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		ImageWorker.cancelPotentialWork(
				CommonUtil.appendJPG(mSubRedditItem.url), mScaleImage);
		Log.d("Canvas", "onDestoryView:" + mSubRedditItem.position);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d("Canvas", "onDestory:" + mSubRedditItem.position);
	}

	@Override
	public void onDetach() {
		super.onDetach();
		Log.d("Canvas", "onDetach:" + mSubRedditItem.position);
	}

	@Override
	public void onPause() {
		super.onPause();
		Log.d("Canvas", "onPauseView:" + mSubRedditItem.position);
	}

	@Override
	public void onStop() {
		super.onStop();
		Log.d("Canvas", "onStopView:" + mSubRedditItem.position);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.button_browser:
			Intent i = new Intent(Intent.ACTION_VIEW,
					Uri.parse(mSubRedditItem.url));
			startActivity(i);
			break;
		case R.id.button_view_picture:
			Intent ttt = new Intent(this.getActivity(), ImageViewActivity.class);
			ttt.putExtra(Common.EXTRA_SUBREDDIT, mSubRedditItem);
			ttt.putExtra(Common.EXTRA_FROM_CANVAS, true);
			this.startActivity(ttt);
			break;
		}

		super.onClick(v);

	}

	@Override
	public void onImageClick() {
		mPictureInfo
				.setVisibility(mPictureInfo.getVisibility() == View.VISIBLE ? View.GONE
						: View.VISIBLE);
	}

}
