package com.zyw.driver.api;

/**
 * Created by zyw on 2015/11/12.
 */
public interface DriverServiceCallback {

    void onUpdatePassenger(String msg);

    void onPassengerTaken(String msg);

    void onPassengerCancel(String msg);

    void onPassengerOffline(String msg);
}
