package com.morihacky.android.rxjava;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import com.morihacky.android.rxjava.rxbus.RxBus;

public class MainActivity
      extends FragmentActivity {

    private RxBus _rxBus = null;

    // This is better done with a DI Library like Dagger
    public RxBus getRxBusSingleton() {
        if (_rxBus == null) {
            _rxBus = new RxBus();
        }

        return _rxBus;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Fragment frag = getSupportFragmentManager()//
              .findFragmentByTag(RotationPersistWorkerFragment.class.getName());

        if (frag != null) {
            getSupportFragmentManager().beginTransaction().remove(frag).commit();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                  .replace(android.R.id.content, new MainFragment(), this.toString())
                  .commit();
        }
    }
}