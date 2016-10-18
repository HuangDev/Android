package com.softgame.reddit;

import android.os.Bundle;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.softgame.reddit.utils.CommonUtil;

public class NoCacheFragmentActivity extends SherlockFragmentActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		this.setTheme(CommonUtil.getCurrentTheme(this, R.string.pref_theme_key));
		super.onCreate(savedInstanceState);
	}
}
