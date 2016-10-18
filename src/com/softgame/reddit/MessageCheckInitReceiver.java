package com.softgame.reddit;

import com.softgame.reddit.utils.CommonUtil;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class MessageCheckInitReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		// check need to start the message after reboot
		String action = intent.getAction();
		if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
			Log.d("RedditET", "reboot  check if need to check message");
			if (CommonUtil.needToMessageCheck(context)) {
				CommonUtil.turnOnMessageCheck(context);
			}
		}
	}

}
