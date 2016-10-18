package com.softgame.reddit.fragment;

import java.lang.ref.WeakReference;
import java.net.URL;

import org.json.custom.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
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
import com.softgame.reddit.SubRedditFragmentActivity;
import com.softgame.reddit.WebviewActivity;
import com.softgame.reddit.dialog.SubredditPopDialog;
import com.softgame.reddit.impl.OnSubredditPopSelectedListener;
import com.softgame.reddit.model.SubRedditItem;
import com.softgame.reddit.model.SubRedditModel;
import com.softgame.reddit.utils.Common;
import com.softgame.reddit.utils.CommonUtil;
import com.softgame.reddit.utils.RedditManager;
import com.softgame.reddit.utils.SubRedditManager;

public class SubRedditListFragment extends ListFragment implements
		OnSharedPreferenceChangeListener, OnItemClickListener,
		OnItemLongClickListener, OnClickListener,
		OnSubredditPopSelectedListener, OnItemSelectedListener {

	LayoutInflater mLayoutInflater;

	public static final String TAG = "SubRedditListFragment";

	public WeakReference<DataTask> mDataTaskWF;

	public SubRedditAdapter mSubRedditAdapter;

	public String mCurrentSubReddit;
	public String mCurrentDefaultUser;
	// hot new ..
	public int mCurrentKind;
	// day week all time...
	public int mCurrentSort = 1;
	//
	public int mCurrentType = 1;

	// boolean load thumbnial

	public boolean mSafeForWork = true;
	public boolean mUsedEmbedView = true;

	public boolean mSeftTextCollape = true;

	public SubRedditListFragment() {
	}

	public static SubRedditListFragment findOrCreateSubRedditRetainFragment(
			FragmentManager manager) {

		SubRedditListFragment fragment = (SubRedditListFragment) manager
				.findFragmentByTag(SubRedditListFragment.TAG);
		if (fragment == null) {
			fragment = new SubRedditListFragment();
			final FragmentTransaction ft = manager.beginTransaction();
			ft.add(fragment, SubRedditListFragment.TAG);
			ft.commit();
		}
		return fragment;
	}

	public static SubRedditListFragment newInstance(int kind) {
		SubRedditListFragment f = new SubRedditListFragment();
		Bundle args = new Bundle();
		args.putInt("kind", kind);
		f.setArguments(args);
		return f;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setRetainInstance(true);
		mCurrentKind = getArguments() != null ? getArguments().getInt("kind")
				: 0;
		mCurrentSubReddit = RedditManager.getCurrentSubReddit(getActivity());

		mCurrentDefaultUser = RedditManager
				.getUserName(SubRedditListFragment.this.getActivity());

		SharedPreferences df = PreferenceManager
				.getDefaultSharedPreferences(this.getActivity());
		mSafeForWork = df.getBoolean(
				this.getString(R.string.pref_safe_for_work), true);

		mUsedEmbedView = df.getBoolean(
				this.getString(R.string.pref_key_embed_view), true);

		mSeftTextCollape = df.getBoolean(
				this.getString((R.string.pref_key_selftext_collape)), true);

		mSubRedditAdapter = new SubRedditAdapter();
		updateDataSate();
	}

	@Override
	public void onResume() {
		super.onResume();
		mSubRedditAdapter.notifyDataSetChanged();
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if (key.equals(Common.PREF_KEY_SUBREDDIT)) {
			if (mCurrentSubReddit == null
					|| !mCurrentSubReddit.equals(sharedPreferences.getString(
							key, Common.DEFAULT_SUBREDDIT))) {
				mCurrentSubReddit = sharedPreferences.getString(key,
						Common.DEFAULT_SUBREDDIT);
				updateDataSate();
			}
			return;
		}

		if (key.equals(Common.PREF_DEFAULT_USER_KEY)) {
			String user = sharedPreferences.getString(
					Common.PREF_DEFAULT_USER_KEY, "");
			if (mCurrentDefaultUser == null || mCurrentDefaultUser.equals("")) {
				if (user != null && !user.equals("")) {
					mCurrentDefaultUser = user;
					updateDataSate();
				}
			} else if (!mCurrentDefaultUser.equals(user)) {
				mCurrentDefaultUser = user;
				updateDataSate();
			}
			return;
		}

		if (key.equals(Common.PREF_DATE_RELOAD)) {
			updateDataSate();
		}

		if (key.equals(this.getActivity().getString(
				R.string.pref_key_selftext_collape))) {
			// GET LOAD PICTURE
			boolean selftextCollape = sharedPreferences.getBoolean(this
					.getActivity()
					.getString(R.string.pref_key_selftext_collape), true);
			if (selftextCollape != mSeftTextCollape) {
				mSeftTextCollape = selftextCollape;
				mSubRedditAdapter.notifyDataSetChanged();
			}
		}

		if (key.equals(this.getActivity()
				.getString(R.string.pref_safe_for_work))) {
			boolean safeForWork = sharedPreferences
					.getBoolean(
							this.getActivity().getString(
									R.string.pref_safe_for_work), true);
			if (safeForWork != mSafeForWork) {
				mSafeForWork = safeForWork;
				mSubRedditAdapter.notifyDataSetChanged();
			}
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

		boolean update = false;
		// check and regist
		SharedPreferences df = PreferenceManager
				.getDefaultSharedPreferences(this.getActivity());
		String cs = df.getString(Common.PREF_KEY_SUBREDDIT,
				Common.DEFAULT_SUBREDDIT);
		if (mCurrentSubReddit == null || !mCurrentSubReddit.equals(cs)) {
			mCurrentSubReddit = cs;
			// updateDataSate();
			update = true;
		}

		String user = df.getString(Common.PREF_DEFAULT_USER_KEY, "");
		if (mCurrentDefaultUser == null || mCurrentDefaultUser.equals("")) {
			if (user != null && !user.equals("")) {
				mCurrentDefaultUser = user;
				// updateDataSate();
				update = true;
			}
		} else if (!mCurrentDefaultUser.equals(user)) {
			mCurrentDefaultUser = user;
			// updateDataSate();
			update = true;
		}

		if (update) {
			updateDataSate();
		}

		df.registerOnSharedPreferenceChangeListener(this);
		this.getListView().setAdapter(mSubRedditAdapter);
		this.getListView().setOnItemClickListener(this);
		this.getListView().setOnItemLongClickListener(this);
	}

	@Override
	public void onDetach() {
		super.onDetach();
		SharedPreferences df = PreferenceManager
				.getDefaultSharedPreferences(this.getActivity());
		df.unregisterOnSharedPreferenceChangeListener(this);
	}

	/**
	 * update the current data to receive new data
	 */
	public void updateDataSate(String after) {
		if (mDataTaskWF != null && mDataTaskWF.get() != null)
			mDataTaskWF.get().cancel(true);
		DataTask d = new DataTask(after, this);
		mDataTaskWF = new WeakReference<SubRedditListFragment.DataTask>(d);
		d.execute();

	}

	public void updateDataSate() {
		if (mDataTaskWF != null && mDataTaskWF.get() != null)
			mDataTaskWF.get().cancel(true);
		DataTask d = new DataTask(this);
		mDataTaskWF = new WeakReference<SubRedditListFragment.DataTask>(d);
		d.execute();
	}

	public class SubRedditAdapter extends BaseAdapter {

		private static final int TYPE_COUNT = 11;
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
			if (position == 0) {
				if (isLoadingData) {
					return Common.SUBREDDIT_TYPE_EMPTY;
				}
				switch (mCurrentKind) {
				case Common.KIND_HOT:
				case Common.KIND_SAVED:
					return Common.SUBREDDIT_TYPE_EMPTY;
				case Common.KIND_CONTROVERSIAL:
				case Common.KIND_TOP:
					return Common.SUBREDDIT_TYPE_DATE_HEADER;
				case Common.KIND_NEW:
					return Common.SUBREDDIT_TYPE_NEW_HEADER;
				}
			}
			// not include 0
			if (position > 0 && position <= subRedditModel.getSize()) {
				SubRedditItem item = (SubRedditItem) getItem(position);
				if (item.is_self) {
					return Common.SUBREDDIT_TYPE_ITEM_SELFPOST;
				} else {
					if (CommonUtil.isPictureUrl(item.url, item.domain,
							item.subreddit)) {
						return Common.SUBREDDIT_TYPE_ITEM_LINK;
					} else {

						return Common.SUBREDDIT_TYPE_ITEM_LINK_NOPIC;
					}

				}
			}
			if (isLoadingData) {
				return Common.SUBREDDIT_TYPE_LAODING;

			}
			if (isFailed) {
				return Common.SUBREDDIT_TYPE_LOAD_FAIL;
			}

			else {
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
			case Common.SUBREDDIT_TYPE_DATE_HEADER:
				if (convertView == null) {
					convertView = (View) mLayoutInflater.inflate(
							R.layout.item_date_spinner, null);

					Spinner dateSpinner = (Spinner) convertView
							.findViewById(R.id.date_spinner);
					dateSpinner.setId(Common.ID_DATE_SPINNER);
					ArrayAdapter<CharSequence> adapter = ArrayAdapter
							.createFromResource(
									SubRedditListFragment.this.getActivity(),
									R.array.date_array,
									R.layout.simple_spinner_text_item);
					adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
					dateSpinner.setAdapter(adapter);
					dateSpinner.setSelection(mCurrentType);
					dateSpinner
							.setOnItemSelectedListener(SubRedditListFragment.this);
				}
				break;

			case Common.SUBREDDIT_TYPE_LOAD_FAIL:
				if (convertView == null) {
					convertView = mLayoutInflater.inflate(
							R.layout.item_load_fail, null);
				}
				break;
			case Common.SUBREDDIT_TYPE_EMPTY:
				if (convertView == null) {
					convertView = mLayoutInflater.inflate(R.layout.item_empty,
							null);
				}
				break;
			case Common.SUBREDDIT_TYPE_NEW_HEADER:
				if (convertView == null) {
					convertView = (View) mLayoutInflater.inflate(
							R.layout.item_date_spinner, null);
					Spinner newSpinner = (Spinner) convertView
							.findViewById(R.id.date_spinner);

					ArrayAdapter<CharSequence> newadapter = ArrayAdapter
							.createFromResource(
									SubRedditListFragment.this.getActivity(),
									R.array.new_array,
									R.layout.simple_spinner_text_item);
					newSpinner.setId(Common.ID_NEW_SPINNER);
					newadapter
							.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
					newSpinner.setAdapter(newadapter);
					newSpinner.setSelection(mCurrentSort);
					newSpinner
							.setOnItemSelectedListener(SubRedditListFragment.this);
				}
				break;
			case Common.SUBREDDIT_TYPE_ITEM_LINK:
				if (convertView == null) {
					convertView = mLayoutInflater.inflate(
							R.layout.item_subreddit_fragment_link, null);
				}
				setUpContentView(convertView, item, position);
				ImageView rw = (ImageView) convertView
						.findViewById(R.id.remote_pic);
				rw.setTag(position);
				rw.setOnClickListener(SubRedditListFragment.this);
				if ((item.over_18 && mSafeForWork)) {
					rw.setImageResource(R.drawable.nsfw_picture);
				} else {
					if (rw != null)
						((SubRedditFragmentActivity) SubRedditListFragment.this
								.getActivity()).getImageWorker().loadImage(
								CommonUtil.appendJPG(item.url), rw);
				}

				// domain
				((TextView) convertView
						.findViewById(R.id.subreddit_info_domain))
						.setText(item.domain);

				break;

			case Common.SUBREDDIT_TYPE_ITEM_LINK_NOPIC:
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
						SubRedditListFragment.this);
				break;

			case Common.SUBREDDIT_TYPE_ITEM_SELFPOST:

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
			case Common.SUBREDDIT_TYPE_LAODING:
				if (convertView == null) {
					convertView = mLayoutInflater.inflate(
							R.layout.item_loading, null);
				}
				break;
			case Common.SUBREDDIT_TYPE_MORE:
				if (convertView == null) {
					convertView = mLayoutInflater.inflate(R.layout.item_more,
							null);
				}
				break;

			case Common.SUBREDDIT_TYPE_NO_MORE:
				if (convertView == null) {
					convertView = mLayoutInflater.inflate(
							R.layout.item_no_more, null);
				}
				break;
			case Common.SUBREDDIT_TYPE_NO_ITEM:
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
					SubRedditListFragment.this.getActivity()));

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

			voteUpWraper.setOnClickListener(SubRedditListFragment.this);
			voteDownWraper.setOnClickListener(SubRedditListFragment.this);

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
		WeakReference<SubRedditListFragment> mSubRedditRetainFragmentWF;
		String after;
		SubRedditModel model;

		public DataTask(final SubRedditListFragment fragment) {
			mSubRedditRetainFragmentWF = new WeakReference<SubRedditListFragment>(
					fragment);
		}

		public DataTask(final String a, final SubRedditListFragment fragment) {
			mSubRedditRetainFragmentWF = new WeakReference<SubRedditListFragment>(
					fragment);
			after = a;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			SubRedditListFragment fragment = mSubRedditRetainFragmentWF.get();
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

			SubRedditListFragment fragment = mSubRedditRetainFragmentWF.get();
			if (fragment != null) {
				JSONObject dataJSON = new JSONObject();
				String result = SubRedditManager.getSubReddit(
						fragment.mCurrentSubReddit, fragment.mCurrentKind,
						Common.TYPE_ARRAY[fragment.mCurrentKind],
						Common.NEW_ARRAY[fragment.mCurrentSort],
						Common.DATA_ARRAY[fragment.mCurrentType], after,
						dataJSON, fragment.getActivity());
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
					|| mSubRedditRetainFragmentWF.get().getActivity() == null || result.equals(Common.RESULT_TASK_CANCLE)) {
				return;
			}
			if (mSubRedditRetainFragmentWF.get().mDataTaskWF != null
					&& mSubRedditRetainFragmentWF.get().mDataTaskWF.get() != null) {
				if (mSubRedditRetainFragmentWF.get().mDataTaskWF.get() != this) {
					// I am not newest, do nothing
					return;
				}
			}

			if (result == Common.RESULT_SUCCESS) {
				mSubRedditRetainFragmentWF.get().mSubRedditAdapter.subRedditModel
						.addData(model);
				mSubRedditRetainFragmentWF.get().mSubRedditAdapter
						.setIsLoadingData(false, true);
			} else {
				mSubRedditRetainFragmentWF.get().mSubRedditAdapter.isFailed = true;
				mSubRedditRetainFragmentWF.get().mSubRedditAdapter
						.setIsLoadingData(false, true);
				if (Common.RESULT_FETCHING_FAIL.equals(result)) {
					Toast.makeText(
							mSubRedditRetainFragmentWF.get().getActivity(),
							"Internet error!", Toast.LENGTH_SHORT).show();
					return;
				} else {
					// show fail
					if (mSubRedditRetainFragmentWF.get().mCurrentKind != Common.KIND_SAVED) {
						Toast.makeText(
								mSubRedditRetainFragmentWF.get().getActivity(),
								"Load "
										+ Common.TYPE_ARRAY_TEXT[mSubRedditRetainFragmentWF
												.get().mCurrentKind]
										+ " failed! Try click reload!(\nReddit may be down!)",
								Toast.LENGTH_SHORT).show();
					}
				}
			}
		}
	}

	// -------------------vote ------------------
	public static class VoteTask extends AsyncTask<Void, Void, String> {
		String name;
		boolean isUp;
		String dir = "0";
		WeakReference<SubRedditListFragment> subRedditRetainFragmentWF;
		WeakReference<SubRedditItem> subRedditItemWF;

		public VoteTask(SubRedditListFragment fragment, SubRedditItem s,
				String d, boolean up) {
			subRedditRetainFragmentWF = new WeakReference<SubRedditListFragment>(
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
		WeakReference<SubRedditListFragment> subRedditRetainFragmentWF;

		public SaveTask(SubRedditListFragment fragment, SubRedditItem item,
				boolean s) {
			save = s;
			name = item.name;
			subRedditItemWF = new WeakReference<SubRedditItem>(item);
			subRedditRetainFragmentWF = new WeakReference<SubRedditListFragment>(
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
		WeakReference<SubRedditListFragment> subRedditRetainFragmentWF;

		public HideTask(SubRedditListFragment fragment, SubRedditItem item,
				boolean h) {
			hide = h;
			name = item.name;
			subRedditItemWF = new WeakReference<SubRedditItem>(item);
			subRedditRetainFragmentWF = new WeakReference<SubRedditListFragment>(
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
			if (((SubRedditFragmentActivity) SubRedditListFragment.this
					.getActivity()).mSubRedditPopDialog == null) {
				((SubRedditFragmentActivity) SubRedditListFragment.this
						.getActivity()).mSubRedditPopDialog = new SubredditPopDialog(
						SubRedditListFragment.this.getActivity());
				((SubRedditFragmentActivity) SubRedditListFragment.this
						.getActivity()).mSubRedditPopDialog
						.setOwnerActivity(SubRedditListFragment.this
								.getActivity());
			}
			((SubRedditFragmentActivity) SubRedditListFragment.this
					.getActivity()).mSubRedditPopDialog.dismiss();

			((SubRedditFragmentActivity) SubRedditListFragment.this
					.getActivity()).mSubRedditPopDialog.setSubRedditItem(
					mSubRedditAdapter.getItem(position), position);
			((SubRedditFragmentActivity) SubRedditListFragment.this
					.getActivity()).mSubRedditPopDialog
					.setOnSubredditPopSelectedListener(this);
			((SubRedditFragmentActivity) SubRedditListFragment.this
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
		case Common.SUBREDDIT_TYPE_ITEM_LINK:
		case Common.SUBREDDIT_TYPE_ITEM_LINK_NOPIC:
		case Common.SUBREDDIT_TYPE_ITEM_SELFPOST:
			SubRedditItem item = mSubRedditAdapter.getItem(position);
			if (item == null) {
				return;
			}
			Intent t = new Intent(SubRedditListFragment.this.getActivity(),
					CommentFragmentActivity.class);
			t.putExtra(Common.INTENT_EXTRA_SUBREDDIT, item);
			this.startActivityForResult(t, Common.REQUEST_ACTIVITY_COMMENT);
			break;

		case Common.SUBREDDIT_TYPE_MORE:
			// mSubRedditAdapter.setIsLoadingData(true, true);
			updateDataSate(mSubRedditAdapter.subRedditModel.after);
			break;
		case Common.SUBREDDIT_TYPE_LOAD_FAIL:
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

				if (isComment && item != null) {
					Intent t = new Intent(
							SubRedditListFragment.this.getActivity(),
							CommentFragmentActivity.class);
					t.putExtra(Common.INTENT_EXTRA_SUBREDDIT, item);
					this.startActivityForResult(t,
							Common.REQUEST_ACTIVITY_COMMENT);
				} else
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
				tt = new Intent(SubRedditListFragment.this.getActivity(),
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
				tt = new Intent(SubRedditListFragment.this.getActivity(),
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
					SubRedditListFragment.this.getActivity(),
					ImageViewActivity.class);
			picIntent.putExtra(Common.EXTRA_SUBREDDIT, itemR);
			this.startActivityForResult(picIntent,
					Common.REQUEST_ACTIVITY_WEBVIEW);

			break;

		case R.id.vote_up_wraper:
			// check login
			if (!RedditManager.isUserAuth(SubRedditListFragment.this
					.getActivity())) {
				Toast.makeText(SubRedditListFragment.this.getActivity(),
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
			if (!RedditManager.isUserAuth(SubRedditListFragment.this
					.getActivity())) {
				Toast.makeText(SubRedditListFragment.this.getActivity(),
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
			// check login
			if (!RedditManager.isUserAuth(SubRedditListFragment.this
					.getActivity())) {
				Toast.makeText(SubRedditListFragment.this.getActivity(),
						this.getString(R.string.login_request),
						Toast.LENGTH_SHORT).show();
				return;
			}
			String dir = updateVoteAndGetResult(item, true);
			mSubRedditAdapter.notifyDataSetChanged();
			new VoteTask(this, item, dir, true).execute();
			break;
		case SubredditPopDialog.ACTION_VOTE_DOWN:

			// check login
			if (!RedditManager.isUserAuth(SubRedditListFragment.this
					.getActivity())) {
				Toast.makeText(SubRedditListFragment.this.getActivity(),
						this.getString(R.string.login_request),
						Toast.LENGTH_SHORT).show();
				return;
			}

			String dird = updateVoteAndGetResult(item, false);
			mSubRedditAdapter.notifyDataSetChanged();
			new VoteTask(this, item, dird, false).execute();
			break;
		case SubredditPopDialog.ACTION_HIDE:

			// check login
			if (!RedditManager.isUserAuth(SubRedditListFragment.this
					.getActivity())) {
				Toast.makeText(SubRedditListFragment.this.getActivity(),
						this.getString(R.string.login_request),
						Toast.LENGTH_SHORT).show();
				return;
			}
			item.hidden = true;
			mSubRedditAdapter.notifyDataSetChanged();
			new HideTask(this, item, true).execute();
			break;
		case SubredditPopDialog.ACTION_UNHIDE:

			// check login
			if (!RedditManager.isUserAuth(SubRedditListFragment.this
					.getActivity())) {
				Toast.makeText(SubRedditListFragment.this.getActivity(),
						this.getString(R.string.login_request),
						Toast.LENGTH_SHORT).show();
				return;
			}

			item.hidden = false;
			mSubRedditAdapter.notifyDataSetChanged();
			new HideTask(this, item, false).execute();
			break;

		case SubredditPopDialog.ACTION_SAVE:
			// check login
			if (!RedditManager.isUserAuth(SubRedditListFragment.this
					.getActivity())) {
				Toast.makeText(SubRedditListFragment.this.getActivity(),
						this.getString(R.string.login_request),
						Toast.LENGTH_SHORT).show();
				return;
			}

			item.saved = true;
			mSubRedditAdapter.notifyDataSetChanged();
			new SaveTask(this, item, true).execute();
			break;

		case SubredditPopDialog.ACTION_UNSAVE:

			// check login
			if (!RedditManager.isUserAuth(SubRedditListFragment.this
					.getActivity())) {
				Toast.makeText(SubRedditListFragment.this.getActivity(),
						this.getString(R.string.login_request),
						Toast.LENGTH_SHORT).show();
				return;
			}
			item.saved = false;
			mSubRedditAdapter.notifyDataSetChanged();
			new SaveTask(this, item, false).execute();
			break;

		case SubredditPopDialog.ACTION_PROFILE:
			Intent authorIntent = new Intent(
					SubRedditListFragment.this.getActivity(),
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
		case Common.ID_NEW_SPINNER:
			if (Common.KIND_NEW == mCurrentKind) {
				if (mCurrentSort == position) {
					return;
				} else {
					mCurrentSort = position;
					updateDataSate();
				}
			}
			break;
		case Common.ID_DATE_SPINNER:
			if (Common.KIND_CONTROVERSIAL == mCurrentKind
					|| Common.KIND_TOP == mCurrentKind) {
				if (mCurrentType == position) {
					return;
				} else {
					mCurrentType = position;
					updateDataSate();
				}
			}
			break;
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {

	}

}
