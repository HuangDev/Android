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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.softgame.reddit.ConversationActivity;
import com.softgame.reddit.R;
import com.softgame.reddit.model.MessageItem;
import com.softgame.reddit.model.MessageModel;
import com.softgame.reddit.utils.Common;
import com.softgame.reddit.utils.CommonUtil;
import com.softgame.reddit.utils.MessageManager;

public class UnreadMessageFragmentList extends ListFragment implements
		OnClickListener, OnItemClickListener {

	public static final String TAG = "UnreadMessageFragmentList";

	// Adapter
	MessageAdapter mMessageAdapter;

	LayoutInflater mLayoutInflater;

	public UnreadMessageFragmentList() {
	}

	public static UnreadMessageFragmentList findOrCreateMessageFragmentList(
			FragmentManager manager) {

		UnreadMessageFragmentList fragment = (UnreadMessageFragmentList) manager
				.findFragmentByTag(UnreadMessageFragmentList.TAG);
		if (fragment == null) {
			fragment = UnreadMessageFragmentList.newInstance();
			// create a new Fragment
			final FragmentTransaction ft = manager.beginTransaction();
			ft.add(android.R.id.content, fragment,
					UnreadMessageFragmentList.TAG);
			ft.commit();
		}
		return fragment;
	}

	// create a new FragmentList
	public static UnreadMessageFragmentList newInstance() {
		return new UnreadMessageFragmentList();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mLayoutInflater = inflater;
		return inflater.inflate(R.layout.item_fragment_email_listview,
				container, false);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setRetainInstance(true);
		mMessageAdapter = new MessageAdapter();
		// load data
		new MessageTask(mMessageAdapter, this.getActivity()).execute();
		// make read
	}

	/*
	 * Message Task
	 */
	public static class MarkReadTask extends AsyncTask<Void, Void, String> {
		WeakReference<Context> contextWF;

		public MarkReadTask(Context context) {
			contextWF = new WeakReference<Context>(context);
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			Log.d(TAG, "start mark read ..");
		}

		@Override
		protected String doInBackground(Void... params) {
			if (contextWF.get() != null) {
				return MessageManager.markReadMessage(contextWF.get());
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
			if (contextWF.get() != null) {
				if (Common.RESULT_SUCCESS.equals(result)) {
					Log.d(TAG, "mark read end!");
				}
			}
		}
	}

	/*
	 * Message Task
	 */
	public static class MessageTask extends AsyncTask<Void, Void, String> {
		JSONObject dataJSON;
		String after;
		WeakReference<MessageAdapter> messageAdpaterWF;
		WeakReference<Activity> contextWF;

		public MessageTask(MessageAdapter sa, Activity context) {
			messageAdpaterWF = new WeakReference<MessageAdapter>(sa);
			contextWF = new WeakReference<Activity>(context);
		}

		public MessageTask(String a, MessageAdapter sa, Activity context) {
			after = a;
			messageAdpaterWF = new WeakReference<MessageAdapter>(sa);
			contextWF = new WeakReference<Activity>(context);
		}

		public MessageTask(String t, String a, String search,
				MessageAdapter sa, Activity context) {
			after = a;
			messageAdpaterWF = new WeakReference<MessageAdapter>(sa);
			contextWF = new WeakReference<Activity>(context);
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			MessageAdapter sa = messageAdpaterWF.get();
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

			if (messageAdpaterWF.get() != null && contextWF.get() != null) {
				String result = MessageManager.getMessage(
						Common.MESSAGE_UNREAD_PATH, after, dataJSON,
						contextWF.get());
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
			if (messageAdpaterWF.get() != null && contextWF.get() != null) {
				if (Common.RESULT_SUCCESS.equals(result)) {
					MessageModel.convertToList(dataJSON,
							messageAdpaterWF.get().mMessageModel);
					messageAdpaterWF.get().setLoading(false, true);
					new MarkReadTask(contextWF.get().getApplicationContext())
							.execute();
				}
			}
		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		this.getListView().setAdapter(mMessageAdapter);
		this.getListView().setOnItemClickListener(this);
	}

	public class MessageAdapter extends BaseAdapter {

		private static final int NUM_COUNT = 6;
		public static final int TYPE_MESSAGE = 0x0;
		public static final int TYPE_NO_ITEM = 0x2;
		public static final int TYPE_NO_MORE = 0x3;
		public static final int TYPE_MORE = 0x4;
		public static final int TYPE_LOADING = 0x1;
		public static final int TYPE_COMMENT_REPLY = 0x5;

		public boolean isLoading = false;
		public MessageModel mMessageModel;

		public MessageAdapter() {
			mMessageModel = new MessageModel();
		}

		public MessageAdapter(MessageModel mm) {
			mMessageModel = mm;
		}

		@Override
		public boolean areAllItemsEnabled() {
			return false;
		}

		@Override
		public boolean isEnabled(int position) {
			switch (this.getItemViewType(position)) {
			case TYPE_NO_ITEM:
			case TYPE_NO_MORE:
			case TYPE_LOADING:
				return false;
			}
			return true;
		}

		@Override
		public int getCount() {
			return mMessageModel.getItemsSize() + 1;
		}

		public void setLoading(boolean loading, boolean refresh) {
			isLoading = loading;
			if (refresh) {
				this.notifyDataSetChanged();
			}
		}

		public void clear(boolean refresh) {
			mMessageModel.mItemList.clear();
			if (refresh) {
				this.notifyDataSetChanged();
			}
		}

		@Override
		public MessageItem getItem(int position) {
			if (position >= 0 && position < mMessageModel.mItemList.size()) {
				return mMessageModel.mItemList.get(position);
			} else {
				return null;
			}
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public int getItemViewType(int position) {
			if (position >= 0 && position < mMessageModel.mItemList.size()) {
				MessageItem s = this.getItem(position);
				if ("t1".equals(s.kind)) {
					return TYPE_COMMENT_REPLY;
				} else {
					return TYPE_MESSAGE;
				}
			} else if (isLoading) {
				return MessageAdapter.TYPE_LOADING;
			} else {
				if (mMessageModel.mItemList.size() == 0) {
					return MessageAdapter.TYPE_NO_ITEM;
				}
				if (mMessageModel.after == null
						|| "".equals(mMessageModel.after)) {
					return MessageAdapter.TYPE_NO_MORE;
				} else {
					return MessageAdapter.TYPE_MORE;
				}

			}
		}

		@Override
		public int getViewTypeCount() {
			return NUM_COUNT;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			MessageItem item = this.getItem(position);
			switch (this.getItemViewType(position)) {
			case MessageAdapter.TYPE_COMMENT_REPLY:
				if (convertView == null) {
					convertView = mLayoutInflater.inflate(
							R.layout.item_comment_reply, null);
				}
				TextView commentAuthor = (TextView) convertView
						.findViewById(R.id.comment_author);
				TextView commentDate = (TextView) convertView
						.findViewById(R.id.comment_date);
				TextView commentSubreddit = (TextView) convertView
						.findViewById(R.id.comment_subreddit);
				TextView commentBody = (TextView) convertView
						.findViewById(R.id.comment_body);

				commentAuthor.setText(item.author);
				commentDate.setText(CommonUtil.getRelateTimeString(
						item.created_utc * 1000,
						UnreadMessageFragmentList.this.getActivity()));
				commentSubreddit.setText(item.subreddit);
				commentBody.setText(item.body);
				break;
			case MessageAdapter.TYPE_MESSAGE:
				if (convertView == null) {
					convertView = mLayoutInflater.inflate(
							R.layout.item_message, null);
				}
				TextView author = (TextView) convertView
						.findViewById(R.id.message_author);
				TextView message_body = (TextView) convertView
						.findViewById(R.id.message_body);
				TextView date = (TextView) convertView
						.findViewById(R.id.comment_date);

				TextView subject = (TextView) convertView
						.findViewById(R.id.message_subject);

				author.setText(item.author);

				subject.setText(item.subject);
				date.setText(CommonUtil.getRelateTimeString(
						item.created_utc * 1000,
						UnreadMessageFragmentList.this.getActivity()));
				message_body.setText(item.body);
				break;
			case MessageAdapter.TYPE_LOADING:
				if (convertView == null) {
					convertView = mLayoutInflater.inflate(
							R.layout.item_loading, null);
				}
				break;

			case MessageAdapter.TYPE_MORE:
				if (convertView == null) {
					convertView = mLayoutInflater.inflate(R.layout.item_more,
							null);
				}
				break;
			case MessageAdapter.TYPE_NO_MORE:
				if (convertView == null) {
					convertView = mLayoutInflater.inflate(
							R.layout.item_no_more, null);
				}
				break;
			case MessageAdapter.TYPE_NO_ITEM:
				if (convertView == null) {
					convertView = mLayoutInflater.inflate(
							R.layout.item_no_item, null);
				}
				break;
			}
			return convertView;

		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position,
			long id) {
		switch (mMessageAdapter.getItemViewType(position)) {
		case MessageAdapter.TYPE_MESSAGE:
			String sender = this.mMessageAdapter.getItem(position)
					.getMessageSender(this.getActivity());
			String messageId = this.mMessageAdapter.getItem(position)
					.getParentId();
			String messageName = this.mMessageAdapter.getItem(position).name;
			Intent t = new Intent(this.getActivity(),
					ConversationActivity.class);
			t.putExtra(Common.EXTRA_MESSAGE_SENDER, sender);
			t.putExtra(Common.EXTRA_MESSAGE_ID, messageId);
			t.putExtra(Common.EXTRA_MESSAGE_NAME, messageName);
			this.getActivity().startActivity(t);
			break;

		}

	}

	@Override
	public void onClick(View v) {

	}
}
