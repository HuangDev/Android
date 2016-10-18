package com.softgame.reddit;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.softgame.reddit.fragment.ConversationListFragment;
import com.softgame.reddit.fragment.SubRedditListFragment;
import com.softgame.reddit.model.MessageItem;
import com.softgame.reddit.utils.Common;
import com.softgame.reddit.utils.RedditManager;

public class ConversationActivity extends NoCacheFragmentActivity implements
		OnClickListener {

	public EditText mInputEdit;
	ImageButton mSendButton;
	String mMessageId;

	ConversationListFragment mConversationListFragment;

	String mMessageSender;
	String mMessageName;
	boolean mFromNotification;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (this.getIntent() != null) {
			mMessageId = this.getIntent().getStringExtra(
					Common.EXTRA_MESSAGE_ID);
			mMessageSender = this.getIntent().getStringExtra(
					Common.EXTRA_MESSAGE_SENDER);
			mMessageName = this.getIntent().getStringExtra(
					Common.EXTRA_MESSAGE_NAME);

			mFromNotification = this.getIntent().getBooleanExtra(
					Common.KEY_FROM_NOTIFICATION, false);
		}
		if (mMessageId == null || "".equals(mMessageId)
				|| mMessageSender == null || "".equals(mMessageSender)
				|| mMessageName == null || "".equals(mMessageName)) {
			Toast.makeText(this, "Messages missing!", Toast.LENGTH_SHORT)
					.show();
			this.finish();
		}

		this.setContentView(R.layout.activity_conversation);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowHomeEnabled(false);
		getSupportActionBar().setDisplayUseLogoEnabled(false);
		getSupportActionBar().setTitle(mMessageSender);
		String inputText = null;
		if (savedInstanceState != null) {
			inputText = savedInstanceState.getString("edit_text");
		}
		mConversationListFragment = ConversationListFragment
				.findOrCreateConversationListFragment(
						this.getSupportFragmentManager(), mMessageId);

		mInputEdit = (EditText) this.findViewById(R.id.conversation_input);

		if (inputText != null && !"".equals(inputText)) {
			mInputEdit.setText(inputText);
		}
		mSendButton = (ImageButton) this.findViewById(R.id.conversation_send);
		mSendButton.setOnClickListener(this);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		if (mInputEdit.getEditableText() != null)
			outState.putString("edit_text", mInputEdit.getEditableText()
					.toString());
		super.onSaveInstanceState(outState);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add("Profile").setIcon(R.drawable.icon_actionbar_profile)
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

		menu.add("Compose").setIcon(R.drawable.icon_actionbar_email_add)
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		switch (id) {
		case android.R.id.home:
			if (mFromNotification) {
				Intent t = new Intent(this, SubRedditFragmentActivity.class);
				this.startActivity(t);
				this.finish();
			} else {
				this.finish();
			}
			break;
		}

		if (mConversationListFragment.getSender() == null
				|| "".equals(mConversationListFragment.getSender())) {
		} else {
			if (item.getTitle().equals("Profile")) {
				Intent authorIntent = new Intent(this,
						OverviewFragmentActivity.class);
				authorIntent.putExtra(Common.EXTRA_USERNAME,
						mConversationListFragment.getSender());
				this.startActivity(authorIntent);
			}
			if (item.getTitle().equals("Compose")) {
				Intent t = new Intent(this, ComposeMessageActivity.class);
				t.putExtra(Common.KEY_REDDITOR_NAME,
						mConversationListFragment.getSender());
				this.startActivity(t);
			}
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.conversation_send:
			if (mInputEdit.getText() == null
					|| "".equals(mInputEdit.getText().toString())) {
				Toast.makeText(ConversationActivity.this,
						"Please input message!", Toast.LENGTH_SHORT).show();
				return;
			}
			MessageItem t = new MessageItem();
			t.body = mInputEdit.getText().toString();
			t.created_utc = System.currentTimeMillis() / 1000;
			t.author = RedditManager.getUserName(this);
			t.dest = mMessageSender;
			t.kind = "t4";
			t.name = mMessageName;
			InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			mgr.hideSoftInputFromWindow(mInputEdit.getWindowToken(), 0);
			mInputEdit.setText("");
			mConversationListFragment.postReply(t);
			// new PostMessageTask(mConversationListFragment, t).execute();
			break;
		}
	}

}
