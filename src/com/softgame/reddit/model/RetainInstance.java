package com.softgame.reddit.model;

import java.util.ArrayList;

import android.os.AsyncTask;

public class RetainInstance {

	// DataTask to load the data;
	public AsyncTask dataTask;
	public AsyncTask saveTask;
	public AsyncTask voteTask;
	public AsyncTask hideTask;
	// EndlessListAdapter customAdapter;

	public int currentSort;
	public int currentType;
	public int currentKind;

	// for adapter
	public ArrayList data;
	public SubRedditModel subRedditModel;
    boolean isLoading;

	public boolean isLoading() {
		return isLoading;
	}

	public void setLoading(boolean isLoading) {
		this.isLoading = isLoading;
	}

	public ArrayList getData() {
		return data;
	}

	public void setData(ArrayList data) {
		this.data = data;
	}

	public SubRedditModel getSubRedditModel() {
		return subRedditModel;
	}

	public void setSubRedditModel(SubRedditModel subRedditModel) {
		this.subRedditModel = subRedditModel;
	}

	// r/pics
	String currentSubReddit;
	String currentSubRedditName;

	public AsyncTask getDataTask() {
		return dataTask;
	}

	public void setDataTask(AsyncTask dataTask) {
		this.dataTask = dataTask;
	}

	public AsyncTask getSaveTask() {
		return saveTask;
	}

	public void setSaveTask(AsyncTask saveTask) {
		this.saveTask = saveTask;
	}

	public AsyncTask getVoteTask() {
		return voteTask;
	}

	public void setVoteTask(AsyncTask voteTask) {
		this.voteTask = voteTask;
	}

	public AsyncTask getHideTask() {
		return hideTask;
	}

	public void setHideTask(AsyncTask hideTask) {
		this.hideTask = hideTask;
	}

	// public EndlessListAdapter getCustomAdapter() {
	// return customAdapter;
	// }
	//
	// public void setCustomAdapter(EndlessListAdapter customAdapter) {
	// this.customAdapter = customAdapter;
	// }

	public String getCurrentSubReddit() {
		return currentSubReddit;
	}

	public void setCurrentSubReddit(String currentSubReddit) {
		this.currentSubReddit = currentSubReddit;
	}

	public String getCurrentSubRedditName() {
		return currentSubRedditName;
	}

	public void setCurrentSubRedditName(String currentSubRedditName) {
		this.currentSubRedditName = currentSubRedditName;
	}

	public int getCurrentKind() {
		return currentKind;
	}

	public void setCurrentKind(int currentKind) {
		this.currentKind = currentKind;
	}

	public int getCurrentSort() {
		return currentSort;
	}

	public void setCurrentSort(int currentSort) {
		this.currentSort = currentSort;
	}

	public int getCurrentType() {
		return currentType;
	}

	public void setCurrentType(int currentType) {
		this.currentType = currentType;
	}

}
