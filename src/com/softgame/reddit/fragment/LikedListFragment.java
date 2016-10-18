package com.softgame.reddit.fragment;

import java.lang.ref.WeakReference;

import org.json.custom.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.softgame.reddit.CommentFragmentActivity;
import com.softgame.reddit.ImageViewActivity;
import com.softgame.reddit.LikedFragmentActivity;
import com.softgame.reddit.OverviewCommentActivity;
import com.softgame.reddit.OverviewFragmentActivity;
import com.softgame.reddit.R;
import com.softgame.reddit.WebviewActivity;
import com.softgame.reddit.dialog.SubredditPopDialog;
import com.softgame.reddit.impl.OnSubredditPopSelectedListener;
import com.softgame.reddit.model.SubRedditItem;
import com.softgame.reddit.model.SubRedditModel;
import com.softgame.reddit.utils.Common;
import com.softgame.reddit.utils.CommonUtil;
import com.softgame.reddit.utils.LikedManager;
import com.softgame.reddit.utils.RedditManager;
import com.softgame.reddit.utils.SubRedditManager;

public class LikedListFragment extends ListFragment implements
		OnItemClickListener, OnClickListener, OnItemLongClickListener,
		OnSubredditPopSelectedListener {

	public static final int TYPE_COUNT_SUM = 8;

	public static final int LIKED_TYPE_LAODING = 0;
	public static final int LIKED_TYPE_NO_ITEM = 1;
	public static final int LIKED_TYPE_MORE = 2;
	public static final int LIKED_TYPE_ITEM_LINK = 3;
	public static final int LIKED_TYPE_ITEM_LINK_NOPIC = 4;
	public static final int LIKED_TYPE_ITEM_SELFPOST = 5;
	public static final int LIKED_TYPE_NO_MORE = 6;
	public static final int LIKED_TYPE_LOAD_FAIL = 7;

	LayoutInflater mLayoutInflater;

	public static final String TAG = "LikedListFragment";

	public WeakReference<DataTask> mDataTaskWF;

	public SubRedditAdapter mSubRedditAdapter;

	public String mCurrentSubReddit;
	public String mCurrentDefaultUser;
	// hot new ..
	public String mCurrentKind;
	// day week all time...
	public int mCurrentSort = 1;
	//
	public int mCurrentType = 1;

	// boolean load thumbnial

	public boolean mNeedLoadThumnail = true;

	public boolean mSafeForWork = true;

	public boolean mUsedEmbedView = true;

	public LikedListFragment() {
	}

	public static LikedListFragment newInstance(String type) {
		LikedListFragment tt = new LikedListFragment();
		Bundle args = new Bundle();
		args.putString("liked_type", type);
		tt.setArguments(args);
		return tt;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setRetainInstance(true);
		mCurrentKind = getArguments() != null ? getArguments().getString(
				"liked_type") : "overview";

		mSubRedditAdapter = new SubRedditAdapter();
		updateDataSate();
		SharedPreferences df = PreferenceManager
				.getDefaultSharedPreferences(this.getActivity());
		mSafeForWork = df.getBoolean(
				this.getString(R.string.pref_safe_for_work), true);

		mUsedEmbedView = df.getBoolean(
				this.getString(R.string.pref_key_embed_view), true);

	}

	/**
	 * update the current data to receive new data
	 */
	public void updateDataSate(String after) {
		if (mDataTaskWF != null && mDataTaskWF.get() != null)
			mDataTaskWF.get().cancel(true);
		DataTask d = new DataTask(after, this);
		mDataTaskWF = new WeakReference<LikedListFragment.DataTask>(d);
		d.execute();

	}

	public void updateDataSate() {
		if (mDataTaskWF != null && mDataTaskWF.get() != null)
			mDataTaskWF.get().cancel(true);
		DataTask d = new DataTask(this);
		mDataTaskWF = new WeakReference<LikedListFragment.DataTask>(d);
		d.execute();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mLayoutInflater = inflater;
		return inflater.inflate(R.layout.item_overview_listview, container,
				false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		this.getListView().setAdapter(mSubRedditAdapter);
		this.getListView().setOnItemClickListener(this);
		this.getListView().setOnItemLongClickListener(this);
	}

	private static class DataTask extends AsyncTask<Void, Void, String> {
		WeakReference<LikedListFragment> mLikedListFragmentWF;
		String after;
		SubRedditModel model;

		public DataTask(final LikedListFragment fragment) {
			mLikedListFragmentWF = new WeakReference<LikedListFragment>(
					fragment);
		}

		public DataTask(final String a, final LikedListFragment fragment) {
			mLikedListFragmentWF = new WeakReference<LikedListFragment>(
					fragment);
			after = a;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			LikedListFragment fragment = mLikedListFragmentWF.get();
			if (fragment != null) {
				if (after == null || "".equals(after)) {
					fragment.mSubRedditAdapter.clear();
				}
				fragment.mSubRedditAdapter.setIsLoadingData(true, true);
			} else {
				this.cancel(true);
			}
		}

		@Override
		protected String doInBackground(Void... params) {
			LikedListFragment fragment = mLikedListFragmentWF.get();
			if (fragment != null) {
				JSONObject dataJSON = new JSONObject();
				String result = LikedManager.getLiked(fragment.mCurrentKind,
						after, dataJSON, fragment.getActivity());
				if (Common.RESULT_SUCCESS.equals(result)) {
					// decode
					model = SubRedditModel.convertToModel(dataJSON);
				}
				return result;
			} else {
				this.cancel(true);
				return Common.RESULT_TASK_CANCLE;
			}
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);

			LikedListFragment fragment = mLikedListFragmentWF.get();

			// if fragment is not null, make it loading to false cause activity
			// may desctory and fragment still alive, and when activity start
			// again, they use this one as new one,which is unbelievable
			if (fragment != null) {
				fragment.mSubRedditAdapter.setIsLoadingData(false, true);
			}

			if (fragment == null || this.isCancelled()
					|| fragment.getActivity() == null
					|| result.equals(Common.RESULT_TASK_CANCLE)) {
				// cancel;
				return;
			}

			if (fragment.mDataTaskWF != null
					&& fragment.mDataTaskWF.get() != null) {
				if (fragment.mDataTaskWF.get() != this) {
					// I am not newest, do nothing
					return;
				}
			}

			if (result == Common.RESULT_SUCCESS) {
				fragment.mSubRedditAdapter.subRedditModel.addData(model);
				fragment.mSubRedditAdapter.notifyDataSetChanged();
			} else {
				fragment.mSubRedditAdapter.isFailed = true;
				if (Common.RESULT_FETCHING_FAIL.equals(result)) {
					Toast.makeText(fragment.getActivity(), "Internet error!",
							Toast.LENGTH_SHORT).show();
					return;
				}

				// show fail
				Toast.makeText(
						fragment.getActivity(),
						"Load " + fragment.mCurrentKind
								+ " failed! (\nReddit may be down!)",
						Toast.LENGTH_SHORT).show();
			}
		}
	}

	public class SubRedditAdapter extends BaseAdapter {

		boolean isLoadingData;
		boolean isFailed;
		public SubRedditModel subRedditModel;

		public SubRedditAdapter() {
			subRedditModel = new SubRedditModel();
		}

		@Override
		public boolean areAllItemsEnabled() {
			return false;
		}

		@Override
		public boolean isEnabled(int position) {
			switch (this.getItemViewType(position)) {
			case Common.SUBREDDIT_TYPE_EMPTY:
			case Common.SUBREDDIT_TYPE_DATE_HEADER:
			case Common.SUBREDDIT_TYPE_LAODING:
			case Common.SUBREDDIT_TYPE_NEW_HEADER:
			case Common.SUBREDDIT_TYPE_NO_MORE:
			case Common.SUBREDDIT_TYPE_NO_ITEM:
				return false;
			}
			return true;
		}

		@Override
		public int getItemViewType(int position) {
			// not include 0
			if (position >= 0 && position < subRedditModel.getSize()) {
				SubRedditItem item = (SubRedditItem) getItem(position);
				if (item.is_self) {
					return Common.SUBREDDIT_TYPE_ITEM_SELFPOST;
				} else {
					// add load thumnail setting
					if (item.thumbnail == null || "".equals(item.thumbnail)
							|| "default".equals(item.thumbnail)
							|| !mNeedLoadThumnail) {
						return Common.SUBREDDIT_TYPE_ITEM_LINK_NOPIC;
					}
					return Common.SUBREDDIT_TYPE_ITEM_LINK;
				}
			}
			if (isLoadingData) {
				return Common.SUBREDDIT_TYPE_LAODING;
			}
			if (isFailed) {
				return Common.SUBREDDIT_TYPE_LOAD_FAIL;
			} else {
				if (subRedditModel.getSize() == 0) {
					return Common.SUBREDDIT_TYPE_NO_ITEM;
				}
				if (subRedditModel.after == null
						|| "".equals(subRedditModel.after)) {
					return Common.SUBREDDIT_TYPE_NO_MORE;
				} else {
					return Common.SUBREDDIT_TYPE_MORE;
				}

			}
		}

		@Override
		public int getViewTypeCount() {
			return TYPE_COUNT_SUM;
		}

		@Override
		public SubRedditItem getItem(int position) {
			if (position >= 0 && position < subRedditModel.getSize()) {
				return subRedditModel.getItemByIndex(position);
			}
			return null;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			SubRedditItem item = this.getItem(position);
			int type = this.getItemViewType(position);
			switch (type) {

			case LikedListFragment.LIKED_TYPE_LOAD_FAIL:
				if (convertView == null) {
					convertView = mLayoutInflater.inflate(
							R.layout.item_load_fail, null);
				}
				break;
			case LikedListFragment.LIKED_TYPE_ITEM_LINK:
				if (convertView == null) {
					convertView = mLayoutInflater.inflate(
							R.layout.item_subreddit_fragment_link, null);
				}
				setUpContentView(convertView, item, position);
				ImageView rw = (ImageView) convertView
						.findViewById(R.id.remote_pic);
				rw.setTag(position);
				rw.setOnClickListener(LikedListFragment.this);
				if ((item.over_18 && mSafeForWork)) {
					rw.setImageResource(R.drawable.nsfw_picture);
				} else {
					if (rw != null)
						((LikedFragmentActivity) LikedListFragment.this
								.getActivity()).getImageWorker().loadImage(
								CommonUtil.appendJPG(item.url), rw);
				}

				// domain
				((TextView) convertView
						.findViewById(R.id.subreddit_info_domain))
						.setText(item.domain);

				break;

			case LikedListFragment.LIKED_TYPE_ITEM_LINK_NOPIC:
				if (convertView == null) {
					convertView = mLayoutInflater.inflate(
							R.layout.item_subreddit_link_nopic, null);
				}
				setUpContentView(convertView, item, position);

				// domain
				((TextView) convertView
						.findViewById(R.id.subreddit_info_domain))
						.setText(item.domain);

				// LINK
				((TextView) convertView.findViewById(R.id.link_address))
						.setText(CommonUtil.getShortUrl(item.url));
				convertView.findViewById(R.id.view_on_web).setTag(position);
				convertView.findViewById(R.id.view_on_web).setOnClickListener(
						LikedListFragment.this);
				break;

			case LikedListFragment.LIKED_TYPE_ITEM_SELFPOST:

				if (convertView == null) {
					convertView = mLayoutInflater.inflate(
							R.layout.item_subreddit_selfpost, null);
				}
				TextView mContentText = (TextView) convertView
						.findViewById(R.id.subreddit_content_text);

				if (item.getSelftext() == null
						|| item.getSelftext().toString().trim().equals("")) {
					mContentText.setVisibility(View.GONE);
				} else {
					mContentText.setText(item.getSelftext());
					// mContentText.setMovementMethod(LinkMovementMethod
					// .getInstance());

					// Linkify.addLinks(mContentText, Linkify.ALL);
				}

				setUpContentView(convertView, item, position);

				break;
			case LikedListFragment.LIKED_TYPE_LAODING:
				if (convertView == null) {
					convertView = mLayoutInflater.inflate(
							R.layout.item_loading, null);
				}
				break;
			case LikedListFragment.LIKED_TYPE_MORE:
				if (convertView == null) {
					convertView = mLayoutInflater.inflate(R.layout.item_more,
							null);
				}
				break;

			case LikedListFragment.LIKED_TYPE_NO_MORE:
				if (convertView == null) {
					convertView = mLayoutInflater.inflate(
							R.layout.item_no_more, null);
				}
				break;
			case LikedListFragment.LIKED_TYPE_NO_ITEM:
				if (convertView == null) {
					convertView = mLayoutInflater.inflate(
							R.layout.item_no_item, null);
				}
				break;
			}

			return convertView;

		}

		private void setUpContentView(View convertView, SubRedditItem item,
				int position) {

			TextView title = (TextView) convertView.findViewById(R.id.title);
			title.setText(item.title);

			/**
			 * LinearLayout subredditComment = (LinearLayout) convertView
			 * .findViewById(R.id.subreddit_comment);
			 * subredditComment.setTag(position);
			 * subredditComment.setOnClickListener(SubRedditListFragment.this);
			 * TextView commentCount = (TextView) convertView
			 * .findViewById(R.id.subreddit_comment_count);
			 * commentCount.setText(item.num_comments + "");
			 */
			// info time
			TextView subreddit_info_time = (TextView) convertView
					.findViewById(R.id.subreddit_info_time);
			subreddit_info_time.setText(CommonUtil.getRelateTimeString(
					item.created_utc * 1000,
					LikedListFragment.this.getActivity()));

			// subreddit
			TextView subreddit_info_subreddit = (TextView) convertView
					.findViewById(R.id.subreddit_info_subreddit);
			subreddit_info_subreddit.setText(item.subreddit);

			// nsfw
			TextView subreddit_info_nsfw = (TextView) convertView
					.findViewById(R.id.subreddit_info_nsfw);
			if (item.over_18) {
				subreddit_info_nsfw.setVisibility(View.VISIBLE);
			} else {
				subreddit_info_nsfw.setVisibility(View.GONE);
			}

			// comments count
			TextView subreddit_info_comment_count = (TextView) convertView
					.findViewById(R.id.subreddit_info_comment_count);
			subreddit_info_comment_count.setText(item.num_comments
					+ " comments");

			// save
			TextView subreddit_info_saved = (TextView) convertView
					.findViewById(R.id.subreddit_info_saved);
			if (item.saved) {
				subreddit_info_saved.setVisibility(View.VISIBLE);
			} else {
				subreddit_info_saved.setVisibility(View.GONE);
			}

			// hide
			TextView subreddit_info_hide = (TextView) convertView
					.findViewById(R.id.subreddit_info_hide);
			if (item.hidden) {
				subreddit_info_hide.setVisibility(View.VISIBLE);
			} else {
				subreddit_info_hide.setVisibility(View.GONE);
			}

			// vote

			LinearLayout voteUpWraper = (LinearLayout) convertView
					.findViewById(R.id.vote_up_wraper);
			LinearLayout voteDownWraper = (LinearLayout) convertView
					.findViewById(R.id.vote_down_wraper);

			ImageView voteUp = (ImageView) convertView
					.findViewById(R.id.vote_up_image);

			// save the grey one
			if (voteUp.getTag() == null)
				voteUp.setTag(voteUp.getDrawable());

			ImageView voteDown = (ImageView) convertView
					.findViewById(R.id.vote_down_image);
			if (voteDown.getTag() == null)
				voteDown.setTag(voteDown.getDrawable());

			TextView score = (TextView) convertView
					.findViewById(R.id.vote_score);

			if (score.getTag() == null) {
				score.setTag(score.getCurrentTextColor());
			}

			updateVoteImageAndText(voteUp, voteDown, score, item);

			voteUpWraper.setTag(position);
			voteDownWraper.setTag(position);

			voteUpWraper.setOnClickListener(LikedListFragment.this);
			voteDownWraper.setOnClickListener(LikedListFragment.this);

		}

		private void updateVoteImageAndText(ImageView voteUp,
				ImageView voteDown, TextView score, SubRedditItem item) {
			if (item.likes == null) {

				voteUp.setImageDrawable((Drawable) voteUp.getTag());
				// voteUp.setImageResource(R.drawable.vote_up_grey);
				voteDown.setImageDrawable((Drawable) voteDown.getTag());
				// voteDown.setImageResource(R.drawable.vote_down_grey);
				// score.setTextColor(getResources().getColor(
				// R.color.vote_no_color));
				score.setTextColor((Integer) score.getTag());
			} else if (item.likes) {
				voteUp.setImageResource(R.drawable.vote_up_selected);
				// voteDown.setImageResource(R.drawable.vote_down_grey);
				voteDown.setImageDrawable((Drawable) voteDown.getTag());
				score.setTextColor(getResources().getColor(
						R.color.vote_up_color));
			}

			else if (!item.likes) {
				// voteUp.setImageResource(R.drawable.vote_up_grey);
				voteUp.setImageDrawable((Drawable) voteUp.getTag());
				voteDown.setImageResource(R.drawable.vote_down_selected);
				score.setTextColor(getResources().getColor(
						R.color.vote_down_color));
			}
			score.setText(item.score + "");
		}

		@Override
		public int getCount() {
			return subRedditModel.getSize() + 1;
		}

		public void setIsLoadingData(boolean isloading, boolean refresh) {
			isLoadingData = isloading;
			if (refresh) {
				this.notifyDataSetChanged();
			}
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		public void clear() {
			subRedditModel.clear();
		}

	}

	/**
	 * update the vote image and return the vote result that VoteTask need
	 * 
	 * @param item
	 * @param isUp
	 * @return
	 */
	private String updateVoteAndGetResult(SubRedditItem item, boolean isUp) {
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
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// if it is more, then change to loading
		int type = mSubRedditAdapter.getItemViewType(position);
		switch (type) {
		case LikedListFragment.LIKED_TYPE_ITEM_LINK:
		case LikedListFragment.LIKED_TYPE_ITEM_LINK_NOPIC:
		case LikedListFragment.LIKED_TYPE_ITEM_SELFPOST:
			SubRedditItem item = mSubRedditAdapter.getItem(position);
			if (item == null) {
				return;
			}
			Intent t = new Intent(LikedListFragment.this.getActivity(),
					CommentFragmentActivity.class);
			t.putExtra(Common.INTENT_EXTRA_SUBREDDIT, item);
			this.startActivityForResult(t, Common.REQUEST_ACTIVITY_COMMENT);
			break;

		case LikedListFragment.LIKED_TYPE_MORE:
			// mSubRedditAdapter.setIsLoadingData(true, true);
			updateDataSate(mSubRedditAdapter.subRedditModel.after);
			break;
		case LikedListFragment.LIKED_TYPE_LOAD_FAIL:
			mSubRedditAdapter.isFailed = false;
			if (mSubRedditAdapter.subRedditModel.after != null
					&& !"".equals(mSubRedditAdapter.subRedditModel.after.trim())) {
				updateDataSate(mSubRedditAdapter.subRedditModel.after);
			} else {
				updateDataSate();
			}
			break;
		}

	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case Common.REQUEST_ACTIVITY_COMMENT:
			if (data != null) {
				SubRedditItem subRedditItem = data
						.getParcelableExtra(Common.INTENT_EXTRA_SUBREDDIT);
				if (subRedditItem != null) {
					try {
						SubRedditItem i = mSubRedditAdapter.subRedditModel.mItemList
								.get(subRedditItem.position);
						i.setSubRedditItem(subRedditItem);
						mSubRedditAdapter.notifyDataSetChanged();
					} catch (Exception e) {
						// do nothing;
					}
				}
			}
			break;
		case Common.REQUEST_ACTIVITY_WEBVIEW:
			if (resultCode == Activity.RESULT_OK && data != null) {
				boolean isComment = data.getBooleanExtra(
						Common.KEY_EXTRA_IS_COMMENT, false);
				SubRedditItem item = data
						.getParcelableExtra(Common.EXTRA_SUBREDDIT);

				// update data
				if (item != null) {
					try {
						SubRedditItem i = mSubRedditAdapter.subRedditModel.mItemList
								.get(item.position);
						i.setSubRedditItem(item);
						mSubRedditAdapter.notifyDataSetChanged();
					} catch (Exception e) {
						// do nothing;
					}
					if (isComment) {
						Intent t = new Intent(
								LikedListFragment.this.getActivity(),
								CommentFragmentActivity.class);
						t.putExtra(Common.INTENT_EXTRA_SUBREDDIT, item);
						this.startActivityForResult(t,
								Common.REQUEST_ACTIVITY_COMMENT);
					}
				}

			}
			break;
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {

		case R.id.view_on_web:

			int position = (Integer) v.getTag();
			SubRedditItem st = mSubRedditAdapter.getItem(position);
			if (st == null) {
				return;
			}
			Intent tt = null;
			// picture
			if (CommonUtil.isPictureUrl(st.url, st.domain, st.subreddit)) {
				tt = new Intent(LikedListFragment.this.getActivity(),
						ImageViewActivity.class);
				tt.putExtra(Common.EXTRA_SUBREDDIT, st);
				this.startActivityForResult(tt, Common.REQUEST_ACTIVITY_WEBVIEW);
			}
			// link that dont support in webview (youtube) ACTIONVIEW
			else if (!CommonUtil.isHoneycomb()
					&& st.domain.equalsIgnoreCase(Common.DOMAIN_YOUTUBE)
					&& mUsedEmbedView) {
				Uri uri = Uri.parse(st.url);
				Intent intent = new Intent(Intent.ACTION_VIEW, uri);
				startActivity(intent);
			}
			// check if it is Comment URL
			else if (CommonUtil.checkIsComment(st.domain, st.url)) {
				Intent ts = new Intent(this.getActivity(),
						OverviewCommentActivity.class);
				ts.putExtra(Common.EXTRA_OVERVIEW_COMMENT_URL, st.url);
				ts.putExtra(Common.EXTRA_OVERVIEW_LOAD_TYPE,
						OverviewCommentActivity.LOAD_TYPE_SHARE_COMMENT);
				startActivity(ts);
			} else if (CommonUtil.checkIsContextComment(st.domain, st.url)) {
				Intent t = new Intent(this.getActivity(),
						OverviewCommentActivity.class);
				t.putExtra(Common.EXTRA_OVERVIEW_COMMENT_URL, st.url);
				t.putExtra(Common.EXTRA_OVERVIEW_LOAD_TYPE,
						OverviewCommentActivity.LOAD_TYPE_SHARE_COMMENT_CONTEXT);
				startActivity(t);
			}

			// start the webview
			else {
				tt = new Intent(LikedListFragment.this.getActivity(),
						WebviewActivity.class);
				tt.putExtra(Common.EXTRA_SUBREDDIT, st);
				this.startActivityForResult(tt, Common.REQUEST_ACTIVITY_WEBVIEW);
			}

			break;

		case R.id.remote_pic:
			int positionr = (Integer) v.getTag();
			SubRedditItem itemR = mSubRedditAdapter.getItem(positionr);
			if (itemR == null) {
				return;
			}
			Intent picIntent = new Intent(LikedListFragment.this.getActivity(),
					ImageViewActivity.class);
			picIntent.putExtra(Common.EXTRA_SUBREDDIT, itemR);
			this.startActivityForResult(picIntent,
					Common.REQUEST_ACTIVITY_WEBVIEW);

			break;

		case R.id.vote_up_wraper:
			// check login
			if (!RedditManager.isUserAuth(LikedListFragment.this.getActivity())) {
				Toast.makeText(LikedListFragment.this.getActivity(),
						this.getString(R.string.login_request),
						Toast.LENGTH_SHORT).show();
				return;
			}
			int pp = (Integer) v.getTag(); // get position
			SubRedditItem itemp = mSubRedditAdapter.getItem(pp);
			String dir = updateVoteAndGetResult(itemp, true);
			mSubRedditAdapter.notifyDataSetChanged();
			new VoteTask(this, itemp, dir, true).execute();
			break;
		case R.id.vote_down_wraper:
			// check login
			if (!RedditManager.isUserAuth(LikedListFragment.this.getActivity())) {
				Toast.makeText(LikedListFragment.this.getActivity(),
						this.getString(R.string.login_request),
						Toast.LENGTH_SHORT).show();
				return;
			}
			int pd = Integer.parseInt(v.getTag().toString());
			SubRedditItem itemd = mSubRedditAdapter.getItem(pd);
			String dird = updateVoteAndGetResult(itemd, false);
			mSubRedditAdapter.notifyDataSetChanged();
			new VoteTask(this, itemd, dird, false).execute();
			break;
		}
	}

	@Override
	public void onSubredditPopSelected(int position, int action) {
		SubRedditItem item = mSubRedditAdapter.getItem(position);
		switch (action) {
		case SubredditPopDialog.ACTION_VOTE_UP:
			String dir = updateVoteAndGetResult(item, true);
			mSubRedditAdapter.notifyDataSetChanged();
			new VoteTask(this, item, dir, true).execute();
			break;
		case SubredditPopDialog.ACTION_VOTE_DOWN:
			String dird = updateVoteAndGetResult(item, false);
			mSubRedditAdapter.notifyDataSetChanged();
			new VoteTask(this, item, dird, false).execute();
			break;
		case SubredditPopDialog.ACTION_HIDE:
			item.hidden = true;
			mSubRedditAdapter.notifyDataSetChanged();
			new HideTask(this, item, true).execute();
			break;
		case SubredditPopDialog.ACTION_UNHIDE:
			item.hidden = false;
			mSubRedditAdapter.notifyDataSetChanged();
			new HideTask(this, item, false).execute();
			break;

		case SubredditPopDialog.ACTION_SAVE:
			item.saved = true;
			mSubRedditAdapter.notifyDataSetChanged();
			new SaveTask(this, item, true).execute();
			break;

		case SubredditPopDialog.ACTION_UNSAVE:
			item.saved = false;
			mSubRedditAdapter.notifyDataSetChanged();
			new SaveTask(this, item, false).execute();
			break;

		case SubredditPopDialog.ACTION_PROFILE:
			Intent authorIntent = new Intent(
					LikedListFragment.this.getActivity(),
					OverviewFragmentActivity.class);
			authorIntent.putExtra(Common.EXTRA_USERNAME, item.author);
			this.startActivity(authorIntent);
			break;
		}
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
			int position, long id) {
		switch (mSubRedditAdapter.getItemViewType(position)) {
		case Common.SUBREDDIT_TYPE_ITEM_LINK:
		case Common.SUBREDDIT_TYPE_ITEM_LINK_NOPIC:
		case Common.SUBREDDIT_TYPE_ITEM_SELFPOST:
			if (((LikedFragmentActivity) LikedListFragment.this.getActivity()).mSubRedditPopDialog == null) {
				((LikedFragmentActivity) LikedListFragment.this.getActivity()).mSubRedditPopDialog = new SubredditPopDialog(
						LikedListFragment.this.getActivity());
				((LikedFragmentActivity) LikedListFragment.this.getActivity()).mSubRedditPopDialog
						.setOwnerActivity(LikedListFragment.this.getActivity());
			}
			((LikedFragmentActivity) LikedListFragment.this.getActivity()).mSubRedditPopDialog
					.dismiss();

			((LikedFragmentActivity) LikedListFragment.this.getActivity()).mSubRedditPopDialog
					.setSubRedditItem(mSubRedditAdapter.getItem(position),
							position);
			((LikedFragmentActivity) LikedListFragment.this.getActivity()).mSubRedditPopDialog
					.setOnSubredditPopSelectedListener(this);
			((LikedFragmentActivity) LikedListFragment.this.getActivity()).mSubRedditPopDialog
					.show();
			break;
		}

		return true;
	}

	// -------------------vote ------------------
	public static class VoteTask extends AsyncTask<Void, Void, String> {
		String name;
		boolean isUp;
		String dir = "0";
		WeakReference<LikedListFragment> likedListFragmentWF;
		WeakReference<SubRedditItem> subRedditItemWF;

		public VoteTask(LikedListFragment fragment, SubRedditItem s, String d,
				boolean up) {
			likedListFragmentWF = new WeakReference<LikedListFragment>(fragment);
			subRedditItemWF = new WeakReference<SubRedditItem>(s);
			isUp = up;
			dir = d;
			name = s.name;
		}

		@Override
		protected String doInBackground(Void... params) {
			if (likedListFragmentWF.get() != null
					&& likedListFragmentWF.get().getActivity() != null) {
				return SubRedditManager.voteSubRedditPost(name, dir,
						likedListFragmentWF.get().getActivity());
			} else {
				this.cancel(true);
				return Common.RESULT_TASK_CANCLE;
			}
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			if (result == Common.RESULT_TASK_CANCLE
					|| likedListFragmentWF.get() == null
					|| subRedditItemWF.get() == null
					|| likedListFragmentWF.get().getActivity() == null) {
				return;
			}
			if (result == Common.RESULT_SUCCESS) {
				Toast.makeText(likedListFragmentWF.get().getActivity(),
						"Vote succeeded!", Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(likedListFragmentWF.get().getActivity(),
						"Vote failed!", Toast.LENGTH_SHORT).show();

				// roll back
				subRedditItemWF.get().likes = subRedditItemWF.get().old_like;
				likedListFragmentWF.get().mSubRedditAdapter
						.notifyDataSetChanged();
			}
		}

	}

	// ---------------------- SAVE --------------------
	public static class SaveTask extends AsyncTask<Void, Void, String> {
		String name;
		boolean save;
		WeakReference<SubRedditItem> subRedditItemWF;
		WeakReference<LikedListFragment> likedListFragmentWF;

		public SaveTask(LikedListFragment fragment, SubRedditItem item,
				boolean s) {
			save = s;
			name = item.name;
			subRedditItemWF = new WeakReference<SubRedditItem>(item);
			likedListFragmentWF = new WeakReference<LikedListFragment>(fragment);
		}

		@Override
		protected String doInBackground(Void... params) {
			if (likedListFragmentWF.get() != null
					&& likedListFragmentWF.get().getActivity() != null) {
				return SubRedditManager.saveSubRedditPost(name, save,
						likedListFragmentWF.get().getActivity());
			} else {
				this.cancel(true);
				return Common.RESULT_TASK_CANCLE;
			}
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			if (result == Common.RESULT_TASK_CANCLE
					|| likedListFragmentWF.get() == null
					|| subRedditItemWF.get() == null
					|| likedListFragmentWF.get().getActivity() == null) {
				return;
			}

			if (Common.RESULT_SUCCESS.equals(result)) {
				Toast.makeText(likedListFragmentWF.get().getActivity(),
						save ? "Save succeeded!" : "Unsave succeeded!",
						Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(likedListFragmentWF.get().getActivity(),
						"Save failed, try again latter!", Toast.LENGTH_SHORT)
						.show();
				subRedditItemWF.get().saved = !save;
				likedListFragmentWF.get().mSubRedditAdapter
						.notifyDataSetChanged();
			}

		}
	}

	// ------------ hide --------------
	public static class HideTask extends AsyncTask<Void, Void, String> {
		String name;
		boolean hide;
		WeakReference<SubRedditItem> subRedditItemWF;
		WeakReference<LikedListFragment> likedListFragmentWF;

		public HideTask(LikedListFragment fragment, SubRedditItem item,
				boolean h) {
			hide = h;
			name = item.name;
			subRedditItemWF = new WeakReference<SubRedditItem>(item);
			likedListFragmentWF = new WeakReference<LikedListFragment>(fragment);
		}

		@Override
		protected String doInBackground(Void... params) {
			if (likedListFragmentWF.get() != null) {
				return SubRedditManager.hideSubReddit(name, hide,
						likedListFragmentWF.get().getActivity());
			} else {
				this.cancel(true);
				return Common.RESULT_TASK_CANCLE;
			}

		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			if (result == Common.RESULT_TASK_CANCLE
					|| likedListFragmentWF.get() == null
					|| subRedditItemWF.get() == null) {
				return;
			}

			if (result == Common.RESULT_SUCCESS) {
				Toast.makeText(likedListFragmentWF.get().getActivity(),
						hide ? "Hide succeeded!" : "Unhiden succeeded!",
						Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(likedListFragmentWF.get().getActivity(),
						"Hide failed!", Toast.LENGTH_LONG).show();
				subRedditItemWF.get().hidden = !hide;
				likedListFragmentWF.get().mSubRedditAdapter
						.notifyDataSetChanged();
			}
		}
	}

}
