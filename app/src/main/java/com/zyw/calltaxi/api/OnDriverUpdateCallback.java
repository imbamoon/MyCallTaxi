package com.zyw.calltaxi.api;

/**
 *Created by zyw on 2015/11/12.
        */
public interface OnDriverUpdateCallback {

    void onUpdateDriver(String msg);

    void onDriverOffline(String message);
}
