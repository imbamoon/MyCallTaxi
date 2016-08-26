package com.zyw.calltaxi.socketclient;

import android.util.Log;

import com.zyw.calltaxi.api.OnMessageReceivedListener;
import com.zyw.calltaxi.utils.LogHelper;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;

/**
 * Created by zyw on 2015/9/20.
 */
public class TaxiRequestHandler extends IoHandlerAdapter {
    private static final String TAG = "TaxiRequestHandler";

    /**
     * 捕捉异常
     * @param session
     * @param cause
     * @throws Exception
     */
    @Override
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
        Log.e(TAG, cause.getMessage(), cause);
        super.exceptionCaught(session, cause);
    }

    /**
     * 接受服务端消息
     * @param session
     * @param message
     * @throws Exception
     */
    @Override
    public void messageReceived(IoSession session, Object message) throws Exception {
        super.messageReceived(session, message);
        //接受服务端的消息
        Log.d(TAG, (String) message);
        String msg = (String) message;
        //用"://"分割字符串
        String[] results = msg.split("://");
        //请求类型
        String requestType = results[0];
        //细节
        String detail = results[1];
        //根据action判断消息的类型
        switch (requestType) {
            //司机接单
            case "driver_accept":
                if (mListener != null) {
                    mListener.onDriverAccept(detail);
                }
                break;
            //司机状态更新
            case "update_driver":
                if (mListener != null) {
                    mListener.onUpdateDriver(detail);
                }
                break;
            //司机下线
            case "driver_offline":
                if (mListener != null){
                    mListener.onDriverOffline(detail);
                }
                break;
            //未知的请求类型
            default:
                LogHelper.d(TAG, "unknown request type:" + requestType);
                break;
        }
    }


    private OnMessageReceivedListener mListener;

    /**
     * 注册接收服务端司机信息的监听器
     * @param listener
     */
    public void setOnMessageReceivedListener(OnMessageReceivedListener listener) {
        mListener = listener;
    }
}
