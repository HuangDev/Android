package com.softgame.reddit.utils;

import android.content.Context;

import com.github.ignition.support.http.IgnitedHttpClient;

public class RedditHttpClientFactory {

	private static RedditHttpClientFactory mFactory;

	// single get factory
	private static RedditHttpClientFactory getFactory() {
		if (mFactory == null) {
			mFactory = new RedditHttpClientFactory();
		}
		return mFactory;
	}

}
