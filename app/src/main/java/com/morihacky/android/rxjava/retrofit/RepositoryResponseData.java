package com.morihacky.android.rxjava.retrofit;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class RepositoryResponseData {

    @SerializedName("items")
    private List<Repository> mItems;

    public List<Repository> getItems() {
        return mItems;
    }

    public void setItems(List<Repository> mItems) {
        this.mItems = mItems;
    }

    @Override
    public String toString() {
        return "RepositoryResponseData{" +
                "items=" + mItems +
                '}';
    }
}
