package com.softgame.reddit.fragment;

import java.lang.ref.WeakReference;

import org.json.custom.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.MenuItem;
import com.softgame.reddit.CacheFragmentActivity;
import com.softgame.reddit.R;
import com.softgame.reddit.model.DraftItem;
import com.softgame.reddit.utils.Common;
import com.softgame.reddit.utils.SubmitManager;

public class SubmitLinkTextFragment extends SherlockFragment implements
		RadioGroup.OnCheckedChangeListener {

	public static final String TAG = "SubmitMessageFragment";

	EditText mTitle;
	EditText mLinkUrl;
	EditText mOptionText;
	EditText mSubreddit;
	EditText mLinkCaptche;
	EditText mTextCaptche;
	LinearLayout mLinkUrlLinear;
	LinearLayout mOptionTextLinear;
	LinearLayout mLinkCaptchaLinear;
	LinearLayout mTextCaptchaLinear;
	RadioGroup mTypeGroup;

	ImageView mLinkCaptchaImage;
	ImageView mTextCaptchaImage;
	String mCurrentTextIden;
	String mCurrentLinkIden;
	ProgressDialog mProgressDialog;
	boolean mNeedToSave;

	String mSharedSubject;
	String mSharedBody;
	boolean mFromShare;
	String mSubredditName;

	WeakReference<SubmitLinkTask> mSubmitLinkTaskWF;
	WeakReference<SubmitTextTask> mSubmitTextTaskWF;

	public SubmitLinkTextFragment() {
	};

	public static SubmitLinkTextFragment findOrCreateComposeMessageFragment(
			FragmentManager manager, String subredditName, boolean fromShare,
			String body, String subject) {
		SubmitLinkTextFragment fragment = (SubmitLinkTextFragment) manager
				.findFragmentByTag(SubmitLinkTextFragment.TAG);
		if (fragment == null) {
			fragment = new SubmitLinkTextFragment();
			// create a new Fragment
			final FragmentTransaction ft = manager.beginTransaction();
			ft.add(android.R.id.content, fragment, SubmitLinkTextFragment.TAG);
			Bundle b = new Bundle();
			if (body != null && !"".equals(body)) {
				b.putString(Common.KEY_BODY_TEXT, body);
			}
			if (subject != null && !"".equals(subject)) {
				b.putString(Common.KEY_SUBJECT_TEXT, subject);
			}
			b.putBoolean(Common.KEY_FROM_SHARE, fromShare);
			if (subredditName != null && !"".equals(subredditName)) {
				b.putString(Common.KEY_SUBREDDIT_NAME, subredditName);
			}

			fragment.setArguments(b);
			ft.commit();
		}
		return fragment;
	}

	@Override
	public void onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu,
			com.actionbarsherlock.view.MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);

		menu.add("Send")
				.setIcon(R.drawable.icon_conversation_send_light)
				.setShowAsAction(
						MenuItem.SHOW_AS_ACTION_IF_ROOM
								| MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getTitle().equals("Send")) {
			if (check()) {
				// submit
				if (isLink()) {
					// Link Post
					SubmitLinkTask linkTask = new SubmitLinkTask(this,
							mProgressDialog,
							mTitle.getText().toString().trim(), mLinkUrl
									.getText().toString().trim(), mSubreddit
									.getText().toString(), mCurrentLinkIden,
							mLinkCaptche.getText().toString());

					mSubmitLinkTaskWF = new WeakReference<SubmitLinkTextFragment.SubmitLinkTask>(
							linkTask);
					linkTask.execute();
				} else {
					// Text Post
					SubmitTextTask textTask = new SubmitTextTask(this,
							mProgressDialog,
							mTitle.getText().toString().trim(), mOptionText
									.getText().toString().trim(), mSubreddit
									.getText().toString().trim(),
							mCurrentTextIden, mTextCaptche.getText().toString()
									.trim());
					mSubmitTextTaskWF = new WeakReference<SubmitLinkTextFragment.SubmitTextTask>(
							textTask);
					textTask.execute();
				}
			}

		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setRetainInstance(true);
		this.setHasOptionsMenu(true);

		if (this.getArguments() != null) {
			mSharedSubject = this.getArguments().getString(
					Common.KEY_SUBJECT_TEXT);
			mSharedBody = this.getArguments().getString(Common.KEY_BODY_TEXT);
			mFromShare = this.getArguments().getBoolean(Common.KEY_FROM_SHARE,
					false);
			mSubredditName = this.getArguments().getString(
					Common.KEY_SUBREDDIT_NAME);
		}
		mProgressDialog = new ProgressDialog(this.getActivity());
		mProgressDialog.setMessage("Submitting...");
		mProgressDialog.setCancelable(true);

		mNeedToSave = mFromShare ? false : true;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (mProgressDialog != null
				&& mSubmitLinkTaskWF != null
				&& mSubmitLinkTaskWF.get() != null
				&& !mSubmitLinkTaskWF.get().getStatus()
						.equals(AsyncTask.Status.FINISHED)) {
			mProgressDialog.show();
		}
	}

	public boolean needSaveDraft() {
		return (mTitle.getEditableText() != null && !"".equals(mTitle
				.getEditableText().toString().trim()))
				|| (mTypeGroup.getCheckedRadioButtonId() == R.id.type_link
						&& mLinkUrl.getEditableText() != null && !""
							.equals(mLinkUrl.getEditableText().toString()
									.trim()))
				|| (mSubreddit.getEditableText() != null && !""
						.equals(mSubreddit.getEditableText().toString().trim()));

	}

	public void saveToDraft() {

		SharedPreferences df = PreferenceManager
				.getDefaultSharedPreferences(this.getActivity());
		SharedPreferences.Editor e = df.edit();

		if (mTitle.getEditableText() != null
				&& !"".equals(mTitle.getEditableText().toString().trim())) {
			e.putString(Common.KEY_DRAFT_TITLE, mTitle.getEditableText()
					.toString().trim());
		}

		e.putBoolean(Common.KEY_DRAFT_IS_LINK,
				mTypeGroup.getCheckedRadioButtonId() == R.id.type_link);

		if (mLinkUrl.getEditableText() != null
				&& !"".equals(mLinkUrl.getEditableText().toString().trim())) {
			e.putString(Common.KEY_DRAFT_LINK_URL, mLinkUrl.getEditableText()
					.toString().trim());
		}
		if (mOptionText.getEditableText() != null
				&& !"".equals(mOptionText.getEditableText().toString().trim())) {
			e.putString(Common.KEY_DRAFT_OPTION_TEXT, mOptionText
					.getEditableText().toString().trim());
		}

		if (mSubreddit.getEditableText() != null
				&& !"".equals(mSubreddit.getEditableText().toString().trim())) {
			e.putString(Common.KEY_DRAFT_SUBREDDIT, mSubreddit
					.getEditableText().toString().trim());
		}
		e.commit();
	}

	@Override
	public void onResume() {
		super.onResume();
		// set the value to

		if (!mFromShare) {
			try {
				SharedPreferences df = PreferenceManager
						.getDefaultSharedPreferences(this.getActivity());
				mTypeGroup
						.check(df.getBoolean(Common.KEY_DRAFT_IS_LINK, true) ? R.id.type_link
								: R.id.type_text);

				mTitle.setText(df.getString(Common.KEY_DRAFT_TITLE, ""));
				mOptionText.setText(df.getString(Common.KEY_DRAFT_OPTION_TEXT,
						""));
				mLinkUrl.setText(df.getString(Common.KEY_DRAFT_LINK_URL, ""));
				if (mSubreddit.getEditableText() != null
						&& !"".equals(mSubreddit.getEditableText().toString()
								.trim())) {

				} else {
					mSubreddit.setText(df.getString(Common.KEY_DRAFT_SUBREDDIT,
							""));
				}
			} catch (Exception e) {
				clearnDraft();
			}
		}
	}

	public void setNeedToSave(boolean save) {
		mNeedToSave = save;
	}

	@Override
	public void onPause() {
		super.onPause();
		if (mNeedToSave) {
			saveToDraft();
		} else {

		}
	}

	public void clearnDraft() {
		SharedPreferences df = PreferenceManager
				.getDefaultSharedPreferences(this.getActivity());
		SharedPreferences.Editor e = df.edit();
		e.putString(Common.KEY_DRAFT_TITLE, "");
		e.putBoolean(Common.KEY_DRAFT_IS_LINK, true);
		e.putString(Common.KEY_DRAFT_LINK_URL, "");
		e.putString(Common.KEY_DRAFT_OPTION_TEXT, "");
		e.putString(Common.KEY_DRAFT_SUBREDDIT, "");
		e.commit();
	}

	public boolean isLink() {
		return mTypeGroup.getCheckedRadioButtonId() == R.id.type_link;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.activity_submit_link_text, container,
				false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		mTypeGroup = (RadioGroup) this.getView().findViewById(R.id.type_group);
		mTitle = (EditText) this.getView().findViewById(R.id.input_title);

		mLinkUrlLinear = (LinearLayout) this.getView().findViewById(
				R.id.input_url_linear);
		mLinkUrl = (EditText) this.getView().findViewById(R.id.input_url);

		mOptionTextLinear = (LinearLayout) this.getView().findViewById(
				R.id.input_text_linear);
		mOptionText = (EditText) this.getView().findViewById(R.id.input_text);

		mLinkCaptchaLinear = (LinearLayout) this.getView().findViewById(
				R.id.link_captcha_linear);
		mTextCaptchaLinear = (LinearLayout) this.getView().findViewById(
				R.id.text_captcha_linear);

		mTextCaptche = (EditText) this.getView().findViewById(
				R.id.text_input_captcha);

		mLinkCaptche = (EditText) this.getView().findViewById(
				R.id.link_input_captcha);

		mLinkCaptchaImage = (ImageView) this.getView().findViewById(
				R.id.link_captcha_pic);
		mTextCaptchaImage = (ImageView) this.getView().findViewById(
				R.id.text_captcha_pic);

		mSubreddit = (EditText) this.getView().findViewById(
				R.id.input_subreddit);

		if (mSubredditName != null && !"".equals(mSubredditName)) {
			mSubreddit.setText(mSubredditName);
		}

		// both gone
		mLinkCaptchaLinear.setVisibility(View.GONE);
		mTextCaptchaLinear.setVisibility(View.GONE);
		mTypeGroup.setOnCheckedChangeListener(this);

		if (mFromShare) {
			if (mSharedBody != null && !"".equals(mSharedBody)
					&& URLUtil.isNetworkUrl(mSharedBody)) {
				mTypeGroup.check(R.id.type_link);
				mLinkUrl.setText(mSharedBody);
				if (mSharedSubject != null && !"".equals(mSharedSubject)) {
					mTitle.setText(mSharedSubject);
				}
			} else if (mSharedBody != null && !"".equals(mSharedBody)) {
				mTypeGroup.check(R.id.type_text);
				if (mSharedSubject != null && !"".equals(mSharedSubject)) {
					mTitle.setText(mSharedSubject);
					mOptionText.setText(mSharedBody);
				} else {
					mTitle.setText(mSharedBody);
				}
			} else {
				if (mSharedSubject != null && !"".equals(mSharedSubject)) {
					mTitle.setText(mSharedSubject);
				}
			}
		}
		((Button) this.getView().findViewById(R.id.pick_subreddit))
				.setOnClickListener((OnClickListener) this.getActivity());
	}

	public void setSubreddit(String subreddit) {
		mSubreddit.setText(subreddit);
	}

	public void updateView() {
		switch (mTypeGroup.getCheckedRadioButtonId()) {
		case R.id.type_link:
			mOptionTextLinear.setVisibility(View.GONE);
			mLinkUrlLinear.setVisibility(View.VISIBLE);
			break;
		case R.id.type_text:
			mOptionTextLinear.setVisibility(View.VISIBLE);
			mLinkUrlLinear.setVisibility(View.GONE);
			break;
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		if (mProgressDialog != null) {
			mProgressDialog.dismiss();
		}

	}

	public static class SubmitLinkTask extends AsyncTask<Void, Void, String> {

		JSONObject infoJSON;

		String title;
		String url;
		String subreddit;
		String iden;
		String captcha;

		Bitmap mCaptchaBitmap;

		WeakReference<SubmitLinkTextFragment> fragmentWF;
		WeakReference<ProgressDialog> progressDialogWF;

		public SubmitLinkTask(SubmitLinkTextFragment fragment,
				ProgressDialog pd, String t, String u, String s, String i,
				String c) {
			title = t;
			url = u;
			subreddit = s;
			iden = i;
			captcha = c;
			fragmentWF = new WeakReference<SubmitLinkTextFragment>(fragment);
			progressDialogWF = new WeakReference<ProgressDialog>(pd);
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
			} else {
				this.cancel(true);
			}
		}

		@Override
		protected String doInBackground(Void... params) {

			if (!this.isCancelled() && fragmentWF != null
					&& fragmentWF.get() != null
					&& fragmentWF.get().getActivity() != null) {

				infoJSON = new JSONObject();

				String result = SubmitManager.commitLinkPost(fragmentWF.get()
						.getActivity(), title, url, subreddit, iden, captcha,
						infoJSON);
				if (result.equals(Common.RESULT_NEED_CAPTCHA)
						&& fragmentWF != null && fragmentWF.get() != null
						&& fragmentWF.get().getActivity() != null) {
					String ca = infoJSON.optJSONObject("json").optString(
							"captcha");
					String url = "http://www.reddit.com/captcha/" + ca + ".png";

					mCaptchaBitmap = ((CacheFragmentActivity) (fragmentWF.get()
							.getActivity())).getImageWorker()
							.processBitmap(url);
				}
				return result;

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
						|| this != fragmentWF.get().mSubmitLinkTaskWF.get()) {
					// not the newest!
					return;
				}

				if (Common.RESULT_REDDIT_BROKE.equals(result)) {
					// reddit broke
					Toast.makeText(fragmentWF.get().getActivity(),
							"reddit broke!", 2 * Toast.LENGTH_LONG).show();
					return;
				}

				if (Common.RESULT_UNKNOW.equals(result)) {
					Toast.makeText(fragmentWF.get().getActivity(),
							"unknow error! subreddit may not exist",
							Toast.LENGTH_SHORT).show();
					return;
				}

				if (Common.RESULT_NO_DEFAULT_USER.equals(result)) {
					Toast.makeText(fragmentWF.get().getActivity(),
							"Login Request!", Toast.LENGTH_LONG).show();
					return;
				}

				if (result.equals(Common.RESULT_FETCHING_FAIL)) {
					Toast.makeText(fragmentWF.get().getActivity(),
							"Connect error! Try again latter.",
							Toast.LENGTH_SHORT).show();
					return;
				}

				if (result.equals(Common.RESULT_SUBMIT_TOO_FAST)) {
					Toast.makeText(fragmentWF.get().getActivity(),
							"Submit too fast, try again latter",
							Toast.LENGTH_LONG).show();
					return;
				}

				if (result.equals(Common.RESULT_SUBREDDIT_NOEXIST)) {
					Toast.makeText(fragmentWF.get().getActivity(),
							"subreddit not exist", Toast.LENGTH_LONG).show();
					return;
				}

				if (result.equals(Common.RESULT_SUBREDDIT_NOTALLLOW)) {
					Toast.makeText(fragmentWF.get().getActivity(),
							"subreddit not allow submit", Toast.LENGTH_LONG)
							.show();
					return;
				}

				if (result.equals(Common.RESULT_NEED_CAPTCHA)) {
					String ca = infoJSON.optJSONObject("json").optString(
							"captcha");
					fragmentWF.get().loadCaptcha(ca, mCaptchaBitmap, true);
					mCaptchaBitmap = null;
					return;
				}
				if (result == Common.RESULT_SUCCESS) {
					fragmentWF.get().submitSuccess();
					return;
				}
			}
		}

	}

	public static class SubmitTextTask extends AsyncTask<Void, Void, String> {

		JSONObject infoJSON;

		String title;
		String optionText;
		String subreddit;
		String iden;
		String captcha;
		Bitmap mCaptchaBitmap;
		WeakReference<SubmitLinkTextFragment> fragmentWF;
		WeakReference<ProgressDialog> progressDialogWF;

		public SubmitTextTask(SubmitLinkTextFragment fragment,
				ProgressDialog pd, String t, String o, String s, String i,
				String c) {
			title = t;
			optionText = o;
			subreddit = s;
			iden = i;
			captcha = c;
			fragmentWF = new WeakReference<SubmitLinkTextFragment>(fragment);
			progressDialogWF = new WeakReference<ProgressDialog>(pd);
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
			} else {
				this.cancel(true);
			}
		}

		@Override
		protected String doInBackground(Void... params) {

			if (!this.isCancelled() && fragmentWF != null
					&& fragmentWF.get() != null
					&& fragmentWF.get().getActivity() != null) {

				infoJSON = new JSONObject();
				String result = SubmitManager.commitTextPost(fragmentWF.get()
						.getActivity(), title, optionText, subreddit, iden,
						captcha, infoJSON);

				if (result.equals(Common.RESULT_NEED_CAPTCHA)
						&& fragmentWF != null && fragmentWF.get() != null
						&& fragmentWF.get().getActivity() != null) {
					String ca = infoJSON.optJSONObject("json").optString(
							"captcha");
					String url = "http://www.reddit.com/captcha/" + ca + ".png";

					mCaptchaBitmap = ((CacheFragmentActivity) (fragmentWF.get()
							.getActivity())).getImageWorker()
							.processBitmap(url);
				}
				return result;
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

				// dismiss
				if (progressDialogWF != null && progressDialogWF.get() != null) {
					progressDialogWF.get().dismiss();
				}

				if (fragmentWF.get() == null
						|| this != fragmentWF.get().mSubmitTextTaskWF.get()) {
					// not the newest!
					return;
				}

				if (Common.RESULT_NO_DEFAULT_USER.equals(result)) {
					Toast.makeText(fragmentWF.get().getActivity(),
							"Login Request!", Toast.LENGTH_LONG).show();
					return;
				}

				if (result.equals(Common.RESULT_SUBMIT_TOO_FAST)) {
					Toast.makeText(fragmentWF.get().getActivity(),
							"Submit too fast, try again latter",
							Toast.LENGTH_LONG).show();
					return;
				}

				if (result.equals(Common.RESULT_SUBREDDIT_NOEXIST)) {
					Toast.makeText(fragmentWF.get().getActivity(),
							"subreddit not exist", Toast.LENGTH_LONG).show();
					return;
				}

				if (result.equals(Common.RESULT_SUBREDDIT_NOTALLLOW)) {
					Toast.makeText(fragmentWF.get().getActivity(),
							"subreddit not allow submit", Toast.LENGTH_LONG)
							.show();
					return;
				}

				if (result.equals(Common.RESULT_UNKNOW)) {
					Toast.makeText(fragmentWF.get().getActivity(),
							"unknow error! subreddit may not exist",
							Toast.LENGTH_LONG).show();
					return;
				}

				if (result.equals(Common.RESULT_FETCHING_FAIL)) {
					Toast.makeText(fragmentWF.get().getActivity(),
							"Connect error!Try again latter.",
							Toast.LENGTH_SHORT).show();
					return;
				}

				if (result.equals(Common.RESULT_NEED_CAPTCHA)) {
					String ca = infoJSON.optJSONObject("json").optString(
							"captcha");
					fragmentWF.get().loadCaptcha(ca, mCaptchaBitmap, false);
					return;
				}
				if (result == Common.RESULT_SUCCESS) {
					fragmentWF.get().submitSuccess();
					return;
				}
			}
		}

	}

	/**
	 * load captcha
	 */
	public void loadCaptcha(String iden, Bitmap image, boolean isLink) {
		if (isLink) {
			mCurrentLinkIden = iden;
			mLinkCaptchaLinear.setVisibility(View.VISIBLE);
			mTextCaptchaLinear.setVisibility(View.GONE);
			mLinkCaptchaImage.setImageBitmap(image);
			mLinkCaptche.requestFocus();
			return;
		}
		mCurrentTextIden = iden;
		mLinkCaptchaLinear.setVisibility(View.GONE);
		mTextCaptchaLinear.setVisibility(View.VISIBLE);
		mTextCaptchaImage.setImageBitmap(image);
		mTextCaptche.requestFocus();
	}

	public void submitSuccess() {
		Toast.makeText(this.getActivity(), "Submit success", Toast.LENGTH_LONG)
				.show();
		mNeedToSave = false;
		clearnDraft();
		this.getActivity().finish();
	}

	public boolean check() {
		if (mTitle.getEditableText() == null
				|| "".equals(mTitle.getEditableText().toString().trim())) {
			Toast.makeText(this.getActivity(), "title request",
					Toast.LENGTH_LONG).show();
			return false;
		}

		if (mLinkUrlLinear.getVisibility() == View.VISIBLE) {
			if (mLinkUrl.getEditableText() == null
					|| "".equals(mLinkUrl.getEditableText().toString().trim())) {
				Toast.makeText(this.getActivity(), "link url request",
						Toast.LENGTH_LONG).show();
				return false;
			}
		}

		if (mLinkCaptchaLinear.getVisibility() == View.VISIBLE) {
			if (mLinkCaptche.getEditableText() == null
					|| "".equals(mLinkCaptche.getEditableText().toString()
							.trim())) {
				Toast.makeText(this.getActivity(), "captche request",
						Toast.LENGTH_LONG).show();
				return false;
			}
		}

		if (mTextCaptchaLinear.getVisibility() == View.VISIBLE) {
			if (mTextCaptche.getEditableText() == null
					|| "".equals(mTextCaptche.getEditableText().toString()
							.trim())) {
				Toast.makeText(this.getActivity(), "captche request",
						Toast.LENGTH_LONG).show();
				return false;
			}
		}

		if (mSubreddit.getEditableText() == null
				|| "".equals(mSubreddit.getEditableText().toString().trim())) {
			Toast.makeText(this.getActivity(), "subreddit request",
					Toast.LENGTH_LONG).show();

			return false;
		} else {
			if (!mSubreddit.getEditableText().toString().trim()
					.matches(Common.SUBREDDIT_PATTEN)) {
				Toast.makeText(this.getActivity(),
						"subreddit can only contain number and alphabet",
						Toast.LENGTH_LONG).show();
				return false;
			}
		}

		return true;
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		updateView();
	}

}
