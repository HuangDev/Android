package com.softgame.reddit.dialog;

import android.app.Dialog;
import android.view.View;

public interface CustomDialogHandler {
	
	// dialog.getWindow().setGravity(Gravity.BOTTOM);
	void onViewClick(Dialog customDialog, View v, int position);
}
