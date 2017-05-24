package com.morihacky.android.rxjava.wiring;

import android.content.Context;
import android.widget.ArrayAdapter;
import com.morihacky.android.rxjava.R;
import java.util.List;

public class LogAdapter extends ArrayAdapter<String> {

  public LogAdapter(Context context, List<String> logs) {
    super(context, R.layout.item_log, R.id.item_log, logs);
  }
}
