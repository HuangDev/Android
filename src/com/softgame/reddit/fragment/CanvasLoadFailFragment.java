package com.softgame.reddit.fragment;

import com.softgame.reddit.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

public class CanvasLoadFailFragment extends CanvasFragment {

	Button mReloadButton;

	public CanvasLoadFailFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.item_canvas_load_fail, container,
				false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mReloadButton = (Button) this.getView().findViewById(R.id.reload);
		mReloadButton.setOnClickListener((OnClickListener) this.getActivity());
	}

}
