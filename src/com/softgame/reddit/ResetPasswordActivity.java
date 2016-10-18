package com.softgame.reddit;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.softgame.reddit.utils.RedditManager;

public class ResetPasswordActivity extends Activity implements OnClickListener {
	EditText mOldPassword;
	EditText mNewPassword;
	EditText mConfirmPassword;
	Button mReset;
	TextView mResult;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.reset_password_activity);

		mOldPassword = (EditText) this.findViewById(R.id.password_old);
		mNewPassword = (EditText) this.findViewById(R.id.password_new);
		mConfirmPassword = (EditText) this.findViewById(R.id.password_confirm);

		mResult = (TextView)this.findViewById(R.id.result);
		
		mReset = (Button) this.findViewById(R.id.reset);
		mReset.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.reset:
			String newPd = mNewPassword.getText().toString();
			String result = RedditManager.resetUserPassword("6615228", "6615228xc",
					ResetPasswordActivity.this);
			if(result !=  null)
			mResult.setText(result);
			Log.d("reset result",result);
			break;
		}
	}

}
