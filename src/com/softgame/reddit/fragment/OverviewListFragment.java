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
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.softgame.reddit.CommentFragmentActivity;
import com.softgame.reddit.ImageViewActivity;
import com.softgame.reddit.OverviewCommentActivity;
import com.softgame.reddit.OverviewFragmentActivity;
import com.softgame.reddit.R;
import com.softgame.reddit.WebviewActivity;
import com.softgame.reddit.dialog.OverviewCommentPopDialog;
import com.softgame.reddit.dialog.OverviewSubredditPopDialog;
import com.softgame.reddit.dialog.SubredditPopDialog;
import com.softgame.reddit.impl.OnOverviewCommentDialogListener;
import com.softgame.reddit.impl.OnSubredditPopSelectedListener;
import com.softgame.reddit.model.OverviewItem;
import com.softgame.reddit.model.OverviewModel;
import com.softgame.reddit.model.SubRedditItem;
import com.softgame.reddit.utils.Common;
import com.softgame.reddit.utils.CommonUtil;
import com.softgame.reddit.utils.OverviewManager;
import com.softgame.reddit.utils.RedditManager;
import com.softgame.reddit.utils.SubRedditManager;

public class OverviewListFragment extends ListFragment implements
		OnItemClickListener, OnClickListener, OnItemLongClickListener,
		OnItemSelectedListener, OnSubredditPopSelectedListener,
		OnOverviewCommentDialogListener {

	public static final int NUM_COUNT = 11;

	public static final int TYPE_LAODING = 0;
	public static final int TYPE_NO_ITEM = 1;
	public static final int TYPE_MORE = 2;
	public static final int TYPE_SUBMITTED_LINK = 3;
	public static final int TYPE_SUBMITTED_SELFPOST = 4;
	public static final int TYPE_SUBMITTED_LINK_NOPIC = 5;
	public static final int TYPE_COMMENT = 6;
	public static final int TYPE_NO_MORE = 7;
	public static final int TYPE_DATA_HEADER = 8;
	public static final int TYPE_EMPTY = 9;
	public static final int TYPE_LOAD_FAIL = 10;

	public static final int SORT_CONTROVERSIAL = 0x2;
	public static final int SORT_TOP = 0x3;
	public static final int DATE_ALL_TIME = 0x5;

	String mCurrentAuthor;
	String mCurrentKind;
	String mSortTypeArray[];
	String mDateTypeArray[];

	public OverviewAdapter mOverviewAdapter;

	int mCurrentSortType;
	int mCurrentDateType = DATE_ALL_TIME;

	public boolean mUsedEmbedView = true;

	LayoutInflater mLayoutInflater;
	WeakReference<OverviewTask> mOverviewTaskWF;
	public boolean mSafeForWork = true;


	public OverviewListFragment() {
	}

	public static OverviewListFragment newInstance(String author, String type) {
		OverviewListFragment tt = new OverviewListFragment();
		Bundle args = new Bundle();
		args.putString("overview_type", type);
		args.putString("current_author", author);
		tt.setArguments(args);
		return tt;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setRetainInstance(true);
		mCurrentKind = getArguments() != null ? getArguments().getString(
				"overview_type") : "overview";
		mCurrentAuthor = getArguments() != null ? getArguments().getString(
				"current_author") : "redditET";

		mSortTypeArray = this.getResources().getStringArray(
				R.array.overview_sort_value_array);
		mDateTypeArray = this.getResources().getStringArray(
				R.array.date_value_array);

		SharedPreferences df = PreferenceManager
				.getDefaultSharedPreferences(this.getActivity());

		mSafeForWork = df.getBoolean(
				this.getString(R.string.pref_safe_for_work), true);

		mUsedEmbedView = df.getBoolean(
				this.getString(R.string.pref_key_embed_view), true);

		mOverviewAdapter = new OverviewAdapter();
		updateDataSate();
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
		this.getListView().setAdapter(mOverviewAdapter);
		this.getListView().setOnItemClickListener(this);
		this.getListView().setOnItemLongClickListener(this);
	}

	/**
	 * update the current data to receive new data
	 */
	private void updateDataSate() {
		if (mOverviewTaskWF != null && mOverviewTaskWF.get() != null)
			mOverviewTaskWF.get().cancel(true);
		OverviewTask task = new OverviewTask(this);
		mOverviewTaskWF = new WeakReference<OverviewTask>(task);
		task.execute();
	}

	/**
	 * update the current data to receive new data
	 */
	private void updateDataSate(String after) {
		if (mOverviewTaskWF != null && mOverviewTaskWF.get() != null)
			mOverviewTaskWF.get().cancel(true);
		OverviewTask task = new OverviewTask(after, this);
		mOverviewTaskWF = new WeakReference<OverviewTask>(task);
		task.execute();
	}

	private static class OverviewTask extends AsyncTask<Void, Void, String> {

		String after;
		WeakReference<OverviewListFragment> fragmentWF;
		OverviewModel data;

		public OverviewTask(OverviewListFragment fragment) {
			fragmentWF = new WeakReference<OverviewListFragment>(fragment);
		}

		public OverviewTask(String a, OverviewListFragment fragment) {
			fragmentWF = new WeakReference<OverviewListFragment>(fragment);
			after = a;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (fragmentWF != null && fragmentWF.get() != null) {
				if (after == null || after.equals("")) {
					fragmentWF.get().mOverviewAdapter.overviewModel.mItemList
							.clear();
				}
				fragmentWF.get().mOverviewAdapter.setIsLoadingData(true, true);
			}
		}

		@Override
		protected String doInBackground(Void... params) {
			if (!this.isCancelled() && fragmentWF != null
					&& fragmentWF.get() != null
					&& fragmentWF.get().getActivity() != null) {

				String date = null;
				if (fragmentWF.get().mCurrentSortType == SORT_CONTROVERSIAL
						|| fragmentWF.get().mCurrentSortType == SORT_TOP) {
					date = fragmentWF.get().mDateTypeArray[fragmentWF.get().mCurrentDateType];
				}
				JSONObject dataJSON = new JSONObject();
				String result = OverviewManager
						.getOverview(fragmentWF.get().mCurrentAuthor,
								fragmentWF.get().mCurrentKind, after,
								fragmentWF.get().mSortTypeArray[fragmentWF
										.get().mCurrentSortType], date,
								dataJSON, fragmentWF.get().getActivity());
				if (Common.RESULT_SUCCESS.equals(result)) {
					data = OverviewModel.newInstance(dataJSON);
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
			if (Common.RESULT_TASK_CANCLE.equals(result)) {
				return;
			}
			if (!this.isCancelled() && fragmentWF != null
					&& fragmentWF.get() != null
					&& fragmentWF.get().getActivity() != null) {

				if (fragmentWF.get().mOverviewTaskWF == null
						|| this != fragmentWF.get().mOverviewTaskWF.get()) {
					// not the newest!
					return;
				}

				if (result == Common.RESULT_SUCCESS) {
					fragmentWF.get().mOverviewAdapter.overviewModel
							.addOverviewModel(data);
				} else {
					fragmentWF.get().mOverviewAdapter.isLoadFailed = true;
					if (result == Common.RESULT_PAGE_NOTFOUND) {
						Toast.makeText(fragmentWF.get().getActivity(),
								"Page missing!", Toast.LENGTH_SHORT).show();

					} else {
						Toast.makeText(fragmentWF.get().getActivity(),
								"Load fail,try again latter!",
								Toast.LENGTH_LONG).show();
					}
				}

				fragmentWF.get().mOverviewAdapter.setIsLoadingData(false, true);
			}
		}
	}

	private class OverviewAdapter extends BaseAdapter {
		public OverviewModel overviewModel;
		public boolean isLoadingData;
		public boolean isLoadFailed;

		public OverviewAdapter() {
			overviewModel = new OverviewModel();
		}

		@Override
		public int getCount() {
			// loading /no_item / date_header
			return overviewModel.mItemList.size() + 2;
		}

		@Override
		public boolean isEnabled(int position) {
			int type = this.getItemViewType(position);
			switch (type) {
			case OverviewListFragment.TYPE_COMMENT:
			case OverviewListFragment.TYPE_DATA_HEADER:
			case OverviewListFragment.TYPE_SUBMITTED_LINK:
			case OverviewListFragment.TYPE_SUBMITTED_SELFPOST:
			case OverviewListFragment.TYPE_MORE:
			case OverviewListFragment.TYPE_SUBMITTED_LINK_NOPIC:
				return true;
			case OverviewListFragment.TYPE_NO_ITEM:
			case OverviewListFragment.TYPE_NO_MORE:
			case OverviewListFragment.TYPE_LAODING:
			case OverviewListFragment.TYPE_EMPTY:
				return false;
			}
			return false;
		}

		@Override
		public boolean areAllItemsEnabled() {
			return false;
		}

		@Override
		public int getViewTypeCount() {
			return NUM_COUNT;
		}

		@Override
		public int getItemViewType(int position) {
			if (position > 0 && position < overviewModel.mItemList.size() + 1) {
				OverviewItem item = overviewModel.mItemList.get(position - 1);
				if (item.kind.equals("t3")) {
					SubRedditItem ip = item.subRedditItem;
					if (ip.is_self) {
						return OverviewListFragment.TYPE_SUBMITTED_SELFPOST;
					} else {
						if (CommonUtil.isPictureUrl(ip.url, ip.domain,
								ip.subreddit)) {
							return OverviewListFragment.TYPE_SUBMITTED_LINK;
						} else {

							return OverviewListFragment.TYPE_SUBMITTED_LINK_NOPIC;
						}
					}
				} else {
					return TYPE_COMMENT;
				}
			}
			if (isLoadingData) {
				if (position == 0) {
					return TYPE_EMPTY;
				}
				return TYPE_LAODING;
			} else if (isLoadFailed) {
				if (position == 0) {
					return TYPE_EMPTY;
				}
				return TYPE_LOAD_FAIL;
			} else {
				if (position == 0) {
					return TYPE_DATA_HEADER;
				}
				if (overviewModel.mItemList.size() == 0) {
					return TYPE_NO_ITEM;
				}

				if (overviewModel.after == null
						|| "".equals(overviewModel.after)) {
					return TYPE_NO_MORE;
				}

				else {
					return TYPE_MORE;
				}

			}
		}

		@Override
		public OverviewItem getItem(int position) {
			if (position > 0 && position < overviewModel.mItemList.size() + 1) {
				return overviewModel.mItemList.get(position - 1);
			} else {
				return null;
			}
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		public void setIsLoadingData(boolean isLoadingData, boolean redrawList) {
			this.isLoadingData = isLoadingData;
			if (redrawList) {
				notifyDataSetChanged();
			}
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			OverviewItem item = this.getItem(position);
			int type = this.getItemViewType(position);
			switch (type) {

			case TYPE_LOAD_FAIL:
				if (convertView == null) {
					convertView = mLayoutInflater.inflate(
							R.layout.item_load_fail, null);
				}
				break;

			case OverviewListFragment.TYPE_EMPTY:
				if (convertView == null) {
					convertView = mLayoutInflater.inflate(R.layout.item_empty,
							null);
				}
				break;
			case OverviewListFragment.TYPE_DATA_HEADER:

				Spinner dataSpinner = null;
				if (convertView == null) {
					convertView = (View) mLayoutInflater.inflate(
							R.layout.item_overview_sort, null);

					// sort
					Spinner sortSpinner = (Spinner) convertView
							.findViewById(R.id.overview_sort_spinner);

					ArrayAdapter<CharSequence> sortAdapter = ArrayAdapter
							.createFromResource(
									OverviewListFragment.this.getActivity(),
									R.array.overview_sort_array,
									R.layout.simple_spinner_text_item);

					sortAdapter
							.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);

					sortSpinner.setAdapter(sortAdapter);
					sortSpinner.setSelection(mCurrentSortType);
					sortSpinner
							.setOnItemSelectedListener(OverviewListFragment.this);

					// date
					dataSpinner = (Spinner) convertView
							.findViewById(R.id.overview_date_spinner);

					ArrayAdapter<CharSequence> adapter = ArrayAdapter
							.createFromResource(
									OverviewListFragment.this.getActivity(),
									R.array.date_array,
									R.layout.simple_spinner_text_item);
					adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
					dataSpinner.setAdapter(adapter);
					dataSpinner.setSelection(mCurrentDateType);
					dataSpinner
							.setOnItemSelectedListener(OverviewListFragment.this);
				}

				if (dataSpinner == null) {
					dataSpinner = (Spinner) convertView
							.findViewById(R.id.overview_date_spinner);
				}
				if (mCurrentSortType == SORT_CONTROVERSIAL
						|| mCurrentSortType == SORT_TOP) {
					dataSpinner.setVisibility(View.VISIBLE);
				} else {
					dataSpinner.setVisibility(View.GONE);
				}

				break;
			case OverviewListFragment.TYPE_COMMENT:
				if (convertView == null) {
					convertView = mLayoutInflater.inflate(
							R.layout.item_overview, null);
				}

				TextView post_title = (TextView) convertView
						.findViewById(R.id.post_title);
				TextView post_subreddit = (TextView) convertView
						.findViewById(R.id.post_subreddit);
				TextView comment_date = (TextView) convertView
						.findViewById(R.id.comment_date);
				TextView comment_author = (TextView) convertView
						.findViewById(R.id.comment_author);
				TextView comment_body = (TextView) convertView
						.findViewById(R.id.comment_body);

				post_title.setText(item.link_title);
				post_subreddit.setText(item.subreddit);
				comment_author.setText(item.author);
				comment_date.setText(CommonUtil.getRelateTimeString(
						item.created_utc * 1000,
						OverviewListFragment.this.getActivity()));
				comment_body.setText(item.body);

				setUpContentView(convertView, item, position);
				break;
			case OverviewListFragment.TYPE_SUBMITTED_LINK:
				if (convertView == null) {
					convertView = mLayoutInflater.inflate(
							R.layout.item_subreddit_fragment_link, null);
				}
				SubRedditItem i = item.subRedditItem;
				setUpSubmittedContentView(convertView, i, position);

				// domain
				((TextView) convertView
						.findViewById(R.id.subreddit_info_domain))
						.setText(i.domain);

				ImageView rw = (ImageView) convertView
						.findViewById(R.id.remote_pic);
				rw.setTag(position);
				rw.setOnClickListener(OverviewListFragment.this);
				if ((i.over_18 && mSafeForWork)) {
					rw.setImageResource(R.drawable.nsfw_picture);
				} else {
					if (rw != null)
						((OverviewFragmentActivity) OverviewListFragment.this
								.getActivity()).getImageWorker().loadImage(
								CommonUtil.appendJPG(i.url), rw);
				}

				break;
			case OverviewListFragment.TYPE_SUBMITTED_LINK_NOPIC:
				if (convertView == null) {
					convertView = mLayoutInflater.inflate(
							R.layout.item_subreddit_link_nopic, null);
				}
				SubRedditItem ipc = item.subRedditItem;

				TextView domain = (TextView) convertView
						.findViewById(R.id.subreddit_info_domain);
				domain.setText(ipc.domain);

				// LINK
				((TextView) convertView.findViewById(R.id.link_address))
						.setText(CommonUtil.getShortUrl(ipc.url));

				setUpSubmittedContentView(convertView, ipc, position);
				convertView.findViewById(R.id.view_on_web).setTag(position);
				convertView.findViewById(R.id.view_on_web).setOnClickListener(
						OverviewListFragment.this);

				break;
			case OverviewListFragment.TYPE_SUBMITTED_SELFPOST:
				if (convertView == null) {
					convertView = mLayoutInflater.inflate(
							R.layout.item_subreddit_selfpost, null);
				}
				SubRedditItem ip = item.subRedditItem;
				TextView mContentText = (TextView) convertView
						.findViewById(R.id.subreddit_content_text);

				if (ip.getSelftext() == null
						|| ip.getSelftext().toString().trim().equals("")) {
					mContentText.setVisibility(View.GONE);
				} else {
					mContentText.setText(ip.getSelftext());
				}
				setUpSubmittedContentView(convertView, ip, position);
				break;
			case OverviewListFragment.TYPE_LAODING:
				if (convertView == null) {
					convertView = mLayoutInflater.inflate(
							R.layout.item_loading, null);
				}
				break;
			case OverviewListFragment.TYPE_MORE:
				if (convertView == null) {
					convertView = mLayoutInflater.inflate(R.layout.item_more,
							null);
				}
				break;

			case OverviewListFragment.TYPE_NO_MORE:
				if (convertView == null) {
					convertView = mLayoutInflater.inflate(
							R.layout.item_no_more, null);
				}
				break;
			case OverviewListFragment.TYPE_NO_ITEM:
				if (convertView == null) {
					convertView = mLayoutInflater.inflate(
							R.layout.item_no_item, null);
				}
				break;

			}

			return convertView;
		}
	}

	private void setUpSubmittedContentView(View convertView,
			SubRedditItem item, int position) {

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
				OverviewListFragment.this.getActivity()));

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
		subreddit_info_comment_count.setText(item.num_comments + " comments");

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

		TextView score = (TextView) convertView.findViewById(R.id.vote_score);

		if (score.getTag() == null) {
			score.setTag(score.getCurrentTextColor());
		}

		updateVoteImageAndText(voteUp, voteDown, score, item);

		voteUpWraper.setTag(position);
		voteDownWraper.setTag(position);

		voteUpWraper.setOnClickListener(OverviewListFragment.this);
		voteDownWraper.setOnClickListener(OverviewListFragment.this);
	}

	@Override
	public void onSubredditPopSelected(int position, int action) {
		switch (action) {
		case SubredditPopDialog.ACTION_VOTE_UP:
			if (mOverviewAdapter.getItemViewType(position) == OverviewListFragment.TYPE_COMMENT) {
				OverviewItem itemp = mOverviewAdapter.getItem(position);
				String r = updateVoteImageAndGetResult(itemp, true);
				mOverviewAdapter.notifyDataSetChanged();
				new VoteTask(this, itemp, r, true);
			} else {
				OverviewItem itemp = mOverviewAdapter.getItem(position);
				String r = updateVoteImageAndGetResult(itemp.subRedditItem,
						true);
				mOverviewAdapter.notifyDataSetChanged();
				new VoteTask(this, itemp, r, true);
			}

			break;
		case SubredditPopDialog.ACTION_VOTE_DOWN:

			if (mOverviewAdapter.getItemViewType(position) == OverviewListFragment.TYPE_COMMENT) {
				OverviewItem itemp = mOverviewAdapter.getItem(position);
				String r = updateVoteImageAndGetResult(itemp, false);
				mOverviewAdapter.notifyDataSetChanged();
				new VoteTask(this, itemp, r, false).execute();
			} else {
				OverviewItem itemp = mOverviewAdapter.getItem(position);
				String r = updateVoteImageAndGetResult(itemp.subRedditItem,
						false);
				mOverviewAdapter.notifyDataSetChanged();
				new VoteTask(this, itemp, r, false).execute();
			}
			break;
		case OverviewSubredditPopDialog.ACTION_HIDE:

			mOverviewAdapter.getItem(position).subRedditItem.hidden = true;
			mOverviewAdapter.notifyDataSetChanged();
			new HideTask(this,
					mOverviewAdapter.getItem(position).subRedditItem, true)
					.execute();
			break;
		case OverviewSubredditPopDialog.ACTION_UNHIDE:
			mOverviewAdapter.getItem(position).subRedditItem.hidden = false;
			mOverviewAdapter.notifyDataSetChanged();
			new HideTask(this,
					mOverviewAdapter.getItem(position).subRedditItem, false)
					.execute();
			break;

		case OverviewSubredditPopDialog.ACTION_SAVE:
			mOverviewAdapter.getItem(position).subRedditItem.saved = true;
			mOverviewAdapter.notifyDataSetChanged();
			new SaveTask(this,
					mOverviewAdapter.getItem(position).subRedditItem, true)
					.execute();
			break;

		case OverviewSubredditPopDialog.ACTION_UNSAVE:
			mOverviewAdapter.getItem(position).subRedditItem.saved = false;
			mOverviewAdapter.notifyDataSetChanged();
			new SaveTask(this,
					mOverviewAdapter.getItem(position).subRedditItem, false)
					.execute();
			break;
		}

	}

	// ---------------------- SAVE --------------------
	public static class SaveTask extends AsyncTask<Void, Void, String> {
		String name;
		boolean save;
		WeakReference<SubRedditItem> subRedditItemWF;
		WeakReference<OverviewListFragment> overviewListFragmentWF;

		public SaveTask(OverviewListFragment fragment, SubRedditItem item,
				boolean s) {
			save = s;
			name = item.name;
			subRedditItemWF = new WeakReference<SubRedditItem>(item);
			overviewListFragmentWF = new WeakReference<OverviewListFragment>(
					fragment);
		}

		@Override
		protected String doInBackground(Void... params) {
			if (overviewListFragmentWF.get() != null
					&& overviewListFragmentWF.get().getActivity() != null) {
				return SubRedditManager.saveSubRedditPost(name, save,
						overviewListFragmentWF.get().getActivity());
			} else {
				this.cancel(true);
				return Common.RESULT_TASK_CANCLE;
			}
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			if (result == Common.RESULT_TASK_CANCLE
					|| overviewListFragmentWF.get() == null
					|| subRedditItemWF.get() == null
					|| overviewListFragmentWF.get().getActivity() == null) {
				return;
			}

			if (Common.RESULT_SUCCESS.equals(result)) {
				Toast.makeText(overviewListFragmentWF.get().getActivity(),
						save ? "Save succeeded!" : "Unsave succeeded!",
						Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(overviewListFragmentWF.get().getActivity(),
						"Save failed, try again latter!", Toast.LENGTH_SHORT)
						.show();
				subRedditItemWF.get().saved = !save;
				overviewListFragmentWF.get().mOverviewAdapter
						.notifyDataSetChanged();
			}

		}
	}

	// ------------ hide --------------
	public static class HideTask extends AsyncTask<Void, Void, String> {
		String name;
		boolean hide;
		WeakReference<SubRedditItem> subRedditItemWF;
		WeakReference<OverviewListFragment> overviewListFragmentWF;

		public HideTask(OverviewListFragment fragment, SubRedditItem item,
				boolean h) {
			hide = h;
			name = item.name;
			subRedditItemWF = new WeakReference<SubRedditItem>(item);
			overviewListFragmentWF = new WeakReference<OverviewListFragment>(
					fragment);
		}

		@Override
		protected String doInBackground(Void... params) {
			if (overviewListFragmentWF.get() != null) {
				return SubRedditManager.hideSubReddit(name, hide,
						overviewListFragmentWF.get().getActivity());
			} else {
				this.cancel(true);
				return Common.RESULT_TASK_CANCLE;
			}

		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			if (result == Common.RESULT_TASK_CANCLE
					|| overviewListFragmentWF.get() == null
					|| subRedditItemWF.get() == null
					|| overviewListFragmentWF.get().getActivity() == null) {
				return;
			}

			if (result == Common.RESULT_SUCCESS) {
				Toast.makeText(overviewListFragmentWF.get().getActivity(),
						hide ? "Hide succeeded!" : "Unhiden succeeded!",
						Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(overviewListFragmentWF.get().getActivity(),
						"Hide failed!", Toast.LENGTH_LONG).show();
				subRedditItemWF.get().hidden = !hide;
				overviewListFragmentWF.get().mOverviewAdapter
						.notifyDataSetChanged();
			}
		}
	}

	private String updateVoteImageAndGetResult(OverviewItem item, boolean isUp) {
		item.old_like = item.likes;
		if (isUp) {
			if (item.likes == null) {
				item.likes = true;
				item.ups += 1;
			} else if (item.likes) {
				// cancel the like
				item.likes = null;
				item.ups = -1;
			} else if (!item.likes) {
				// change to like
				item.likes = true;
				item.ups += 1;
				item.downs -= 1;
			}
		}

		if (!isUp) {
			if (item.likes == null) {
				item.likes = false;
				item.downs -= 1;
			} else if (item.likes) {
				item.likes = false;
				item.ups -= 1;
				item.downs += 1;
			} else if (!item.likes) {
				item.likes = null;
				item.downs -= 1;
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

		return dir;
	}

	/**
	 * update the vote image and return the vote result that VoteTask need
	 * 
	 * @param item
	 * @param isUp
	 * @return
	 */
	private String updateVoteImageAndGetResult(SubRedditItem item, boolean isUp) {
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

	private void setUpContentView(View convertView, OverviewItem item,
			int position) {

		LinearLayout voteUpWraper = (LinearLayout) convertView
				.findViewById(R.id.vote_up_wraper);
		LinearLayout voteDownWraper = (LinearLayout) convertView
				.findViewById(R.id.vote_down_wraper);

		// vote
		ImageView voteUp = (ImageView) convertView
				.findViewById(R.id.vote_up_image);
		ImageView voteDown = (ImageView) convertView
				.findViewById(R.id.vote_down_image);

		// save the grey one
		if (voteUp.getTag() == null)
			voteUp.setTag(voteUp.getDrawable());

		if (voteDown.getTag() == null)
			voteDown.setTag(voteDown.getDrawable());

		TextView score = (TextView) convertView.findViewById(R.id.vote_score);

		if (score.getTag() == null) {
			score.setTag(score.getCurrentTextColor());
		}

		updateVoteImageAndText(voteUp, voteDown, score, item);

		voteUpWraper.setTag(position);
		voteDownWraper.setTag(position);
		voteUpWraper.setOnClickListener(OverviewListFragment.this);
		voteDownWraper.setOnClickListener(OverviewListFragment.this);
	}

	private void updateVoteImageAndText(ImageView voteUp, ImageView voteDown,
			TextView score, SubRedditItem item) {
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
			score.setTextColor(getResources().getColor(R.color.vote_up_color));
		}

		else if (!item.likes) {
			// voteUp.setImageResource(R.drawable.vote_up_grey);
			voteUp.setImageDrawable((Drawable) voteUp.getTag());
			voteDown.setImageResource(R.drawable.vote_down_selected);
			score.setTextColor(getResources().getColor(R.color.vote_down_color));
		}
		score.setText(item.score + "");
	}

	private void updateVoteImageAndText(ImageView voteUp, ImageView voteDown,
			TextView score, OverviewItem item) {
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
			score.setTextColor(getResources().getColor(R.color.vote_up_color));
		}

		else if (!item.likes) {
			// voteUp.setImageResource(R.drawable.vote_up_grey);
			voteUp.setImageDrawable((Drawable) voteUp.getTag());
			voteDown.setImageResource(R.drawable.vote_down_selected);
			score.setTextColor(getResources().getColor(R.color.vote_down_color));
		}
		long ss = item.ups - item.downs;
		score.setText(ss + "");
	}

	// -------------------vote ------------------
	public static class VoteTask extends AsyncTask<Void, Void, String> {
		String name;
		boolean isUp;
		String dir = "0";
		boolean isOverview;
		WeakReference<OverviewListFragment> fragmentWF;
		WeakReference<OverviewItem> overviewItemWF;
		WeakReference<SubRedditItem> subredditItemWF;

		public VoteTask(OverviewListFragment fragment, OverviewItem s,
				String d, boolean up) {
			fragmentWF = new WeakReference<OverviewListFragment>(fragment);
			overviewItemWF = new WeakReference<OverviewItem>(s);
			isUp = up;
			dir = d;
			name = s.name;
			isOverview = true;
		}

		public VoteTask(OverviewListFragment fragment, SubRedditItem s,
				String d, boolean up) {
			fragmentWF = new WeakReference<OverviewListFragment>(fragment);
			subredditItemWF = new WeakReference<SubRedditItem>(s);
			isUp = up;
			dir = d;
			name = s.name;
			isOverview = false;
		}

		@Override
		protected String doInBackground(Void... params) {
			if (fragmentWF.get() != null
					&& fragmentWF.get().getActivity() != null) {
				return SubRedditManager.voteSubRedditPost(name, dir, fragmentWF
						.get().getActivity());
			} else {
				this.cancel(true);
				return Common.RESULT_TASK_CANCLE;
			}
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);

			if (result == Common.RESULT_TASK_CANCLE || fragmentWF.get() == null
					|| fragmentWF.get().getActivity() == null) {
				return;
			}

			if (result == Common.RESULT_SUCCESS) {
				Toast.makeText(fragmentWF.get().getActivity(),
						"Vote succeeded!", Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(fragmentWF.get().getActivity(), "Vote failed!",
						Toast.LENGTH_SHORT).show();
				// roll back
				if (isOverview) {
					if (overviewItemWF != null && overviewItemWF.get() != null) {
						overviewItemWF.get().likes = overviewItemWF.get().old_like;
						fragmentWF.get().mOverviewAdapter
								.notifyDataSetChanged();
					}
				} else {
					if (subredditItemWF != null
							&& subredditItemWF.get() != null) {
						subredditItemWF.get().likes = subredditItemWF.get().old_like;
						fragmentWF.get().mOverviewAdapter
								.notifyDataSetChanged();
					}
				}
			}
		}

	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		switch (parent.getId()) {
		case R.id.overview_sort_spinner:
			if (mCurrentSortType == position) {
				return;
			} else {
				mCurrentSortType = position;
				updateDataSate();
			}
			break;
		case R.id.overview_date_spinner:
			if (mCurrentDateType == position) {
				return;
			} else {
				mCurrentDateType = position;
				updateDataSate();
			}
			break;
		}

	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {

	}

	@Override
	public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
			int position, long id) {
		switch (mOverviewAdapter.getItemViewType(position)) {
		case TYPE_SUBMITTED_LINK:
		case TYPE_SUBMITTED_SELFPOST:
		case TYPE_SUBMITTED_LINK_NOPIC:
			// show the submit dialog
			new OverviewSubredditPopDialog(this.getActivity())
					.setOnSubredditPopSelectedListener(this)
					.setSubRedditItem(
							mOverviewAdapter.getItem(position).subRedditItem,
							position).show();
			break;

		case TYPE_COMMENT:
			// show the comment dialog
			new OverviewCommentPopDialog(this).setOverviewItemItem(
					mOverviewAdapter.getItem(position), position).show();
			break;
		}
		return false;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {

		case R.id.view_on_web:

			int position = (Integer) v.getTag();
			SubRedditItem st = mOverviewAdapter.getItem(position).subRedditItem;
			if (st == null) {
				return;
			}
			Intent tt = null;
			// picture
			if (CommonUtil.isPictureUrl(st.url, st.domain, st.subreddit)) {
				tt = new Intent(OverviewListFragment.this.getActivity(),
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
				tt = new Intent(OverviewListFragment.this.getActivity(),
						WebviewActivity.class);
				tt.putExtra(Common.EXTRA_SUBREDDIT, st);
				this.startActivityForResult(tt, Common.REQUEST_ACTIVITY_WEBVIEW);
			}

			break;

		case R.id.remote_pic:
			int ppr = (Integer) v.getTag();
			SubRedditItem itemR = mOverviewAdapter.getItem(ppr).subRedditItem;
			if (itemR == null) {
				return;
			}
			Intent picIntent = new Intent(
					OverviewListFragment.this.getActivity(),
					ImageViewActivity.class);
			picIntent.putExtra(Common.EXTRA_SUBREDDIT, itemR);
			this.startActivityForResult(picIntent,
					Common.REQUEST_ACTIVITY_WEBVIEW);

			break;

		// must be the subreddit item
		case R.id.subreddit_comment:
			int pss = (Integer) v.getTag();
			SubRedditItem item = mOverviewAdapter.getItem(pss).subRedditItem;
			Intent t = new Intent(OverviewListFragment.this.getActivity(),
					CommentFragmentActivity.class);
			t.putExtra(Common.INTENT_EXTRA_SUBREDDIT, item);
			this.startActivityForResult(t, Common.REQUEST_ACTIVITY_COMMENT);
			break;

		case R.id.vote_up_wraper:

			// check login
			if (!RedditManager.isUserAuth(OverviewListFragment.this
					.getActivity())) {
				Toast.makeText(OverviewListFragment.this.getActivity(),
						this.getString(R.string.login_request),
						Toast.LENGTH_SHORT).show();
				return;
			}
			int pp = (Integer) v.getTag(); // get position
			if (mOverviewAdapter.getItemViewType(pp) == OverviewListFragment.TYPE_COMMENT) {
				OverviewItem itemp = mOverviewAdapter.getItem(pp);
				String r = updateVoteImageAndGetResult(itemp, true);
				mOverviewAdapter.notifyDataSetChanged();
				new VoteTask(this, itemp, r, true);
			} else {
				OverviewItem itemp = mOverviewAdapter.getItem(pp);
				String r = updateVoteImageAndGetResult(itemp.subRedditItem,
						true);
				mOverviewAdapter.notifyDataSetChanged();
				new VoteTask(this, itemp, r, true);
			}

			break;
		case R.id.vote_down_wraper:

			// check login
			if (!RedditManager.isUserAuth(OverviewListFragment.this
					.getActivity())) {
				Toast.makeText(OverviewListFragment.this.getActivity(),
						this.getString(R.string.login_request),
						Toast.LENGTH_SHORT).show();
				return;
			}

			int pd = (Integer) v.getTag();
			if (mOverviewAdapter.getItemViewType(pd) == OverviewListFragment.TYPE_COMMENT) {
				OverviewItem itemp = mOverviewAdapter.getItem(pd);
				String r = updateVoteImageAndGetResult(itemp, false);
				mOverviewAdapter.notifyDataSetChanged();
				new VoteTask(this, itemp, r, false).execute();
			} else {
				OverviewItem itemp = mOverviewAdapter.getItem(pd);
				String r = updateVoteImageAndGetResult(itemp.subRedditItem,
						false);
				mOverviewAdapter.notifyDataSetChanged();
				new VoteTask(this, itemp, r, false).execute();
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
						SubRedditItem i = mOverviewAdapter.overviewModel.mItemList
								.get(subRedditItem.position).subRedditItem;
						if (i != null) {
							i.setSubRedditItem(subRedditItem);
							mOverviewAdapter.notifyDataSetChanged();
						}
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
				if (isComment && item != null) {
					Intent t = new Intent(
							OverviewListFragment.this.getActivity(),
							CommentFragmentActivity.class);
					t.putExtra(Common.INTENT_EXTRA_SUBREDDIT, item);
					this.startActivityForResult(t,
							Common.REQUEST_ACTIVITY_COMMENT);
				} else {
					// update data
					if (item != null) {
						try {
							SubRedditItem i = mOverviewAdapter
									.getItem(item.position).subRedditItem;
							if (i != null) {
								i.setSubRedditItem(item);
								mOverviewAdapter.notifyDataSetChanged();
							}
						} catch (Exception e) {
							// do nothing;
						}
					}
				}

			}
			break;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position,
			long id) {
		int type = mOverviewAdapter.getItemViewType(position);
		switch (type) {
		case OverviewListFragment.TYPE_LOAD_FAIL:
			mOverviewAdapter.isLoadFailed = false;
			if (mOverviewAdapter.overviewModel.after != null
					&& !"".equals(mOverviewAdapter.overviewModel.after.trim())) {
				updateDataSate(mOverviewAdapter.overviewModel.after);
			} else {
				updateDataSate();
			}
			break;

		case TYPE_COMMENT:
			// start the overview
			String linkId = mOverviewAdapter.getItem(position).link_id;
			if (linkId == null || linkId.length() < 4) {
				Toast.makeText(getActivity(), "Comments missing!",
						Toast.LENGTH_SHORT).show();
			}

			// http://www.reddit.com/comments/wj1qj.json
			String ld = linkId.substring(3, linkId.length());
			String comment_linkId = "comments/" + ld;
			Intent tt = new Intent(this.getActivity(),
					OverviewCommentActivity.class);

			tt.putExtra(Common.EXTRA_OVERVIEW_LOAD_TYPE,
					OverviewCommentActivity.LOAD_TYPE_LINK_ID);
			tt.putExtra(Common.EXTRA_OVERVIEW_COMMENT_URL, comment_linkId);
			this.getActivity().startActivity(tt);
			break;

		case OverviewListFragment.TYPE_MORE:
			updateDataSate(mOverviewAdapter.overviewModel.after);
			break;

		case OverviewListFragment.TYPE_SUBMITTED_LINK:
		case OverviewListFragment.TYPE_SUBMITTED_LINK_NOPIC:
		case OverviewListFragment.TYPE_SUBMITTED_SELFPOST:
			SubRedditItem item = mOverviewAdapter.getItem(position).subRedditItem;
			if (item == null) {
				return;
			}
			Intent t = new Intent(OverviewListFragment.this.getActivity(),
					CommentFragmentActivity.class);
			t.putExtra(Common.INTENT_EXTRA_SUBREDDIT, item);
			this.startActivityForResult(t, Common.REQUEST_ACTIVITY_COMMENT);
			break;
		}
	}

	@Override
	public void onOverviewCommentDialogSelected(View v,
			OverviewItem overviewItem, int position) {
		switch (v.getId()) {
		case R.id.overview_all_comment:
			// start the overview
			String linkId = overviewItem.link_id;
			if (linkId == null || linkId.length() < 4) {
				Toast.makeText(getActivity(), "Comments missing!",
						Toast.LENGTH_SHORT).show();
			}

			// http://www.reddit.com/comments/wj1qj.json
			String ld = linkId.substring(3, linkId.length());
			String comment_linkId = "comments/" + ld;
			Intent tt = new Intent(this.getActivity(),
					OverviewCommentActivity.class);

			tt.putExtra(Common.EXTRA_OVERVIEW_LOAD_TYPE,
					OverviewCommentActivity.LOAD_TYPE_LINK_ID);
			tt.putExtra(Common.EXTRA_OVERVIEW_COMMENT_URL, comment_linkId);
			this.getActivity().startActivity(tt);
			break;
		case R.id.comment_vote_up:
			String dir = updateVoteImageAndGetResult(overviewItem, true);
			mOverviewAdapter.notifyDataSetChanged();
			new VoteTask(this, overviewItem, dir, true).execute();
			break;
		case R.id.comment_vote_down:
			String d = updateVoteImageAndGetResult(overviewItem, false);
			mOverviewAdapter.notifyDataSetChanged();
			new VoteTask(this, overviewItem, d, false).execute();
			break;
		}

	}
}
