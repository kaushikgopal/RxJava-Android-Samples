package com.morihacky.android.rxjava.retrofit;

import android.text.TextUtils;

import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static java.lang.String.format;

public class GithubService {

  private GithubService() {}

  public static GithubApi createGithubService(final String githubToken) {
    Retrofit.Builder builder =
        new Retrofit.Builder()
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl("https://api.github.com");

    if (!TextUtils.isEmpty(githubToken)) {

      OkHttpClient client =
          new OkHttpClient.Builder()
              .addInterceptor(
                  chain -> {
                    Request request = chain.request();
                    Request newReq =
                        request
                            .newBuilder()
                            .addHeader("Authorization", format("token %s", githubToken))
                            .build();
                    return chain.proceed(newReq);
                  })
              .build();

      builder.client(client);
    }

    return builder.build().create(GithubApi.class);
  }
}
