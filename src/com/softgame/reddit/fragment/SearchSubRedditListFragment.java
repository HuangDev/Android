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
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.util.Log;
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
import com.softgame.reddit.SearchSubRedditActivity;
import com.softgame.reddit.WebviewActivity;
import com.softgame.reddit.dialog.SubredditPopDialog;
import com.softgame.reddit.impl.OnSubredditPopSelectedListener;
import com.softgame.reddit.model.SubRedditItem;
import com.softgame.reddit.model.SubRedditModel;
import com.softgame.reddit.utils.Common;
import com.softgame.reddit.utils.CommonUtil;
import com.softgame.reddit.utils.RedditManager;
import com.softgame.reddit.utils.SubRedditManager;

public class SearchSubRedditListFragment extends ListFragment implements
		OnItemClickListener, OnItemLongClickListener, OnClickListener,
		OnSubredditPopSelectedListener, OnItemSelectedListener {
	public static final String TAG = "SubRedditListFragment";

	public static final int SEARCH_SORT_SPINNER_ID = 0x1352;
	LayoutInflater mLayoutInflater;

	public WeakReference<DataTask> mDataTaskWF;

	public SubRedditAdapter mSubRedditAdapter;

	public String mCurrentSubReddit;

	public int mCurrentSort;
	public boolean mRestrict_ON = false;
	public String mSearchItem;

	boolean mSafeForWork;
	boolean mUsedEmbedView;

	public SearchSubRedditListFragment() {
	}

	public static SearchSubRedditListFragment findOrCreateSubRedditRetainFragment(
			int containerId, FragmentManager manager, String subreddit, long id) {

		SearchSubRedditListFragment fragment = (SearchSubRedditListFragment) manager
				.findFragmentByTag(SearchSubRedditListFragment.TAG + id);
		if (fragment == null) {
			fragment = new SearchSubRedditListFragment();
			final FragmentTransaction ft = manager.beginTransaction();
			Bundle bundler = new Bundle();
			bundler.putString("subreddit", subreddit);
			fragment.setArguments(bundler);
			ft.add(containerId, fragment, SearchSubRedditListFragment.TAG + id);
			ft.commit();
		}
		return fragment;
	}

	public void setRestrictOn(boolean isOn) {
		if (mRestrict_ON != isOn) {
			mRestrict_ON = isOn;
		}
		if (mSearchItem != null && !"".equals(mSearchItem)) {
			updateDataSate(mSearchItem);
		}
	}

	public void setSearchItem(String searchItem) {
		if (searchItem == null || "".equals(searchItem)
				|| searchItem.equals(mSearchItem)) {
			return;
		}
		mSearchItem = searchItem;
		updateDataSate(mSearchItem);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setRetainInstance(true);
		mCurrentSubReddit = getArguments() != null ? getArguments().getString(
				"subreddit") : Common.DEFAULT_SUBREDDIT;

		Log.d(TAG, "current subreddit:" + mCurrentSubReddit);
		mSubRedditAdapter = new SubRedditAdapter();

		SharedPreferences df = PreferenceManager
				.getDefaultSharedPreferences(this.getActivity());

		mSafeForWork = df.getBoolean(
				this.getString(R.string.pref_safe_for_work), true);

		mUsedEmbedView = df.getBoolean(
				this.getString(R.string.pref_key_embed_view), true);

		if (mSearchItem != null) {
			updateDataSate(mSearchItem);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mLayoutInflater = inflater;
		return inflater.inflate(R.layout.subreddit_fragment_listview,
				container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		this.getListView().setAdapter(mSubRedditAdapter);
		this.getListView().setOnItemClickListener(this);
		this.getListView().setOnItemLongClickListener(this);
	}

	/**
	 * update the current data to receive new data
	 */
	public void updateDataSate(String search, String after) {
		if (mDataTaskWF != null && mDataTaskWF.get() != null)
			mDataTaskWF.get().cancel(true);
		DataTask d = new DataTask(search, after, this);
		mDataTaskWF = new WeakReference<SearchSubRedditListFragment.DataTask>(d);
		d.execute();
	}

	public void updateDataSate(String search) {
		if (mDataTaskWF != null && mDataTaskWF.get() != null)
			mDataTaskWF.get().cancel(true);
		DataTask d = new DataTask(search, this);
		mDataTaskWF = new WeakReference<SearchSubRedditListFragment.DataTask>(d);
		d.execute();
	}

	public class SubRedditAdapter extends BaseAdapter {

		private static final int TYPE_COUNT = 10;
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
			case Common.SEARCH_TYPE_SORT:
			case Common.SEARCH_TYPE_LAODING:
			case Common.SEARCH_TYPE_NO_MORE:
			case Common.SEARCH_TYPE_NO_ITEM:
			case Common.SEARCH_TYPE_EMPTY:
				return false;
			}
			return true;
		}

		@Override
		public int getItemViewType(int position) {
			if (position == 0) {
				if (isLoadingData) {
					return Common.SEARCH_TYPE_EMPTY;
				}
				if (mSearchItem == null || "".equals(mSearchItem)) {
					return Common.SEARCH_TYPE_EMPTY;
				}
				return Common.SEARCH_TYPE_SORT;
			}
			// not include 0
			if (position > 0 && position <= subRedditModel.getSize()) {
				SubRedditItem item = (SubRedditItem) getItem(position);
				if (item.is_self) {
					return Common.SEARCH_TYPE_ITEM_SELFPOST;
				} else {
					if (item.thumbnail == null || "".equals(item.thumbnail)
							|| "default".equals(item.thumbnail)) {
						return Common.SEARCH_TYPE_ITEM_LINK_NOPIC;
					}
					return Common.SEARCH_TYPE_ITEM_LINK;
				}
			}
			if (isLoadingData) {
				return Common.SEARCH_TYPE_LAODING;
			} else if (isFailed) {
				return Common.SEARCH_TYPE_LOAD_FAIL;
			} else {
				if (subRedditModel.getSize() == 0) {
					return Common.SEARCH_TYPE_NO_ITEM;
				}
				if (subRedditModel.after == null
						|| "".equals(subRedditModel.after)) {
					return Common.SEARCH_TYPE_NO_MORE;
				} else {
					return Common.SEARCH_TYPE_MORE;
				}

			}
		}

		@Override
		public int getViewTypeCount() {
			return TYPE_COUNT;
		}

		@Override
		public SubRedditItem getItem(int position) {
			if (position > 0 && position <= subRedditModel.getSize()) {
				return subRedditModel.getItemByIndex(position - 1);
			}
			return null;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			SubRedditItem item = this.getItem(position);
			int type = this.getItemViewType(position);
			switch (type) {
			case Common.SEARCH_TYPE_EMPTY:
				if (convertView == null) {
					convertView = mLayoutInflater.inflate(R.layout.item_empty,
							null);
				}
				break;

			case Common.SEARCH_TYPE_LOAD_FAIL:
				if (convertView == null) {
					convertView = mLayoutInflater.inflate(
							R.layout.item_load_fail, null);
				}
				break;

			case Common.SEARCH_TYPE_SORT:
				if (convertView == null) {
					convertView = (View) mLayoutInflater.inflate(
							R.layout.item_date_spinner, null);

					Spinner dateSpinner = (Spinner) convertView
							.findViewById(R.id.date_spinner);
					dateSpinner.setId(SEARCH_SORT_SPINNER_ID);
					ArrayAdapter<CharSequence> adapter = ArrayAdapter
							.createFromResource(
									SearchSubRedditListFragment.this
											.getActivity(),
									R.array.search_sort_array,
									R.layout.simple_spinner_text_item);
					adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
					dateSpinner.setAdapter(adapter);
					dateSpinner
							.setSelection(SearchSubRedditListFragment.this.mCurrentSort);
					dateSpinner
							.setOnItemSelectedListener(SearchSubRedditListFragment.this);
				}

				break;
			case Common.SEARCH_TYPE_ITEM_LINK:
				if (convertView == null) {
					convertView = mLayoutInflater.inflate(
							R.layout.item_subreddit_fragment_link, null);
				}
				setUpContentView(convertView, item, position);
				ImageView rw = (ImageView) convertView
						.findViewById(R.id.remote_pic);
				rw.setTag(position);
				rw.setOnClickListener(SearchSubRedditListFragment.this);
				if ((item.over_18 && mSafeForWork)) {
					rw.setImageResource(R.drawable.nsfw_picture);
				} else {
					if (rw != null)
						((SearchSubRedditActivity) SearchSubRedditListFragment.this
								.getActivity()).getImageWorker().loadImage(
								CommonUtil.appendJPG(item.url), rw);
				}

				// domain
				((TextView) convertView
						.findViewById(R.id.subreddit_info_domain))
						.setText(item.domain);
				break;

			case Common.SEARCH_TYPE_ITEM_LINK_NOPIC:
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
						SearchSubRedditListFragment.this);
				break;
			case Common.SEARCH_TYPE_ITEM_SELFPOST:

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
				}

				setUpContentView(convertView, item, position);

				break;
			case Common.SEARCH_TYPE_LAODING:
				if (convertView == null) {
					convertView = mLayoutInflater.inflate(
							R.layout.item_loading, null);
				}
				break;
			case Common.SEARCH_TYPE_MORE:
				if (convertView == null) {
					convertView = mLayoutInflater.inflate(R.layout.item_more,
							null);
				}
				break;

			case Common.SEARCH_TYPE_NO_MORE:
				if (convertView == null) {
					convertView = mLayoutInflater.inflate(
							R.layout.item_no_more, null);
				}
				break;
			case Common.SEARCH_TYPE_NO_ITEM:
				if (convertView == null) {
					convertView = mLayoutInflater.inflate(
							R.layout.item_no_item, null);
				}
				TextView t = (TextView) convertView
						.findViewById(R.id.item_no_item);
				if (mSearchItem == null || "".equals(mSearchItem)) {
					t.setText("");
				} else {
					t.setText(R.string.label_no_item);
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
					SearchSubRedditListFragment.this.getActivity()));

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

			voteUpWraper.setOnClickListener(SearchSubRedditListFragment.this);
			voteDownWraper.setOnClickListener(SearchSubRedditListFragment.this);

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
			return subRedditModel.getSize() + 2;
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

	private static class DataTask extends AsyncTask<Void, Void, String> {
		WeakReference<SearchSubRedditListFragment> mSubRedditRetainFragmentWF;
		String after;
		SubRedditModel model;
		String searchItem;

		public DataTask(String search,
				final SearchSubRedditListFragment fragment) {
			searchItem = search;
			mSubRedditRetainFragmentWF = new WeakReference<SearchSubRedditListFragment>(
					fragment);
		}

		public DataTask(String search, final String a,
				final SearchSubRedditListFragment fragment) {
			searchItem = search;
			mSubRedditRetainFragmentWF = new WeakReference<SearchSubRedditListFragment>(
					fragment);
			after = a;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			SearchSubRedditListFragment fragment = mSubRedditRetainFragmentWF
					.get();
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

			if (mSubRedditRetainFragmentWF == null
					|| mSubRedditRetainFragmentWF.get() == null
					|| mSubRedditRetainFragmentWF.get().getActivity() == null) {
				this.cancel(true);
				return Common.RESULT_TASK_CANCLE;
			}

			SearchSubRedditListFragment fragment = mSubRedditRetainFragmentWF
					.get();
			if (fragment != null) {
				JSONObject dataJSON = new JSONObject();
				String result = SubRedditManager.getRedditSearch(
						fragment.mCurrentSubReddit, searchItem,
						Common.SEARCH_SORT_VALUE_ARRAY[fragment.mCurrentSort],
						fragment.mRestrict_ON, after, dataJSON,
						fragment.getActivity());
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

			if (mSubRedditRetainFragmentWF == null
					|| mSubRedditRetainFragmentWF.get() == null
					|| mSubRedditRetainFragmentWF.get().getActivity() == null
					|| result.equals(Common.RESULT_TASK_CANCLE)) {
				return;
			}


			SearchSubRedditListFragment fragment = mSubRedditRetainFragmentWF
					.get();

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
				fragment.mSubRedditAdapter.setIsLoadingData(false, true);
			} else {
				fragment.mSubRedditAdapter.isFailed = true;
				fragment.mSubRedditAdapter.setIsLoadingData(false, true);
				Toast.makeText(fragment.getActivity(), "Load failed!",
						Toast.LENGTH_SHORT).show();
			}
		}

	}

	// -------------------vote ------------------
	public static class VoteTask extends AsyncTask<Void, Void, String> {
		String name;
		boolean isUp;
		String dir = "0";
		WeakReference<SearchSubRedditListFragment> subRedditRetainFragmentWF;
		WeakReference<SubRedditItem> subRedditItemWF;

		public VoteTask(SearchSubRedditListFragment fragment, SubRedditItem s,
				String d, boolean up) {
			subRedditRetainFragmentWF = new WeakReference<SearchSubRedditListFragment>(
					fragment);
			subRedditItemWF = new WeakReference<SubRedditItem>(s);
			isUp = up;
			dir = d;
			name = s.name;
		}

		@Override
		protected String doInBackground(Void... params) {
			if (subRedditRetainFragmentWF.get() != null
					&& subRedditRetainFragmentWF.get().getActivity() != null) {
				return SubRedditManager.voteSubRedditPost(name, dir,
						subRedditRetainFragmentWF.get().getActivity());
			} else {
				this.cancel(true);
				return Common.RESULT_TASK_CANCLE;
			}
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			if (result == Common.RESULT_TASK_CANCLE
					|| subRedditRetainFragmentWF.get() == null
					|| subRedditItemWF.get() == null
					|| subRedditRetainFragmentWF.get().getActivity() == null) {
				return;
			}
			if (result == Common.RESULT_SUCCESS) {
				Toast.makeText(subRedditRetainFragmentWF.get().getActivity(),
						"Vote succeeded!", Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(subRedditRetainFragmentWF.get().getActivity(),
						"Vote failed!", Toast.LENGTH_SHORT).show();

				// roll back
				subRedditItemWF.get().likes = subRedditItemWF.get().old_like;
				subRedditRetainFragmentWF.get().mSubRedditAdapter
						.notifyDataSetChanged();
			}
		}

	}

	// ---------------------- SAVE --------------------
	public static class SaveTask extends AsyncTask<Void, Void, String> {
		String name;
		boolean save;
		WeakReference<SubRedditItem> subRedditItemWF;
		WeakReference<SearchSubRedditListFragment> subRedditRetainFragmentWF;

		public SaveTask(SearchSubRedditListFragment fragment,
				SubRedditItem item, boolean s) {
			save = s;
			name = item.name;
			subRedditItemWF = new WeakReference<SubRedditItem>(item);
			subRedditRetainFragmentWF = new WeakReference<SearchSubRedditListFragment>(
					fragment);
		}

		@Override
		protected String doInBackground(Void... params) {
			if (subRedditRetainFragmentWF.get() != null
					&& subRedditRetainFragmentWF.get().getActivity() != null) {
				return SubRedditManager.saveSubRedditPost(name, save,
						subRedditRetainFragmentWF.get().getActivity());
			} else {
				this.cancel(true);
				return Common.RESULT_TASK_CANCLE;
			}
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			if (result == Common.RESULT_TASK_CANCLE
					|| subRedditRetainFragmentWF.get() == null
					|| subRedditItemWF.get() == null
					|| subRedditRetainFragmentWF.get().getActivity() == null) {
				return;
			}

			if (Common.RESULT_SUCCESS.equals(result)) {
				Toast.makeText(subRedditRetainFragmentWF.get().getActivity(),
						save ? "Save succeeded!" : "Unsave succeeded!",
						Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(subRedditRetainFragmentWF.get().getActivity(),
						"Save failed, try again latter!", Toast.LENGTH_SHORT)
						.show();
				subRedditItemWF.get().saved = !save;
				subRedditRetainFragmentWF.get().mSubRedditAdapter
						.notifyDataSetChanged();
			}

		}
	}

	// ------------ hide --------------
	public static class HideTask extends AsyncTask<Void, Void, String> {
		String name;
		boolean hide;
		WeakReference<SubRedditItem> subRedditItemWF;
		WeakReference<SearchSubRedditListFragment> subRedditRetainFragmentWF;

		public HideTask(SearchSubRedditListFragment fragment,
				SubRedditItem item, boolean h) {
			hide = h;
			name = item.name;
			subRedditItemWF = new WeakReference<SubRedditItem>(item);
			subRedditRetainFragmentWF = new WeakReference<SearchSubRedditListFragment>(
					fragment);
		}

		@Override
		protected String doInBackground(Void... params) {
			if (subRedditRetainFragmentWF.get() != null) {
				return SubRedditManager.hideSubReddit(name, hide,
						subRedditRetainFragmentWF.get().getActivity());
			} else {
				this.cancel(true);
				return Common.RESULT_TASK_CANCLE;
			}

		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			if (result == Common.RESULT_TASK_CANCLE
					|| subRedditRetainFragmentWF.get() == null
					|| subRedditItemWF.get() == null) {
				return;
			}

			if (result == Common.RESULT_SUCCESS) {
				Toast.makeText(subRedditRetainFragmentWF.get().getActivity(),
						hide ? "Hide succeeded!" : "Unhiden succeeded!",
						Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(subRedditRetainFragmentWF.get().getActivity(),
						"Hide failed!", Toast.LENGTH_LONG).show();
				subRedditItemWF.get().hidden = !hide;
				subRedditRetainFragmentWF.get().mSubRedditAdapter
						.notifyDataSetChanged();
			}
		}
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
			int position, long id) {
		switch (mSubRedditAdapter.getItemViewType(position)) {
		case Common.SUBREDDIT_TYPE_ITEM_LINK:
		case Common.SUBREDDIT_TYPE_ITEM_LINK_NOPIC:
		case Common.SUBREDDIT_TYPE_ITEM_SELFPOST:
			if (((SearchSubRedditActivity) SearchSubRedditListFragment.this
					.getActivity()).mSubRedditPopDialog == null) {
				((SearchSubRedditActivity) SearchSubRedditListFragment.this
						.getActivity()).mSubRedditPopDialog = new SubredditPopDialog(
						SearchSubRedditListFragment.this.getActivity());
				((SearchSubRedditActivity) SearchSubRedditListFragment.this
						.getActivity()).mSubRedditPopDialog
						.setOwnerActivity(SearchSubRedditListFragment.this
								.getActivity());
			}
			((SearchSubRedditActivity) SearchSubRedditListFragment.this
					.getActivity()).mSubRedditPopDialog.dismiss();

			((SearchSubRedditActivity) SearchSubRedditListFragment.this
					.getActivity()).mSubRedditPopDialog.setSubRedditItem(
					mSubRedditAdapter.getItem(position), position);
			((SearchSubRedditActivity) SearchSubRedditListFragment.this
					.getActivity()).mSubRedditPopDialog
					.setOnSubredditPopSelectedListener(this);
			((SearchSubRedditActivity) SearchSubRedditListFragment.this
					.getActivity()).mSubRedditPopDialog.show();
			break;
		}

		return true;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// if it is more, then change to loading
		int type = mSubRedditAdapter.getItemViewType(position);
		switch (type) {
		case Common.SEARCH_TYPE_ITEM_LINK:
		case Common.SEARCH_TYPE_ITEM_LINK_NOPIC:
		case Common.SEARCH_TYPE_ITEM_SELFPOST:
			SubRedditItem item = mSubRedditAdapter.getItem(position);
			if (item == null) {
				return;
			}
			Intent t = new Intent(SearchSubRedditListFragment.this.getActivity(),
					CommentFragmentActivity.class);
			t.putExtra(Common.INTENT_EXTRA_SUBREDDIT, item);
			this.startActivityForResult(t, Common.REQUEST_ACTIVITY_COMMENT);
			break;
		case Common.SEARCH_TYPE_MORE:
			mSubRedditAdapter.setIsLoadingData(true, true);
			updateDataSate(mSearchItem, mSubRedditAdapter.subRedditModel.after);
			break;
		case Common.SEARCH_TYPE_LOAD_FAIL:
			mSubRedditAdapter.isFailed = false;
			if (mSubRedditAdapter.subRedditModel.after != null
					&& !"".equals(mSubRedditAdapter.subRedditModel.after.trim())) {
				updateDataSate(mSearchItem,mSubRedditAdapter.subRedditModel.after);
			} else {
				updateDataSate(mSearchItem);
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
				SubRedditItem item = data
						.getParcelableExtra(Common.EXTRA_SUBREDDIT);
				Intent t = new Intent(
						SearchSubRedditListFragment.this.getActivity(),
						CommentFragmentActivity.class);
				t.putExtra(Common.INTENT_EXTRA_SUBREDDIT, item);
				this.startActivityForResult(t, Common.REQUEST_ACTIVITY_COMMENT);
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
				tt = new Intent(SearchSubRedditListFragment.this.getActivity(),
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
				tt = new Intent(SearchSubRedditListFragment.this.getActivity(),
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
			Intent picIntent = new Intent(
					SearchSubRedditListFragment.this.getActivity(),
					ImageViewActivity.class);
			picIntent.putExtra(Common.EXTRA_SUBREDDIT, itemR);
			this.startActivityForResult(picIntent,
					Common.REQUEST_ACTIVITY_WEBVIEW);

			break;

		case R.id.vote_up_wraper:
			// check login
			if (!RedditManager.isUserAuth(SearchSubRedditListFragment.this
					.getActivity())) {
				Toast.makeText(SearchSubRedditListFragment.this.getActivity(),
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
			if (!RedditManager.isUserAuth(SearchSubRedditListFragment.this
					.getActivity())) {
				Toast.makeText(SearchSubRedditListFragment.this.getActivity(),
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
					SearchSubRedditListFragment.this.getActivity(),
					OverviewFragmentActivity.class);
			authorIntent.putExtra(Common.EXTRA_USERNAME, item.author);
			this.startActivity(authorIntent);
			break;
		}
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		int pid = parent.getId();
		switch (pid) {
		case SEARCH_SORT_SPINNER_ID:
			if (mCurrentSort == position) {
				return;
			} else {
				mCurrentSort = position;
				updateDataSate(mSearchItem);
			}
			break;
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {

	}

}
