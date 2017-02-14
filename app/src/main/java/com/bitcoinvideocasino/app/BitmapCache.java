package com.bitcoinvideocasino.app;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.util.LruCache;

class BitmapCache {

  /*
  If this is not enough, we can start cashing images to disc.
  I don't think it's necessary to start using threads to load images from resources right now though.

  https://developer.android.com/topic/performance/graphics/index.html
  */

  final static String TAG = "BitmapCache";
  private Activity mActivity;
  private LruCache<Integer, Bitmap> mBitmapCache;

  BitmapCache(Activity a) {
    mActivity = a;

    Log.d(TAG, "BitmapCache: maxMemory = " + Runtime.getRuntime().maxMemory());

    // Get max available VM memory, exceeding this amount will throw an
    // OutOfMemory exception. Stored in kilobytes as LruCache takes an
    // int in its constructor.
    final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

    // Use 1/8th of the available memory for this memory cache.
    final int cacheSize = maxMemory / 4;

    mBitmapCache = new LruCache<Integer, Bitmap>(cacheSize) {
      @Override
      protected int sizeOf(Integer key, Bitmap bitmap) {
        // The cache size will be measured in kilobytes rather than
        // number of items.
        return bitmap.getByteCount() / 1024;
      }
    };
  }

  Bitmap getBitmap(int key) {
    long end;
    boolean nullvalue = false;
    long start = System.currentTimeMillis();
    Bitmap bitmap = mBitmapCache.get(key);

    if (bitmap == null) {
      nullvalue = true;
      Log.d(TAG, "getBitmap: bitmap is null");
      bitmap = BitmapFactory.decodeResource(mActivity.getResources(), key);
    }
    end = System.currentTimeMillis();
    if (nullvalue) {
      Log.d(TAG, "getBitmap: time loading image = " + (end - start));
    } else {
      Log.d(TAG, "getBitmap: time loading cached = " + (end - start));
    }
    return bitmap;
  }

  int x = 0;
  int y = 0;

  void addBitmap(int key) {
    Log.d(TAG, "BitmapCache: size = " + mBitmapCache.size());
    Log.d(TAG, "BitmapCache: createcount = " + mBitmapCache.createCount());
    Log.d(TAG, "BitmapCache: evictcount = " + mBitmapCache.evictionCount());

    if (mBitmapCache.get(key) == null) {
      Bitmap b = BitmapFactory.decodeResource(mActivity.getResources(), key);
      mBitmapCache.put(key, b);
      Log.d(TAG, "addBitmap X: " + ++x);
    }
    Log.d(TAG, "addBitmap Y: " + ++y);
  }

  void clear() {
    mBitmapCache.evictAll();
  }
}