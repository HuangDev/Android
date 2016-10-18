package com.softgame.reddit;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.BaseAdapter;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.softgame.reddit.cache.ImageCache;
import com.softgame.reddit.cache.ImageCache.ImageCacheParams;
import com.softgame.reddit.cache.ImageFetcher;
import com.softgame.reddit.cache.ImageResizer;
import com.softgame.reddit.cache.ImageWorker;
import com.softgame.reddit.cache.RetainFragment;
import com.softgame.reddit.cache.Utils;
import com.softgame.reddit.utils.Common;
import com.softgame.reddit.utils.CommonUtil;

public abstract class CacheFragmentActivity extends SherlockFragmentActivity {

	protected ImageResizer mImageWorker;
	protected RetainFragment mRetainFragment;

	protected int mWidth;
	protected int mHeight;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		this.setTheme(CommonUtil.getCurrentTheme(this, R.string.pref_theme_key));
		super.onCreate(savedInstanceState);
		ImageCacheParams cacheParams = new ImageCacheParams(
				Common.IMAGE_CACHE_DIR);

		// Allocate a third of the per-app memory limit to the bitmap memory
		// cache. This value
		// should be chosen carefully based on a number of factors. Refer to the
		// corresponding
		// Android Training class for more discussion:
		// http://developer.android.com/training/displaying-bitmaps/
		// In this case, we aren't using memory for much else other than this
		// activity and the
		// ImageDetailActivity so a third lets us keep all our sample image
		// thumbnails in memory
		// at once.
		cacheParams.memCacheSize = 1024 * 1024 * Utils.getMemoryClass(this) / 12;
		Log.d("ImageWork","MemorySize:" + cacheParams.memCacheSize);
		if(cacheParams.memCacheSize > 1024 * 1024 * 3){
			cacheParams.memCacheSize = 1024 * 1024 * 3;
		}

		// DisplayMetrics metrics = new DisplayMetrics();
		// getWindowManager().getDefaultDisplay().getMetrics(metrics);

		// The ImageWorker takes care of loading images into our ImageView
		// children asynchronously
		DisplayMetrics displaymetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
		mHeight = displaymetrics.heightPixels;
		mWidth = displaymetrics.widthPixels;
		mImageWorker = new ImageFetcher(this, mWidth, mHeight);
		mImageWorker.setPictureQualityLevel(CommonUtil.getPictureQualityLevel(this));
		// mImageWorker.setAdapter(Images.imageThumbWorkerUrlsAdapter);
		//mImageWorker.setLoadingImage(R.drawable.loading_pic);
		mImageWorker.setLoadFailImage(R.drawable.icon_pic_not_found);
		mRetainFragment = RetainFragment.findOrCreateRetainFragment(this
				.getSupportFragmentManager());

		mImageWorker.setImageCache(ImageCache.findOrCreateCache(this,
				mRetainFragment, cacheParams));

	}

	public ImageWorker getImageWorker() {
		return mImageWorker;
	}

	public RetainFragment getRetainFragment() {
		return mRetainFragment;
	}

	public boolean isPortaitMode() {
		return mHeight > mWidth;
	}
	
	
	

}
