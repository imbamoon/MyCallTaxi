package com.zyw.calltaxi.api;

/**
 * Created by zyw on 2015/11/12.
 * 接收服务端司机信息的监听器
 */
public interface OnMessageReceivedListener {

    void onUpdateDriver(String message);

    void onDriverAccept(String message);

    void onDriverOffline(String detail);
}
