package com.softgame.reddit.fragment;

import java.lang.ref.WeakReference;

import org.json.custom.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.softgame.reddit.R;
import com.softgame.reddit.utils.Common;
import com.softgame.reddit.utils.LocalDataCenter;
import com.softgame.reddit.utils.RedditManager;

public class LoginFragment extends Fragment implements OnClickListener,
		OnCancelListener {

	public static final String TAG = "LoginFragment";

	EditText mUsername;
	EditText mPassword;
	Button mBtnLogin;
	Button mBtnCancel;
	TextView mResult;
	WeakReference<ProgressDialog> mProgressDialogWF;
	private WeakReference<LoginTask> mLoginTaskWF;

	public LoginFragment() {
	}

	public static LoginFragment findOrCreateLoginFragment(
			FragmentManager manager, long id) {
		LoginFragment fragment = (LoginFragment) manager
				.findFragmentByTag(LoginFragment.TAG + id);
		if (fragment == null) {
			fragment = new LoginFragment();
			final FragmentTransaction ft = manager.beginTransaction();
			ft.add(android.R.id.content, fragment, LoginFragment.TAG + id);
			ft.commit();
		}

		return fragment;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setRetainInstance(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.activity_login, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		mUsername = (EditText) this.getView().findViewById(R.id.login_username);
		mPassword = (EditText) this.getView().findViewById(R.id.login_password);
		mBtnLogin = (Button) this.getView().findViewById(R.id.login_button);
		mBtnLogin.setOnClickListener(this);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (mLoginTaskWF != null
				&& mLoginTaskWF.get() != null
				&& !mLoginTaskWF.get().getStatus()
						.equals(AsyncTask.Status.FINISHED)) {
			showProgressDialog();
		}

	}

	/**
	 * a task to process log in
	 */

	private static class LoginTask extends AsyncTask<Void, Void, String> {

		String u;
		String p;

		WeakReference<LoginFragment> fragmentWF;
		JSONObject authJSON;

		public LoginTask(LoginFragment fragment, String username,
				String password) {
			fragmentWF = new WeakReference<LoginFragment>(fragment);
			u = username;
			p = password;
			authJSON = new JSONObject();
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (fragmentWF != null && fragmentWF.get() != null
					&& fragmentWF.get().getActivity() != null
					&& fragmentWF.get().mLoginTaskWF != null
					&& fragmentWF.get().mLoginTaskWF.get() == this) {
				fragmentWF.get().showProgressDialog();
			} else {
				this.cancel(true);
			}

		}

		@Override
		protected String doInBackground(Void... params) {
			if (fragmentWF != null && fragmentWF.get() != null
					&& fragmentWF.get().getActivity() != null
					&& fragmentWF.get().mLoginTaskWF != null
					&& fragmentWF.get().mLoginTaskWF.get() == this) {

				return RedditManager.login(fragmentWF.get().getActivity(), u,
						p, authJSON);
			} else {
				this.cancel(true);
				return Common.RESULT_TASK_CANCLE;
			}
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			if (this.isCancelled() || Common.RESULT_TASK_CANCLE.equals(result)) {
				// cancel
				return;
			}

			if (!this.isCancelled() && fragmentWF != null
					&& fragmentWF.get() != null
					&& fragmentWF.get().getActivity() != null
					&& fragmentWF.get().mLoginTaskWF != null
					&& fragmentWF.get().mLoginTaskWF.get() == this) {
				fragmentWF.get().hideProgressDialog();

				if (result.equals(Common.RESULT_SUCCESS)) {
					// save to preference
					LocalDataCenter.setUserData(fragmentWF.get().getActivity(),
							authJSON);
					LocalDataCenter.setDefaultUser(fragmentWF.get()
							.getActivity(), u);

					fragmentWF.get().getActivity()
							.setResult(Activity.RESULT_OK);
					Toast.makeText(fragmentWF.get().getActivity(),
							"Login succeed!", Toast.LENGTH_LONG).show();
					// RedditManager.resetInit();
					fragmentWF.get().getActivity().finish();
				} else {
					Toast.makeText(fragmentWF.get().getActivity(), result,
							Toast.LENGTH_LONG).show();
				}
			}
		}

	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		hideProgressDialog();
	}

	private void showProgressDialog() {
		if (mProgressDialogWF != null && mProgressDialogWF.get() != null) {
			mProgressDialogWF.get().dismiss();
		}

		ProgressDialog pd = new ProgressDialog(this.getActivity());
		pd.setMessage(this.getString(R.string.login_progress_message));
		pd.setCancelable(true);
		pd.setOnCancelListener(this);
		mProgressDialogWF = new WeakReference<ProgressDialog>(pd);
		pd.show();
	}

	private void hideProgressDialog() {
		if (mProgressDialogWF != null && mProgressDialogWF.get() != null) {
			mProgressDialogWF.get().dismiss();
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		hideProgressDialog();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.login_button:
			String username = mUsername.getEditableText().toString().trim();
			String password = mPassword.getEditableText().toString().trim();
			if (username == null || username.equals("")) {
				Toast.makeText(getActivity(), R.string.login_no_username,
						Toast.LENGTH_SHORT).show();
				return;
			}
			if (password == null || password.equals("")) {
				Toast.makeText(getActivity(), R.string.login_no_password,
						Toast.LENGTH_SHORT).show();
				return;
			}

			if (mLoginTaskWF != null && mLoginTaskWF.get() != null) {
				mLoginTaskWF.get().cancel(true);
			}

			LoginTask loginTask = new LoginTask(this, username, password);
			mLoginTaskWF = new WeakReference<LoginFragment.LoginTask>(loginTask);
			loginTask.execute();
			break;
		}
	}

	@Override
	public void onCancel(DialogInterface dialog) {
		if (mLoginTaskWF != null
				&& mLoginTaskWF.get() != null
				&& !mLoginTaskWF.get().getStatus()
						.equals(AsyncTask.Status.FINISHED)) {
			Toast.makeText(this.getActivity(), "Login canceled!",
					Toast.LENGTH_SHORT).show();
			mLoginTaskWF.get().cancel(true);
			hideProgressDialog();
		}
	}
}
