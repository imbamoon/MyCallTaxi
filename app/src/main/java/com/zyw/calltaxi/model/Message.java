package com.zyw.calltaxi.model;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by zyw on 2015/11/9.
 * 构造socket通信信息的类
 * 负责解析json数据
 */
public class Message {

    private String mMessageStr;

    private JSONObject mJsonMsg;

    public Message() {

    }

    /**
     * 构造函数传入字符串jsonMsg转化为JSON对象mJsonMsg
     * @param jsonMsg
     */
    public Message(String jsonMsg) {
        try {
            mJsonMsg = new JSONObject(jsonMsg);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 从JSON对象中获得driverName
     * @return
     */
    public String getDriverName() {
        String name = null;
        try {
            name = mJsonMsg.getString("driverName");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return name;
    }

    /**
     * 从JSON对象中获得driverPhoneNumber
     * @return
     */
    public String getDriverPhoneNumber() {
        String phoneNumber = null;
        try {
            phoneNumber = mJsonMsg.getString("driverPhoneNumber");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return phoneNumber;
    }

    /**
     * 从JSON对象中获得passengerName
     * @return
     */
    public String getPassengerName(){
        String name = null;
        try{
            name = mJsonMsg.getString("passengerName");
        }catch (JSONException e){
            e.printStackTrace();
        }
        return name;
    }

    /**
     * 从JSON对象中获得passengerPhoneNumber
     * @return
     */
    public String getPassengerPhoneNumber(){
        String phoneNumber = null;
        try {
            phoneNumber = mJsonMsg.getString("passengerPhoneNumber");
        }catch (JSONException e){
            e.printStackTrace();
        }
        return phoneNumber;
    }

    /**
     * 从JSON对象中获得JSON对象location，再构造成double[]
     * @return
     */
    public double[] getLocation() {
        double[] latLng = new double[2];
        try {
            JSONObject location = mJsonMsg.getJSONObject("location");
            latLng[0] = location.getDouble("lat");
            latLng[1] = location.getDouble("lng");
            return latLng;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 从JSON对象中获得JSON对象destination，再构造成Destination对象
     * @return
     */
    public Destination getDestination() {
        JSONObject destJson = null;
        Destination destination = null;
        try {
            destJson = mJsonMsg.getJSONObject("destination");
            destination = new Destination();
            destination.setDetailAdress(destJson.getString("detailAdress"));//具体地址
            destination.setLocation(new double[]{destJson.getDouble("destLat"),
                    destJson.getDouble("destLng")});//经纬度数组
            destination.setDistance(destJson.getDouble("distance"));//距离

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return destination;
    }

    /**
     * 设置JSON对象
     * @param jsonMsg
     */
    public void setJsonMsg(JSONObject jsonMsg) {
        mJsonMsg = jsonMsg;
    }

    /**
     * 设置mMessageStr
     * @param messageStr
     */
    public void setMessageStr(String messageStr) {
        mMessageStr = messageStr;
    }

    /**
     * 成员变量转化为字符串
     * @return
     */
    @Override
    public String toString() {
        return mMessageStr + mJsonMsg.toString();
    }

    /**
     * Message构造器
     */
    public static class MessageBuilder {
        private final JSONObject J;
        private String type;

        /**
         * 构造函数实例化J
         */
        public MessageBuilder() {
            J = new JSONObject();
        }

        /**
         * 设置司机名
         * @param name
         * @return
         */
        public MessageBuilder setDriverName(String name) {
            try {
                J.put("driverName", name);
                return this;
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        /**
         * 设置司机电话号
         * @param phoneNumber
         * @return
         */
        public MessageBuilder setDriverPhoneNumber(String phoneNumber) {
            try {
                J.put("driverPhoneNumber", phoneNumber);
                return this;
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        /**
         * 设置乘客名
         * @param name
         * @return
         */
        public MessageBuilder setPassengerName(String name){
            try {
                J.put("passengerName", name);
                return this;
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        /**
         * 设置乘客电话号
         * @param phoneNumber
         * @return
         */
        public MessageBuilder setPassengerPhoneNumber(String phoneNumber){
            try {
                J.put("passengerPhoneNumber", phoneNumber);
                return this;
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        /**
         * 设置乘客位置
         * @param location
         * @return
         */
        public MessageBuilder setLocation(double[] location) {
            JSONObject locationJson = new JSONObject();
            try {
                locationJson.put("lat", location[0]);
                locationJson.put("lng", location[1]);
                J.put("location", locationJson);
                return this;
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        /**
         * 设置目的地
         * @param destination
         * @return
         */
        public MessageBuilder setDestination(Destination destination) {
            try {
                JSONObject destJson = new JSONObject();
                destJson.put("detailAdress", destination.getDetailAdress());
                destJson.put("destLat",destination.getLocation()[0]);
                destJson.put("destLng",destination.getLocation()[1]);
                destJson.put("distance", destination.getDistance());
                J.put("destination", destJson);
                return this;
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        /**
         * 设置type字符串
         * @param requestType
         * @return
         */
        public MessageBuilder setRequestType(String requestType) {
            this.type = requestType;
            return this;
        }

        /**
         * 构造成Message
         * @return
         */
        public Message build() {
            final Message message = new Message();
            message.setJsonMsg(J);
            message.setMessageStr(type + "://");
            return message;
        }
    }
}
