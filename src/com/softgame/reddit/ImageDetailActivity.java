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
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.softgame.reddit.cache.DiskLruCache;
import com.softgame.reddit.fragment.ImageDetailFragment;
import com.softgame.reddit.fragment.ImageViewFragment;
import com.softgame.reddit.model.SubRedditItem;
import com.softgame.reddit.utils.Common;
import com.softgame.reddit.utils.CommonUtil;
import com.softgame.reddit.utils.SubRedditManager;

public class ImageDetailActivity extends CacheFragmentActivity {
	ImageDetailFragment mImageDetailFragment;
	long mFragmentId = 0L;

	BroadcastReceiver mExternalStorageReceiver;
	boolean mExternalStorageAvailable = false;
	boolean mExternalStorageWriteable = false;
	// default to Environment.DIRECTORY_PICTURES
	String mSavePath;

	Uri mSaveUri;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		// this.getSupportActionBar().setDisplayShowHomeEnabled(true);
		this.getSupportActionBar().setDisplayShowTitleEnabled(false);

		if (savedInstanceState != null)
			mFragmentId = savedInstanceState.getLong("fragment_id");
		if (mFragmentId == 0L) {
			mFragmentId = System.currentTimeMillis();
		}

		SubRedditItem subredditItem = null;
		if (this.getIntent() != null) {
			subredditItem = (SubRedditItem) this.getIntent()
					.getParcelableExtra(Common.EXTRA_SUBREDDIT);
		}

		if (subredditItem == null) {
			Toast.makeText(this, "link missing!", Toast.LENGTH_LONG).show();
			finish();
			return;
		}

		mImageDetailFragment = ImageDetailFragment
				.findOrCreateImageViewFragment(
						this.getSupportFragmentManager(), subredditItem,
						mFragmentId);

		// hide the actionBar if need
		if (this.getRetainFragment() != null) {
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add("Open in Browser").setIcon(R.drawable.icon_actionbar_browser)
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		menu.add("Save Picture to SDCard")
				.setIcon(R.drawable.icon_actionbar_save_picture)
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

		menu.add("Share the picture").setIcon(R.drawable.icon_actionbar_share)
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
			this.finish();
			break;
		}

		if (item.getTitle().equals("Open in Browser")) {
			Intent i = new Intent(Intent.ACTION_VIEW,
					Uri.parse(mImageDetailFragment.getSubredditItem().url));
			startActivity(i);
		}

		if (item.getTitle().equals("Share the picture")) {
			if (mExternalStorageAvailable && mExternalStorageWriteable) {
				addPictureToSDCard(false);
			} else {
				Toast.makeText(ImageDetailActivity.this,
						"SDCard is busy, share failed", Toast.LENGTH_SHORT)
						.show();
			}
		}

		if (item.getTitle().equals("Save Picture to SDCard")) {
			if (mExternalStorageAvailable && mExternalStorageWriteable) {
				addPictureToSDCard(true);
			} else {
				Toast.makeText(ImageDetailActivity.this,
						"SDCard is busy, save failed", Toast.LENGTH_SHORT)
						.show();
			}
		}

		return super.onOptionsItemSelected(item);
	}

	public void createPictureShareIntent(Uri uri) {
		// Uri uri = addPictureToSDCard(false);
		if (uri != null) {
			Intent shareIntent = new Intent(Intent.ACTION_SEND);
			shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
			// shareIntent.setType("image/*");
			shareIntent.setType("*/*");
			shareIntent.putExtra(Intent.EXTRA_TEXT,
					mImageDetailFragment.getSubredditItem().title);
			// For a file in shared storage. For data in private storage,
			// use a
			// ContentProvider.
			shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
			startActivity(Intent.createChooser(shareIntent, "Share via"));
		}
	}

	@TargetApi(8)
	public Uri addPictureToSDCard(final boolean showToast) {
		try {
			if (CommonUtil.isLowThanFroyo()) {
				Toast.makeText(this,
						"not support android version low than 2.2",
						Toast.LENGTH_SHORT).show();
				return null;
			}
			// get file
			String fileName = mImageWorker.getFileByUrl(CommonUtil
					.appendJPG(mImageDetailFragment.getSubredditItem().url));
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
						CommonUtil.appendJPG(mImageDetailFragment
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
									ImageDetailActivity.this
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

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putLong("fragment_id", mFragmentId);
		super.onSaveInstanceState(outState);
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

				if (imagevIewActivityWF.get().getImageActionMode() != null)
					imagevIewActivityWF.get().getImageActionMode().invalidate();
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
				if (imageViewActivityWF.get().getImageActionMode() != null)
					imageViewActivityWF.get().getImageActionMode().invalidate();
			}

		}
	}

}
