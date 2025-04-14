package com.example.m_learning_onlinecources;

import android.app.Application;

import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.GooglePlayServicesUtil;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        GoogleApiAvailability.getInstance()
                .isGooglePlayServicesAvailable(this);
    }
}