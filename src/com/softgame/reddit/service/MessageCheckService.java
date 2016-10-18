package com.softgame.reddit.service;

import org.json.custom.JSONObject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.softgame.reddit.ConversationActivity;
import com.softgame.reddit.R;
import com.softgame.reddit.UnreadMessageActivity;
import com.softgame.reddit.fragment.NewMessageFragmentList;
import com.softgame.reddit.model.MessageItem;
import com.softgame.reddit.model.MessageModel;
import com.softgame.reddit.utils.Common;
import com.softgame.reddit.utils.CommonUtil;
import com.softgame.reddit.utils.MessageManager;
import com.softgame.reddit.utils.RedditManager;

public class MessageCheckService extends CustomService {

	public static final String TAG = "MessageCheckService";

	@Override
	protected void onHandleIntent(Intent intent) {
		Log.d(TAG, "start to check the message");

		// get the user information
		if (RedditManager.isUserAuth(this)) {
			MessageModel mMessageModel = new MessageModel();
			JSONObject dataJSON = new JSONObject();
			// get the unread message
			String result = MessageManager.getMessage(
					Common.TYPE_MESSAGE_UNREAD, null, dataJSON, this);

			if (Common.RESULT_SUCCESS.equals(result)) {
				MessageModel.convertToList(dataJSON, mMessageModel);
				if (mMessageModel.mItemList.isEmpty()) {
					MessageCheckService.this.stopSelf();
					return;
				}
				NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
				mNotificationManager.notify(R.drawable.icon,
						makeNotification(mMessageModel));
			}
			// clear pendingIntent and Stop Service Log.d(TAG,
			// "download data error with result:" + result);
			super.clearPendingIntent();
			// MessageCheckService.this.stopSelf();
		} else {
			Log.d(TAG, "no login user");
			MessageCheckService.this.stopSelf();
		}
	}

	

	private Notification makeNotification(MessageModel mm) {
		int icon = R.drawable.icon_logo_email;
		CharSequence contentTitle;
		CharSequence contentText;
		long when = System.currentTimeMillis();
		PendingIntent pIntent;
		contentTitle = "You have " + mm.getItemsSize() + " new messages!";
		contentText = "from: ";
		for (MessageItem m : mm.mItemList) {
			contentText = contentText + m.author;
		}
		Intent tt = new Intent(this, UnreadMessageActivity.class);
		tt.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		
		pIntent = PendingIntent.getActivity(this.getApplicationContext(), 0,
				tt, PendingIntent.FLAG_UPDATE_CURRENT);
		NotificationCompat.Builder builder = new NotificationCompat.Builder(
				MessageCheckService.this);

		if (CommonUtil.isNeedToVibrate(MessageCheckService.this)) {
			builder.setVibrate(Common.VIBRATE_PATTENT);
		}

		Uri sound = CommonUtil.getRingStone(MessageCheckService.this);
		if (sound != null) {
			builder.setSound(sound, RingtoneManager.TYPE_NOTIFICATION);
		}
		builder.setAutoCancel(true);
		builder.setSmallIcon(icon);
		builder.setContentTitle(contentTitle);
		builder.setContentText(contentText);
		builder.setContentIntent(pIntent);
		builder.setWhen(when);

		return builder.getNotification();
	}
}
