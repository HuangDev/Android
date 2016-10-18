package com.softgame.reddit.fragment;

import java.lang.reflect.Method;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CacheManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.softgame.reddit.R;
import com.softgame.reddit.model.SubRedditItem;
import com.softgame.reddit.utils.Common;
import com.softgame.reddit.utils.CommonUtil;

public class WebViewFragment extends SherlockFragment {

	public static final String TAG = "WebViewFragment";

	WebView mWebView;
	String mUrl;
	ProgressBar mProgressBar;

	ImageButton mCommentActivity;
	SubRedditItem mSubRedditItem;

	LinearLayout mBack;

	public static final String DOMAIN_ALLOW_JAVASCRIPT = "imgur.com,i.imgur.com,youtube.com";

	public WebViewFragment() {
	}

	public static WebViewFragment findOrCreateRedditorFragment(
			FragmentManager manager, SubRedditItem subreddititem, long id) {
		WebViewFragment fragment = (WebViewFragment) manager
				.findFragmentByTag(WebViewFragment.TAG + id);
		if (fragment == null) {
			fragment = new WebViewFragment();
			Bundle args = new Bundle();
			args.putParcelable("subreddit_item", subreddititem);
			fragment.setArguments(args);
			final FragmentTransaction ft = manager.beginTransaction();
			ft.add(android.R.id.content, fragment, WebViewFragment.TAG + id);
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
		this.setRetainInstance(true);
	}

	@Override
	public void onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu,
			com.actionbarsherlock.view.MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);

	}

	public SubRedditItem getSubredditItem() {
		return mSubRedditItem;
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
		return dir;
	}

	@Override
	public boolean onOptionsItemSelected(
			com.actionbarsherlock.view.MenuItem item) {
		if (item.getTitle().equals("Comments")) {
			Intent dd = new Intent();
			dd.putExtra(Common.EXTRA_SUBREDDIT, mSubRedditItem);
			this.getActivity().setResult(Activity.RESULT_OK, dd);
			this.getActivity().finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_webview, container, false);
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

		mProgressBar = (ProgressBar) this.getView().findViewById(
				R.id.progress_bar);

		mWebView = (WebView) this.getView().findViewById(R.id.webview);

		this.getView().findViewById(R.id.image_nsfw).setVisibility(View.GONE);
		mProgressBar.setVisibility(View.VISIBLE);
		mWebView.setVisibility(View.VISIBLE);
		if (mSubRedditItem.domain != null
				&& !"".equals(mSubRedditItem.domain.trim())
				&& DOMAIN_ALLOW_JAVASCRIPT
						.indexOf(mSubRedditItem.domain.trim()) != -1) {
			mWebView.getSettings().setJavaScriptEnabled(true);
		} else {
			mWebView.getSettings().setJavaScriptEnabled(false);
		}
		mProgressBar.setVisibility(View.VISIBLE);
		mWebView.loadUrl(mSubRedditItem.url);

		mWebView.setWebViewClient(new CustomWebViewClient());
		mWebView.getSettings().setBuiltInZoomControls(true);
		mWebView.getSettings().setAppCacheEnabled(false);
		mWebView.setWebChromeClient(new WebChromeClient() {

			public void onProgressChanged(WebView view, int progress) {
				mProgressBar.setProgress(progress);
			}
		});
	}

	private class CustomWebViewClient extends WebViewClient {
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			view.loadUrl(url);
			return true;
		}

		@Override
		public void onReceivedError(WebView view, int errorCode,
				String description, String failingUrl) {
			super.onReceivedError(view, errorCode, description, failingUrl);

		}

		@Override
		public void onPageFinished(WebView view, String url) {
			super.onPageFinished(view, url);
			mProgressBar.setVisibility(View.GONE);
		}

	}

	public boolean onKeyBackClick(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK) && mWebView.canGoBack()) {
			mWebView.goBack();
			return true;
		}
		return false;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mWebView.stopLoading();
		mWebView.clearHistory();
		mWebView.clearCache(true);
		mWebView = null;
	}
}
