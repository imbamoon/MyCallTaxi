package com.zyw.driver.model;

/**
 * Created by zyw on 2015/11/10.
 */
public class Destination {
    /**
     * 目的地详细地址信息
     */
    private String mDetailAdress;
    /**
     * 目的地在地图上的坐标
     */
    private double[] mLocation;
    /**
     * 目的地和乘客当前位置的距离
     */
    private double mDistance;

    public String getDetailAdress() {
        return mDetailAdress;
    }

    public void setDetailAdress(String detailAdress) {
        mDetailAdress = detailAdress;
    }

    public double[] getLocation() {
        return mLocation;
    }

    public void setLocation(double[] location) {
        mLocation = location;
    }

    public double getDistance() {
        return mDistance;
    }

    public void setDistance(double distance) {
        mDistance = distance;
    }
}
