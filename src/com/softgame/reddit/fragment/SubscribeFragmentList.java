package com.softgame.reddit.fragment;

import java.lang.ref.WeakReference;

import org.json.custom.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.softgame.reddit.CacheFragmentActivity;
import com.softgame.reddit.R;
import com.softgame.reddit.dialog.MineSubredditDialog;
import com.softgame.reddit.impl.OnSubscribeItemClickListener;
import com.softgame.reddit.model.Subscribe;
import com.softgame.reddit.model.SubscribeModel;
import com.softgame.reddit.utils.Common;
import com.softgame.reddit.utils.CommonUtil;
import com.softgame.reddit.utils.RedditManager;
import com.softgame.reddit.utils.SubscribeManager;

/**
 * this is a list fragment for subscribe
 * 
 * @author xinyunxixi
 */
public class SubscribeFragmentList extends ListFragment implements
		OnClickListener, OnSharedPreferenceChangeListener {

	public static final int ACTION_SUBREDDIT_ITEM = 0x116;
	public static final int ACTION_SUBREDDIT_SUBSCRIBE = 0x117;
	public static final int ACTION_SUBREDDIT_UNSUBSCRIBE = 0x118;

	LayoutInflater mLayoutInflater;
	// view
	private int mType;
	boolean isLoaded;

	// loading data task Array
	WeakReference<SubscribeTask> mSubscribeTaskWF;
	// Adapter
	SubscribeAdapter mSubscribeAdapter;

	boolean mHaveUserLogin;
	boolean mSafeForWork;

	public SubscribeFragmentList() {
	}

	// create a new FragmentList
	public static SubscribeFragmentList newInstance(int type) {
		SubscribeFragmentList f = new SubscribeFragmentList();
		// Supply num input as an argument.
		Bundle args = new Bundle();
		args.putInt(Common.KEY_SUBSCRIBE_TYPE, type);
		((Fragment) f).setArguments(args);
		return f;
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {

		if (key.equals(Common.PREF_DEFAULT_USER_KEY)) {
			SubscribeFragmentList.this.checkLoginUserUpdate();
		}
	}

	public void checkLoginUserUpdate() {
		boolean change = RedditManager.isUserAuth(getActivity());
		if (change != mHaveUserLogin) {
			mHaveUserLogin = change;
			if (mHaveUserLogin && mType == MineSubredditDialog.KIND_MY_REDDITS) {
				// reLoad
				updateDataState();
			} else {
				notifyDataChanged();
			}
		}

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		this.setRetainInstance(true);
		super.onCreate(savedInstanceState);
		mType = getArguments() != null ? getArguments().getInt(
				Common.KEY_SUBSCRIBE_TYPE) : MineSubredditDialog.KIND_POPULAR;
		mHaveUserLogin = RedditManager.isUserAuth(getActivity());
		mSafeForWork = CommonUtil.safeForWork(this.getActivity());

		mSubscribeAdapter = new SubscribeAdapter(this.getActivity(), mType);
		updateDataState();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mLayoutInflater = inflater;
		return inflater.inflate(R.layout.fragment_listview, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		// check and regist
		checkLoginUserUpdate();
		SharedPreferences df = PreferenceManager
				.getDefaultSharedPreferences(this.getActivity());
		df.registerOnSharedPreferenceChangeListener(this);
		this.getListView().setAdapter(mSubscribeAdapter);
	}

	// unregist the preference
	@Override
	public void onDetach() {
		super.onDetach();
		SharedPreferences df = PreferenceManager
				.getDefaultSharedPreferences(this.getActivity());
		df.unregisterOnSharedPreferenceChangeListener(this);
	}

	public void notifyDataChanged() {
		if (mSubscribeAdapter != null) {
			mSubscribeAdapter.notifyDataSetChanged();
		}
	}

	private void updateDataState() {
		if (mSubscribeTaskWF != null
				&& mSubscribeTaskWF.get() != null
				&& !AsyncTask.Status.FINISHED.equals(mSubscribeTaskWF.get()
						.getStatus())) {
			mSubscribeTaskWF.get().cancel(true);
		}
		SubscribeTask task = new SubscribeTask(mType, mSubscribeAdapter,
				this.getActivity());
		mSubscribeTaskWF = new WeakReference<SubscribeFragmentList.SubscribeTask>(
				task);
		task.execute();
	}

	private void updateDataState(String after) {
		if (mSubscribeTaskWF != null
				&& mSubscribeTaskWF.get() != null
				&& !AsyncTask.Status.FINISHED.equals(mSubscribeTaskWF.get()
						.getStatus())) {
			mSubscribeTaskWF.get().cancel(true);
		}
		SubscribeTask task = new SubscribeTask(mType, after, mSubscribeAdapter,
				this.getActivity());
		mSubscribeTaskWF = new WeakReference<SubscribeFragmentList.SubscribeTask>(
				task);
		task.execute();
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		switch (mSubscribeAdapter.getItemViewType(position)) {
		case SubscribeAdapter.TYPE_MORE:
			String after = mSubscribeAdapter.mSubscribeModel.after;
			updateDataState(after);
			break;

		case SubscribeAdapter.TYPE_ITEM:
			Subscribe ss = mSubscribeAdapter.mSubscribeModel.subscribeList
					.get(position);
			((OnSubscribeItemClickListener) this.getActivity())
					.onSubscribeItemClick(ss, ACTION_SUBREDDIT_ITEM);
			break;
		case SubscribeAdapter.TYPE_LOAD_FAIL:
			mSubscribeAdapter.isFailed = false;
			String af = mSubscribeAdapter.mSubscribeModel.after;
			if (af == null || "".equals(af)) {
				updateDataState();
			} else {
				updateDataState(af);
			}
			break;
		}
	}

	/*
	 * Mine Subscribe Task
	 */
	public static class SubscribeTask extends AsyncTask<Void, Void, String> {
		JSONObject dataJSON;
		int type;
		String after;
		WeakReference<SubscribeAdapter> subscribeAdpaterWF;
		WeakReference<Context> contextWF;

		public SubscribeTask(int t, SubscribeAdapter sa, Context context) {
			subscribeAdpaterWF = new WeakReference<SubscribeAdapter>(sa);
			contextWF = new WeakReference<Context>(context);
			type = t;
		}

		public SubscribeTask(int t, String a, SubscribeAdapter sa,
				Context context) {
			type = t;
			after = a;
			subscribeAdpaterWF = new WeakReference<SubscribeAdapter>(sa);
			contextWF = new WeakReference<Context>(context);
		}

		public SubscribeTask(int t, String a, String search,
				SubscribeAdapter sa, Context context) {
			type = t;
			after = a;
			subscribeAdpaterWF = new WeakReference<SubscribeAdapter>(sa);
			contextWF = new WeakReference<Context>(context);
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			SubscribeAdapter sa = subscribeAdpaterWF.get();
			if (sa != null || contextWF.get() == null) {
				sa.setLoading(true, false);
				if (after == null || "".equals(after)) {
					sa.clear(false);
				}
				sa.notifyDataSetChanged();
				dataJSON = new JSONObject();
			} else {
				this.cancel(true);
			}

		}

		@Override
		protected String doInBackground(Void... params) {

			if (subscribeAdpaterWF.get() != null && contextWF.get() != null) {
				String result = SubscribeManager.getSubscribeList(
						Common.SUBREDDIT_VALUE_ARRAY[type], after, dataJSON,
						contextWF.get());
				if (this.isCancelled()) {
					return Common.RESULT_TASK_CANCLE;
				}
				return result;
			} else {
				return Common.RESULT_TASK_CANCLE;
			}
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			if (Common.RESULT_TASK_CANCLE.equals(result)) {
				// do nothing
			}
			if (subscribeAdpaterWF.get() != null && contextWF.get() != null) {
				if (Common.RESULT_SUCCESS.equals(result)) {
					SubscribeModel.convertToList(dataJSON,
							subscribeAdpaterWF.get().mSubscribeModel);
					subscribeAdpaterWF.get().isLoading = false;
					subscribeAdpaterWF.get().notifyDataSetChanged();
				} else {
					subscribeAdpaterWF.get().isLoading = false;
					subscribeAdpaterWF.get().isFailed = true;
					subscribeAdpaterWF.get().notifyDataSetChanged();
				}
			}
		}
	}

	public static class SubscribeSubRedditTask extends
			AsyncTask<Void, Void, String> {
		boolean isSubscribe;
		WeakReference<Context> contextWF;
		String subredditName;
		String displayName = "";

		public SubscribeSubRedditTask(Context context, String sn,
				boolean subscribe) {
			contextWF = new WeakReference<Context>(context);
			subredditName = sn;
			isSubscribe = subscribe;
			displayName = "";
		}

		public SubscribeSubRedditTask(Context context, String sn, String dn,
				boolean subscribe) {
			contextWF = new WeakReference<Context>(context);
			subredditName = sn;
			isSubscribe = subscribe;
			displayName = dn;
		}

		@Override
		protected String doInBackground(Void... params) {
			if (contextWF == null || contextWF.get() == null) {
				this.cancel(true);
				return Common.RESULT_TASK_CANCLE;
			}

			return SubscribeManager.subscribeSubReddit(subredditName,
					isSubscribe, contextWF.get());
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			if (result.equals(Common.RESULT_TASK_CANCLE) || contextWF == null
					|| contextWF.get() == null) {
				return;
			}

			if (Common.RESULT_SUCCESS.equals(result)) {
				// successful subscribe

				if(displayName != null && !"".equals(displayName))
				Toast.makeText(
						contextWF.get(),
						(isSubscribe ? "Subscribe " : "Unsubscribe ")
								+ displayName + " succeeded!"
								+ "\n note: result shows after next refresh",
						Toast.LENGTH_SHORT).show();
			}

			else if (Common.RESULT_FETCHING_FAIL.equals(result)) {
				Toast.makeText(contextWF.get(), "Internet connent fail",
						Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(
						contextWF.get(),
						(isSubscribe ? "Subscribe" : "Subscribe ")
								+ displayName + " failed!", Toast.LENGTH_SHORT)
						.show();
			}

		}
	}

	public class SubscribeAdapter extends BaseAdapter {

		private final int TYPE_COUNT = 6;
		// subscribe type such as ama pic fun... load from reddit
		public static final int TYPE_ITEM = 0x1;
		// load from reddit and no item
		public static final int TYPE_NO_ITEM = 0x2;
		public static final int TYPE_MORE = 0x3;
		public static final int TYPE_NO_MORE = 0x4;
		public static final int TYPE_LOADING = 0x0;
		public static final int TYPE_LOAD_FAIL = 0x5;

		public boolean isLoading = false;
		public boolean isFailed = false;
		private int mType;
		public SubscribeModel mSubscribeModel;

		public SubscribeAdapter(Context context, int type) {
			mType = type;
			mSubscribeModel = new SubscribeModel();
		}

		public void setLoading(boolean loading, boolean update) {
			isLoading = loading;
			if (update) {
				this.notifyDataSetChanged();
			}
		}

		public void clear(boolean update) {
			mSubscribeModel.subscribeList.clear();
			if (update) {
				this.notifyDataSetChanged();
			}
		}

		@Override
		public boolean areAllItemsEnabled() {
			return false;
		}

		@Override
		public boolean isEnabled(int position) {
			switch (this.getItemViewType(position)) {
			case TYPE_ITEM:
			case TYPE_MORE:
			case TYPE_LOAD_FAIL:
				return true;
			}
			return false;
		}

		@Override
		public int getItemViewType(int position) {
			if (position >= 0
					&& position < mSubscribeModel.subscribeList.size()) {
				return TYPE_ITEM;
			} else if (isLoading) {
				return SubscribeAdapter.TYPE_LOADING;
			} else if (isFailed) {
				return SubscribeAdapter.TYPE_LOAD_FAIL;
			}

			else {
				if (mSubscribeModel.subscribeList.size() == 0) {
					return SubscribeAdapter.TYPE_NO_ITEM;
				}
				if (mSubscribeModel.after == null
						|| "".equals(mSubscribeModel.after)) {
					return SubscribeAdapter.TYPE_NO_MORE;
				} else {
					return SubscribeAdapter.TYPE_MORE;
				}

			}
		}

		@Override
		public int getViewTypeCount() {
			return TYPE_COUNT;
		}

		@Override
		public int getCount() {
			return mSubscribeModel.subscribeList.size() + 1;
		}

		@Override
		public Subscribe getItem(int position) {
			if (position >= 0
					&& position < mSubscribeModel.subscribeList.size()) {
				return mSubscribeModel.subscribeList.get(position);
			} else {
				return null;
			}
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			int type = this.getItemViewType(position);
			switch (type) {
			case SubscribeAdapter.TYPE_LOADING:
				if (convertView == null) {
					convertView = mLayoutInflater.inflate(
							R.layout.item_subscribe_loading, null);
				}
				break;
			case SubscribeAdapter.TYPE_ITEM:
				Subscribe item = this.getItem(position);
				if (convertView == null) {
					convertView = mLayoutInflater.inflate(
							R.layout.item_subscribe, null);
				}
				// name
				TextView display = (TextView) convertView
						.findViewById(R.id.subscribe_displayname);
				display.setText(item.display_name);

				// count
				TextView count = (TextView) convertView
						.findViewById(R.id.subscribe_count);
				count.setText(item.subscribers + " subscribers");
				// time
				TextView tv = (TextView) convertView
						.findViewById(R.id.subscribe_time);
				CharSequence time = CommonUtil.getRelateTimeString(
						item.created_utc * 1000,
						SubscribeFragmentList.this.getActivity());
				time = (String) time.subSequence(0, time.length() - 3);
				tv.setText("a community for " + time);

				TextView nsfw = (TextView) convertView
						.findViewById(R.id.subscribe_nsfw);
				nsfw.setVisibility(item.over18 ? View.VISIBLE : View.GONE);

				// button subscribe
				ImageView subscribe = (ImageView) convertView
						.findViewById(R.id.subscribe_button);
				subscribe.setTag(position);
				subscribe.setOnClickListener(SubscribeFragmentList.this);

				// button unsubscribe
				ImageView unsubscribe = (ImageView) convertView
						.findViewById(R.id.unsubscribe_button);
				unsubscribe.setTag(position);
				unsubscribe.setOnClickListener(SubscribeFragmentList.this);

				if (!mHaveUserLogin) {
					unsubscribe.setVisibility(View.GONE);
					subscribe.setVisibility(View.GONE);
				} else if (mType == MineSubredditDialog.KIND_MY_REDDITS) {
					unsubscribe.setVisibility(View.VISIBLE);
					subscribe.setVisibility(View.GONE);
				} else {
					unsubscribe.setVisibility(View.GONE);
					subscribe.setVisibility(View.VISIBLE);
				}

				// header pic
				ImageView rv = (ImageView) convertView
						.findViewById(R.id.subscribe_header_pic);

				if (item.over18 && mSafeForWork) {
					rv.setImageResource(R.drawable.icon_subreddit_all);
				} else if (item.header_pic == null
						|| "".equals(item.header_pic)) {
					rv.setImageResource(R.drawable.icon_subreddit_all);
				} else {
					((CacheFragmentActivity) SubscribeFragmentList.this
							.getActivity()).getImageWorker().loadImage(
							item.header_pic, rv);
				}
				break;
			case SubscribeAdapter.TYPE_MORE:
				if (convertView == null) {
					convertView = mLayoutInflater.inflate(
							R.layout.item_subscribe_more, null);
				}
				break;
			case SubscribeAdapter.TYPE_LOAD_FAIL:
				if (convertView == null) {
					return convertView = mLayoutInflater.inflate(
							R.layout.item_subscribe_load_fail, null);
				}
				break;
			case SubscribeAdapter.TYPE_NO_MORE:
				if (convertView == null) {
					convertView = mLayoutInflater.inflate(
							R.layout.item_subscribe_no_more, null);
				}
				break;
			case SubscribeAdapter.TYPE_NO_ITEM:
				if (convertView == null) {
					convertView = mLayoutInflater.inflate(
							R.layout.item_subscribe_no_item, null);
				}

				break;
			}
			return convertView;
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.subscribe_button:
			int p = (Integer) v.getTag();
			Subscribe s = this.mSubscribeAdapter.getItem(p);
			Toast.makeText(
					this.getActivity(),
					"Subscribing  " + s.display_name
							+ "\n note: result shows after next refresh",
					Toast.LENGTH_LONG).show();
			new SubscribeSubRedditTask(this.getActivity(), s.name,
					s.display_name, true).execute();
			break;
		case R.id.unsubscribe_button:
			int up = (Integer) v.getTag();
			Subscribe us = this.mSubscribeAdapter.getItem(up);
			Toast.makeText(
					this.getActivity(),
					"UnSubscribing " + us.display_name
							+ "\n  note: result shows after next refresh",
					Toast.LENGTH_LONG).show();
			new SubscribeSubRedditTask(this.getActivity(), us.name,
					us.display_name, false).execute();
			break;
		}
	}
}
