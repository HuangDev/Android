package com.softgame.reddit;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.softgame.reddit.SubredditWidget.SubRedditWidgetService;
import com.softgame.reddit.dialog.MineSubredditDialog;
import com.softgame.reddit.fragment.SubscribeFragmentList;
import com.softgame.reddit.impl.OnSubscribeItemClickListener;
import com.softgame.reddit.model.Subscribe;
import com.softgame.reddit.utils.Common;
import com.softgame.reddit.utils.CommonUtil;
import com.softgame.reddit.utils.RedditManager;

public class WidgetConfigureActivity extends CacheFragmentActivity implements
		OnSubscribeItemClickListener, OnClickListener,
		RadioGroup.OnCheckedChangeListener {

	public MineSubredditDialog mMineSubscribeDialog;
	public boolean mNeedToChange;

	LinearLayout mChooseSubReddit;
	RadioGroup mSubRedditTypeGroup;
	RadioGroup mControversalGroup;
	RadioGroup mNewGroup;
	RadioGroup mTopGroup;
	LinearLayout mGroupLinear;
	TextView mGroupTitle;
	TextView mSubRedditNameTextView;

	public String mCurrentSubReddit;
	public String mCurrentSubRedditName;
	public String mCurrentDefaultUser;
	// hot new ..
	public int mCurrentKind = 0;
	// day week all time...
	public int mCurrentControversalSort = 2;
	public int mCurrentTopSort = 2;
	public int mCurrentNewSort = 1;

	public static int[] KIND_IDS = new int[] { R.id.type_hot, R.id.type_new,
			R.id.type_controversial, R.id.type_top };
	public static int[] NEW_IDS = new int[] { R.id.new_sort_new,
			R.id.new_sort_rising };
	public static int[] CONTROVERSAL_IDS = new int[] {
			R.id.controversal_sort_hour, R.id.controversal_sort_today,
			R.id.controversal_sort_week, R.id.controversal_sort_month,
			R.id.controversal_sort_year, R.id.controversal_sort_all_time };

	public static int[] TOP_IDS = new int[] { R.id.top_sort_hour,
			R.id.top_sort_today, R.id.top_sort_week, R.id.top_sort_month,
			R.id.top_sort_year, R.id.top_sort_all_time };

	public boolean mIsRefresh = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_widget_setting);
		this.getSupportActionBar().setTitle("Widget Setting");
		this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		this.getSupportActionBar().setDisplayShowHomeEnabled(true);
		this.getSupportActionBar().setDisplayUseLogoEnabled(true);

		// get current subreddit
		mCurrentSubReddit = RedditManager
				.getWidgetSubReddit(getApplicationContext());
		mCurrentSubRedditName = RedditManager
				.getWidgetSubReddtiName(getApplicationContext());
		mCurrentKind = CommonUtil.getWidgetCurrentType(getApplicationContext(),
				R.string.key_widget_subreddit_type, 0, 3);
		mCurrentControversalSort = CommonUtil.getWidgetCurrentType(
				getApplicationContext(), R.string.key_widget_controversal_sort,
				2, 5);
		mCurrentTopSort = CommonUtil.getWidgetCurrentType(
				getApplicationContext(), R.string.key_widget_top_sort, 2, 5);
		mCurrentNewSort = CommonUtil.getWidgetCurrentType(
				getApplicationContext(), R.string.key_widget_new_sort, 1, 2);

		mChooseSubReddit = (LinearLayout) this
				.findViewById(R.id.choose_subreddit);

		mSubRedditNameTextView = (TextView) this
				.findViewById(R.id.subreddit_name);
		mSubRedditNameTextView.setText(mCurrentSubRedditName);

		mChooseSubReddit.setOnClickListener(this);
		mSubRedditTypeGroup = (RadioGroup) this
				.findViewById(R.id.group_subreddit_type);

		mGroupLinear = (LinearLayout) this.findViewById(R.id.group_linear);
		mControversalGroup = (RadioGroup) this
				.findViewById(R.id.group_controversal_sort_type);
		mNewGroup = (RadioGroup) this.findViewById(R.id.group_new_sort_type);
		mTopGroup = (RadioGroup) this.findViewById(R.id.group_top_sort_type);

		mGroupTitle = (TextView) this.findViewById(R.id.group_title);

		mSubRedditTypeGroup.setOnCheckedChangeListener(this);
		mTopGroup.setOnCheckedChangeListener(this);
		mControversalGroup.setOnCheckedChangeListener(this);
		mNewGroup.setOnCheckedChangeListener(this);
		updateViews();
		mMineSubscribeDialog = new MineSubredditDialog(
				WidgetConfigureActivity.this);
	}

	/**
	 * SubscribeDialog need to use Activity to find the framentId
	 * 
	 * @param id
	 * @return
	 */
	@Override
	public View findViewById(int id) {
		View v = super.findViewById(id);
		if (v == null && mMineSubscribeDialog != null) {
			return mMineSubscribeDialog.findViewById(id);
		} else {
			return v;
		}
	}

	public void updateViews() {
		mSubRedditTypeGroup.check(KIND_IDS[mCurrentKind]);
		mControversalGroup.check(CONTROVERSAL_IDS[mCurrentControversalSort]);
		mNewGroup.check(NEW_IDS[mCurrentNewSort]);
		mTopGroup.check(TOP_IDS[mCurrentTopSort]);

		mGroupLinear.setVisibility(View.VISIBLE);
		mControversalGroup.setVisibility(View.GONE);
		mTopGroup.setVisibility(View.GONE);
		mNewGroup.setVisibility(View.GONE);
		switch (mCurrentKind) {
		case Common.KIND_HOT:
			mGroupLinear.setVisibility(View.GONE);
			mGroupTitle.setText("Hot link from");
			break;
		case Common.KIND_NEW:
			mNewGroup.setVisibility(View.VISIBLE);
			mGroupTitle.setText("New link from");
			break;
		case Common.KIND_CONTROVERSIAL:
			mGroupTitle.setText("Controversal link from");
			mControversalGroup.setVisibility(View.VISIBLE);
			break;
		case Common.KIND_TOP:
			mGroupTitle.setText("Top link from");
			mTopGroup.setVisibility(View.VISIBLE);
			break;
		}

		mSubRedditNameTextView.setText(mCurrentSubRedditName);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			if (mNeedToChange) {
				updateWidget();
			}
			this.finish();
			break;
		}
		return true;
	}

	@Override
	public void onBackPressed() {
		if (mNeedToChange) {
			updateWidget();
		}
		super.onBackPressed();
	}

	// update
	public void updateWidget() {
		Intent t = new Intent(this, SubRedditWidgetService.class);
		t.putExtra(Common.KEY_WIDGET_REFRESH, true);
		this.startService(t);
	}

	// deal with Subscribe Pop Dialog Call back
	@Override
	public void onSubscribeItemClick(Subscribe s, int action) {
		switch (action) {
		case MineSubredditDialog.ACTION_SUBREDDIT_DONE:
			// do nothing
			break;
		case MineSubredditDialog.ACTION_SUBREDDIT_FRONT:
			mCurrentSubReddit = Common.DEFAULT_SUBREDDIT;
			mCurrentSubRedditName = Common.DEFAULT_SUBREDDIT_NAME;
			break;
		case MineSubredditDialog.ACTION_SUBREDDIT_ALL:
			mCurrentSubReddit = Common.SUBREDDIT_ALL;
			mCurrentSubRedditName = Common.SUBREDDIT_ALL_NAME;
			break;
		case MineSubredditDialog.ACTION_SUBREDDIT_FRIENDS:
			mCurrentSubReddit = Common.SUBREDDIT_FRIEND;
			mCurrentSubRedditName = Common.SUBREDDIT_FRIEND_NAME;
			break;
		case MineSubredditDialog.ACTION_SUBREDDIT_MOD:
			mCurrentSubReddit = Common.SUBREDDIT_MOD;
			mCurrentSubRedditName = Common.SUBREDDIT_MOD_NAME;
			break;
		case MineSubredditDialog.ACTION_SUBREDDIT_SEARCH:
			// do the search
			Intent search = new Intent(this, SearchSubscribeActivity.class);
			this.startActivityForResult(search, Common.REQUEST_SEARCH_SUBSCRIBE);
			mMineSubscribeDialog.dismiss();
			break;
		case SubscribeFragmentList.ACTION_SUBREDDIT_ITEM:
			mCurrentSubReddit = s.url;
			mCurrentSubRedditName = s.display_name;
			break;
		}
		mNeedToChange = true;
		saveSubRedditToPreference();
		updateViews();
		mMineSubscribeDialog.dismiss();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case Common.REQUEST_SEARCH_SUBSCRIBE:
			if (resultCode == Activity.RESULT_OK && data != null) {
				String s = data.getStringExtra(Common.EXTRA_SUBREDDIT);
				String sn = data.getStringExtra(Common.EXTRA_SUBREDDIT_NAME);
				if (s != null && !"".equals(s) && sn != null && !"".equals(sn)) {
					mCurrentSubReddit = s;
					mCurrentSubRedditName = sn;
					mNeedToChange = true;
					saveSubRedditToPreference();
					updateViews();
				}
			}
			break;
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.choose_subreddit:
			mMineSubscribeDialog.show();
			break;
		}

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mMineSubscribeDialog != null) {
			mMineSubscribeDialog.dismiss();
		}

	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		switch (group.getId()) {
		case R.id.group_subreddit_type:
			for (int i = 0; i < KIND_IDS.length; i++) {
				if (KIND_IDS[i] == checkedId) {
					mCurrentKind = i;
					break;
				}
			}
			break;
		case R.id.group_controversal_sort_type:
			for (int i = 0; i < CONTROVERSAL_IDS.length; i++) {
				if (CONTROVERSAL_IDS[i] == checkedId) {
					mCurrentControversalSort = i;
					break;
				}
			}
			break;
		case R.id.group_new_sort_type:
			for (int i = 0; i < NEW_IDS.length; i++) {
				if (NEW_IDS[i] == checkedId) {
					mCurrentNewSort = i;
					break;
				}
			}
			break;
		case R.id.group_top_sort_type:
			for (int i = 0; i < TOP_IDS.length; i++) {
				if (TOP_IDS[i] == checkedId) {
					mCurrentTopSort = i;
					break;
				}
			}

			break;
		}

		mNeedToChange = true;
		updateViews();
		saveSortToPreference();
	}

	public void saveSortToPreference() {
		SharedPreferences pf = PreferenceManager
				.getDefaultSharedPreferences(this);
		SharedPreferences.Editor editor = pf.edit();

		editor.putInt(this.getString(R.string.key_widget_top_sort),
				mCurrentTopSort);
		editor.putInt(this.getString(R.string.key_widget_new_sort),
				mCurrentNewSort);
		editor.putInt(this.getString(R.string.key_widget_controversal_sort),
				mCurrentControversalSort);
		editor.putInt(this.getString(R.string.key_widget_subreddit_type),
				mCurrentKind);
		editor.commit();
	}

	public void saveSubRedditToPreference() {
		SharedPreferences pf = PreferenceManager
				.getDefaultSharedPreferences(this);
		SharedPreferences.Editor editor = pf.edit();
		editor.putString(Common.PREF_KEY_WIDGET_SUBREDDIT, mCurrentSubReddit);
		editor.putString(Common.PREF_KEY_WIDGET_SUBREDDIT_NAME,
				mCurrentSubRedditName);
		editor.commit();
	}

}
