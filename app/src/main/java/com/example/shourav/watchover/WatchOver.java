package com.example.shourav.watchover;

import android.support.multidex.MultiDexApplication;

public class WatchOver extends MultiDexApplication {
    private static WatchOver sInstance;
    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
    }

    public static WatchOver getInstance() {
        return sInstance;
    }
}
