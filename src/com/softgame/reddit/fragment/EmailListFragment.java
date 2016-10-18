package com.softgame.reddit.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.softgame.reddit.R;

public class EmailListFragment extends ListFragment {

	public static final String TAG = "EmailListFragment";
	String[] mEmailTextArray;
	LayoutInflater mLayoutInflater;

	EmailAdapter mEmailAdapter;
	
	public EmailListFragment(){}
	public static EmailListFragment findOrCreateEmailListFragment(
			FragmentManager manager, long id) {
		EmailListFragment fragment = (EmailListFragment) manager
				.findFragmentByTag(EmailListFragment.TAG + id);
		if (fragment == null) {
			fragment = new EmailListFragment();
			final FragmentTransaction ft = manager.beginTransaction();
			ft.add(android.R.id.content, fragment, EmailListFragment.TAG + id);
			ft.commit();
		}
		return fragment;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mEmailTextArray = activity.getResources().getStringArray(
				R.array.label_email_text_array);
		mLayoutInflater = activity.getLayoutInflater();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setRetainInstance(true);
		mEmailAdapter = new EmailAdapter();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.item_fragment_email_listview,
				container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		this.getListView().setAdapter(mEmailAdapter);
	}


	public class EmailAdapter extends BaseAdapter {

		public static final int TYPE_COUNTS = 3;
		public static final int TYPE_TITLE = 0x0;
		public static final int TYPE_ITEM = 0x1;
		public static final int TYPE_ITEM_WITH_COUNT = 0x2;

		@Override
		public int getCount() {
			return 8;
		}

		@Override
		public String getItem(int position) {
			return mEmailTextArray[position];
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public boolean areAllItemsEnabled() {
			return false;
		}

		@Override
		public int getItemViewType(int position) {
			switch (position) {
			case 0:
			case 4:
			case 6:
				return TYPE_TITLE;
			case 1:
				return TYPE_ITEM_WITH_COUNT;
			default:
				return TYPE_ITEM;
			}
		}

		@Override
		public int getViewTypeCount() {
			return TYPE_COUNTS;
		}

		@Override
		public boolean isEnabled(int position) {
			switch (getItemViewType(position)) {
			case TYPE_TITLE:
				return false;
			}
			return true;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			switch (getItemViewType(position)) {
			case TYPE_TITLE:
				if (convertView == null) {
					convertView = mLayoutInflater.inflate(
							R.layout.item_email_title, null);
				}
				TextView t = (TextView) convertView
						.findViewById(R.id.email_title);
				t.setText(this.getItem(position));
				break;
			case TYPE_ITEM:
				if (convertView == null) {
					convertView = mLayoutInflater.inflate(
							R.layout.item_email_item, null);
				}
				TextView tt = (TextView) convertView
						.findViewById(R.id.email_item_text);
				tt.setText(this.getItem(position));
				break;
			case TYPE_ITEM_WITH_COUNT:
				if (convertView == null) {
					convertView = mLayoutInflater.inflate(
							R.layout.item_email_item_count, null);
				}
				TextView ttt = (TextView) convertView
						.findViewById(R.id.email_item_text);
				ttt.setText(this.getItem(position));

				TextView count = (TextView) convertView
						.findViewById(R.id.email_item_count);
				count.setText("3");
				break;
			}
			return convertView;
		}
	}

}
