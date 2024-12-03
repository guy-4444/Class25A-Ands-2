package com.guyi.class25a_ands_2;

import android.app.Application;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        MCT6.initHelper();
    }
}
