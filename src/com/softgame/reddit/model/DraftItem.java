package com.softgame.reddit.model;

import android.os.Parcel;
import android.os.Parcelable;

public class DraftItem implements Parcelable {
	public boolean isLink = true;
	public String title = "";
	public String optionText = "";
	public String linkUrl = "";
	public String subreddit = "";

	public DraftItem(){}
	
	@Override
	public int describeContents() {
		return 0;
	}

	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(isLink ? 1 : -1);
		dest.writeString(title);
		dest.writeString(optionText);
		dest.writeString(linkUrl);
		dest.writeString(subreddit);
	}

	public static final Parcelable.Creator<DraftItem> CREATOR = new Parcelable.Creator<DraftItem>() {
		public DraftItem createFromParcel(Parcel in) {
			return new DraftItem(in);
		}

		public DraftItem[] newArray(int size) {
			return new DraftItem[size];
		}
	};

	public DraftItem(Parcel in) {
		isLink = in.readInt() == 1 ? true : false;
		title = in.readString();
		optionText = in.readString();
		linkUrl = in.readString();
		subreddit = in.readString();
	}

}
