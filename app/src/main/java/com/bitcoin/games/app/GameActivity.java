package com.bitcoin.games.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.bitcoin.games.R;
import com.bitcoin.games.lib.Bitcoin;
import com.bitcoin.games.lib.BitcoinGames;
import com.bitcoin.games.lib.CommonActivity;
import com.bitcoin.games.lib.JSONBalanceResult;
import com.bitcoin.games.lib.NetBalanceTask;

class CreditBTCItem {
  public String mConversion;
  public String mHappyText;
  public long mCreditBTCValue;

  CreditBTCItem(String conversion, String happyText, long creditBTCValue) {
    mConversion = conversion;
    mHappyText = happyText;
    mCreditBTCValue = creditBTCValue;
  }
}


abstract public class GameActivity extends CommonActivity {
  @Override
  protected void attachBaseContext(Context newBase) {
    super.attachBaseContext(MyContextWrapper.wrap(newBase, "en"));
  }

  public class GameState {
    final static public int ERROR = -1;
  }

  class AutoSpeed {
    final static public int SLOW = 0;
    final static public int MEDIUM = 1;
    final static public int FAST = 2;
  }

  int mGameState;
  boolean mIsWaitingForServer;
  boolean mIsGameBusy;
  boolean mDidScaleContents;
  ProgressDialog mConnectingDialog;
  String mServerSeedHash;
  boolean mIsAutoOn = false;
  boolean mIsFirstAutoAction;
  GameNetBalanceTask mNetBalanceTask;
  static final private String TAG = "GameActivity";
  ViewGroup mContainer;
  ViewGroup mContents;
  ViewGroup mCreditsHolder;
  ViewGroup mWinHolder;
  SoundPool mSoundPool;
  TextView mTextBet;
  BitmapCache mBitmapCache;
  long mCreditBTCValue;
  Typeface mArial;
  Typeface mArialBold;
  boolean mIsTimeUpdateRunning;
  boolean mCreditsAreDirty;
  long mLastNetBalanceCheck;
  boolean mBlinkOn;
  Handler mHandler;
  Button mBTCButton;

  long mLastBlink;
  int mTimeUpdateDelay = 500;
  final int BLINK_DELAY = 500;
  final int BALANCE_CHECK_DELAY = 10000;
  boolean mShowDecimalCredits;

  public static String KEY_USE_FAKE_CREDITS = "com.bitcoin.games.KEY_USE_FAKE_CREDITS";
  boolean mUseFakeCredits;

  //@Override
  public void onCreate(Bundle savedInstanceState, int contentViewResource) {
    super.onCreate(savedInstanceState);

    requestWindowFeature(Window.FEATURE_NO_TITLE);
    getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    setContentView(contentViewResource);

    // Default to false in case it wasn't set when calling the activity
    mUseFakeCredits = false;
    Bundle extras = getIntent().getExtras();
    if (extras != null) {
      mUseFakeCredits = extras.getBoolean(KEY_USE_FAKE_CREDITS);
    }

    // Set the music volume instead of ringer volume when hardware volume buttons are pressed
    setVolumeControlStream(AudioManager.STREAM_MUSIC);

    mBitmapCache = new BitmapCache(this);
    mHandler = new Handler();
    mGameState = GameState.ERROR;
    //mCreditBTCValue = 100000;
    // Note that VideoPoker can change this value
    mCreditBTCValue = Bitcoin.stringAmountToLong("0.001");
    mDidScaleContents = false;
    mArial = Typeface.createFromAsset(getAssets(), "fonts/arial.ttf");
    mArialBold = Typeface.createFromAsset(getAssets(), "fonts/arialbd.ttf");
    mTextBet = (TextView) findViewById(R.id.bet_text);
    mTextBet.setText(getString(R.string.bet_amount, 1));
    mBTCButton = (Button) findViewById(R.id.btc_button);
    mIsGameBusy = false;
    mIsFirstAutoAction = false;
    mShowDecimalCredits = false;

    mContainer = (ViewGroup) findViewById(R.id.container);
    mContents = (ViewGroup) findViewById(R.id.contents);
    mCreditsHolder = (ViewGroup) findViewById(R.id.credits_holder);
    mWinHolder = (ViewGroup) findViewById(R.id.win_holder);

    mSoundPool = new SoundPool(7, AudioManager.STREAM_MUSIC, 0);
    mCreditsAreDirty = false;

    // TB - This should be called in the super class onCreate(), so that the fake credits and CreditBTC are correctly set.
    // Otherwise an incorrect number might first get displayed before switching over to the correct value.
    // BitcoinGames bvc = BitcoinGames.getInstance(this);
    // updateCredits( mUseFakeCredits ? bvc.mFakeIntBalance : bvc.mIntBalance );
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    //getMenuInflater().inflate(R.menu.activity_videopoker, menu);
    return true;
  }

  void checkConnectingAlert() {
    if (mConnectingDialog != null && !shouldConnectingDialogShow()) {
      mConnectingDialog.cancel();
      mConnectingDialog = null;
    }
  }

  void addCardBitmapsToCache() {
    mBitmapCache.addBitmap(R.drawable.card_2c);
    mBitmapCache.addBitmap(R.drawable.card_3c);
    mBitmapCache.addBitmap(R.drawable.card_4c);
    mBitmapCache.addBitmap(R.drawable.card_5c);
    mBitmapCache.addBitmap(R.drawable.card_6c);
    mBitmapCache.addBitmap(R.drawable.card_7c);
    mBitmapCache.addBitmap(R.drawable.card_8c);
    mBitmapCache.addBitmap(R.drawable.card_9c);
    mBitmapCache.addBitmap(R.drawable.card_tc);
    mBitmapCache.addBitmap(R.drawable.card_jc);
    mBitmapCache.addBitmap(R.drawable.card_qc);
    mBitmapCache.addBitmap(R.drawable.card_kc);
    mBitmapCache.addBitmap(R.drawable.card_ac);
    mBitmapCache.addBitmap(R.drawable.card_2h);
    mBitmapCache.addBitmap(R.drawable.card_3h);
    mBitmapCache.addBitmap(R.drawable.card_4h);
    mBitmapCache.addBitmap(R.drawable.card_5h);
    mBitmapCache.addBitmap(R.drawable.card_6h);
    mBitmapCache.addBitmap(R.drawable.card_7h);
    mBitmapCache.addBitmap(R.drawable.card_8h);
    mBitmapCache.addBitmap(R.drawable.card_9h);
    mBitmapCache.addBitmap(R.drawable.card_th);
    mBitmapCache.addBitmap(R.drawable.card_jh);
    mBitmapCache.addBitmap(R.drawable.card_qh);
    mBitmapCache.addBitmap(R.drawable.card_kh);
    mBitmapCache.addBitmap(R.drawable.card_ah);
    mBitmapCache.addBitmap(R.drawable.card_2s);
    mBitmapCache.addBitmap(R.drawable.card_3s);
    mBitmapCache.addBitmap(R.drawable.card_4s);
    mBitmapCache.addBitmap(R.drawable.card_5s);
    mBitmapCache.addBitmap(R.drawable.card_6s);
    mBitmapCache.addBitmap(R.drawable.card_7s);
    mBitmapCache.addBitmap(R.drawable.card_8s);
    mBitmapCache.addBitmap(R.drawable.card_9s);
    mBitmapCache.addBitmap(R.drawable.card_ts);
    mBitmapCache.addBitmap(R.drawable.card_js);
    mBitmapCache.addBitmap(R.drawable.card_qs);
    mBitmapCache.addBitmap(R.drawable.card_ks);
    mBitmapCache.addBitmap(R.drawable.card_as);
    mBitmapCache.addBitmap(R.drawable.card_2d);
    mBitmapCache.addBitmap(R.drawable.card_3d);
    mBitmapCache.addBitmap(R.drawable.card_4d);
    mBitmapCache.addBitmap(R.drawable.card_5d);
    mBitmapCache.addBitmap(R.drawable.card_6d);
    mBitmapCache.addBitmap(R.drawable.card_7d);
    mBitmapCache.addBitmap(R.drawable.card_8d);
    mBitmapCache.addBitmap(R.drawable.card_9d);
    mBitmapCache.addBitmap(R.drawable.card_td);
    mBitmapCache.addBitmap(R.drawable.card_jd);
    mBitmapCache.addBitmap(R.drawable.card_qd);
    mBitmapCache.addBitmap(R.drawable.card_kd);
    mBitmapCache.addBitmap(R.drawable.card_ad);
    mBitmapCache.addBitmap(R.drawable.card_back);
  }

  String getClientSeed() {
    // TB TODO - Randomize this!
    return "123";
  }

  boolean shouldConnectingDialogShow() {
    BitcoinGames bvc = BitcoinGames.getInstance(this);
    Log.v(TAG, "fake:" + bvc.mFakeIntBalance);
    return (mServerSeedHash == null || bvc.mIntBalance < 0 || bvc.mFakeIntBalance < 0);
  }

  int getDelayFromAutoSpeed(int autoSpeed) {
    int delay = 0;
    if (autoSpeed == AutoSpeed.MEDIUM) {
      delay = 1000;
    } else if (autoSpeed == AutoSpeed.SLOW) {
      delay = 2000;
    }

    return delay;
  }

  public static void handleCriticalConnectionError(final Activity a) {
    AlertDialog.Builder builder = new AlertDialog.Builder(a);
    builder.setMessage("Error connecting to server. Please check your internet connection and try again.")
        .setCancelable(false)
        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int id) {
            dialog.cancel();
            a.finish();
          }
        });
    AlertDialog alert = builder.create();
    alert.show();
  }

  void abortConnectingDialog() {
    mConnectingDialog.cancel();
    mConnectingDialog = null;
    handleCriticalConnectionError(this);
  }

  void showConnectingDialog() {
    if (!shouldConnectingDialogShow()) {
      Log.e(TAG, "Trying to call showConnectingDialog yet it is not supposed to show.");
      return;
    }
    if (mConnectingDialog != null) {
      Log.e(TAG, "Trying to call showConnectingDialog when a dialog is already up (non-null).");
      return;
    }
    mConnectingDialog = ProgressDialog.show(this, "", "Connecting to server...", true);

    // Abort if it screws up after X seconds
    mHandler.postDelayed(new Runnable() {
      public void run() {
        if (mConnectingDialog != null) {
          abortConnectingDialog();
        }
      }
    }, 7000);
  }

  @Override
  public void onResume() {
    super.onResume();

    // Let other stuff draw before popping up the dialog
    // TB TODO - This is kind of sloppy with the delay. The problem is that otherwise
    // the screen looks all screwed up (incorrect sizes) while the dialog is up.
    mHandler.postDelayed(new Runnable() {
      public void run() {
        if (shouldConnectingDialogShow()) {
          showConnectingDialog();
        }
      }
    }, 500);
  }

  @Override
  public void onPause() {
    super.onPause();

    if (mIsAutoOn) {
      mIsAutoOn = false;
      // TB TODO - Also update the button
    }

    if (mConnectingDialog != null) {
      mConnectingDialog.cancel();
      mConnectingDialog = null;
    }
  }

  @Override
  public void onWindowFocusChanged(boolean hasFocus) {
    super.onWindowFocusChanged(hasFocus);
    mTextBet.setTextSize(TypedValue.COMPLEX_UNIT_PX, mCreditsHolder.getHeight() * 0.8f);
  }

  abstract public void updateControls();

  // TB - This is retarded
  class CountUpRunnable implements Runnable {
    public long mCurrent;
    public long mGoal;
    public long mStartingIntBalance;
    public long mDelta;
    public boolean mShouldStop;

    // TB TODO - Add checks to ensure it doesn't get used more than once at the same time?
    public void run() {
      if (mShouldStop) {
        return;
      }
      mCurrent += mDelta;
      if (!mCreditsAreDirty) {
        Log.v(TAG, "Counting up credits that are not dirty!");
      }
      if (mCurrent >= mGoal) {
        updateWin(mGoal, false);
        updateCredits(mStartingIntBalance + mGoal);
        mCreditsAreDirty = false;
        return;
      }

      updateWin(mCurrent, false);
      updateCredits(mStartingIntBalance + (long) mCurrent);

      int delay = 50;
      mHandler.postDelayed(mCountUpRunnable, delay);
    }
  }

  ;
  CountUpRunnable mCountUpRunnable = new CountUpRunnable();

  void startCountUpWins(final long goal, final long startingIntBalance, final long delta) {
    if (startingIntBalance < 0) {
      Log.e(TAG, "Counting up from a negative startingIntBalance!");
    }
    // Make credits dirty so that a balance server response will not mess with the counting up sequence.
    mCreditsAreDirty = true;
    mCountUpRunnable.mCurrent = 0;
    mCountUpRunnable.mGoal = goal;
    mCountUpRunnable.mStartingIntBalance = startingIntBalance;
    //mCountUpRunnable.mDelta = mCreditBTCValue * 1;
    mCountUpRunnable.mDelta = delta;
    mCountUpRunnable.mShouldStop = false;
    mCountUpRunnable.run();
  }

  void stopCountUpWins() {
    mCountUpRunnable.mShouldStop = true;
  }

  void showEarlyExitDialog() {
    final Activity that = this;
    AlertDialog.Builder builder = new AlertDialog.Builder(that);
    builder.setMessage("You are in the middle of a game. If you leave, you will be forfeiting your bet.\n\nAre you sure you want to leave this game?")
        .setCancelable(false)
        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int id) {
            dialog.cancel();
          }
        })
        .setPositiveButton("Quit Game", new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int id) {
            dialog.cancel();
            that.finish();
          }
        });
    AlertDialog alert = builder.create();
    alert.show();
  }

  public void addImageToViewGroup(int resource, ViewGroup parent, LinearLayout.LayoutParams layout) {
    ImageView img = new ImageView(this);
    // TB TODO - Use cached images?
    img.setImageResource(resource);
    if (layout == null) {
      layout = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
    }
    parent.addView(img, layout);
  }

  public void addNumberToViewGroup(int number, ViewGroup parent, float textSize) {
    String credits = String.valueOf(number);
    TextView numberView = new TextView(this);
    numberView.setText(credits);
    numberView.setTypeface(Typeface.SANS_SERIF, Typeface.BOLD);
    numberView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
//    numberView.setTextColor(ContextCompat.getColor(this, R.color.credits));
    parent.addView(numberView);
  }

  public void addCreditsNumberToViewGroup(long intbalance, ViewGroup parent, float textSize) {
    int wholeNumber = (int) (intbalance / mCreditBTCValue);
    addNumberToViewGroup(wholeNumber, parent, textSize);

    // TB TODO - Show decimals!!!
    if (mShowDecimalCredits) {
      int rem = (int) (intbalance - (wholeNumber * mCreditBTCValue));
      int dec = (int) ((rem * 10) / mCreditBTCValue);
      if (dec != 0) {
        TextView t = new TextView(this);
        t.setText(".");
        t.setTypeface(Typeface.SANS_SERIF, Typeface.BOLD);
        t.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
//        t.setTextColor(ContextCompat.getColor(this, R.color.credits));
        parent.addView(t);
        addNumberToViewGroup(dec, parent, textSize);
      }
    }

  }

  // prize is now in satoshis
  public void updateWin(long prize, boolean showDouble) {
    mWinHolder.removeAllViews();

    if (prize == 0) {
      return;
    }

    TextView c = new TextView(this);
    // TB TODO - Use cached images?

    c.setTypeface(Typeface.SANS_SERIF, Typeface.BOLD);
    c.setTextSize(TypedValue.COMPLEX_UNIT_PX, mWinHolder.getHeight() * 0.8f);

    if (showDouble) {
      c.setText(R.string.text_double);
    } else {
      c.setText(R.string.text_win);
    }
    LayoutParams layout = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
    layout.setMargins(0, 0, 15, 0);
    mWinHolder.addView(c, layout);

    // addNumberToViewGroup( prize, mWinHolder );
    addCreditsNumberToViewGroup(prize, mWinHolder, mWinHolder.getHeight() * 0.8f);
  }

  void updateCredits(Long intbalance, int letterCreditsResource) {
    mCreditsHolder.removeAllViews();

    if (intbalance < 0) {
      Log.e(TAG, "Trying to update credits to negative value: " + intbalance);
      return;
    }

    addCreditsNumberToViewGroup(intbalance, mCreditsHolder, mCreditsHolder.getHeight() * 0.8f);

    TextView c = new TextView(this);
    c.setText(letterCreditsResource);
    c.setTypeface(Typeface.SANS_SERIF, Typeface.BOLD);
    c.setTextSize(TypedValue.COMPLEX_UNIT_PX, mCreditsHolder.getHeight() * 0.8f);
//    c.setTextColor(ContextCompat.getColor(this, R.color.credits));
    LayoutParams layout = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
    layout.setMargins(15, 0, 0, 0);
    mCreditsHolder.addView(c, layout);
  }

  public void updateCredits(Long intbalance) {
    updateCredits(intbalance, R.string.credits);
  }

  Runnable mTimeUpdateRunnable = new Runnable() {
    public void run() {
      timeUpdate();
    }
  };

  void timeUpdate() {
    long now = System.currentTimeMillis();
    if (now - mLastBlink >= BLINK_DELAY) {
      mBlinkOn = !mBlinkOn;
      mLastBlink = now;
    }

    // TB TODO - Should never be possible to even be here if you have no account key...
    if (now - mLastNetBalanceCheck >= BALANCE_CHECK_DELAY) {
      mLastNetBalanceCheck = now;
      BitcoinGames bvc = BitcoinGames.getInstance(this);
      if (bvc.mAccountKey != null) {
        //Log.v(TAG, bvc.mAccountKey);
        mNetBalanceTask = new GameNetBalanceTask(this);
        mNetBalanceTask.execute(Long.valueOf(0));
      }
    }

    mHandler.postDelayed(mTimeUpdateRunnable, mTimeUpdateDelay);

  }

  public void updateBTCButton(long creditBTCValue) {
    if (creditBTCValue == Bitcoin.stringAmountToLong("0.05")) {
      mBTCButton.setText(getResources().getString(R.string.button_btc_credit_value, "0.05"));
    } else if (creditBTCValue == Bitcoin.stringAmountToLong("0.01")) {
      mBTCButton.setText(getResources().getString(R.string.button_btc_credit_value, "0.01"));
    } else if (creditBTCValue == Bitcoin.stringAmountToLong("0.005")) {
      mBTCButton.setText(getResources().getString(R.string.button_btc_credit_value, "0.005"));
    } else if (creditBTCValue == Bitcoin.stringAmountToLong("0.001")) {
      mBTCButton.setText(getResources().getString(R.string.button_btc_credit_value, "0.001"));
    } else if (creditBTCValue == Bitcoin.stringAmountToLong("0.0001")) {
      mBTCButton.setText(getResources().getString(R.string.button_btc_credit_value, "0.0001"));
    } else {
      Log.e(TAG, "Error: updateBTCButton called with invalid creditBTCValue");
    }
  }


  public void handleCreditBTCChanged() {
    throw new RuntimeException("handleCreditBTCChanged is not implemented in game class");
  }


  public void showCreditBTCDialog(final String settingCreditBTCValue, final CreditBTCItem[] items) {

    ListAdapter creditBTCAdapter = new ArrayAdapter<CreditBTCItem>(getApplicationContext(), R.layout.list_row_credit_btc, items) {

      ViewHolder holder;
      Drawable icon;

      class ViewHolder {
        ImageView icon;
        TextView title;
        TextView happyText;
      }

      public View getView(int position, View convertView, ViewGroup parent) {
        final LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {
          convertView = inflater.inflate(R.layout.list_row_credit_btc, null);

          holder = new ViewHolder();
          holder.icon = (ImageView) convertView.findViewById(R.id.icon);
          holder.title = (TextView) convertView.findViewById(R.id.title);
          holder.happyText = (TextView) convertView.findViewById(R.id.happy_text);
          convertView.setTag(holder);
        } else {
          // view already defined, retrieve view holder
          holder = (ViewHolder) convertView.getTag();
        }

        // Show an image next to the currently selected item
        Drawable drawable = null;
        if (items[position].mCreditBTCValue == mCreditBTCValue) {
          drawable = getResources().getDrawable(R.mipmap.ic_launcher);
        }

        //holder.title.setText(items[position]);
        holder.title.setText(items[position].mConversion);
        holder.icon.setImageDrawable(drawable);

        holder.happyText.setText(items[position].mHappyText);

        return convertView;
      }
    };


    final CommonActivity that = this;
    AlertDialog.Builder builder = new AlertDialog.Builder(this);

    builder.setTitle("Credit Value");

    builder.setAdapter(creditBTCAdapter, new DialogInterface.OnClickListener() {

      public void onClick(DialogInterface dialog, int item) {
        // TB TODO - Actually change the friggen paytable!
        // TB TODO - Prettier dialog? Could use the art from the web site?
        mCreditBTCValue = items[item].mCreditBTCValue;
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(that);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putLong(settingCreditBTCValue, mCreditBTCValue);
        editor.commit();

        updateCredits(mUseFakeCredits ? BitcoinGames.getInstance(that).mFakeIntBalance : BitcoinGames.getInstance(that).mIntBalance);
        updateBTCButton(mCreditBTCValue);

        handleCreditBTCChanged();
      }

    });

    AlertDialog alert = builder.create();
    alert.show();
    alert.getWindow().setLayout((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 400, getResources().getDisplayMetrics()), alert.getWindow().getAttributes().height);
  }


  void playSound(int soundID) {
    AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
    float actualVolume = (float) audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
    float maxVolume = (float) audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
    float volume = actualVolume / maxVolume;
    mSoundPool.play(soundID, volume, volume, 1, 0, 1f);
  }


  // http://www.vanteon.com/downloads/Scaling_Android_Apps_White_Paper.pdf
  // Scales the contents of the given view so that it completely fills the given
  // container on one axis (that is, we're scaling isotropically).
  void scaleContents(View rootView, View container) {
    //mContentsUsableWidth = mContents.getWidth();
    //mContentsUsableHeight = mContents.getHeight();
    /*
    // Compute the scaling ratio
		float xScale = (float)container.getWidth() / rootView.getWidth(); 
		float yScale = (float)container.getHeight() / rootView.getHeight(); 
		float scale = Math.min(xScale, yScale); 
		if( mDidScaleContents ) {
			Log.e(TAG, "Error: aleady called scaleContents!");
		}
    		
		mContentsUsableHeight = (int)(mContents.getHeight() * scale);
		mContentsUsableWidth = (int)(mContents.getWidth() * scale);
		
		// Scale our contents
		scaleViewAndChildren(rootView, scale); 
		*/
    return;
  }


  // Scale the given view, its contents, and all of its children by the given factor.
  public static void scaleViewAndChildren(View root, float scale) {
    // Retrieve the view's layout information
    ViewGroup.LayoutParams layoutParams = root.getLayoutParams();
    // Scale the view itself
    if (layoutParams.width != ViewGroup.LayoutParams.MATCH_PARENT && layoutParams.width != ViewGroup.LayoutParams.WRAP_CONTENT) {
      layoutParams.width *= scale;
    }
    if (layoutParams.height != ViewGroup.LayoutParams.MATCH_PARENT && layoutParams.height != ViewGroup.LayoutParams.WRAP_CONTENT) {
      layoutParams.height *= scale;
    }
    // If this view has margins, scale those too
    if (layoutParams instanceof ViewGroup.MarginLayoutParams) {
      ViewGroup.MarginLayoutParams marginParams = (ViewGroup.MarginLayoutParams) layoutParams;
      marginParams.leftMargin *= scale;
      marginParams.rightMargin *= scale;
      marginParams.topMargin *= scale;
      marginParams.bottomMargin *= scale;
    }
    // Set the layout information back into the view
    root.setLayoutParams(layoutParams);

    // Scale the view's padding
    root.setPadding((int) (root.getPaddingLeft() * scale),
        (int) (root.getPaddingTop() * scale),
        (int) (root.getPaddingRight() * scale),
        (int) (root.getPaddingBottom() * scale));
    // If the root view is a TextView, scale the size of its text
    if (root instanceof TextView) {
      TextView textView = (TextView) root;
      textView.setTextSize(textView.getTextSize() * scale);
    }
    // If the root view is a ViewGroup, scale all of its children recursively
    if (root instanceof ViewGroup) {
      ViewGroup groupView = (ViewGroup) root;
      for (int cnt = 0; cnt < groupView.getChildCount(); ++cnt)
        scaleViewAndChildren(groupView.getChildAt(cnt), scale);
    }
  }

  int getCardResourceFromCard(String cardName) {
    int r = 0;
    if (cardName.equals("back")) {
      r = R.drawable.card_back;
    } else if (cardName.equals("back_yellow")) {
      r = R.drawable.card_back_yellow;
    } else {
      char suit = cardName.charAt(1);
      char rank = cardName.charAt(0);
      if (suit == 'c') {
        switch (rank) {
          case '2':
            r = R.drawable.card_2c;
            break;
          case '3':
            r = R.drawable.card_3c;
            break;
          case '4':
            r = R.drawable.card_4c;
            break;
          case '5':
            r = R.drawable.card_5c;
            break;
          case '6':
            r = R.drawable.card_6c;
            break;
          case '7':
            r = R.drawable.card_7c;
            break;
          case '8':
            r = R.drawable.card_8c;
            break;
          case '9':
            r = R.drawable.card_9c;
            break;
          case 't':
            r = R.drawable.card_tc;
            break;
          case 'j':
            r = R.drawable.card_jc;
            break;
          case 'q':
            r = R.drawable.card_qc;
            break;
          case 'k':
            r = R.drawable.card_kc;
            break;
          case 'a':
            r = R.drawable.card_ac;
            break;
        }
      } else if (suit == 'h') {
        switch (rank) {
          case '2':
            r = R.drawable.card_2h;
            break;
          case '3':
            r = R.drawable.card_3h;
            break;
          case '4':
            r = R.drawable.card_4h;
            break;
          case '5':
            r = R.drawable.card_5h;
            break;
          case '6':
            r = R.drawable.card_6h;
            break;
          case '7':
            r = R.drawable.card_7h;
            break;
          case '8':
            r = R.drawable.card_8h;
            break;
          case '9':
            r = R.drawable.card_9h;
            break;
          case 't':
            r = R.drawable.card_th;
            break;
          case 'j':
            r = R.drawable.card_jh;
            break;
          case 'q':
            r = R.drawable.card_qh;
            break;
          case 'k':
            r = R.drawable.card_kh;
            break;
          case 'a':
            r = R.drawable.card_ah;
            break;
        }
      } else if (suit == 's') {
        switch (rank) {
          case '2':
            r = R.drawable.card_2s;
            break;
          case '3':
            r = R.drawable.card_3s;
            break;
          case '4':
            r = R.drawable.card_4s;
            break;
          case '5':
            r = R.drawable.card_5s;
            break;
          case '6':
            r = R.drawable.card_6s;
            break;
          case '7':
            r = R.drawable.card_7s;
            break;
          case '8':
            r = R.drawable.card_8s;
            break;
          case '9':
            r = R.drawable.card_9s;
            break;
          case 't':
            r = R.drawable.card_ts;
            break;
          case 'j':
            r = R.drawable.card_js;
            break;
          case 'q':
            r = R.drawable.card_qs;
            break;
          case 'k':
            r = R.drawable.card_ks;
            break;
          case 'a':
            r = R.drawable.card_as;
            break;
        }
      } else if (suit == 'd') {
        switch (rank) {
          case '2':
            r = R.drawable.card_2d;
            break;
          case '3':
            r = R.drawable.card_3d;
            break;
          case '4':
            r = R.drawable.card_4d;
            break;
          case '5':
            r = R.drawable.card_5d;
            break;
          case '6':
            r = R.drawable.card_6d;
            break;
          case '7':
            r = R.drawable.card_7d;
            break;
          case '8':
            r = R.drawable.card_8d;
            break;
          case '9':
            r = R.drawable.card_9d;
            break;
          case 't':
            r = R.drawable.card_td;
            break;
          case 'j':
            r = R.drawable.card_jd;
            break;
          case 'q':
            r = R.drawable.card_qd;
            break;
          case 'k':
            r = R.drawable.card_kd;
            break;
          case 'a':
            r = R.drawable.card_ad;
            break;
        }
      }

    }
    return r;
  }

  class GameNetBalanceTask extends NetBalanceTask {

    GameNetBalanceTask(CommonActivity a) {
      super(a);
      mShowDialogOnError = false;
    }

    public void onSuccess(JSONBalanceResult result) {
      super.onSuccess(result);

      // TB - Don't update credits if we are waiting for a /deal /update etc result, since
      if (!mIsWaitingForServer && !mCreditsAreDirty) {
        updateCredits(mUseFakeCredits ? result.fake_intbalance : result.intbalance);
      }
      checkConnectingAlert();
      updateControls();
    }
  }
}