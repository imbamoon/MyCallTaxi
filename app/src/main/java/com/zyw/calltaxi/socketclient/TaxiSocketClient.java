package com.zyw.calltaxi.socketclient;

import com.zyw.calltaxi.model.Destination;
import com.zyw.calltaxi.model.Message;
import com.zyw.calltaxi.utils.LogHelper;

import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;

/**
 * Created by zyw on 2015/9/20.
 * socket通信代理
 */
public class TaxiSocketClient {

    private static final String TAG = "LocationClient";
    /**
     * socketConnector对象
     */
    private NioSocketConnector mConnector;
    /**
     * 处理消息的Handler对象
     */
    private TaxiRequestHandler mHandler;
    /**
     * 客户端连接成功后取得的会话对象
     */
    private IoSession mSession;
    /**
     * 是否连接到服务器端的标志位
     */
    private boolean isConnected = false;
    /**
     * 是否已经初始化完成
     */
    private boolean initiated = false;
    /**
     * 会话是否创建的标志位，用来在第一次的时候给服务端发送客户端的用户信息
     */
    private boolean mSessionCreated = false;
    /**
     * 连接服务器超时的回调
     */
    private Callback mCallback;
    /**
     * 开启socket连接的用户名
     */
    private String mName;
    /**
     * 开启socket连接的手机号
     */
    private String mPhoneNumber;

    public TaxiSocketClient(String name, String phoneNumber, Callback callback) {
        mHandler = new TaxiRequestHandler();//注册处理消息的Handler对象
        mCallback = callback;//注册内部接口CallBack，有onTimeOut和onConnected两个抽象方法需要TaxiSocketService实现
        mName = name;
        mPhoneNumber = phoneNumber;
    }

    /**
     * 初始化客户端
     */
    public void init() {
        //如果已经初始化。不再重复进行
        if (initiated) {
            return;
        }
        new Thread() {
            @Override
            public void run() {
                mConnector = new NioSocketConnector();//实例化SocketConnector
                mConnector.setHandler(mHandler);//给SocketConnector设置消息处理器
                mConnector.getFilterChain().addLast("codec",
                        new ProtocolCodecFilter(new TextLineCodecFactory(Charset.forName("UTF-8"))));//给SocketConnector添加文本按行滤镜
                mConnector.setConnectTimeoutMillis(10000);//给SocketConnector设置10s连接超时
                ConnectFuture future = mConnector.connect(new InetSocketAddress("192.168.1.101", 9988));//通过SocketConnector与特定地址创建未来连接
                future.awaitUninterruptibly();//未来连接等待直到连接成功
                try {
                    mSession = future.getSession();//未来连接成功后取得会话对象
                    isConnected = mSession.isConnected();//socket是否连接成功
                    if (isConnected) {
                        if (mCallback != null) {//如果CallBack接口已被TaxiSocketService实现
                            mCallback.onConnected();//调用被实现的onConnected方法进行socket连接成功后的工作
                        }
                    }
                } catch (Exception e) {
                    reset();
                    if (mCallback != null) {//如果CallBack接口已被TaxiSocketService实现
                        mCallback.onTimeOut();//调用被实现的onTimeOut方法进行socket连接超时后的工作
                    }
                    e.printStackTrace();
                }
            }
        }.start();
        initiated = true;
    }

    /**
     * 重置SocketConnector
     */
    public void reset() {
        mConnector.dispose();
        mConnector = null;
        initiated = false;
    }

    public TaxiRequestHandler getHandler() {
        return mHandler;
    }

    /**
     * 通过socket向服务器发送信息
     * @param msg
     */
    public void sendMessage(Message msg) {
        mSession.write((msg.toString()));//将msg转化成字符串
    }

    /**
     * 乘客位置更新
     * @param location
     */
    public void updateLocation(double[] location) {
        String type = "passenger_update_location";
        Message updateMsg = new Message.MessageBuilder()
                .setRequestType(type)
                .setPassengerName(mName)
                .setPassengerPhoneNumber(mPhoneNumber)
                .setLocation(location)
                .build();
        sendMessage(updateMsg);
    }

    /**
     * 乘客登陆
     */
    public void passengerLogin() {
        String type = "passenger_login";
        Message msg = new Message.MessageBuilder()
                .setRequestType(type)
                .setPassengerName(mName)
                .setPassengerPhoneNumber(mPhoneNumber)
                .build();
        sendMessage(msg);
    }

    /**
     * 乘客叫车
     * @param destination 目的地
     */
    public void callTaxi(Destination destination) {
        Message msg = new Message.MessageBuilder()
                .setRequestType("passenger_call_taxi")
                .setPassengerName(mName)
                .setPassengerPhoneNumber(mPhoneNumber)
                .setDestination(destination)
                .build();
        sendMessage(msg);
    }

    /**
     * 乘客取消叫车
     */
    public void cancelCall() {
        Message msg = new Message.MessageBuilder()
                .setRequestType("passenger_cancel_call")
                .setPassengerName(mName)
                .setPassengerPhoneNumber(mPhoneNumber)
                .build();
        sendMessage(msg);
    }


    /**
     * 关闭客户端连接的方法
     */
    public void disconnect() {
        LogHelper.d(TAG, "disconnect");
        mCallback = null;//将实现的回调
        if (mSession != null && isConnected) {
            Message msg = new Message.MessageBuilder()
                    .setRequestType("disconnect")
                    .setPassengerName(mName)
                    .setPassengerPhoneNumber(mPhoneNumber)
                    .build();
            sendMessage(msg);//向服务端发送关闭连接的请求
            mSession.close(true);
            mConnector.dispose();
        }
    }

    /**
     * 交给TaxiSocketService实现，
     */
    public interface Callback {
        void onTimeOut();//socket连接成功怎么做由TaxiSocketService实现

        void onConnected();//socket连接超时怎么做由TaxiSocketService实现
    }

}
