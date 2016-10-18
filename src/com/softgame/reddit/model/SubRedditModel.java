package com.softgame.reddit.model;

import java.util.ArrayList;

import org.json.custom.JSONArray;
import org.json.custom.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * A List Wraper for SubRedditItem and infomation such as kind after,note:
 * remove useless item such as modhash, before
 * 
 * @author xilong
 * 
 */
public class SubRedditModel implements Parcelable {
	public ArrayList<SubRedditItem> mItemList;
	// String modhash;
	// String kind;
	public String after;

	// String before;

	public SubRedditModel() {
		mItemList = new ArrayList<SubRedditItem>();
	}

	public void addData(SubRedditModel model) {
		this.after = model.after;

		for (SubRedditItem s : model.mItemList) {
			s.position = this.mItemList.size();
			this.mItemList.add(s);
		}
	}

	/**
	 * decode the JSONObject and return a new SubRedditModel
	 * 
	 * @param srJSObject
	 * @return
	 */
	public static SubRedditModel convertToModel(JSONObject srJSObject) {
		SubRedditModel model = new SubRedditModel();
		JSONObject data = srJSObject.optJSONObject("data");
		if (data != null) {
			model.after = data.optString("after", "");
			JSONArray children = data.optJSONArray("children");
			if (children == null) {
				return model;
			}
			for (int i = 0; i < children.length(); i++) {
				JSONObject childData = children.optJSONObject(i);
				if (childData != null) {
					SubRedditItem s = new SubRedditItem(childData);
					model.mItemList.add(s);
				}
			}
		}

		return model;
	}

	public int getSize() {
		return mItemList.size();
	}

	public SubRedditItem getItemByIndex(int index) {
		return mItemList.get(index);
	}

	public void clear() {
		mItemList.clear();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeTypedList(mItemList);
		// dest.writeList(mItemList);
		// dest.writeString(modhash);
		// dest.writeString(kind);
		dest.writeString(after);
		// dest.writeString(before);
	}

	public static final Parcelable.Creator<SubRedditModel> CREATOR = new Parcelable.Creator<SubRedditModel>() {
		public SubRedditModel createFromParcel(Parcel in) {
			return new SubRedditModel(in);
		}

		public SubRedditModel[] newArray(int size) {
			return new SubRedditModel[size];
		}
	};

	private SubRedditModel(Parcel in) {
		mItemList = new ArrayList<SubRedditItem>();
		in.readTypedList(mItemList, SubRedditItem.CREATOR);
		// in.readList(mItemList, SubRedditItem.class.getClassLoader());
		// modhash = in.readString();
		// kind = in.readString();
		after = in.readString();
		// before = in.readString();
	}

}
