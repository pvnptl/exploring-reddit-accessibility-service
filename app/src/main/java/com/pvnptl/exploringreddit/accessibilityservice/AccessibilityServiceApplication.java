package com.pvnptl.exploringreddit.accessibilityservice;

import android.app.Application;

/**
 * Created by pvnptl on 05/12/16.
 */
public class AccessibilityServiceApplication extends Application {
    private static AccessibilityServiceApplication mInstance;

    public static Application getInstance() {
        return mInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        mInstance = null;
    }
}