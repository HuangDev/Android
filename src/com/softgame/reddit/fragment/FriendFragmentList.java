package com.softgame.reddit.fragment;

import java.lang.ref.WeakReference;

import org.json.custom.JSONArray;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.softgame.reddit.R;
import com.softgame.reddit.impl.OnFriendItemClick;
import com.softgame.reddit.model.FriendItem;
import com.softgame.reddit.model.FriendModel;
import com.softgame.reddit.utils.Common;
import com.softgame.reddit.utils.RedditorManager;

/**
 * this is a list fragment for subscribe
 * 
 * @author xinyunxixi
 */
public class FriendFragmentList extends ListFragment implements
		OnItemClickListener {

	public static final String TAG = "FriendFragmentList";

	LayoutInflater mLayoutInflater;
	boolean isLoaded;

	// loading data task Array
	WeakReference<GetFriendsTask> mGetFriendTaskWF;
	// Adapter
	FriendAdapter mFriendAdapter;
	FriendModel mFriendModel;

	public FriendFragmentList() {
	}

	public static FriendFragmentList findOrCreateFriendFragmentList(
			FragmentManager manager) {
		FriendFragmentList fragment = (FriendFragmentList) manager
				.findFragmentByTag(FriendFragmentList.TAG);
		if (fragment == null) {
			fragment = new FriendFragmentList();
		}
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		this.setRetainInstance(true);
		super.onCreate(savedInstanceState);
		mFriendModel = new FriendModel();
		mFriendAdapter = new FriendAdapter();
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
		this.getListView().setAdapter(mFriendAdapter);
		this.getListView().setOnItemClickListener(this);
	}


	private void updateDataState() {
		if (mGetFriendTaskWF != null
				&& mGetFriendTaskWF.get() != null
				&& !AsyncTask.Status.FINISHED.equals(mGetFriendTaskWF.get()
						.getStatus())) {
			mGetFriendTaskWF.get().cancel(true);
		}
		GetFriendsTask task = new GetFriendsTask(this);
		task.execute();
	}

	/*
	 * Mine Subscribe Task
	 */
	public static class GetFriendsTask extends AsyncTask<Void, Void, String> {
		JSONArray dataJSON;
		String after;
		WeakReference<FriendFragmentList> fragmentWF;

		public GetFriendsTask(FriendFragmentList fragment) {
			fragmentWF = new WeakReference<FriendFragmentList>(fragment);
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (fragmentWF == null || fragmentWF.get() == null
					|| fragmentWF.get().getActivity() == null) {
				this.cancel(true);
			} else {
				fragmentWF.get().mFriendAdapter.setLoading(true, true);
				dataJSON = new JSONArray();
			}
		}

		@Override
		protected String doInBackground(Void... params) {

			if (this.isCancelled() || fragmentWF == null
					|| fragmentWF.get() == null
					|| fragmentWF.get().getActivity() == null) {
				this.cancel(true);
			} else {
				return RedditorManager.getFriendList(fragmentWF.get()
						.getActivity(), dataJSON);

			}
			return Common.RESULT_TASK_CANCLE;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			if (Common.RESULT_TASK_CANCLE.equals(result) || fragmentWF == null
					|| fragmentWF.get() == null
					|| fragmentWF.get().getActivity() == null) {
				// do nothing
				return;
			}
			if (Common.RESULT_SUCCESS.equals(result)) {
				FriendModel.ConvertToList(fragmentWF.get().mFriendModel,
						dataJSON);
			}
			fragmentWF.get().mFriendAdapter.setLoading(false, true);
		}
	}

	public class FriendAdapter extends BaseAdapter {

		private final int TYPE_COUNT = 4;
		// subscribe type such as ama pic fun... load from reddit
		public static final int TYPE_ITEM = 0x1;
		// load from reddit and no item
		public static final int TYPE_NO_ITEM = 0x2;
		public static final int TYPE_LOADING = 0x0;
		public static final int TYPE_FAIL = 0x3;

		public boolean isLoading = false;
		public boolean isFail = false;

		public void setLoading(boolean loading, boolean update) {
			isLoading = loading;
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
			case TYPE_FAIL:
				return true;
			}
			return false;
		}

		@Override
		public int getItemViewType(int position) {

			if (position >= 0 && position < mFriendModel.mFriendList.size()) {
				return TYPE_ITEM;
			} else if (isLoading) {
				return FriendAdapter.TYPE_LOADING;
			} else if (isFail) {
				return FriendAdapter.TYPE_FAIL;
			} else {
				if (mFriendModel.mFriendList.size() == 0) {
					return FriendAdapter.TYPE_NO_ITEM;
				}
				return FriendAdapter.TYPE_ITEM;
			}
		}

		@Override
		public int getViewTypeCount() {
			return TYPE_COUNT;
		}

		@Override
		public int getCount() {
			if (isLoading || isFail) {
				return 1;
			}
			if (mFriendModel.mFriendList.size() == 0) {
				return 1;
			}
			return mFriendModel.mFriendList.size();
		}

		@Override
		public FriendItem getItem(int position) {
			if (position >= 0 && position < mFriendModel.mFriendList.size()) {
				return mFriendModel.mFriendList.get(position);
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
			case FriendAdapter.TYPE_LOADING:
				if (convertView == null) {
					convertView = mLayoutInflater.inflate(
							R.layout.item_subscribe_loading, null);
				}
				break;
			case FriendAdapter.TYPE_ITEM:
				FriendItem item = this.getItem(position);
				if (convertView == null) {
					convertView = mLayoutInflater.inflate(R.layout.item_friend,
							null);
				}
				// name
				TextView display = (TextView) convertView
						.findViewById(R.id.friend_name);
				display.setText(item.name);
				break;
			case FriendAdapter.TYPE_NO_ITEM:
				if (convertView == null) {
					convertView = mLayoutInflater.inflate(
							R.layout.item_subscribe_no_item, null);
				}
				break;
			case FriendAdapter.TYPE_FAIL:
				if (convertView == null) {
					convertView = mLayoutInflater.inflate(
							R.layout.item_subscribe_load_fail, null);
				}
				break;
			}
			return convertView;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		switch (mFriendAdapter.getItemViewType(position)) {
		case FriendAdapter.TYPE_ITEM:
			((OnFriendItemClick) this.getActivity())
					.onFriendNameClick(mFriendAdapter.getItem(position).name);
			break;
		case FriendAdapter.TYPE_FAIL:
			// refresh
			mFriendAdapter.isFail = false;
			updateDataState();
			break;
		}
	}
}
