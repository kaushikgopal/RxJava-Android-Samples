package com.morihacky.android.rxjava.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import com.morihacky.android.rxjava.R;
import com.morihacky.android.rxjava.retrofit.GithubApi;
import com.morihacky.android.rxjava.retrofit.User;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.ArrayList;

import retrofit.GsonConverterFactory;
import retrofit.Retrofit;
import retrofit.RxJavaCallAdapterFactory;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static android.text.TextUtils.isEmpty;
import static java.lang.String.format;

public class RetrofitAsyncTaskDeathFragment
      extends Fragment {

    @Bind(R.id.btn_demo_retrofit_async_death_username) EditText _username;
    @Bind(R.id.log_list) ListView _resultList;

    private GithubApi _api;
    private ArrayAdapter<String> _adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _api = _createGithubApi();
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View layout = inflater.inflate(R.layout.fragment_retrofit_async_task_death,
              container,
              false);
        ButterKnife.bind(this, layout);

        _adapter = new ArrayAdapter<>(getActivity(),
              R.layout.item_log,
              R.id.item_log,
              new ArrayList<String>());
        //_adapter.setNotifyOnChange(true);
        _resultList.setAdapter(_adapter);

        return layout;
    }

    @OnClick(R.id.btn_demo_retrofit_async_death)
    public void onGetGithubUserClicked() {
        _adapter.clear();

        /*new AsyncTask<String, Void, User>() {
            @Override
            protected User doInBackground(String... params) {
                return _api.getUser(params[0]);
            }

            @Override
            protected void onPostExecute(User user) {
                _adapter.add(format("%s  = [%s: %s]", _username.getText(), user.name, user.email));
            }
        }.execute(_username.getText().toString());*/

        _api.user(_username.getText().toString())
              .subscribeOn(Schedulers.io())
              .observeOn(AndroidSchedulers.mainThread())
              .subscribe(new Observer<User>() {
                  @Override
                  public void onCompleted() {
                  }

                  @Override
                  public void onError(Throwable e) {
                  }

                  @Override
                  public void onNext(User user) {
                      _adapter.add(format("%s  = [%s: %s]",
                            _username.getText(),
                            user.name,
                            user.email));
                  }
              });
    }

    // -----------------------------------------------------------------------------------

    private GithubApi _createGithubApi() {
        Retrofit.Builder builder = new Retrofit.Builder()
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl("https://api.github.com");

        final String githubToken = getResources().getString(R.string.github_oauth_token);

        if (!TextUtils.isEmpty(githubToken)) {
            OkHttpClient client = new OkHttpClient();
            client.interceptors().add(new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {
                    Request request = chain.request();
                    Request newReq = request.newBuilder()
                            .addHeader("Authorization", format("token %s", githubToken))
                            .build();
                    return chain.proceed(newReq);
                }
            });
            builder.client(client);
        }

        return builder.build().create(GithubApi.class);
    }

    // -----------------------------------------------------------------------------------

    private class GetGithubUser
          extends AsyncTask<String, Void, User> {

        @Override
        protected User doInBackground(String... params) {
            return _api.getUser(params[0]);
        }

        @Override
        protected void onPostExecute(User user) {
            _adapter.add(format("%s  = [%s: %s]", _username.getText(), user.name, user.email));
        }
    }
}
