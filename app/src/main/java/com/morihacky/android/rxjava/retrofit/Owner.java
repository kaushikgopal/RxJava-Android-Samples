package com.morihacky.android.rxjava.retrofit;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Owner implements Serializable {

    @SerializedName("login")
    private String mLogin;

    @SerializedName("avatar_url")
    private String mAvatarUrl;

    public String getLogin() {
        return mLogin;
    }

    public void setLogin(String mLogin) {
        this.mLogin = mLogin;
    }

    public String getAvatarUrl() {
        return mAvatarUrl;
    }

    public void setAvatarUrl(String mAvatarUrl) {
        this.mAvatarUrl = mAvatarUrl;
    }

    @Override
    public String toString() {
        return "Owner{" +
                "mLogin='" + mLogin + '\'' +
                ", mAvatarUrl='" + mAvatarUrl + '\'' +
                '}';
    }
}
