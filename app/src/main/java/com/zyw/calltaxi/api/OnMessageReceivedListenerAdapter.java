package com.zyw.calltaxi.api;

/**
 * Created by zyw on 2015/11/12.
 * 接口是不能实例化的，所以setOnMessageReceivedListener()传入的参数应是实现了OnMessageReceivedListener接口的类
 */
public class OnMessageReceivedListenerAdapter implements OnMessageReceivedListener {
    @Override
    public void onUpdateDriver(String message) {}
    @Override
    public void onDriverAccept(String message) {}
    @Override
    public void onDriverOffline(String detail) {}
}
