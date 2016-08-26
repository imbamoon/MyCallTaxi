package com.zyw.driver.api;

/**
 * Created by zyw on 2015/11/12.
 */
public interface OnMessageReceivedListener {

    void onUpdatePassenger(String message);

    void onPassengerTaken(String message);

    void onPassengerCancel(String message);

    void onPassengerOffline(String message);

}
