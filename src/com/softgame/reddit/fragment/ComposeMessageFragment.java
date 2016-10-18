package com.softgame.reddit.fragment;

import java.lang.ref.WeakReference;

import org.json.custom.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.MenuItem;
import com.softgame.reddit.CacheFragmentActivity;
import com.softgame.reddit.ComposeMessageActivity;
import com.softgame.reddit.R;
import com.softgame.reddit.impl.OnFriendItemClick;
import com.softgame.reddit.utils.Common;
import com.softgame.reddit.utils.SubmitManager;

public class ComposeMessageFragment extends SherlockFragment implements
		OnClickListener, OnFriendItemClick {

	public static final String TAG = "ComposeMessageFragment";

	EditText mSubject;
	EditText mMessage;
	EditText mTo;
	EditText mCaptche;
	Button mFriendsButton;

	String mName;

	LinearLayout mCaptchaLinear;
	ImageView mCaptchaImage;
	String mCurrentIden;
	ProgressDialog mProgressDialog;
	WeakReference<ComposeMessageTask> mComposeMessageTaskWF;

	public ComposeMessageFragment() {
	};

	public static ComposeMessageFragment findOrCreateComposeMessageFragment(
			FragmentManager manager) {
		ComposeMessageFragment fragment = (ComposeMessageFragment) manager
				.findFragmentByTag(ComposeMessageFragment.TAG);
		if (fragment == null) {
			fragment = new ComposeMessageFragment();
			// create a new Fragment
			Log.d(CommentRetainFragment.TAG, "creat a new Fragment:"
					+ CommentRetainFragment.TAG);
			final FragmentTransaction ft = manager.beginTransaction();
			ft.add(android.R.id.content, fragment, ComposeMessageFragment.TAG);
			ft.commit();
		}
		return fragment;
	}

	public static ComposeMessageFragment findOrCreateComposeMessageFragment(
			FragmentManager manager, String name) {
		ComposeMessageFragment fragment = (ComposeMessageFragment) manager
				.findFragmentByTag(ComposeMessageFragment.TAG);
		if (fragment == null) {
			fragment = new ComposeMessageFragment();
			Bundle argument = new Bundle();
			argument.putString(Common.KEY_REDDITOR_NAME, name);
			fragment.setArguments(argument);
			final FragmentTransaction ft = manager.beginTransaction();
			ft.add(android.R.id.content, fragment, ComposeMessageFragment.TAG);
			ft.commit();
		}
		return fragment;
	}

	@Override
	public void onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu,
			com.actionbarsherlock.view.MenuInflater inflater) {
		menu.add("Send").setIcon(R.drawable.icon_actionbar_send)
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getTitle().equals("Send")) {
			if (check()) {
				// submit
				ComposeMessageTask ct = new ComposeMessageTask(this,
						mProgressDialog, mTo.getText().toString().trim(),
						mSubject.getText().toString().trim(), mMessage
								.getText().toString().trim(), mCurrentIden,
						mCaptche.getText().toString().trim());
				mComposeMessageTaskWF = new WeakReference<ComposeMessageFragment.ComposeMessageTask>(
						ct);
				ct.execute();
			}

		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setRetainInstance(true);
		this.setHasOptionsMenu(true);
		if (this.getArguments() != null)
			mName = this.getArguments().getString(Common.KEY_REDDITOR_NAME);
		mProgressDialog = new ProgressDialog(this.getActivity());
		mProgressDialog.setMessage("Submitting message, please wait!");
		mProgressDialog.setCancelable(true);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (mProgressDialog != null
				&& mComposeMessageTaskWF != null
				&& mComposeMessageTaskWF.get() != null
				&& !mComposeMessageTaskWF.get().getStatus()
						.equals(AsyncTask.Status.FINISHED)) {
			mProgressDialog.show();
		}

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.activity_compose_message, container,
				false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mSubject = (EditText) this.getView().findViewById(R.id.compose_subject);
		mMessage = (EditText) this.getView().findViewById(R.id.compose_message);
		mTo = (EditText) this.getView().findViewById(R.id.compose_to);
		if (mName != null && !"".equals(mName)) {
			mTo.setText(mName);
		}
		mCaptchaLinear = (LinearLayout) this.getView().findViewById(
				R.id.compose_captcha_linear);
		mCaptchaImage = (ImageView) this.getView().findViewById(
				R.id.compose_captcha_pic);
		mCaptche = (EditText) this.getView().findViewById(R.id.compose_captcha);
		mFriendsButton = (Button) this.getView().findViewById(
				R.id.button_friends);
		mFriendsButton.setOnClickListener(this);
	}

	@Override
	public void onDetach() {
		super.onDetach();
		if (mProgressDialog != null) {
			mProgressDialog.dismiss();
		}

	}

	private static class ComposeMessageTask extends
			AsyncTask<Void, Void, String> {
		String to;
		String subject;
		String message;
		WeakReference<ComposeMessageFragment> fragmentWF;
		WeakReference<ProgressDialog> progressDialogWF;
		String iden;
		String captcha;
		JSONObject infoJSON;

		public ComposeMessageTask(ComposeMessageFragment fragment,
				ProgressDialog pd, String t, String s, String m, String i,
				String c) {
			fragmentWF = new WeakReference<ComposeMessageFragment>(fragment);
			progressDialogWF = new WeakReference<ProgressDialog>(pd);
			to = t;
			subject = s;
			message = m;
			iden = i;
			captcha = c;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (fragmentWF != null && fragmentWF.get() != null
					&& fragmentWF.get().getActivity() != null) {
				// show sumbmit dialog
				if (progressDialogWF.get() != null) {
					progressDialogWF.get().show();
				}

			}
		}

		@Override
		protected String doInBackground(Void... params) {
			if (!this.isCancelled() && fragmentWF != null
					&& fragmentWF.get() != null
					&& fragmentWF.get().getActivity() != null) {

				infoJSON = new JSONObject();
				return SubmitManager.composeMessage(fragmentWF.get()
						.getActivity(), subject, message, to, iden, captcha,
						infoJSON);

			} else {
				this.cancel(true);
				return Common.RESULT_TASK_CANCLE;
			}
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);

			Log.d(TAG, infoJSON.toString());
			if (Common.RESULT_TASK_CANCLE.equals(result)) {
				return;
			}

			if (!this.isCancelled() && fragmentWF != null
					&& fragmentWF.get() != null
					&& fragmentWF.get().getActivity() != null) {

				// dismis

				if (progressDialogWF != null && progressDialogWF.get() != null) {
					progressDialogWF.get().dismiss();
				}

				if (fragmentWF.get() == null
						|| this != fragmentWF.get().mComposeMessageTaskWF.get()) {
					// not the newest!
					return;
				}

				if (Common.RESULT_NO_TO_USER.equals(result)) {
					Toast.makeText(fragmentWF.get().getActivity(),
							"No user found!", Toast.LENGTH_LONG).show();
					return;
				}

				if (Common.RESULT_NO_DEFAULT_USER.equals(result)) {
					Toast.makeText(fragmentWF.get().getActivity(),
							"Login Request!", Toast.LENGTH_LONG).show();
					return;
				}

				if (result == Common.RESULT_FETCHING_FAIL) {
					Toast.makeText(fragmentWF.get().getActivity(),
							"Connect error!Try again latter.",
							Toast.LENGTH_SHORT).show();
				}

				if (result.equals(Common.RESULT_NEED_CAPTCHA)) {
					String ca = infoJSON.optJSONObject("json").optString(
							"captcha");
					fragmentWF.get().loadCaptcha(ca);
				}
				if (result == Common.RESULT_SUCCESS) {
					fragmentWF.get().submitSuccess();
				}
			}
		}
	}

	/**
	 * load captcha
	 */
	public void loadCaptcha(String iden) {
		mCurrentIden = iden;
		mCaptchaLinear.setVisibility(View.VISIBLE);
		String url = "http://www.reddit.com/captcha/" + mCurrentIden + ".png";
		((CacheFragmentActivity) (this.getActivity())).getImageWorker()
				.loadImage(url, mCaptchaImage);
		Toast.makeText(this.getActivity(),"Input Captcha",Toast.LENGTH_SHORT).show();
	}

	public void submitSuccess() {
		Toast.makeText(this.getActivity(), "Submit success", Toast.LENGTH_LONG)
				.show();
		mTo.setText("");
		mSubject.setText("");
		mMessage.setText("");
		mCaptche.setText("");
		mCaptchaLinear.setVisibility(View.GONE);
	}

	public boolean check() {
		if (mTo.getText() == null || "".equals(mTo.getText().toString().trim())) {
			Toast.makeText(this.getActivity(), "please enter a username",
					Toast.LENGTH_LONG).show();
			return false;
		}

		if (mSubject.getText() == null
				|| "".equals(mSubject.getText().toString().trim())) {
			Toast.makeText(this.getActivity(), "please enter a subject",
					Toast.LENGTH_LONG).show();
			return false;
		}

		if (mMessage.getText() == null
				|| "".equals(mMessage.getText().toString().trim())) {
			Toast.makeText(this.getActivity(), "please enter a message",
					Toast.LENGTH_LONG).show();
			return false;
		}

		if (mCaptchaLinear.getVisibility() == View.VISIBLE) {
			if (mCaptche.getText() == null
					|| "".equals(mCaptche.getText().toString().trim())) {
				Toast.makeText(this.getActivity(), "please enter the captche",
						Toast.LENGTH_LONG).show();
				return false;
			}

		}
		return true;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.button_friends:
			((ComposeMessageActivity) this.getActivity()).showFriendsDilaog();
			break;
		}
	}

	@Override
	public void onFriendNameClick(String name) {
		mTo.setText(name);
	}

}
