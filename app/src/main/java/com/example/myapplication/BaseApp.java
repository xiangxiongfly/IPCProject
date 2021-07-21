package com.example.myapplication;

import android.app.Application;

public class BaseApp extends Application {

    public static BaseApp context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
    }
}
