package com.softgame.reddit.fragment;

import java.lang.ref.WeakReference;

import org.json.custom.JSONObject;

import android.app.Activity;
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
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockListFragment;
import com.softgame.reddit.ComposeMessageActivity;
import com.softgame.reddit.OverviewFragmentActivity;
import com.softgame.reddit.R;
import com.softgame.reddit.model.RedditorItem;
import com.softgame.reddit.utils.Common;
import com.softgame.reddit.utils.CommonUtil;
import com.softgame.reddit.utils.RedditorManager;

public class RedditorFragment extends ListFragment implements OnClickListener {

	public static final String TAG = "RedditorFragment";
	String mUsername;
	WeakReference<RedditorTask> mRedditorTaskWF;
	WeakReference<AddFriendTask> mAddFriendTaskWF;
	RedditorItem mRedditorItem;

	public RedditorAdapter mRedditorAdapter;

	public static RedditorFragment newInstance(String name) {
		RedditorFragment fragment = new RedditorFragment();
		Bundle args = new Bundle();
		args.putString("username", name);
		fragment.setArguments(args);
		return fragment;
	}

	public RedditorFragment() {
	}

	public static RedditorFragment findOrCreateRedditorFragment(
			FragmentManager manager, String name, int id) {
		RedditorFragment fragment = (RedditorFragment) manager
				.findFragmentByTag(RedditorFragment.TAG + id);
		if (fragment == null) {
			fragment = new RedditorFragment();

			Bundle args = new Bundle();
			args.putString("username", name);
			fragment.setArguments(args);

			final FragmentTransaction ft = manager.beginTransaction();
			ft.add(android.R.id.content, fragment, RedditorFragment.TAG + id);
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
		this.setRetainInstance(true);
		mUsername = getArguments() != null ? getArguments().getString(
				"username") : "redditet";
		mRedditorAdapter = new RedditorAdapter();

		// start the task
		if (mRedditorTaskWF == null || mRedditorTaskWF.get() == null) {
			RedditorTask t = new RedditorTask(this);
			mRedditorTaskWF = new WeakReference<RedditorFragment.RedditorTask>(
					t);
			t.execute();
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.item_redditor_listview, container,
				false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		this.getListView().setAdapter(mRedditorAdapter);
	}

	public static class RedditorTask extends AsyncTask<Void, Void, String> {
		WeakReference<RedditorFragment> redditorFragmentWF;
		JSONObject infoJSON;

		public RedditorTask(RedditorFragment fragment) {
			redditorFragmentWF = new WeakReference<RedditorFragment>(fragment);
			infoJSON = new JSONObject();
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			redditorFragmentWF.get().mRedditorAdapter.setIsLoading(true, true);
		}

		@Override
		protected void onPostExecute(String result) {
			if (redditorFragmentWF.get() == null
					|| redditorFragmentWF.get().getActivity() == null
					|| this.isCancelled()
					|| Common.RESULT_TASK_CANCLE.equals(result)) {
				return;
			}

			if (Common.RESULT_SUCCESS.equals(result)) {
				redditorFragmentWF.get().mRedditorItem = new RedditorItem(
						infoJSON);
				redditorFragmentWF.get().mRedditorAdapter.setIsLoading(false,
						true);

			} else {
				Toast.makeText(redditorFragmentWF.get().getActivity(),
						"Load failed!", Toast.LENGTH_SHORT).show();
				redditorFragmentWF.get().getActivity().finish();
			}

		}

		@Override
		protected String doInBackground(Void... params) {
			if (redditorFragmentWF.get() == null || this.isCancelled()) {
				this.cancel(true);
				return Common.RESULT_TASK_CANCLE;
			} else {
				String result = RedditorManager.getRedditorProfile(
						redditorFragmentWF.get().mUsername, infoJSON,
						redditorFragmentWF.get().getActivity());
				return result;
			}
		}

	}

	public static class AddFriendTask extends AsyncTask<Void, Void, String> {
		WeakReference<RedditorFragment> redditorFragmentWF;
		JSONObject infoJSON;

		public AddFriendTask(RedditorFragment fragment) {
			redditorFragmentWF = new WeakReference<RedditorFragment>(fragment);
			infoJSON = new JSONObject();
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected String doInBackground(Void... params) {
			if (redditorFragmentWF.get() == null || this.isCancelled()) {
				this.cancel(true);
				return Common.RESULT_TASK_CANCLE;
			} else {
				return RedditorManager.addFriendRedditor(
						redditorFragmentWF.get().mRedditorItem.name,
						redditorFragmentWF.get().mRedditorItem.id,
						redditorFragmentWF.get().getActivity());
			}
		}

		@Override
		protected void onPostExecute(String result) {
			if (redditorFragmentWF == null || redditorFragmentWF.get() == null
					|| redditorFragmentWF.get().getActivity() == null
					|| this.isCancelled()
					|| Common.RESULT_TASK_CANCLE.equals(result)) {
				return;
			}

			if (Common.RESULT_SUCCESS.equals(result)) {
				Toast.makeText(redditorFragmentWF.get().getActivity(),
						"Add friend succeeded!", Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(redditorFragmentWF.get().getActivity(),
						"Add friend failed!", Toast.LENGTH_SHORT).show();
			}

		}

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.button_add_friend:
			if (mAddFriendTaskWF != null && mAddFriendTaskWF.get() == null) {
				Log.d(RedditorFragment.TAG, "task is already running!");
				Toast.makeText(getActivity(), "Adding friend!",
						Toast.LENGTH_SHORT).show();
			} else {
				AddFriendTask addFriendTask = new AddFriendTask(this);
				mAddFriendTaskWF = new WeakReference<RedditorFragment.AddFriendTask>(
						addFriendTask);
				addFriendTask.execute();
				Toast.makeText(getActivity(), "Adding friend!",
						Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.button_send_message:
			Intent t = new Intent(this.getActivity(),
					ComposeMessageActivity.class);
			t.putExtra(Common.KEY_REDDITOR_NAME, mUsername);
			this.getActivity().startActivity(t);
			break;
		}
	}

	public class RedditorAdapter extends BaseAdapter {
		private boolean isLoadingData;
		public static final int TYPE_COUNTS = 2;
		public static final int TYPE_LOADING = 0;
		public static final int TYPE_ITEM = 1;

		public void setIsLoading(boolean isLoading, boolean refresh) {
			isLoadingData = isLoading;
			if (refresh) {
				this.notifyDataSetChanged();
			}
		}

		@Override
		public int getCount() {
			return 1;
		}

		@Override
		public boolean areAllItemsEnabled() {
			return false;
		}

		@Override
		public int getItemViewType(int position) {
			if (isLoadingData) {
				return TYPE_LOADING;
			} else {
				return TYPE_ITEM;
			}
		}

		@Override
		public int getViewTypeCount() {
			return TYPE_COUNTS;
		}

		@Override
		public boolean isEnabled(int position) {
			return false;
		}

		@Override
		public RedditorItem getItem(int position) {
			return mRedditorItem;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			RedditorItem item = this.getItem(position);
			switch (this.getItemViewType(position)) {
			case TYPE_LOADING:
				if (convertView == null) {
					convertView = RedditorFragment.this.getActivity()
							.getLayoutInflater()
							.inflate(R.layout.item_loading, null);
				}
				break;
			case TYPE_ITEM:
				if (convertView == null) {
					convertView = RedditorFragment.this.getActivity()
							.getLayoutInflater()
							.inflate(R.layout.item_redditor_profile, null);
				}

				TextView name = (TextView) convertView
						.findViewById(R.id.redditor_name);

				Button addFriendButton = (Button) convertView
						.findViewById(R.id.button_add_friend);
				Button sendMessageButton = (Button) convertView
						.findViewById(R.id.button_send_message);
				addFriendButton.setOnClickListener(RedditorFragment.this);
				sendMessageButton.setOnClickListener(RedditorFragment.this);

				name.setText(item.name);

				TextView linkKarma = (TextView) convertView
						.findViewById(R.id.redditor_link_karma);
				TextView commentKarma = (TextView) convertView
						.findViewById(R.id.redditor_comment_karma);

				TextView ageText = (TextView) convertView
						.findViewById(R.id.redditor_age);
				CharSequence age = CommonUtil.getRelateTimeString(
						item.age * 1000, RedditorFragment.this.getActivity());
				ageText.setText("profile created " + age);

				linkKarma.setText(item.link_karma + "");
				commentKarma.setText(item.comment_karma + "");
				break;
			}

			return convertView;
		}
	}

}
