package com.softgame.reddit.fragment;

import java.lang.ref.WeakReference;

import org.json.custom.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.softgame.reddit.ConversationActivity;
import com.softgame.reddit.OverviewCommentActivity;
import com.softgame.reddit.R;
import com.softgame.reddit.impl.OnSubscribeItemClickListener;
import com.softgame.reddit.model.MessageItem;
import com.softgame.reddit.model.MessageModel;
import com.softgame.reddit.utils.Common;
import com.softgame.reddit.utils.CommonUtil;
import com.softgame.reddit.utils.MessageManager;

public class MessageFragmentList extends ListFragment implements
		OnClickListener, OnItemClickListener {
	// view
	private String mType;
	OnSubscribeItemClickListener listener;
	boolean isLoaded;

	// Adapter
	MessageAdapter mMessageAdapter;

	WeakReference<MessageTask> mMessageTaskWF;

	String[] mInBoxTypeArray;

	LayoutInflater mLayoutInflater;

	public MessageFragmentList() {
	}

	// create a new FragmentList
	public static MessageFragmentList newInstance(String type) {
		MessageFragmentList f = new MessageFragmentList();
		// Supply num input as an argument.
		Bundle args = new Bundle();
		args.putString(Common.KEY_MESSAGE_TYPE, type);
		f.setArguments(args);
		return f;
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
		mType = getArguments() != null ? getArguments().getString(
				Common.KEY_MESSAGE_TYPE) : "inbox";
		new MessageTask(mType, this).execute();
	}

	public void updateDataState() {
		new MessageTask(mType, this).execute();
	}

	public void updateDataState(String after) {
		new MessageTask(mType, after, this).execute();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		this.getListView().setAdapter(mMessageAdapter);
		this.getListView().setOnItemClickListener(this);
	}

	/*
	 * Message Task
	 */
	public static class MessageTask extends AsyncTask<Void, Void, String> {
		JSONObject dataJSON;
		String type;
		String after;
		WeakReference<MessageFragmentList> fragmentWF;

		public MessageTask(String t, MessageFragmentList fragment) {
			fragmentWF = new WeakReference<MessageFragmentList>(fragment);
			type = t;
		}

		public MessageTask(String t, String a, MessageFragmentList fragment) {
			type = t;
			after = a;
			fragmentWF = new WeakReference<MessageFragmentList>(fragment);
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (fragmentWF == null || fragmentWF.get() == null
					|| fragmentWF.get().getActivity() == null) {
				this.cancel(true);
			} else {
				if (after == null || "".equals(after)) {
					fragmentWF.get().mMessageAdapter.clear(false);
				}
				fragmentWF.get().mMessageAdapter.setLoading(true, true);
				dataJSON = new JSONObject();
			}

		}

		@Override
		protected String doInBackground(Void... params) {
			if (fragmentWF == null || fragmentWF.get() == null
					|| fragmentWF.get().getActivity() == null) {
				this.cancel(true);
				return Common.RESULT_TASK_CANCLE;
			} else {
				String result = MessageManager.getMessage(type, after,
						dataJSON, fragmentWF.get().getActivity());
				return result;
			}
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
				MessageModel.convertToList(dataJSON,
						fragmentWF.get().mMessageAdapter.mMessageModel);
			}
			fragmentWF.get().mMessageAdapter.setLoading(false, true);
		}
	}

	public class MessageAdapter extends BaseAdapter {

		private static final int NUM_COUNT = 6;

		public static final int TYPE_COMMENT_REPLY = 0x0;
		public static final int TYPE_MESSAGE = 0x1;
		public static final int TYPE_NO_ITEM = 0x2;
		public static final int TYPE_NO_MORE = 0x3;
		public static final int TYPE_MORE = 0x4;
		public static final int TYPE_LOADING = 0x5;

		public boolean isLoading = false;
		public MessageModel mMessageModel;

		public MessageAdapter() {
			mMessageModel = new MessageModel();
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

				TextView newComment = (TextView) convertView
						.findViewById(R.id.comment_new);
				TextView commentAuthor = (TextView) convertView
						.findViewById(R.id.comment_author);
				TextView commentDate = (TextView) convertView
						.findViewById(R.id.comment_time);
				TextView commentSubreddit = (TextView) convertView
						.findViewById(R.id.comment_subreddit);
				TextView commentBody = (TextView) convertView
						.findViewById(R.id.comment);

				newComment.setVisibility(item.isNew ? View.VISIBLE : View.GONE);
				commentAuthor.setText(item.author);
				commentDate.setText(CommonUtil.getRelateTimeString(
						item.created_utc * 1000,
						MessageFragmentList.this.getActivity()));
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
				String f = "(" + item.getCount() + ")";

				author.setText(item.getMessageSender(MessageFragmentList.this
						.getActivity()) + f);

				subject.setText(item.subject);
				date.setText(CommonUtil.getRelateTimeString(
						item.created_utc * 1000,
						MessageFragmentList.this.getActivity()));
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
		case MessageAdapter.TYPE_COMMENT_REPLY:

			Intent tt = new Intent(this.getActivity(),
					OverviewCommentActivity.class);

			tt.putExtra(
					Common.EXTRA_OVERVIEW_COMMENT_URL,
					"http://www.reddit.com/"
							+ this.mMessageAdapter.getItem(position).context);
			tt.putExtra(Common.EXTRA_OVERVIEW_LOAD_TYPE,
					OverviewCommentActivity.LOAD_TYPE_SHARE_COMMENT_CONTEXT);
			this.getActivity().startActivity(tt);

			break;
		case MessageAdapter.TYPE_MESSAGE:
			String sender = this.mMessageAdapter.getItem(position)
					.getMessageSender(this.getActivity());
			String messageId = this.mMessageAdapter.getItem(position).id;
			String messageName = this.mMessageAdapter.getItem(position).name;
			Intent t = new Intent(this.getActivity(),
					ConversationActivity.class);
			t.putExtra(Common.EXTRA_MESSAGE_SENDER, sender);
			t.putExtra(Common.EXTRA_MESSAGE_ID, messageId);
			t.putExtra(Common.EXTRA_MESSAGE_NAME, messageName);
			this.getActivity().startActivity(t);
			break;
		case MessageAdapter.TYPE_MORE:
			updateDataState(this.mMessageAdapter.mMessageModel.after);
			break;
		}

	}

	@Override
	public void onClick(View v) {

	}
}
