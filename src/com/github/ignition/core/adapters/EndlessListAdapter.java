/* Copyright (c) 2009-2011 Matthias Kaeppler
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.ignition.core.adapters;

import java.util.ArrayList;
import java.util.List;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.softgame.reddit.model.SubRedditItem;
import com.softgame.reddit.model.SubRedditModel;
import com.softgame.reddit.utils.Common;

public abstract class EndlessListAdapter<T> extends BaseAdapter {

	private boolean isLoadingData;

	ArrayList<T> data = new ArrayList<T>();
	// this model save newest load data (part of all data)
	public SubRedditModel subRedditModel;

	public EndlessListAdapter() {
		data.clear();
	}

	public void setSubRedditModel(SubRedditModel s) {
		subRedditModel = s;
	}

	public EndlessListAdapter(ArrayList d, SubRedditModel s) {
		data = d;
		subRedditModel = s;
	}

	@Override
	public int getItemViewType(int position) {
		// not include 0
		if (position > 0 && position <= data.size()) {
			SubRedditItem item = (SubRedditItem) getItem(position);
			if (item.is_self) {
				return Common.SUBREDDIT_TYPE_ITEM_SELFPOST;
			} else {
				if (item.thumbnail == null || "".equals(item.thumbnail)) {
					return Common.SUBREDDIT_TYPE_ITEM_LINK_NOPIC;
				}
				return Common.SUBREDDIT_TYPE_ITEM_LINK;
			}
		}
		if (isLoadingData) {
			return Common.SUBREDDIT_TYPE_LAODING;
		} else {
			if (data.size() == 0) {
				return Common.SUBREDDIT_TYPE_NO_ITEM;
			}
			if (subRedditModel.after == null || "".equals(subRedditModel.after)) {
				return Common.SUBREDDIT_TYPE_NO_MORE;
			} else {
				return Common.SUBREDDIT_TYPE_MORE;
			}

		}
	}

	@Override
	public T getItem(int position) {
		if (position > 0 && position <= data.size()) {
			return data.get(position - 1);
		} else {
			return null;
		}
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public boolean isEnabled(int position) {
		return true;
	}

	@Override
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

	@Override
	public final View getView(int position, View convertView, ViewGroup parent) {
		return doGetView(position, convertView, parent);
	}

	protected abstract View doGetView(int position, View convertView,
			ViewGroup parent);

	@Override
	public int getViewTypeCount() {
		return 10;
	}

	public ArrayList<T> getData() {
		return data;
	}

	public void addAll(List<T> items) {
		data.addAll(items);
		notifyDataSetChanged();
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
