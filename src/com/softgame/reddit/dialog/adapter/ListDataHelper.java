package com.softgame.reddit.dialog.adapter;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;

import com.softgame.reddit.impl.OnDataSetChanged;
import com.softgame.reddit.model.SubRedditItem;
import com.softgame.reddit.model.SubRedditModel;

public abstract class ListDataHelper<T> {

	public static final int TYPE_LAODING = 0;
	public static final int TYPE_NO_ITEM = 1;
	public static final int TYPE_MORE = 2;
	public static final int TYPE_ITEM_LINK = 3;
	public static final int TYPE_ITEM_SELFPOST = 4;
	public static final int TYPE_NO_MORE = 5;

	private boolean isLoadingData;

	private ArrayList<T> data = new ArrayList<T>();
	public SubRedditModel subRedditModel;

	public OnDataSetChanged listener;
	private AbsListView listView;

	public ListDataHelper() {
		data.clear();
	}

	public void setSubRedditModel(SubRedditModel s) {
		subRedditModel = s;
	}

	public AbsListView getListView() {
		return listView;
	}

	public int getCount() {
		return data.size() + 1;
	}

	public int getItemViewType(int position) {
		if (position >= 0 && position < data.size()) {
			SubRedditItem item = (SubRedditItem) getItem(position);
			if (item.is_self) {
				return TYPE_ITEM_SELFPOST;
			} else {
				return TYPE_ITEM_LINK;
			}
		}
		if (isLoadingData) {
			return TYPE_LAODING;
		} else {
			if (data.size() == 0) {
				return TYPE_NO_ITEM;
			}
			if (subRedditModel.after == null || "".equals(subRedditModel.after)) {
				return TYPE_NO_MORE;
			} else {
				return TYPE_MORE;
			}

		}
	}

	/**
	 * set the data changed listener to update the view
	 * 
	 * @param l
	 */
	public void setDataSetChangeListener(OnDataSetChanged l) {
		listener = l;
	}

	public T getItem(int position) {
		if (position >= 0 && position < data.size()) {
			return data.get(position);
		} else {
			return null;
		}
	}

	public long getItemId(int position) {
		return position;
	}

	public boolean isEnabled(int position) {
		return true;
	}

	public boolean areAllItemsEnabled() {
		return true;
	}

	public void setIsLoadingData(boolean isLoadingData) {
		setIsLoadingData(isLoadingData, true);
	}

	public void setIsLoadingData(boolean isLoadingData, boolean redrawList) {
		this.isLoadingData = isLoadingData;
		if (redrawList) {
			notifyDataSetChanged();
		}
	}

	public boolean isLoadingData() {
		return isLoadingData;
	}

	public final View getView(int position, LayoutInflater inflater,
			ViewGroup container, Bundle savedInstanceState) {
		return doGetView(position, inflater, container, savedInstanceState);
	}

	protected abstract View doGetView(int position, LayoutInflater inflater,
			ViewGroup container, Bundle savedInstanceState);

	public int getViewTypeCount() {
		return 6;
	}

	public ArrayList<T> getData() {
		return data;
	}

	public void addAll(List<T> items) {
		data.addAll(items);
		notifyDataSetChanged();
	}

	private void notifyDataSetChanged() {
		if (listener != null) {
			listener.OnDataSetChangedListener();
		}
	}

	public void addAll(List<T> items, boolean redrawList) {
		data.addAll(items);
		if (redrawList) {
			notifyDataSetChanged();
		}
	}

	public void clear() {
		data.clear();
		notifyDataSetChanged();
	}

	public void remove(int position) {
		data.remove(position);
		notifyDataSetChanged();
	}
}
