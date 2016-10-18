package com.softgame.reddit.fragment;

import java.lang.ref.WeakReference;

import org.json.custom.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.util.Log;
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
import com.softgame.reddit.fragment.SubscribeFragmentList.SubscribeSubRedditTask;
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
public class SearchSubscribeFragmentList extends ListFragment implements
		OnClickListener {

	public static final String TAG = "SeachSubscribeFragmentList";
	public static final int ACTION_SUBREDDIT_ITEM = 0x106;
	public static final int ACTION_SUBREDDIT_SUBSCRIBE = 0x107;
	public static final int ACTION_SUBREDDIT_UNSUBSCRIBE = 0x108;

	boolean isLoaded;

	public String mSearchItem;
	SubscribeModel mSubscribeModel;
	// loading data task Array
	WeakReference<SearchSubscribeTask> mSearchSubscribeTaskWF;
	// Adapter
	SearchSubscribeAdapter mSubscribeAdapter;
	LayoutInflater mLayoutInflater;

	boolean mIsUserAuth;

	public SearchSubscribeFragmentList() {
	}

	public static SearchSubscribeFragmentList findOrCreateFragment(
			int container_id, FragmentManager manager, long id) {

		SearchSubscribeFragmentList fragment = (SearchSubscribeFragmentList) manager
				.findFragmentByTag(SearchSubscribeFragmentList.TAG + id);
		if (fragment == null) {
			fragment = new SearchSubscribeFragmentList();
			final FragmentTransaction ft = manager.beginTransaction();
			ft.add(container_id, fragment, SearchSubscribeFragmentList.TAG + id);
			ft.commit();
		}
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		this.setRetainInstance(true);
		super.onCreate(savedInstanceState);
		mIsUserAuth = RedditManager.isUserAuth(this.getActivity());
		mSubscribeModel = new SubscribeModel();
		mSubscribeAdapter = new SearchSubscribeAdapter();
		if (mSearchItem != null && !"".equals(mSearchItem)) {
			updateDataState(mSearchItem);
		}
	}

	public void setSearchItem(String search) {
		if (search != null && !"".equals(search) && !search.equals(mSearchItem)) {
			mSearchItem = search;
			updateDataState(mSearchItem);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mLayoutInflater = inflater;
		return inflater.inflate(R.layout.fragment_listview, container, false);
	}

	public void notifyLogchange() {
		if (mSubscribeAdapter != null) {
			mSubscribeAdapter.notifyDataSetChanged();
		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		this.getListView().setAdapter(mSubscribeAdapter);
	}

	private void updateDataState(String search) {
		if (mSearchSubscribeTaskWF != null
				&& mSearchSubscribeTaskWF.get() != null
				&& !AsyncTask.Status.FINISHED.equals(mSearchSubscribeTaskWF
						.get().getStatus())) {
			mSearchSubscribeTaskWF.get().cancel(true);
		}
		SearchSubscribeTask task = new SearchSubscribeTask(search, this);
		mSearchSubscribeTaskWF = new WeakReference<SearchSubscribeFragmentList.SearchSubscribeTask>(
				task);
		task.execute();
	}

	private void updateDataState(String search, String a) {
		if (mSearchSubscribeTaskWF != null
				&& mSearchSubscribeTaskWF.get() != null
				&& !AsyncTask.Status.FINISHED.equals(mSearchSubscribeTaskWF
						.get().getStatus())) {
			mSearchSubscribeTaskWF.get().cancel(true);
		}
		SearchSubscribeTask task = new SearchSubscribeTask(search, this, a);
		mSearchSubscribeTaskWF = new WeakReference<SearchSubscribeFragmentList.SearchSubscribeTask>(
				task);
		task.execute();
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {

		if (mSubscribeAdapter.getItemViewType(position) == SearchSubscribeAdapter.TYPE_MORE) {
			String after = mSubscribeModel.after;
			updateDataState(mSearchItem, after);
		} else if (mSubscribeAdapter.getItemViewType(position) == SearchSubscribeAdapter.TYPE_ITEM) {
			{
				Subscribe s = mSubscribeModel.subscribeList.get(position);
				Intent ss = new Intent();
				ss.putExtra(Common.EXTRA_SUBREDDIT, s.url);
				ss.putExtra(Common.EXTRA_SUBREDDIT_NAME, s.display_name);
				this.getActivity().setResult(Activity.RESULT_OK, ss);
				this.getActivity().finish();
			}
		}
	}

	/*
	 * Mine Subscribe Task
	 */
	public static class SearchSubscribeTask extends
			AsyncTask<Void, Void, String> {
		JSONObject dataJSON;
		String searchItem;
		String after;
		WeakReference<SearchSubscribeFragmentList> fragmentWF;

		public SearchSubscribeTask(String search,
				SearchSubscribeFragmentList fragment) {
			fragmentWF = new WeakReference<SearchSubscribeFragmentList>(
					fragment);
			searchItem = search;
		}

		public SearchSubscribeTask(String search,
				SearchSubscribeFragmentList fragment, String a) {
			this(search, fragment);
			after = a;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (fragmentWF == null || fragmentWF.get() == null
					|| fragmentWF.get().getActivity() == null) {
				this.cancel(true);
			} else {
				if (after == null || "".equals(after)) {
					fragmentWF.get().mSubscribeAdapter.clear(false);
				}
				dataJSON = new JSONObject();
				fragmentWF.get().mSubscribeAdapter.setLoading(true, true);
			}
		}

		@Override
		protected String doInBackground(Void... params) {
			if (this.isCancelled() || fragmentWF == null
					|| fragmentWF.get() == null
					|| fragmentWF.get().getActivity() == null) {
				this.cancel(true);
				return Common.RESULT_TASK_CANCLE;
			} else {
				return SubscribeManager.getSearchSubscribeList(searchItem,
						after, dataJSON, fragmentWF.get().getActivity());
			}
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			if (this.isCancelled() || fragmentWF == null
					|| fragmentWF.get() == null
					|| fragmentWF.get().getActivity() == null
					|| Common.RESULT_TASK_CANCLE.equals(result)) {
				return;
			}

			if (Common.RESULT_FETCHING_FAIL.equals(result)) {
				Toast.makeText(fragmentWF.get().getActivity(),
						"connect failed!", Toast.LENGTH_SHORT).show();
			}
			if (Common.RESULT_SUCCESS.equals(result)) {
				SubscribeModel.convertToList(dataJSON,
						fragmentWF.get().mSubscribeModel);
			}
			fragmentWF.get().mSubscribeAdapter.setLoading(false, true);

		}
	}

	public class SearchSubscribeAdapter extends BaseAdapter {

		private final int TYPE_COUNT = 5;
		// subscribe type such as ama pic fun... load from reddit
		public static final int TYPE_ITEM = 0x1;
		// load from reddit and no item
		public static final int TYPE_NO_ITEM = 0x2;
		public static final int TYPE_MORE = 0x3;
		public static final int TYPE_NO_MORE = 0x4;
		public static final int TYPE_LOADING = 0x0;

		public boolean isLoading = false;

		public SearchSubscribeAdapter() {
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
				return SearchSubscribeAdapter.TYPE_LOADING;
			} else {
				if (mSubscribeModel.subscribeList.size() == 0) {
					return SearchSubscribeAdapter.TYPE_NO_ITEM;
				}
				if (mSubscribeModel.after == null
						|| "".equals(mSubscribeModel.after)) {
					return SearchSubscribeAdapter.TYPE_NO_MORE;
				} else {
					return SearchSubscribeAdapter.TYPE_MORE;
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
			case SearchSubscribeAdapter.TYPE_LOADING:
				if (convertView == null) {
					convertView = mLayoutInflater.inflate(
							R.layout.item_subscribe_loading, null);
				}
				break;
			case SearchSubscribeAdapter.TYPE_ITEM:
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
						SearchSubscribeFragmentList.this.getActivity());
				time = (String) time.subSequence(0, time.length() - 3);
				tv.setText("a community for " + time);

				// nsfw
				TextView nsfw = (TextView) convertView
						.findViewById(R.id.subscribe_nsfw);
				nsfw.setVisibility(item.over18 ? View.VISIBLE : View.GONE);

				// button subscribe
				ImageView subscribe = (ImageView) convertView
						.findViewById(R.id.subscribe_button);
				subscribe.setTag(position);
				subscribe.setOnClickListener(SearchSubscribeFragmentList.this);

				// button unsubscribe
				ImageView unsubscribe = (ImageView) convertView
						.findViewById(R.id.unsubscribe_button);
				unsubscribe.setTag(position);
				unsubscribe
						.setOnClickListener(SearchSubscribeFragmentList.this);

				if (!mIsUserAuth) {
					unsubscribe.setVisibility(View.GONE);
					subscribe.setVisibility(View.GONE);
				} else {
					unsubscribe.setVisibility(View.GONE);
					subscribe.setVisibility(View.VISIBLE);
				}

				// header pic
				ImageView rv = (ImageView) convertView
						.findViewById(R.id.subscribe_header_pic);

				if (item.header_pic == null || "".equals(item.header_pic)
						|| "null".equals(item.header_pic)) {
					rv.setImageResource(R.drawable.icon_subreddit_all);
				} else {
					Log.d(TAG, "item.header_pic:" + item.header_pic);
					((CacheFragmentActivity) SearchSubscribeFragmentList.this
							.getActivity()).getImageWorker().loadImage(
							item.header_pic, rv);
				}
				break;
			case SearchSubscribeAdapter.TYPE_MORE:
				if (convertView == null) {
					convertView = mLayoutInflater.inflate(
							R.layout.item_subscribe_more, null);
				}
				break;
			case SearchSubscribeAdapter.TYPE_NO_MORE:
				if (convertView == null) {
					convertView = mLayoutInflater.inflate(
							R.layout.item_subscribe_no_more, null);
				}
				break;
			case SearchSubscribeAdapter.TYPE_NO_ITEM:
				if (convertView == null) {
					convertView = mLayoutInflater.inflate(
							R.layout.item_subscribe_no_item, null);
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
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.subscribe_button:
			int p = (Integer) v.getTag();
			Subscribe s = this.mSubscribeAdapter.getItem(p);
			Toast.makeText(this.getActivity(),
					"Subscribing  " + s.display_name, Toast.LENGTH_LONG).show();
			new SubscribeSubRedditTask(this.getActivity(), s.name,
					s.display_name, true).execute();
			break;
		case R.id.unsubscribe_button:
			int up = (Integer) v.getTag();
			Subscribe us = this.mSubscribeAdapter.getItem(up);
			Toast.makeText(this.getActivity(),
					"UnSubscribing " + us.display_name, Toast.LENGTH_LONG)
					.show();
			new SubscribeSubRedditTask(this.getActivity(), us.name,
					us.display_name, false).execute();
			break;
		}
	}
}