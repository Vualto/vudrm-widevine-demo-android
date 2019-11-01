package com.vualto.vudrm.widevinedemo.view.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.vualto.vudrm.widevinedemo.BuildConfig;
import com.vualto.vudrm.widevine.data.KidProvider;

/**
 * Created by Adam Gerber on 29/04/2019.
 * <p>
 * Copyright © 2019 Vualto Ltd. All rights reserved.
 */

public class SourceItem implements Parcelable {

    public final static String INTENT_KEY = "SOURCE_ITEM";
    public static final Parcelable.Creator<SourceItem> CREATOR
            = new Parcelable.Creator<SourceItem>() {

        @Override
        public SourceItem createFromParcel(Parcel in) {
            return new SourceItem(in);
        }

        @Override
        public SourceItem[] newArray(int size) {
            return new SourceItem[size];
        }
    };
    public final String name, URL, drmToken, licenseURL;
    public final KidProvider kidProvider;

    private SourceItem(Parcel in) {
        name = in.readString();
        URL = in.readString();
        drmToken = in.readString();
        kidProvider = in.readParcelable(KidProvider.class.getClassLoader());
        licenseURL = in.readString();
    }

    public SourceItem(String name, String URL) {
        this(name, URL, null, null);
    }

    public SourceItem(String name, String URL, String drmToken, KidProvider kidProvider) {
        this(name, URL, drmToken, kidProvider, BuildConfig.WIDEVINE_LICENSE_SERVER_URL);
    }


    public SourceItem(String name, String URL, String drmToken, KidProvider kidProvider, String licenseURL) {
        this.name = name;
        this.URL = URL;
        this.drmToken = drmToken;
        this.kidProvider = kidProvider;
        this.licenseURL = licenseURL;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(name);
        parcel.writeString(URL);
        parcel.writeString(drmToken);
        parcel.writeParcelable(kidProvider, i);
        parcel.writeString(licenseURL);
    }

    @Override
    public String toString() {
        return "SourceItem{" +
                "name='" + name + '\'' +
                ", URL='" + URL + '\'' +
                ", drmToken='" + drmToken + '\'' +
                ", licenseURL='" + licenseURL + '\'' +
                ", kidProvider=" + kidProvider +
                '}';
    }

}
