package com.softgame.reddit;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.WindowManager.LayoutParams;
import android.widget.Toast;

import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.softgame.reddit.cache.DiskLruCache;
import com.softgame.reddit.fragment.ImageViewFragment;
import com.softgame.reddit.model.SubRedditItem;
import com.softgame.reddit.utils.Common;
import com.softgame.reddit.utils.CommonUtil;
import com.softgame.reddit.utils.SubRedditManager;

public class ImageViewActivity extends CacheFragmentActivity {
	ActionMode mActionMode;
	ImageViewFragment mImageViewFragment;
	long mFragmentId = 0L;
	boolean mSafeForWork = true;

	BroadcastReceiver mExternalStorageReceiver;
	boolean mExternalStorageAvailable = false;
	boolean mExternalStorageWriteable = false;
	// default to Environment.DIRECTORY_PICTURES
	String mSavePath;
	Uri mSaveUri;

	boolean mFromCanvas = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (!super.isPortaitMode()) {
			getWindow().addFlags(LayoutParams.FLAG_FULLSCREEN);
		}

		this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		// this.getSupportActionBar().setDisplayShowHomeEnabled(true);
		this.getSupportActionBar().setDisplayShowTitleEnabled(true);
		this.getSupportActionBar().setDisplayShowHomeEnabled(false);
		this.getSupportActionBar().setDisplayUseLogoEnabled(false);
		this.getSupportActionBar().setTitle("Picture");

		if (savedInstanceState != null)
			mFragmentId = savedInstanceState.getLong("fragment_id");
		if (mFragmentId == 0L) {
			mFragmentId = System.currentTimeMillis();
		}

		SubRedditItem subredditItem = null;
		if (this.getIntent() != null) {
			subredditItem = (SubRedditItem) this.getIntent()
					.getParcelableExtra(Common.EXTRA_SUBREDDIT);
			mFromCanvas = this.getIntent().getBooleanExtra(
					Common.EXTRA_FROM_CANVAS, false);
		}

		if (subredditItem == null) {
			Toast.makeText(this, "link missing!", Toast.LENGTH_LONG).show();
			finish();
			return;
		}

		mImageViewFragment = ImageViewFragment.findOrCreateImageViewFragment(
				this.getSupportFragmentManager(), subredditItem, mFragmentId);

		if (!isPortaitMode()) {
			this.getSupportActionBar().hide();
		} else if (this.getRetainFragment() != null) {
			if (this.getRetainFragment().isFullScreen()) {
				this.getSupportActionBar().hide();
			}
		}

		mSavePath = CommonUtil.getSavePicturePath(this);

		// hide the actionBar if need
		if (this.getRetainFragment() != null) {
			if (this.getRetainFragment().isFullScreen()) {
				this.getSupportActionBar().hide();
			}
		}

		startWatchingExternalStorage();
	}

	void startWatchingExternalStorage() {
		mExternalStorageReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				Log.i("test", "Storage: " + intent.getData());
				updateExternalStorageState();
			}
		};
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
		filter.addAction(Intent.ACTION_MEDIA_REMOVED);
		registerReceiver(mExternalStorageReceiver, filter);
		updateExternalStorageState();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		stopWatchingExternalStorage();
	}

	void stopWatchingExternalStorage() {
		unregisterReceiver(mExternalStorageReceiver);
	}

	void updateExternalStorageState() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			mExternalStorageAvailable = mExternalStorageWriteable = true;
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			mExternalStorageAvailable = true;
			mExternalStorageWriteable = false;
		} else {
			mExternalStorageAvailable = mExternalStorageWriteable = false;
		}
	}

	public void showActionMode() {
		mActionMode = startActionMode(new AnActionModeOfEpicProportions());
	}

	public boolean dismissActionMode() {
		if (mActionMode != null) {
			mActionMode.finish();
			return true;
		} else {
			return false;
		}
	}

	public void invalidateActionMode() {
		if (mActionMode != null) {
			mActionMode.invalidate();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (!mImageViewFragment.getSubredditItem().saved) {
			menu.add("UnSaved").setIcon(R.drawable.icon_actionbar_unsaved)
					.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

		} else {
			menu.add("Saved").setIcon(R.drawable.icon_actionbar_saved)
					.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

		}
		menu.add("Comments").setIcon(R.drawable.icon_actionbar_comment)
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

		menu.add("Share the picture").setIcon(R.drawable.icon_actionbar_share)
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

		menu.add("Action Mode").setIcon(R.drawable.icon_actionbar_context)
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			setResultData(false);
			this.finish();
			break;
		}

		if (item.getTitle().equals("Comments")) {
			if (mFromCanvas) {
				Intent t = new Intent(ImageViewActivity.this,
						CommentFragmentActivity.class);
				t.putExtra(Common.INTENT_EXTRA_SUBREDDIT,
						mImageViewFragment.getSubredditItem());
				this.startActivity(t);
			} else {
				setResultData(true);
			}
			this.finish();
			return true;
		} else if (item.getTitle().equals("UnSaved")) {
			// save
			mImageViewFragment.getSubredditItem().saved = true;
			new SaveTask(this, mImageViewFragment.getSubredditItem(), true)
					.execute();
			ImageViewActivity.this.invalidateOptionsMenu();

		} else if (item.getTitle().equals("Saved")) {
			mImageViewFragment.getSubredditItem().saved = false;
			new SaveTask(this, mImageViewFragment.getSubredditItem(), false)
					.execute();
			ImageViewActivity.this.invalidateOptionsMenu();
			// unsave
		} else if (item.getTitle().equals("Action Mode")) {
			mActionMode = startActionMode(new AnActionModeOfEpicProportions());
		} else if (item.getTitle().equals("Share the picture")) {
			// Intent shareIntent = createShareIntent();
			// if (shareIntent != null)
			// startActivity(Intent
			// .createChooser(shareIntent, "Share via"));
			if (mExternalStorageAvailable && mExternalStorageWriteable) {
				addPictureToSDCard(false);
			} else {
				Toast.makeText(ImageViewActivity.this,
						"SDCard is busy, share failed", Toast.LENGTH_SHORT)
						.show();
			}
		}

		return super.onOptionsItemSelected(item);
	}

	private final class AnActionModeOfEpicProportions implements
			ActionMode.Callback {
		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			return true;
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			menu.clear();
			// Used to put dark icons on light action bar
			if (mImageViewFragment.getSubredditItem().likes == null) {
				menu.add("Upvote")
						.setIcon(R.drawable.icon_actionbar_vote_up_grey)
						.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

				menu.add("Downvote")
						.setIcon(R.drawable.icon_actionbar_vote_down_grey)
						.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
			} else {
				menu.add("Upvote")
						.setIcon(
								mImageViewFragment.getSubredditItem().likes ? R.drawable.vote_up_selected
										: R.drawable.icon_actionbar_vote_up_grey)
						.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

				menu.add("Downvote")
						.setIcon(
								mImageViewFragment.getSubredditItem().likes ? R.drawable.icon_actionbar_vote_down_grey
										: R.drawable.vote_down_selected)
						.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

			}

			if (mImageViewFragment != null
					&& mImageViewFragment.getSubredditItem() != null) {
				if (mImageViewFragment.getSubredditItem().over_18
						&& mSafeForWork) {
					// dont add open in browser
				} else {
					menu.add("Open in Browser")
							.setIcon(R.drawable.icon_actionbar_browser)
							.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
					menu.add("Save Picture to SDCard")
							.setIcon(R.drawable.icon_actionbar_save_picture)
							.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
				}
			}

			menu.add("Full Screen")
					.setIcon(R.drawable.icon_actionbar_fullscreen)
					.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

			menu.add("Share the link").setIcon(R.drawable.icon_actionbar_share)
					.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

			return true;
		}

		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

			if (item.getItemId() == R.id.abs__action_mode_close_button) {
				ImageViewActivity.this.dismissActionMode();
			}

			if (item.getTitle().equals("Open in Browser")) {
				Intent i = new Intent(Intent.ACTION_VIEW,
						Uri.parse(mImageViewFragment.getSubredditItem().url));
				startActivity(i);
			}

			if (item.getTitle().equals("Full Screen")) {
				ImageViewActivity.this.dismissActionMode();
				ImageViewActivity.this.getSupportActionBar().hide();
				if (ImageViewActivity.this.getRetainFragment() != null) {
					ImageViewActivity.this.getRetainFragment().setFullScreen(
							true);
				}
			}

			if (item.getTitle().equals("Upvote")) {
				String dir = mImageViewFragment.updateVoteAndGetResult(true);
				new VoteTask(ImageViewActivity.this,
						mImageViewFragment.getSubredditItem(), dir, true)
						.execute();

				ImageViewActivity.this.invalidateActionMode();
			}
			if (item.getTitle().equals("Downvote")) {
				String dir = mImageViewFragment.updateVoteAndGetResult(false);
				new VoteTask(ImageViewActivity.this,
						mImageViewFragment.getSubredditItem(), dir, false)
						.execute();
				ImageViewActivity.this.invalidateActionMode();
			}

			if (item.getTitle().equals("Save Picture to SDCard")) {
				if (mExternalStorageAvailable && mExternalStorageWriteable) {
					addPictureToSDCard(true);
				} else {
					Toast.makeText(ImageViewActivity.this,
							"SDCard is busy, save failed", Toast.LENGTH_SHORT)
							.show();
				}
			}

			if (item.getTitle().equals("Share the link")) {
				createLinkShareIntent();
			}

			return true;
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
			mActionMode = null;
		}
	}

	public void createLinkShareIntent() {
		Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
		sharingIntent.setType("text/plain");
		String shareBody = "http://www.reddit.com"
				+ mImageViewFragment.getSubredditItem().permalink;
		String subject = "Reddit this! "
				+ mImageViewFragment.getSubredditItem().title;
		sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, subject
				+ "\n" + shareBody);
		startActivity(Intent.createChooser(sharingIntent, "Share link via"));
	}

	public void createPictureShareIntent(Uri uri) {
		// Uri uri = addPictureToSDCard(false);
		if (uri != null) {
			Intent shareIntent = new Intent(Intent.ACTION_SEND);
			shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
			// shareIntent.setType("image/*");
			shareIntent.setType("*/*");
			shareIntent.putExtra(Intent.EXTRA_TEXT,
					mImageViewFragment.getSubredditItem().title);
			// For a file in shared storage. For data in private storage,
			// use a
			// ContentProvider.
			shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
			startActivity(Intent
					.createChooser(shareIntent, "Share picture via"));
		}
	}

	public Uri addPictureToSDCard(final boolean showToast) {
		try {
			// get file
			String fileName = mImageWorker.getFileByUrl(CommonUtil
					.appendJPG(mImageViewFragment.getSubredditItem().url));
			if (fileName != null && !"".equals(fileName.trim())) {
				File copyPath;
				// copy path
				if (Environment.DIRECTORY_PICTURES.equals(mSavePath)) {
					copyPath = Environment
							.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
				} else {
					copyPath = new File(
							Environment.getExternalStorageDirectory() + "/"
									+ mSavePath);
					if (!copyPath.exists()) {
						// create and if not successful use public picture
						// folder
						if (copyPath.mkdirs()) {
							copyPath = Environment
									.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
						}
					}
				}

				// start copy
				InputStream in = null;
				OutputStream out = null;
				String outFile = DiskLruCache.createFilePath(copyPath,
						CommonUtil.appendJPG(mImageViewFragment
								.getSubredditItem().url));
				// check if the file exist
				if (new File(outFile).exists()) {
					if (showToast)
						Toast.makeText(this, "Picture already saved!",
								Toast.LENGTH_LONG).show();
				} else {
					// download it
					try {
						File diskFile = new File(outFile);
						in = new FileInputStream(fileName);
						out = new FileOutputStream(diskFile);
						byte[] buffer = new byte[1024];
						int read;
						while ((read = in.read(buffer)) != -1) {
							out.write(buffer, 0, read);
						}

						in.close();
						in = null;
						out.flush();
						out.close();
						out = null;

						if (showToast)
							Toast.makeText(this,
									"picture saved to SDCard succeeded!",
									Toast.LENGTH_LONG).show();

					} catch (Exception e) {
						Log.e("tag", e.getMessage());
						if (showToast)
							Toast.makeText(this,
									"picture saved to SDCard failed!",
									Toast.LENGTH_LONG).show();
						return null;
					}
				}
				// Tell the media scanner about the new file so that it is
				// immediately available to the user.
				MediaScannerConnection.scanFile(this,
						new String[] { outFile.toString() }, null,
						new MediaScannerConnection.OnScanCompletedListener() {
							public void onScanCompleted(String path, Uri uri) {
								Log.i("ExternalStorage", "Scanned " + path
										+ ":");
								if (!showToast) {
									mSaveUri = uri;
									ImageViewActivity.this
											.createPictureShareIntent(uri);
									Log.i("ExternalStorage", "Uri:" + mSaveUri
											+ ":");
								}
							}
						});

				return mSaveUri;
			} else {
				Toast.makeText(
						this,
						showToast ? "Picture unreach, save failed"
								: "Picture unreach, share failed",
						Toast.LENGTH_LONG).show();
			}

		} catch (Exception e) {
			Toast.makeText(
					this,
					showToast ? "save failed, try again latter"
							: "share fail, try again latter", Toast.LENGTH_LONG)
					.show();
			return null;
		}
		return null;
	}

	public boolean onKeyDown(int keycode, KeyEvent e) {
		switch (keycode) {
		case KeyEvent.KEYCODE_MENU:
			if (!this.getSupportActionBar().isShowing()) {
				this.getSupportActionBar().show();
				if (this.getRetainFragment() != null) {
					this.getRetainFragment().setFullScreen(false);
				}
				return true;
			}
			break;

		case KeyEvent.KEYCODE_BACK:

			if (mActionMode != null) {
				return this.dismissActionMode();
			} else if (!this.getSupportActionBar().isShowing()) {
				this.getSupportActionBar().show();
				if (this.getRetainFragment() != null) {
					this.getRetainFragment().setFullScreen(false);
				}
				return true;
			} else {
				// return;
				setResultData(false);
				this.finish();
				return true;
			}
		}
		return super.onKeyDown(keycode, e);
	}

	private void setResultData(boolean isComment) {
		Intent dd = new Intent();
		dd.putExtra(Common.EXTRA_SUBREDDIT,
				mImageViewFragment.getSubredditItem());
		dd.putExtra(Common.KEY_EXTRA_IS_COMMENT, isComment);
		this.setResult(Activity.RESULT_OK, dd);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putLong("fragment_id", mFragmentId);
		super.onSaveInstanceState(outState);
	}

	public ActionMode getImageActionMode() {
		return mActionMode;
	}

	// -------------------vote ------------------
	public static class VoteTask extends AsyncTask<Void, Void, String> {
		String name;
		boolean isUp;
		String dir = "0";
		WeakReference<ImageViewActivity> imagevIewActivityWF;
		WeakReference<SubRedditItem> subRedditItemWF;

		public VoteTask(ImageViewActivity activity, SubRedditItem s, String d,
				boolean up) {
			imagevIewActivityWF = new WeakReference<ImageViewActivity>(activity);
			subRedditItemWF = new WeakReference<SubRedditItem>(s);
			isUp = up;
			dir = d;
			name = s.name;
		}

		@Override
		protected String doInBackground(Void... params) {
			if (imagevIewActivityWF != null
					&& imagevIewActivityWF.get() != null) {
				return SubRedditManager.voteSubRedditPost(name, dir,
						imagevIewActivityWF.get());
			} else {
				this.cancel(true);
				return Common.RESULT_TASK_CANCLE;
			}
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			if (result.equals(Common.RESULT_TASK_CANCLE)
					|| imagevIewActivityWF == null
					|| imagevIewActivityWF.get() == null
					|| subRedditItemWF == null || subRedditItemWF.get() == null
					|| imagevIewActivityWF == null) {
				return;
			}
			if (result == Common.RESULT_SUCCESS) {
				Toast.makeText(imagevIewActivityWF.get(), "Vote succeeded!",
						Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(imagevIewActivityWF.get(), "Vote failed!",
						Toast.LENGTH_SHORT).show();

				// roll back
				subRedditItemWF.get().likes = subRedditItemWF.get().old_like;

				imagevIewActivityWF.get().invalidateActionMode();
			}
		}

	}

	// ---------------------- SAVE --------------------
	public static class SaveTask extends AsyncTask<Void, Void, String> {
		String name;
		boolean save;
		WeakReference<SubRedditItem> subRedditItemWF;
		WeakReference<ImageViewActivity> imageViewActivityWF;

		public SaveTask(ImageViewActivity activity, SubRedditItem item,
				boolean s) {
			save = s;
			name = item.name;
			subRedditItemWF = new WeakReference<SubRedditItem>(item);
			imageViewActivityWF = new WeakReference<ImageViewActivity>(activity);
		}

		@Override
		protected String doInBackground(Void... params) {
			if (imageViewActivityWF != null
					&& imageViewActivityWF.get() != null
					&& subRedditItemWF != null && subRedditItemWF.get() != null) {
				return SubRedditManager.saveSubRedditPost(name, save,
						imageViewActivityWF.get());
			} else {
				this.cancel(true);
				return Common.RESULT_TASK_CANCLE;
			}
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			if (result.equals(Common.RESULT_TASK_CANCLE)
					|| imageViewActivityWF == null
					|| imageViewActivityWF.get() == null
					|| subRedditItemWF == null || subRedditItemWF.get() == null) {
				return;
			}

			if (Common.RESULT_SUCCESS.equals(result)) {
				Toast.makeText(imageViewActivityWF.get(),
						save ? "Save succeeded!" : "Unsave succeeded!",
						Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(imageViewActivityWF.get(),
						"Save failed, try again latter!", Toast.LENGTH_SHORT)
						.show();
				subRedditItemWF.get().saved = !save;
				imageViewActivityWF.get().invalidateOptionsMenu();
			}

		}
	}

}
