package com.softgame.reddit.fragment;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import org.json.custom.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.app.SherlockListFragment;
import com.softgame.reddit.ConversationActivity;
import com.softgame.reddit.R;
import com.softgame.reddit.model.MessageItem;
import com.softgame.reddit.model.MessageModel;
import com.softgame.reddit.utils.Common;
import com.softgame.reddit.utils.CommonUtil;
import com.softgame.reddit.utils.MessageManager;
import com.softgame.reddit.utils.RedditCommentManager;
import com.softgame.reddit.utils.RedditManager;
import com.softgame.reddit.utils.RedditorManager;

public class ConversationListFragment extends SherlockListFragment {

	public static final String TAG = "ConversationListFragment";

	public MessageModel mMessageModel;

	ConversationAdapter mAdapter;
	LayoutInflater mInflater;
	String mLoginUser;
	String mMessageId;
	String mSender;

	public ConversationListFragment() {
	}

	ArrayList<MessageItem> mAddMessageItemList;

	public static ConversationListFragment findOrCreateConversationListFragment(
			FragmentManager manager, String messageId) {
		ConversationListFragment fragment = (ConversationListFragment) manager
				.findFragmentByTag(ConversationListFragment.TAG);
		if (fragment == null) {
			fragment = new ConversationListFragment();
			Bundle b = new Bundle();
			b.putString(Common.KEY_MESSAGE_ID, messageId);
			fragment.setArguments(b);
			final FragmentTransaction ft = manager.beginTransaction();
			ft.add(R.id.fragment_container, fragment,
					ConversationListFragment.TAG);
			ft.commit();
		}
		return fragment;
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		switch (mAdapter.getItemViewType(position)) {
		case ConversationAdapter.TYPE_MORE:
			new MessageTask(mMessageId, mMessageModel.after, this).execute();
			break;
		}
	}

	public String getSender() {
		return mSender;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		this.setRetainInstance(true);
		super.onCreate(savedInstanceState);
		if (this.getArguments() != null) {
			mMessageId = this.getArguments().getString(Common.KEY_MESSAGE_ID);
		}
		if (mMessageId == null || "".equals(mMessageId)) {
			Toast.makeText(this.getActivity(), "Messages missing!",
					Toast.LENGTH_SHORT).show();
			this.getActivity().finish();
		}
		mLoginUser = RedditManager.getUserName(this.getActivity());
		mMessageModel = new MessageModel();
		mAddMessageItemList = new ArrayList<MessageItem>();
		mAdapter = new ConversationAdapter();
		new MessageTask(mMessageId, this).execute();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mInflater = inflater;
		return inflater.inflate(R.layout.item_fragment_conversation_listview,
				container, false);
	}

	public void postReply(MessageItem item) {
		addMessageItem(item);
		new PostMessageTask(this, item).execute();
	}

	public void addMessageItem(MessageItem messageItem) {
		// mMessageModel.mIndexList.
		mAddMessageItemList.add(messageItem);
		// add again
		mMessageModel.addToIndexList(messageItem);
		mAdapter.notifyDataSetChanged();
		this.getListView().smoothScrollToPosition(
				mMessageModel.getIndexListSize());
	}

	public void removeMessageItem(MessageItem messageItem) {
		if (mAddMessageItemList.remove(messageItem)
				&& mMessageModel.mIndexList.mMessageList.remove(messageItem)) {
			mAdapter.notifyDataSetChanged();
		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		this.getListView().setAdapter(mAdapter);
		if (mSender != null && !"".equals(mSender)) {
			((SherlockFragmentActivity) (this.getActivity()))
					.getSupportActionBar().setTitle(mSender);
		}
	}

	public class ConversationAdapter extends BaseAdapter {

		boolean isLoading = false;
		private int TYPE_COUNT = 5;
		// must start from 0
		public static final int TYPE_CHATS_HIS = 0;
		public static final int TYPE_CHATS_MY = 1;
		public static final int TYPE_LOADING = 2;
		public static final int TYPE_MORE = 3;
		public static final int TYPE_EMPTY = 4;

		public void setIsLoading(boolean loading, boolean refresh) {
			isLoading = loading;
			if (refresh) {
				this.notifyDataSetChanged();
			}
		}

		@Override
		public int getCount() {
			return mMessageModel.mIndexList.getCount() + 1;
		}

		@Override
		public int getViewTypeCount() {
			return TYPE_COUNT;
		}

		@Override
		public boolean areAllItemsEnabled() {
			return false;
		}

		@Override
		public boolean isEnabled(int position) {
			if (this.getItemViewType(position) == TYPE_MORE) {
				return true;
			} else {
				return false;
			}
		}

		@Override
		public int getItemViewType(int position) {

			if (position < mMessageModel.mIndexList.getCount()) {
				String author = this.getItem(position).author;
				if (author.equalsIgnoreCase(mLoginUser)) {
					return TYPE_CHATS_MY;
				} else {
					return TYPE_CHATS_HIS;
				}

			}
			if (isLoading) {
				return TYPE_LOADING;
			}
			if (mMessageModel.after != null && !"".equals(mMessageModel.after)) {
				return TYPE_MORE;
			} else {
				return TYPE_EMPTY;
			}

		}

		@Override
		public MessageItem getItem(int position) {
			if (position < mMessageModel.mIndexList.getCount()) {
				return mMessageModel.mIndexList.getItem(position);
			} else {
				return null;
			}
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			int type = this.getItemViewType(position);
			MessageItem item = this.getItem(position);
			switch (type) {
			case TYPE_CHATS_MY:
				if (convertView == null) {
					convertView = mInflater.inflate(
							R.layout.item_conversation_mine, null);
				}

				TextView messageM = (TextView) convertView
						.findViewById(R.id.conversation_message);
				TextView dateM = (TextView) convertView
						.findViewById(R.id.conversation_date);

				messageM.setText(item.body);

				dateM.setText(CommonUtil.getRelateTimeString(
						item.created_utc * 1000,
						ConversationListFragment.this.getActivity()));

				break;
			case TYPE_CHATS_HIS:
				if (convertView == null) {
					convertView = mInflater.inflate(
							R.layout.item_conversation_his, null);
				}

				TextView messageH = (TextView) convertView
						.findViewById(R.id.conversation_message);
				TextView dateH = (TextView) convertView
						.findViewById(R.id.conversation_date);
				messageH.setText(item.body);
				dateH.setText(CommonUtil.getRelateTimeString(
						item.created_utc * 1000,
						ConversationListFragment.this.getActivity()));
				break;
			case TYPE_LOADING:
				if (convertView == null) {
					convertView = mInflater
							.inflate(R.layout.item_loading, null);
				}
				break;
			case TYPE_MORE:
				if (convertView == null) {
					convertView = mInflater.inflate(R.layout.item_more, null);
				}

				break;
			case TYPE_EMPTY:
				if (convertView == null) {
					convertView = mInflater.inflate(R.layout.item_empty, null);
				}
				break;

			}
			return convertView;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}
	}

	/*
	 * Message Task
	 */
	public static class MessageTask extends AsyncTask<Void, Void, String> {
		JSONObject dataJSON;
		String messageId;
		String after;
		WeakReference<ConversationListFragment> fragmentWF;

		public MessageTask(String id, ConversationListFragment fragment) {
			fragmentWF = new WeakReference<ConversationListFragment>(fragment);
			messageId = id;
		}

		public MessageTask(String id, String a,
				ConversationListFragment fragment) {
			fragmentWF = new WeakReference<ConversationListFragment>(fragment);
			messageId = id;
			after = a;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (fragmentWF == null || fragmentWF.get() == null
					|| fragmentWF.get().getActivity() == null) {
				this.cancel(true);
				return;
			}
			if (after == null || "".equals(after)) {
				fragmentWF.get().mMessageModel.clear();
			}
			// clear load MessageItem
			fragmentWF.get().mMessageModel.mItemList.clear();
			fragmentWF.get().mAdapter.setIsLoading(true, true);
			dataJSON = new JSONObject();
		}

		@Override
		protected String doInBackground(Void... params) {
			if (fragmentWF == null || fragmentWF.get() == null
					|| fragmentWF.get().getActivity() == null) {
				this.cancel(true);
				return Common.RESULT_TASK_CANCLE;
			}

			String result = MessageManager.getMessage("messages/" + messageId,
					after, dataJSON, fragmentWF.get().getActivity());
			return result;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			if (fragmentWF == null || fragmentWF.get() == null
					|| fragmentWF.get().getActivity() == null
					|| Common.RESULT_TASK_CANCLE.equals(result)) {
				return;
			}
			if (Common.RESULT_SUCCESS.equals(result)) {
				MessageModel.convertToList(dataJSON,
						fragmentWF.get().mMessageModel);
				if (!fragmentWF.get().mMessageModel.mItemList.isEmpty()) {
					MessageItem fristItem = fragmentWF.get().mMessageModel.mItemList
							.get(0);
					fragmentWF.get().mMessageModel.mIndexList.mMessageList
							.removeAll(fragmentWF.get().mAddMessageItemList);
					fragmentWF.get().mMessageModel.convertIndexList(fristItem);
					fragmentWF.get().mMessageModel.mIndexList.mMessageList
							.addAll(fragmentWF.get().mAddMessageItemList);
				}
				String sender = fragmentWF.get().mMessageModel
						.getSender(RedditManager.getUserName(fragmentWF.get()
								.getActivity()));

				String title = ((SherlockFragmentActivity) fragmentWF.get()
						.getActivity()).getSupportActionBar().getTitle()
						.toString();

				if (!sender.equalsIgnoreCase(title)) {
					((SherlockFragmentActivity) fragmentWF.get().getActivity())
							.getSupportActionBar().setTitle(sender);
					fragmentWF.get().mSender = sender;
				} else {
					fragmentWF.get().mSender = sender;
				}
				// change the author
				fragmentWF.get().mAdapter.setIsLoading(false, true);
			}
		}
	}

	public static class PostMessageTask extends AsyncTask<Void, Void, String> {
		JSONObject infoJSON;
		int position;
		WeakReference<MessageItem> messageItemWF;
		WeakReference<ConversationListFragment> fragmentWF;
		WeakReference<Context> applicationContextWF;

		public PostMessageTask(ConversationListFragment fragment, MessageItem m) {
			fragmentWF = new WeakReference<ConversationListFragment>(fragment);
			applicationContextWF = new WeakReference<Context>(fragment
					.getActivity().getApplicationContext());
			messageItemWF = new WeakReference<MessageItem>(m);
			infoJSON = new JSONObject();
		}

		@Override
		protected String doInBackground(Void... params) {
			if (this.isCancelled() || messageItemWF.get() == null
					|| applicationContextWF == null
					|| applicationContextWF.get() == null) {
				return Common.RESULT_TASK_CANCLE;
			}
			return RedditCommentManager.postComment(applicationContextWF.get(),
					messageItemWF.get().body, messageItemWF.get().name,
					infoJSON);
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			if (result.equals(Common.RESULT_TASK_CANCLE) || this.isCancelled()
					|| applicationContextWF == null
					|| applicationContextWF.get() == null) {
				return;
			}
			if (Common.RESULT_TOO_LATE.equals(result)) {
				Toast.makeText(applicationContextWF.get(), "Too late to reply",
						Toast.LENGTH_LONG).show();
				rollBack();

			} else if (Common.RESULT_TRY_TOO_MUCH.equals(result)) {
				Toast.makeText(applicationContextWF.get(),
						"Try too much, try again latter!", Toast.LENGTH_LONG)
						.show();
				rollBack();

			} else if (Common.RESULT_SUCCESS.equals(result)) {
				Toast.makeText(applicationContextWF.get(),
						"Message post succeeded!", Toast.LENGTH_LONG).show();
				return;
			} else {
				Toast.makeText(applicationContextWF.get(),
						"Message post failed!", Toast.LENGTH_LONG).show();
				rollBack();
			}
		}

		private void rollBack() {
			// data and activity should not be null for roll back
			if (messageItemWF == null || messageItemWF.get() == null
					|| fragmentWF == null || fragmentWF.get() == null
					|| fragmentWF.get().getActivity() == null) {
				return;
			} else {
				fragmentWF.get().removeMessageItem(messageItemWF.get());
				((ConversationActivity) fragmentWF.get().getActivity()).mInputEdit
						.setText(messageItemWF.get().body);
			}

		}
	}

}
