package com.morihacky.android.rxjava;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import com.morihacky.android.rxjava.app.R;
import com.morihacky.android.rxjava.rxbus.RxBus;
import timber.log.Timber;

public class MainActivity
    extends FragmentActivity {

  private RxBus _rxBus = null;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    Timber.plant(new Timber.DebugTree());

    getSupportFragmentManager().beginTransaction()
        .addToBackStack(this.toString())
        .replace(R.id.activity_main, new MainFragment(), this.toString())
        //.replace(R.id.activity_main, new TimingDemoFragment(), this.toString())
        .commit();
  }

  // This is better done with a DI Library like Dagger
  public RxBus getRxBusSingleton() {
    if (_rxBus == null) {
      _rxBus = new RxBus();
    }

    return _rxBus;
  }
}
