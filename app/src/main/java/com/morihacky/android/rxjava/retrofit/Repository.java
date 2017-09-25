package com.morihacky.android.rxjava.retrofit;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Repository implements Serializable {

    @SerializedName("name")
    private String mName;

    @SerializedName("full_name")
    private String mFullName;

    @SerializedName("owner")
    private Owner mOwner;

    @SerializedName("description")
    private String mDescription;

    @SerializedName("url")
    private String mUrl;

    @SerializedName("size")
    private int mSize;

    @SerializedName("stargazers_count")
    private int mStargazersCount;

    @SerializedName("watchers_count")
    private int mWatchersCount;

    @SerializedName("language")
    private String mLanguage;

    @SerializedName("forks_count")
    private int mForksCount;

    @SerializedName("forks")
    private int mForks;

    @SerializedName("watchers")
    private int mWatchers;

    public String getName() {
        return mName;
    }

    public void setName(String mName) {
        this.mName = mName;
    }

    public String getFullName() {
        return mFullName;
    }

    public void setFullName(String mFullName) {
        this.mFullName = mFullName;
    }

    public Owner getOwner() {
        return mOwner;
    }

    public void setOwner(Owner mOwner) {
        this.mOwner = mOwner;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String mDescription) {
        this.mDescription = mDescription;
    }

    public String getUrl() {
        return mUrl;
    }

    public void setUrl(String mUrl) {
        this.mUrl = mUrl;
    }

    public int getSize() {
        return mSize;
    }

    public void setSize(int mSize) {
        this.mSize = mSize;
    }

    public int getStargazersCount() {
        return mStargazersCount;
    }

    public void setStargazersCount(int mStargazersCount) {
        this.mStargazersCount = mStargazersCount;
    }

    public int getWatchersCount() {
        return mWatchersCount;
    }

    public void setWatchersCount(int mWatchersCount) {
        this.mWatchersCount = mWatchersCount;
    }

    public String getLanguage() {
        return mLanguage;
    }

    public void setLanguage(String mLanguage) {
        this.mLanguage = mLanguage;
    }

    public int getForksCount() {
        return mForksCount;
    }

    public void setForksCount(int mForksCount) {
        this.mForksCount = mForksCount;
    }

    public int getForks() {
        return mForks;
    }

    public void setForks(int mForks) {
        this.mForks = mForks;
    }

    public int getWatchers() {
        return mWatchers;
    }

    public void setWatchers(int mWatchers) {
        this.mWatchers = mWatchers;
    }

    @Override
    public String toString() {
        return "Repository{" +
                "mName='" + mName + '\'' +
                ", mFullName='" + mFullName + '\'' +
                ", mOwner=" + mOwner +
                ", mDescription='" + mDescription + '\'' +
                ", mUrl='" + mUrl + '\'' +
                ", mSize=" + mSize +
                ", mStargazersCount=" + mStargazersCount +
                ", mWatchersCount=" + mWatchersCount +
                ", mLanguage='" + mLanguage + '\'' +
                ", mForksCount=" + mForksCount +
                ", mForks=" + mForks +
                ", mWatchers=" + mWatchers +
                '}';
    }
}