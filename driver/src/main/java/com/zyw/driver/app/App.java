package com.zyw.driver.app;

import android.app.Application;

import com.baidu.mapapi.SDKInitializer;

/**
 * Created by zyw on 2015/11/13.
 */
public class App extends Application{

    @Override
    public void onCreate() {
        super.onCreate();
        SDKInitializer.initialize(this);
    }
}
