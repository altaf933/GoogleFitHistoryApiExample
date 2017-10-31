package com.fit.steps;

import android.app.Application;

import com.apimanager.GoogleApiManager;

/**
 * Created by altafshaikh on 31/10/17.
 */

public class FitApplication extends Application {

    private GoogleApiManager mGoogleApiManager;
    private static FitApplication mInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        mGoogleApiManager = new GoogleApiManager(mInstance);
    }

    public static synchronized FitApplication getInstance() {
        return mInstance;
    }

    public GoogleApiManager getGoogleApiMangerInstance() {
        return this.mGoogleApiManager;
    }

    public static GoogleApiManager getGoogleApiManager() {
        return getInstance().getGoogleApiMangerInstance();
    }
}
