package com.bitcoinvideocasino.app;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import com.bitcoinvideocasino.R;

import de.marcreichelt.android.RealViewSwitcher;

public class SlotsHelpActivity extends Activity {

  RealViewSwitcher mViewSwitcher;
  final static String TAG = "SlotsHelpActivity";

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Log.v(TAG, "SlotsHelpActivity onCreate");

    requestWindowFeature(Window.FEATURE_NO_TITLE);
    getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

    setContentView(R.layout.activity_slots_help);

        /*
        mViewSwitcher = new RealViewSwitcher(getApplicationContext());
        int[] imageResources = new int[] { R.drawable.slt_help_howtoplay, R.drawable.slt_help_paytables1, R.drawable.slt_help_paytables2 };
        for( int i = 0; i < imageResources.length; i++ ) {
            ImageView view = new ImageView(getApplicationContext());
            view.setImageResource( imageResources[i] );
            mViewSwitcher.addView(view);
        }
        setContentView(mViewSwitcher);
        */
  }

}

