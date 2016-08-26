package com.zyw.calltaxi.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.zyw.calltaxi.R;
import com.zyw.calltaxi.service.TaxiSocketService;
import com.zyw.calltaxi.utils.LogHelper;

/**
 * Created by zyw on 2015/11/12.
 */
public class WaitingActivity extends AppCompatActivity {

    private ServiceConnection mServiceConnection;
    private String mDriverPhoneNumber;
    private TextView mTvWaiting;
    private TextView mTvOrdered;
    private Button mBtnCancel;
    private Button mBtnCall;
    private Button mBtnDone;
    private TaxiSocketService.TaxiSocketServiceBinder mBinder = null;
    private TaxiOrderReceiver mReceiver;
    private boolean mStillWaiting = true;
    private Dialog mDoneDialog = null;
    private Dialog mCancelDialog = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiting);
        initViews();
        bindService();
        IntentFilter filter = new IntentFilter();
        filter.addAction(TaxiOrderReceiver.ACTION_DRIVER_ACCEPT);
        mReceiver = new TaxiOrderReceiver();
        registerReceiver(mReceiver, filter);
    }

    private void initViews() {
        mTvWaiting = (TextView) findViewById(R.id.tv_waiting);
        mTvOrdered = (TextView) findViewById(R.id.tv_ordered);
        mBtnCancel = (Button) findViewById(R.id.btn_cancel);
        mBtnDone = (Button) findViewById(R.id.btn_done);
        mBtnCall = (Button) findViewById(R.id.btn_call);
        mBtnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelCall();
            }
        });
    }

    private void cancelCall() {
        if (mBinder != null) {
            mBinder.cancelCall();
            finish();
        }
    }

    private void bindService() {
        mServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mBinder = (TaxiSocketService.TaxiSocketServiceBinder) service;
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mBinder = null;
            }
        };
        Intent intent = new Intent(this, TaxiSocketService.class);
        bindService(intent, mServiceConnection, BIND_AUTO_CREATE);
    }

    /**
     * 点击后退键
     */
    @Override
    public void onBackPressed() {
        if (mStillWaiting) {
            showCancelDialog();//等待司机接单，显示取消订单对话框
        } else {
            showDoneDialog();//司机已经接单，显示离开本页面对话框
        }
    }

    /**
     * 显示订单确认对话框
     */
    private void showDoneDialog() {
        if (mDoneDialog == null) {
            createDoneDialog();//若没有对话框实例则创建对话框
        } else {
            if (mDoneDialog.isShowing()) {
                mDoneDialog.dismiss();//若对话框已经显示则消失
            } else {
                mDoneDialog.show();//若没显示则显示
            }
        }
    }

    /**
     * 显示订单取消对话框
     */
    private void showCancelDialog() {
        if (mCancelDialog == null) {
            createCancelDialog();
        } else {
            if (mCancelDialog.isShowing()){
                mCancelDialog.dismiss();
            }else {
                mCancelDialog.show();
            }
        }
    }

    private void createCancelDialog() {
        mCancelDialog = new AlertDialog.Builder(this)
                .setTitle("取消订单")
                .setMessage("真的要取消约车？")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        cancelCall();
                    }
                })
                .setNegativeButton("不了",null)
                .show();
    }

    private void createDoneDialog() {
        mDoneDialog = new AlertDialog.Builder(this)
                .setTitle("将离开本页面")
                .setMessage("您确定已经记下了司机的信息？\n手机：" + mDriverPhoneNumber)
                .setNegativeButton("取消", null)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setResult(Activity.RESULT_OK);
                        finish();
                    }
                })
                .show();
    }

    /**
     * btnHandlerDone点击事件
     * @param view
     */
    public void btnHandlerDone(View view) {
        showDoneDialog();
    }

    /**
     * btnHandlerCall点击事件
     * @param view
     */
    public void btnHandlerCall(View view) {
        Intent dialIntent = new Intent(Intent.ACTION_VIEW);
        dialIntent.setData(Uri.parse("tel:" + mDriverPhoneNumber));
        startActivity(dialIntent);
    }

    @Override
    protected void onDestroy() {
        unbindService(mServiceConnection);
        mServiceConnection = null;
        unregisterReceiver(mReceiver);
        mReceiver = null;
        super.onDestroy();
    }

    /**
     * 广播接收器，接收司机接单广播
     */
    public class TaxiOrderReceiver extends BroadcastReceiver {

        public static final String ACTION_DRIVER_ACCEPT = "com.zyw.driver.DRIVER_ACCEPT";
        public final String TAG = LogHelper.makeLogTag(TaxiOrderReceiver.class);

        @Override
        public void onReceive(Context context, Intent intent) {
            LogHelper.d(TAG, "broadcast received " + intent.getAction());
            if (intent.getAction().equals(ACTION_DRIVER_ACCEPT)) {
                mStillWaiting = false;
                String driverName = intent.getStringExtra(TaxiSocketService.EXTRA_DRIVER_NAME);
                mDriverPhoneNumber = intent.getStringExtra(TaxiSocketService.EXTRA_DRIVER_PHONE_NUMBER);
                mTvWaiting.setVisibility(View.GONE);//隐藏，不保留控件占有的空间
                mBtnCancel.setVisibility(View.GONE);//取消订单，不可见
                mBtnDone.setVisibility(View.VISIBLE);//订单确认，可见
                mBtnCall.setVisibility(View.VISIBLE);//打电话，可见
                mTvOrdered.setVisibility(View.VISIBLE);//显示司机姓名、电话
                mTvOrdered.setText(getString(R.string.ordered, driverName, mDriverPhoneNumber));
            }
        }
    }
}
