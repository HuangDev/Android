/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.softgame.reddit.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.softgame.reddit.ImageDetailActivity;
import com.softgame.reddit.R;
import com.softgame.reddit.cache.ImageWorker;
import com.softgame.reddit.model.SubRedditItem;
import com.softgame.reddit.utils.CommonUtil;
import com.softgame.reddit.view.ScaleImageView;

/**
 * This fragment will populate the children of the ViewPager from
 * {@link ImageDetailActivity}.
 */
public class ImageDetailFragment extends Fragment {

	public static final String TAG = "ImageDetailFragment";
	private ScaleImageView mScaleImageView;
	SubRedditItem mSubRedditItem;

	public ImageDetailFragment() {
	}

	public static ImageDetailFragment findOrCreateImageViewFragment(
			FragmentManager manager, SubRedditItem subreddititem, long id) {
		ImageDetailFragment fragment = (ImageDetailFragment) manager
				.findFragmentByTag(ImageDetailFragment.TAG + id);
		if (fragment == null) {
			fragment = new ImageDetailFragment();
			Bundle args = new Bundle();
			args.putParcelable("subreddit_item", subreddititem);
			fragment.setArguments(args);
			final FragmentTransaction ft = manager.beginTransaction();
			ft.add(android.R.id.content, fragment, ImageViewFragment.TAG + id);
			ft.commit();
		}
		return fragment;
	}

	/**
	 * Populate image number from extra, use the convenience factory method
	 * {@link ImageDetailFragment#newInstance(int)} to create this fragment.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setRetainInstance(true);
		if (this.getArguments() != null) {
			mSubRedditItem = this.getArguments()
					.getParcelable("subreddit_item");
		}

		if (mSubRedditItem == null) {
			Toast.makeText(this.getActivity(), "Post missing",
					Toast.LENGTH_LONG).show();
			this.getActivity().finish();
		}
	}
	
	public SubRedditItem getSubredditItem(){
		return mSubRedditItem;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate and locate the main ImageView
		final View v = inflater.inflate(R.layout.image_detail_fragment,
				container, false);
		mScaleImageView = (ScaleImageView) v.findViewById(R.id.imageView);
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		// Use the parent activity to load the image asynchronously into the
		// ImageView (so a single
		// cache can be used over all pages in the ViewPager
		ImageWorker imageWorker = ((ImageDetailActivity) getActivity())
				.getImageWorker();
		imageWorker.loadImage(CommonUtil.appendJPG(mSubRedditItem.url),
				mScaleImageView);

	}

}
