package com.softgame.reddit;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.softgame.reddit.fragment.SearchSubscribeFragmentList;

public class SearchSubscribeActivity extends CacheFragmentActivity implements
		OnClickListener {

	private static final String TAG = "SubRedditFragmentActivity";

	SearchSubscribeFragmentList mSeachSubscribeFrament;
	EditText mEditText;
	long mFragmentId = 0L;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.activity_search_subscribe);

		if (savedInstanceState != null)
			mFragmentId = savedInstanceState.getLong("fragment_id");
		if (mFragmentId == 0L) {
			mFragmentId = System.currentTimeMillis();
		}

		mSeachSubscribeFrament = SearchSubscribeFragmentList
				.findOrCreateFragment(R.id.fragment_container,
						getSupportFragmentManager(), mFragmentId);

		this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		this.getSupportActionBar().setTitle("Search Reddits");
		this.getSupportActionBar().setDisplayShowHomeEnabled(false);
		mEditText = (EditText) this.findViewById(R.id.search_input);
		mEditText.setHint("search reddit");
		if (mSeachSubscribeFrament.mSearchItem != null
				& !"".equals(mSeachSubscribeFrament)) {
			mEditText.setText(mSeachSubscribeFrament.mSearchItem);
		}
		this.findViewById(R.id.search).setOnClickListener(this);

	};

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			this.finish();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putLong("fragment_id", mFragmentId);
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.search:
			if (mEditText.getEditableText() == null
					|| "".equals(mEditText.getEditableText().toString().trim())) {
				Toast.makeText(this, "Input request!", Toast.LENGTH_SHORT)
						.show();
			} else {
				mSeachSubscribeFrament.setSearchItem(mEditText
						.getEditableText().toString());
			}
			break;
		}

	}

}
