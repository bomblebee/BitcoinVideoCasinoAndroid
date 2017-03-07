package com.bitcoin.games.lib;

import android.app.Application;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class CommonApplication extends Application {

  private static final String TAG = "CommonApplication";
  private static List<NetAsyncTask> mPendingTasks = new ArrayList<NetAsyncTask>();
  public static final String APPLICATION_VERSION = "0.5";

  public void abortNetAsyncTasks() {
    for (NetAsyncTask task : mPendingTasks) {
      Log.v(TAG, "ABORTING TASK!");
      task.abort();
    }
    mPendingTasks.clear();
  }

  public void addNetAsyncTask(NetAsyncTask task) {
    mPendingTasks.add(task);
  }

  public void removeNetAsyncTask(NetAsyncTask task) {
    boolean found = mPendingTasks.remove(task);
    if (!found) {
      Log.e(TAG, "Trying to remove net async task that's not in the list");
    }
  }

}
