package com.softgame.reddit.dialog;

import android.app.Dialog;
import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.softgame.reddit.R;
import com.softgame.reddit.fragment.CommentRetainFragment;
import com.softgame.reddit.fragment.FriendFragmentList;
import com.softgame.reddit.utils.RedditManager;

/**
 * login in
 * 
 * @author xinyunxixi
 * 
 */
public class FriendDialog extends Dialog{

	FriendFragmentList mFriendFragmentList;

	public FriendDialog(SherlockFragmentActivity context) {
		this(context, R.style.TransparentDialogTheme);
	}

	public FriendDialog(SherlockFragmentActivity context, int theme) {
		super(context, theme);
		this.setContentView(R.layout.dialog_friends);
		mFriendFragmentList = FriendFragmentList
				.findOrCreateFriendFragmentList(context
						.getSupportFragmentManager());
		final FragmentTransaction ft = context.getSupportFragmentManager()
				.beginTransaction();
		ft.add(R.id.friend_list_container, mFriendFragmentList,
				FriendFragmentList.TAG);
		ft.commit();
		this.setCanceledOnTouchOutside(true);
	}

}
