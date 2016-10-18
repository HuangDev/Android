package com.softgame.reddit.fragment;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import com.softgame.reddit.R;

public class SettingFragment extends PreferenceFragment {

	public SettingFragment(){}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Load the preferences from an XML resource
		PreferenceManager.setDefaultValues(getActivity(), R.xml.preference,
				false);
		addPreferencesFromResource(R.xml.preference);
		
	}
}
