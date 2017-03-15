package com.morihacky.android.rxjava;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import com.morihacky.android.rxjava.fragments.MainFragment;
import com.morihacky.android.rxjava.fragments.RotationPersist1WorkerFragment;
import com.morihacky.android.rxjava.fragments.RotationPersist2WorkerFragment;
import com.morihacky.android.rxjava.rxbus.RxBus;

public class MainActivity extends AppCompatActivity {

  private RxBus _rxBus = null;

  @Override
  public void onBackPressed() {
    super.onBackPressed();
    _removeWorkerFragments();
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if (savedInstanceState == null) {
      getSupportFragmentManager()
          .beginTransaction()
          .replace(android.R.id.content, new MainFragment(), this.toString())
          .commit();
    }
  }

  // This is better done with a DI Library like Dagger
  public RxBus getRxBusSingleton() {
    if (_rxBus == null) {
      _rxBus = new RxBus();
    }

    return _rxBus;
  }

  private void _removeWorkerFragments() {
    Fragment frag =
        getSupportFragmentManager()
            .findFragmentByTag(RotationPersist1WorkerFragment.class.getName());

    if (frag != null) {
      getSupportFragmentManager().beginTransaction().remove(frag).commit();
    }

    frag =
        getSupportFragmentManager()
            .findFragmentByTag(RotationPersist2WorkerFragment.class.getName());

    if (frag != null) {
      getSupportFragmentManager().beginTransaction().remove(frag).commit();
    }
  }
}
