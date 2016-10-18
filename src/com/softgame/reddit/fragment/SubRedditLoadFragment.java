package com.softgame.reddit.fragment;

import java.lang.ref.WeakReference;

import org.json.custom.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.softgame.reddit.impl.OnDataRefreshListener;
import com.softgame.reddit.model.SubRedditItem;
import com.softgame.reddit.model.SubRedditModel;
import com.softgame.reddit.utils.Common;
import com.softgame.reddit.utils.CommonUtil;
import com.softgame.reddit.utils.RedditManager;
import com.softgame.reddit.utils.SubRedditManager;

public class SubRedditLoadFragment extends SherlockFragment implements
		OnSharedPreferenceChangeListener {
	LayoutInflater mLayoutInflater;

	public static final String TAG = "SubRedditLoadFragment";

	public WeakReference<DataTask> mDataTaskWF;

	public String mCurrentSubReddit;
	public String mCurrentDefaultUser;
	// hot new ..
	public int mCurrentKind;
	// day week all time...
	public int mCurrentSort = 1;
	public int mCurrentType = 2;

	public boolean mShowActionBar = true;
	// this to indicate that the viewpager need to refresh itself all items.

	public WeakReference<OnDataRefreshListener> mOnDataRefreshListenerWF;

	public SubRedditLoadFragment() {
	}

	public static SubRedditLoadFragment findOrCreateSubRedditLoadFragment(
			FragmentManager manager, int kind, int sort, int type) {

		SubRedditLoadFragment fragment = (SubRedditLoadFragment) manager
				.findFragmentByTag(SubRedditLoadFragment.TAG);
		if (fragment == null) {
			fragment = new SubRedditLoadFragment();
			Bundle args = new Bundle();
			args.putInt("kind", kind);
			args.putInt("sort", sort);
			args.putInt("type", type);
			fragment.setArguments(args);
			final FragmentTransaction ft = manager.beginTransaction();
			ft.add(fragment, SubRedditLoadFragment.TAG);
			ft.commit();
		}
		return fragment;
	}

	boolean mIsLoadingData;
	boolean mIsFailed = false;
	public SubRedditModel mSubRedditModel;

	public static final int TYPE_SELFPOST = 0x0;
	public static final int TYPE_LINK_NOPIC = 0x1;
	public static final int TYPE_LINK_PIC = 0x2;
	public static final int TYPE_LOAD_MORE = 0x3;
	public static final int TYPE_NO_MORE = 0x4;
	public static final int TYPE_LOADING = 0x5;
	public static final int TYPE_LOAD_FAIL = 0x6;

	public void setDataRefreshListener(OnDataRefreshListener listener) {
		mOnDataRefreshListenerWF = new WeakReference<OnDataRefreshListener>(
				listener);
	}

	public int getItemViewType(int position) {
		// not include 0
		if (position >= 0 && position < mSubRedditModel.getSize()) {
			SubRedditItem item = (SubRedditItem) getItem(position);
			if (item.is_self) {
				return SubRedditLoadFragment.TYPE_SELFPOST;
			} else {

				if (CommonUtil.isPictureUrl(item.url, item.domain,
						mCurrentSubReddit)) {
					return SubRedditLoadFragment.TYPE_LINK_PIC;
				} else {

					return SubRedditLoadFragment.TYPE_LINK_NOPIC;
				}
			}
		}

		if (mIsLoadingData) {
			return SubRedditLoadFragment.TYPE_LOADING;
		}
		if (mIsFailed) {
			return SubRedditLoadFragment.TYPE_LOAD_FAIL;
		}
		if (mSubRedditModel.after == null || "".equals(mSubRedditModel.after)) {
			return SubRedditLoadFragment.TYPE_NO_MORE;
		} else {
			return SubRedditLoadFragment.TYPE_LOAD_MORE;
		}
	}

	public SubRedditItem getItem(int position) {
		if (position >= 0 && position < mSubRedditModel.getSize()) {
			return mSubRedditModel.getItemByIndex(position);
		}
		return null;
	}

	public int getCount() {
		// subreddit with loading more
		return mSubRedditModel.getSize() + 1;
	}

	public void setIsLoadingData(boolean isloading) {
		mIsLoadingData = isloading;
	}

	public boolean isLoadingData() {
		return mIsLoadingData;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setRetainInstance(true);
		this.setHasOptionsMenu(true);
		mCurrentKind = getArguments() != null ? getArguments().getInt("kind")
				: 0;
		mCurrentSort = getArguments() != null ? getArguments().getInt("sort")
				: 1;
		mCurrentType = getArguments() != null ? getArguments().getInt("type")
				: 2;
		mCurrentSubReddit = RedditManager.getCanvasSubReddit(getActivity());

		mCurrentDefaultUser = RedditManager
				.getUserName(SubRedditLoadFragment.this.getActivity());

		mSubRedditModel = new SubRedditModel();

		updateDataSate();

		// create data saver
	}

	public void registSharedPreference(Context context) {

		SharedPreferences df = PreferenceManager
				.getDefaultSharedPreferences(this.getActivity());
		df.registerOnSharedPreferenceChangeListener(this);
	}

	public void unRegistSharedPreference(Context context) {
		SharedPreferences df = PreferenceManager
				.getDefaultSharedPreferences(this.getActivity());
		df.unregisterOnSharedPreferenceChangeListener(this);

	}
	

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		registSharedPreference(this.getActivity());
	}

	@Override
	public void onDetach() {
		super.onDetach();
		unRegistSharedPreference(this.getActivity());
	}

	public void setCurrentKind(int kind) {
		if (mCurrentKind != kind) {
			mCurrentKind = kind;
			updateDataSate();
		}

	}

	public void setCurrentSort(int sort) {
		if (mCurrentSort != sort) {
			mCurrentSort = sort;
			updateDataSate();
		}
	}

	public void setType(int type) {
		if (mCurrentType != type) {
			mCurrentType = type;
			updateDataSate();
		}
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if (key.equals(Common.PREF_KEY_CANVAS_SUBREDDIT)) {
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
			} else if (!mCurrentDefaultUser.equals("user")) {
				mCurrentDefaultUser = user;
				updateDataSate();
			}
			return;
		}

	}

	public void loadMore() {
		if (mSubRedditModel.after != null && !"".equals(mSubRedditModel)) {
			updateDataSate(mSubRedditModel.after);
		}
	}

	public void loadMoreIfNeeed() {
		if (mSubRedditModel.after != null && !"".equals(mSubRedditModel)) {
			updateDataSate(mSubRedditModel.after);
		} else {
			updateDataSate();
		}
	}

	/**
	 * update the current data to receive new data
	 */
	public void updateDataSate(String after) {
		if (mDataTaskWF != null && mDataTaskWF.get() != null)
			mDataTaskWF.get().cancel(true);
		DataTask d = new DataTask(after, this);
		mDataTaskWF = new WeakReference<SubRedditLoadFragment.DataTask>(d);
		d.execute();

	}

	public void updateDataSate() {
		if (mDataTaskWF != null && mDataTaskWF.get() != null)
			mDataTaskWF.get().cancel(true);
		DataTask d = new DataTask(this);
		mDataTaskWF = new WeakReference<SubRedditLoadFragment.DataTask>(d);
		d.execute();
	}

	public void updateViewPager(boolean clear) {
		if (mOnDataRefreshListenerWF != null
				&& mOnDataRefreshListenerWF.get() != null) {
			mOnDataRefreshListenerWF.get().onDataRefresh(clear);
		}
	}

	private static class DataTask extends AsyncTask<Void, Void, String> {
		WeakReference<SubRedditLoadFragment> fragmentWF;
		String after;
		SubRedditModel model;

		public DataTask(final SubRedditLoadFragment fragment) {
			fragmentWF = new WeakReference<SubRedditLoadFragment>(fragment);
		}

		public DataTask(final String a, final SubRedditLoadFragment fragment) {
			fragmentWF = new WeakReference<SubRedditLoadFragment>(fragment);
			after = a;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (fragmentWF.get() != null) {

				if (after == null || "".equals(after)) {
					fragmentWF.get().mSubRedditModel.clear();
					fragmentWF.get().setIsLoadingData(true);
					// clear and update view pager
					fragmentWF.get().updateViewPager(true);
				} else if (fragmentWF.get().mIsFailed) {
					fragmentWF.get().mIsFailed = false;
					fragmentWF.get().updateViewPager(false);
				}

			} else {
				this.cancel(true);
			}
		}

		@Override
		protected String doInBackground(Void... params) {
			if (fragmentWF.get() != null) {
				JSONObject dataJSON = new JSONObject();
				String result = SubRedditManager.getSubReddit(
						fragmentWF.get().mCurrentSubReddit,
						fragmentWF.get().mCurrentKind,
						Common.TYPE_ARRAY[fragmentWF.get().mCurrentKind],
						Common.NEW_ARRAY[fragmentWF.get().mCurrentSort],
						Common.DATA_ARRAY[fragmentWF.get().mCurrentType],
						after, dataJSON, fragmentWF.get().getActivity());
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

			// if fragment is not null, make it loading to false cause activity
			// may desctory and fragment still alive, and when activity start
			// again, they use this one as new one,which is unbelievable
			if (fragmentWF == null || this.isCancelled()
					|| fragmentWF.get().getActivity() == null
					|| result.equals(Common.RESULT_TASK_CANCLE)) {
				// cancel;
				return;
			}

			if (fragmentWF.get().mDataTaskWF != null
					&& fragmentWF.get().mDataTaskWF.get() != null) {
				if (fragmentWF.get().mDataTaskWF.get() != this) {
					// I am not newest, do nothing
					return;
				}
			}

			fragmentWF.get().setIsLoadingData(false);
			if (result == Common.RESULT_SUCCESS) {
				fragmentWF.get().mIsFailed = false;
				fragmentWF.get().mSubRedditModel.addData(model);
				fragmentWF.get().updateViewPager(false);
			} else {
				fragmentWF.get().mIsFailed = true;
				fragmentWF.get().updateViewPager(false);
				// show fail
				Toast.makeText(
						fragmentWF.get().getActivity(),
						"Load"
								+ Common.TYPE_ARRAY_TEXT[fragmentWF.get().mCurrentKind]
								+ "failed!", Toast.LENGTH_SHORT).show();
			}
		}

	}

}
