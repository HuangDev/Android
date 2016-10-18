package com.softgame.reddit.fragment;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.softgame.reddit.CacheFragmentActivity;
import com.softgame.reddit.R;
import com.softgame.reddit.cache.ImageWorker;
import com.softgame.reddit.model.SubRedditItem;
import com.softgame.reddit.utils.Common;
import com.softgame.reddit.utils.CommonUtil;
import com.softgame.reddit.view.ScaleImageView;

public class ImageViewFragment extends SherlockFragment {
	public static final String TAG = "WebViewFragment";

	ScaleImageView mScaleView;
	SubRedditItem mSubRedditItem;
	boolean mShowDetailInfo;

	public ImageViewFragment() {
	}

	public static ImageViewFragment findOrCreateImageViewFragment(
			FragmentManager manager, SubRedditItem subreddititem, long id) {
		ImageViewFragment fragment = (ImageViewFragment) manager
				.findFragmentByTag(ImageViewFragment.TAG + id);
		if (fragment == null) {
			fragment = new ImageViewFragment();
			Bundle args = new Bundle();
			args.putParcelable("subreddit_item", subreddititem);
			fragment.setArguments(args);
			final FragmentTransaction ft = manager.beginTransaction();
			ft.add(android.R.id.content, fragment, ImageViewFragment.TAG + id);
			ft.commit();
		}
		return fragment;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (this.getArguments() != null) {
			mSubRedditItem = this.getArguments()
					.getParcelable("subreddit_item");
		}

		mShowDetailInfo = CommonUtil.needToShowPictureInfo(getActivity());

		this.setRetainInstance(true);
	}

	public SubRedditItem getSubredditItem() {
		return mSubRedditItem;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.item_fragment_imageview, container,
				false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (mSubRedditItem == null) {
			Toast.makeText(this.getActivity(), "link missing!",
					Toast.LENGTH_LONG).show();
			this.getActivity().finish();
			return;
		}
		mScaleView = (ScaleImageView) this.getView().findViewById(R.id.image);
			((CacheFragmentActivity) this.getActivity()).getImageWorker()
					.loadImage(CommonUtil.appendJPG(mSubRedditItem.url),
							mScaleView);
		if (mShowDetailInfo) {
			setUpContentView(this.getView(), mSubRedditItem);
		} else {
			this.getView().findViewById(R.id.picture_info)
					.setVisibility(View.GONE);
		}
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		ImageWorker.cancelWork(mScaleView);
	}

	protected void setUpContentView(View convertView, SubRedditItem item) {

		TextView title = (TextView) convertView.findViewById(R.id.title);
		title.setText(item.title);

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

	}

	public void invalidateView() {
		setUpContentView(this.getView(), mSubRedditItem);
	}

	public String updateVoteAndGetResult(boolean isUp) {
		return updateVoteAndGetResult(mSubRedditItem, isUp);
	}

	/**
	 * update the vote image and return the vote result that VoteTask need
	 * 
	 * @param item
	 * @param isUp
	 * @return
	 */
	public String updateVoteAndGetResult(SubRedditItem item, boolean isUp) {
		int s = 0;
		item.old_like = item.likes;
		if (isUp) {
			if (item.likes == null) {
				item.likes = true;
				s = 1;
			} else if (item.likes) {
				// cancel the like
				item.likes = null;
				s = -1;
			} else if (!item.likes) {
				// change to like
				item.likes = true;
				s = 2;
			}
		}

		if (!isUp) {
			if (item.likes == null) {
				item.likes = false;
				s = -1;
			} else if (item.likes) {
				item.likes = false;
				s = -2;
			} else if (!item.likes) {
				item.likes = null;
				s = 1;
			}

		}

		String dir = "0";
		if (item.likes == null) {
			dir = "0";
		} else if (item.likes) {
			dir = "1";
		} else if (!item.likes) {
			dir = "-1";
		}
		item.score = item.score + s;
		invalidateView();
		return dir;
	}

}
