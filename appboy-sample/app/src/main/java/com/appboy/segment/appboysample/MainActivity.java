package com.appboy.segment.appboysample;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.util.Log;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.appboy.Constants;
import com.appboy.support.AppboyLogger;
import com.appboy.ui.AppboyFeedFragment;

public class MainActivity extends AppCompatActivity {
  private static final String TAG = String.format("%s.%s", Constants.APPBOY_LOG_TAG_PREFIX, MainActivity.class.getName());

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    AppboyLogger.setLogLevel(Log.VERBOSE);
    setContentView(R.layout.activity_main);
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    final FragmentManager fragmentManager = getSupportFragmentManager();
    Fragment currentFragment = fragmentManager.findFragmentById(R.id.root);

    if (currentFragment == null) {
      fragmentManager.beginTransaction().add(R.id.root, new MainFragment()).commit();
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (!AppboySegmentApplication.sAppboySegmentEnabled) {
      Toast.makeText(this, "Appboy integration disabled. Doing nothing.", Toast.LENGTH_LONG).show();
      return super.onOptionsItemSelected(item);
    }
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();
    if (id == R.id.newsfeed) {
      replaceCurrentFragment(new AppboyFeedFragment());
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  private void replaceCurrentFragment(Fragment newFragment) {
    FragmentManager fragmentManager = getSupportFragmentManager();
    Fragment currentFragment = fragmentManager.findFragmentById(R.id.root);

    if (currentFragment != null && currentFragment.getClass().equals(newFragment.getClass())) {
      Log.i(TAG, String.format("Fragment of type %s is already the active fragment. Ignoring request to replace " +
          "current fragment.", currentFragment.getClass()));
      return;
    }

    hideSoftKeyboard();
    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
    fragmentTransaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out,
        android.R.anim.fade_in, android.R.anim.fade_out);
    fragmentTransaction.replace(R.id.root, newFragment, newFragment.getClass().toString());
    if (currentFragment != null) {
      fragmentTransaction.addToBackStack(newFragment.getClass().toString());
    } else {
      fragmentTransaction.addToBackStack(null);
    }
    fragmentTransaction.commit();
  }

  private void hideSoftKeyboard() {
    if (getCurrentFocus() != null) {
      InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
      inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
          InputMethodManager.RESULT_UNCHANGED_SHOWN);
    }
  }
}
