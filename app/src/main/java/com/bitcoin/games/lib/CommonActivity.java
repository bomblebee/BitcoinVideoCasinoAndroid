package com.bitcoin.games.lib;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;


public class CommonActivity extends Activity {
  public Handler mHandler;
  static String TAG = "CommonActivity";

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mHandler = new Handler();
  }

  @Override
  public void onPause() {
    super.onPause();
    ((CommonApplication) this.getApplication()).abortNetAsyncTasks();
  }

  @Override
  public void onResume() {
    super.onResume();
  }


}
