package com.softgame.reddit;

import android.os.Bundle;

public class SettingActivity extends RedditPreferenceActivity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		this.setTheme(com.actionbarsherlock.R.style.Theme_Sherlock);
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preference);
		this.findPreference(this.getString(R.string.pref_theme_key))
				.setOnPreferenceChangeListener(this);
		this.findPreference(this.getString(R.string.pref_contact_developer_key))
				.setOnPreferenceClickListener(this);

		this.findPreference(this.getString(R.string.pref_share_app_key))
				.setOnPreferenceClickListener(this);

		this.findPreference(this.getString(R.string.pref_report_bug_key))
				.setOnPreferenceClickListener(this);

		this.findPreference(this.getString(R.string.pref_check_message_key))
				.setOnPreferenceChangeListener(this);

		this.findPreference(this.getString(R.string.pref_check_rate_key))
				.setOnPreferenceChangeListener(this);
		this.findPreference(this.getString(R.string.pref_key_clear_cache))
				.setOnPreferenceClickListener(this);

		this.findPreference(this.getString(R.string.pref_key_clear_cache_rate))
				.setOnPreferenceChangeListener(this);

		this.findPreference(this.getString(R.string.pref_key_rate_redditet))
				.setOnPreferenceClickListener(this);
		this.findPreference(this.getString(R.string.pref_key_go_to_redditet))
				.setOnPreferenceClickListener(this);

		this.findPreference(
				this.getString(R.string.pref_key_subscribe_redditet))
				.setOnPreferenceChangeListener(this);
		

	}
}
