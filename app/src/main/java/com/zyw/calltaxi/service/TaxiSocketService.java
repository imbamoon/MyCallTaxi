package com.zyw.calltaxi.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.zyw.calltaxi.api.OnDriverUpdateCallback;
import com.zyw.calltaxi.api.OnMessageReceivedListenerAdapter;
import com.zyw.calltaxi.model.Destination;
import com.zyw.calltaxi.model.Message;
import com.zyw.calltaxi.socketclient.TaxiSocketClient;
import com.zyw.calltaxi.ui.LoginActivity;
import com.zyw.calltaxi.ui.WaitingActivity;
import com.zyw.calltaxi.utils.LogHelper;

/**
 * Created by zyw on 2015/11/9.
 * socket通信的service
 */
public class TaxiSocketService extends Service {
    private static final String TAG = LogHelper.makeLogTag(TaxiSocketService.class);

    public static final String EXTRA_DRIVER_NAME = "extra_driver_name";//司机姓名intent键

    public static final String EXTRA_DRIVER_PHONE_NUMBER = "extra_driver_phone_number";//司机电话号intent键

    private TaxiSocketClient mSocketClient;//socket通信客户端

    private TaxiSocketServiceBinder mBinder;//service与activity通信的通道

    private boolean isWaitingOrdered = false;//是否在等待订单

    private OnDriverUpdateCallback mCallback;//司机更新信息的回调

    /**
     * TaxiSocketService与Activity绑定成功要做的工作，工作内容是开始监听socket连接：超时、成功的事件
     * @param intent
     * @return
     */
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        LogHelper.d(TAG, "service onBind");
        mBinder = new TaxiSocketServiceBinder();//service与activity通信的通道
        String name = intent.getStringExtra(LoginActivity.EXTRA_NAME);//获取乘客登陆姓名
        String phoneNumber = intent.getStringExtra(LoginActivity.EXTRA_PHONE_NUMBER);//获取乘客登陆电话号
        mSocketClient = new TaxiSocketClient(name,phoneNumber,new TaxiSocketClient.Callback() {//实例化socket通信客户端对象，传入参数实现了Callback接口（其实是new了一个实现Callback接口的匿名内部类，并不是实例化Callback接口，接口不能实例化）
            /**
             * 当socket连接超时时要做的工作，工作内容是打印超时日志
             */
            @Override
            public void onTimeOut() {
                LogHelper.d(TAG, "onTimeOut");
            }

            /**
             * 当socket连接成功时要做的工作，工作内容就是注册监听器OnMessageReceivedListener：司机接单、司机更新、司机下线事件
             */
            @Override
            public void onConnected() {
                //当连接成功后，先让用户登陆
                mBinder.passengerLogin();
                //注册服务端消息接收监听器，实现司机状态改变
                mSocketClient.getHandler().setOnMessageReceivedListener(new OnMessageReceivedListenerAdapter(){

                    /**
                     * 当司机接单时要做的工作，工作内容是发送Broadcast
                     * @param msg
                     */
                    @Override
                    public void onDriverAccept(String msg) {//msg包含司机姓名和电话号
                        super.onDriverAccept(msg);
                        LogHelper.d(TAG,"onDriverAccept");
                        Message message = new Message(msg);
                        Intent intent = new Intent(WaitingActivity.TaxiOrderReceiver.ACTION_DRIVER_ACCEPT);
                        intent.putExtra(EXTRA_DRIVER_NAME,message.getDriverName());
                        intent.putExtra(EXTRA_DRIVER_PHONE_NUMBER,message.getDriverPhoneNumber());
                        sendBroadcast(intent);
                    }

                    /**
                     * 司机更新时要做的工作，工作内容是
                     * @param message
                     */
                    @Override
                    public void onUpdateDriver(String message) {
                        super.onUpdateDriver(message);
                        if (mCallback != null) {//若OnDriverUpdateCallback已被MapActivity实现
                            mCallback.onUpdateDriver(message);//执行MapActivity实现的onUpdateDriver方法，把message回调给MapActivity
                        }
                    }


                    @Override
                    public void onDriverOffline(String message) {
                        if (mCallback != null){//若OnDriverUpdateCallback已被MapActivity实现
                            mCallback.onDriverOffline(message);//执行MapActivity实现的onDriverOffline方法，把message回调给MapActivity
                        }
                    }
                });
            }
        });
        mSocketClient.init();
        return mBinder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        disconnect();
    }

    private void disconnect() {
        mSocketClient.disconnect();
    }

    /**
     * Socket与Activity通信的通道
     */
    public class TaxiSocketServiceBinder extends Binder {

        /**
         * 乘客登陆
         */
        public void passengerLogin(){
            mSocketClient.passengerLogin();
        }

        /**
         * 设置司机更新的回调
         * @param callback
         */
        public void setOnDriverUpdateCallback(OnDriverUpdateCallback callback) {
            mCallback = callback;
        }

        /**
         * 移除所有的回调
         */
        public void removeCallback(){
            mCallback = null;
        }

        /**
         * 乘客更新位置
         * @param location 经纬度数组
         */
        public void updateLocation(double[] location){
            mSocketClient.updateLocation(location);
        }

        /**
         * 乘客叫车
         * @param destination 目的地
         */
        public void callTaxi(Destination destination) {
            isWaitingOrdered = true;
            mSocketClient.callTaxi(destination);
        }

        /**
         * 乘客取消叫车
         */
        public void cancelCall() {
            if (isWaitingOrdered) {
                isWaitingOrdered = false;
                mSocketClient.cancelCall();
            }
        }
    }
}
