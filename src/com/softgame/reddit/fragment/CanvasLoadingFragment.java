package com.softgame.reddit.fragment;

import com.softgame.reddit.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class CanvasLoadingFragment extends CanvasFragment {
   
	public CanvasLoadingFragment(){}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.item_canvas_loading, container, false);
	}

}
