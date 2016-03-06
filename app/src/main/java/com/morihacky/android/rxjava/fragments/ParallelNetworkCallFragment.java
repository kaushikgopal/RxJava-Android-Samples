package com.morihacky.android.rxjava.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.morihacky.android.rxjava.R;
import com.morihacky.android.rxjava.wiring.LogAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func3;
import rx.schedulers.Schedulers;

import static android.os.Looper.getMainLooper;

/**
 *
 */
public class ParallelNetworkCallFragment extends BaseFragment {

    @Bind(R.id.list_threading_log)
    ListView _logList;
    private LogAdapter _adapter;
    private List<String> _logs;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_parallel_network_call, container, false);
        ButterKnife.bind(this, layout);
        return layout;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        _setupLogger();
        EventBus.getDefault().register(this);
    }

    @SuppressWarnings("unused")
    public void onEvent(String log) {
        _log(log);
    }

    @OnClick(R.id.btn_orchectrate_1)
    public void orchectrate_1() {
        try {
            long start = System.currentTimeMillis();
            orchestration_1();
            _log("Finished in: " + (System.currentTimeMillis() - start) + "ms");

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @SuppressWarnings("unused")
    @OnClick(R.id.btn_orchectrate_2)
    public void orchestration_2() {
        Observable<Integer> vals = Observable.range(1, 10);

        vals
              .flatMap(new Func1<Integer, Observable<Integer>>() {
                  @Override
                  public Observable<Integer> call(Integer integer) {
                      return Observable.just(integer)
                            .subscribeOn(Schedulers.computation())
                            .map(new Func1<Integer, Integer>() {
                                @Override
                                public Integer call(Integer integer) {
                                    return intenseCalculation(integer);
                                }
                            });
                  }
              })
              .subscribe(new Action1<Integer>() {
                  @Override
                  public void call(Integer o) {
                      _log("Subscriber received "
                           + o + " on "
                           + Thread.currentThread().getName());
                  }
              })
        ;
    }


    private void orchestration_1() {
        final ExecutorService executor = new ThreadPoolExecutor(4, 4, 1, TimeUnit.MINUTES, new LinkedBlockingQueue<Runnable>());
        try {

            Future<String> f1 = executor.submit(new CallToRemoteServiceA());
            Observable<String> f1Observable = Observable.from(f1);
            Observable<String> f3Observable = f1Observable
                  .flatMap(new Func1<String, Observable<String>>() {
                      @Override
                      public Observable<String> call(String s) {
                          _log("Observed from f1: " + s);
                          Future<String> f3 = executor.submit(new CallToRemoteServiceC(s));
                          return Observable.from(f3);
                      }
                  });

            Future<Integer> f2 = executor.submit(new CallToRemoteServiceB());
            Observable<Integer> f2Observable = Observable.from(f2);
            Observable<Integer> f4Observable = f2Observable
                  .flatMap(new Func1<Integer, Observable<Integer>>() {
                      @Override
                      public Observable<Integer> call(Integer integer) {
                          _log("Observed from f2: " + integer);
                          Future<Integer> f4 = executor.submit(new CallToRemoteServiceD(integer));
                          return Observable.from(f4);
                      }
                  });

            Observable<Integer> f5Observable = f2Observable
                  .flatMap(new Func1<Integer, Observable<Integer>>() {
                      @Override
                      public Observable<Integer> call(Integer integer) {
                          _log("Observed from f2: " + integer);
                          Future<Integer> f5 = executor.submit(new CallToRemoteServiceE(integer));
                          return Observable.from(f5);
                      }
                  });

            Observable.zip(f3Observable, f4Observable, f5Observable, new Func3<String, Integer, Integer, Map<String, String>>() {
                @Override
                public Map<String, String> call(String s, Integer integer, Integer integer2) {
                    Map<String, String> map = new HashMap<String, String>();
                    map.put("f3", s);
                    map.put("f4", String.valueOf(integer));
                    map.put("f5", String.valueOf(integer2));
                    return map;
                }
            }).subscribe(new Action1<Map<String, String>>() {
                @Override
                public void call(Map<String, String> map) {
                    _log(map.get("f3") + " => " + (Integer.valueOf(map.get("f4")) * Integer.valueOf(map.get("f5"))));
                }
            });

        } finally {
            executor.shutdownNow();
        }
    }


    // -----------------------------------------------------------------------------------

    private static final class CallToRemoteServiceA implements Callable<String> {
        @Override
        public String call() throws Exception {
            print("A called");
            // simulate fetching data from remote service
            Thread.sleep(100);
            return "responseA";
        }
    }

    private static final class CallToRemoteServiceB implements Callable<Integer> {
        @Override
        public Integer call() throws Exception {
            print("B called");
            // simulate fetching data from remote service
            Thread.sleep(40);
            return 100;
        }
    }

    private static final class CallToRemoteServiceC implements Callable<String> {

        private final String dependencyFromA;

        public CallToRemoteServiceC(String dependencyFromA) {
            this.dependencyFromA = dependencyFromA;
        }

        @Override
        public String call() throws Exception {
            print("C called");
            // simulate fetching data from remote service
            Thread.sleep(60);
            return "responseB_" + dependencyFromA;
        }
    }

    private static final class CallToRemoteServiceD implements Callable<Integer> {

        private final Integer dependencyFromB;

        public CallToRemoteServiceD(Integer dependencyFromB) {
            this.dependencyFromB = dependencyFromB;
        }

        @Override
        public Integer call() throws Exception {
            print("D called");
            // simulate fetching data from remote service
            Thread.sleep(140);
            return 40 + dependencyFromB;
        }
    }

    private static final class CallToRemoteServiceE implements Callable<Integer> {

        private final Integer dependencyFromB;

        public CallToRemoteServiceE(Integer dependencyFromB) {
            this.dependencyFromB = dependencyFromB;
        }

        @Override
        public Integer call() throws Exception {
            print("E called");
            // simulate fetching data from remote service
            Thread.sleep(55);
            return 5000 + dependencyFromB;
        }
    }

    // -----------------------------------------------------------------------------------
    private static final Random rand = new Random();

    public static void waitSleep() {
        try {
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public int intenseCalculation(int i) {
        try {
            int time = randInt(1, 5);
            _log("Calculating " + i + " during " + time + "s on " + Thread.currentThread().getName());
            Thread.sleep(time * 1000);
            return i;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static int randInt(int min, int max) {
        return rand.nextInt((max - min) + 1) + min;
    }

    // -----------------------------------------------------------------------------------

    private void _setupLogger() {
        _logs = new ArrayList<>();
        _adapter = new LogAdapter(getActivity(), new ArrayList<String>());
        _logList.setAdapter(_adapter);
    }

    private void _log(String logMsg) {
        _logs.add(logMsg);
        System.out.println(logMsg);

        // You can only do below stuff on main thread.
        new Handler(getMainLooper()).post(new Runnable() {

            @Override
            public void run() {
                _adapter.clear();
                _adapter.addAll(_logs);
            }
        });
    }

    private static void print(String event) {
        EventBus.getDefault().post(event);
    }


}
