package com.morihacky.android.rxjava.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import butterknife.ButterKnife;
import butterknife.Bind;
import butterknife.OnClick;
import com.morihacky.android.rxjava.R;
import com.morihacky.android.rxjava.retrofit.GithubApi;
import com.morihacky.android.rxjava.retrofit.User;
import java.util.ArrayList;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
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

        RestAdapter.Builder builder = new RestAdapter.Builder().setEndpoint(
              "https://api.github.com/");
        //.setLogLevel(RestAdapter.LogLevel.FULL);

        final String githubToken = getResources().getString(R.string.github_oauth_token);
        if (!isEmpty(githubToken)) {
            builder.setRequestInterceptor(new RequestInterceptor() {
                @Override
                public void intercept(RequestFacade request) {
                    request.addHeader("Authorization", format("token %s", githubToken));
                }
            });
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
