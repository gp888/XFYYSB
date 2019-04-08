package com.gp.xfyysb;

import android.app.Application;

import com.iflytek.cloud.SpeechUtility;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        SpeechUtility.createUtility(this, "appid=5aebcbd4");
    }
}
