package com.zyw.calltaxi.ui;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;

import com.baidu.location.BDLocation;
import com.baidu.location.Poi;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;
import com.zyw.calltaxi.R;
import com.zyw.calltaxi.api.OnDriverUpdateCallback;
import com.zyw.calltaxi.map.BaiduMapManager;
import com.zyw.calltaxi.model.Destination;
import com.zyw.calltaxi.model.Driver;
import com.zyw.calltaxi.model.Message;
import com.zyw.calltaxi.service.TaxiSocketService;
import com.zyw.calltaxi.utils.LogHelper;
import com.zyw.calltaxi.utils.ToastUtils;

import java.util.List;

public class MapActivity extends AppCompatActivity implements BaiduMapManager.OnLocationUpdateListener, View.OnClickListener {
    private static final String TAG = LogHelper.makeLogTag(MapActivity.class);

    private static final int REQUEST_GET_DEST = 0x1001;//获取目的地请求码
    private static final int REQUEST_WAITING_ORDER = 0x1002;//等待订单请求码

    public static final String EXTRA_CITY = "extra_city";//城市intent键
    public static final String EXTRA_DEST = "destination_key";//目的地关键字intent键
    public static final String EXTRA_LATITUDE = "extra_latitude";//纬度intent键
    public static final String EXTRA_LONGITUDE = "extra_longitude";//经度intent键

    protected MapView mMapView;//地图视图

    protected BaiduMapManager mMapManager;//地图管理员

    protected Button mBtnDestInPut;//目的地输入按钮

    protected Button mBtnCallTaxi;//叫车按钮

    private String mCity;//城市

    private ServiceConnection mSocketServiceConnection;//ServiceConnection接口

    private TaxiSocketService.TaxiSocketServiceBinder mServiceBinder = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_baidu_map);
        initViews();
        initBaiduMap();
        prepareService();
    }

    private void prepareService() {
        final String name = getIntent().getStringExtra(LoginActivity.EXTRA_NAME);
        final String phoneNumber = getIntent().getStringExtra(LoginActivity.EXTRA_PHONE_NUMBER);
        /**
         * 实现Service连接接口，实现抽象方法
         */
        mSocketServiceConnection = new ServiceConnection() {
            /**
             * 当ServiceConnected时执行的工作，工作内容是实现OnDriverUpdateCallback，将service获得的driver信息更新在地图上
             * @param name
             * @param service
             */
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                LogHelper.d(TAG, "onServiceConnceted");
                /**
                 * 实现OnDriverUpdateCallback接口，实现抽象方法
                 */
                OnDriverUpdateCallback callback = new OnDriverUpdateCallback() {
                    @Override
                    public void onUpdateDriver(String msg) {
                        updateDriver(msg);
                    }

                    @Override
                    public void onDriverOffline(String message) {
                        driverOffline(message);
                    }
                };//实现OnDriverUpdateCallback接口
                mServiceBinder = (TaxiSocketService.TaxiSocketServiceBinder) service;//获得与service通信的通道IBinder
                mServiceBinder.setOnDriverUpdateCallback(callback);//把已实现的OnDriverUpdateCallback接口set给TaxiSocketService
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mServiceBinder.removeCallback();
                mServiceBinder = null;
            }
        };
        Intent intent = new Intent(this, TaxiSocketService.class);
        intent.putExtra(LoginActivity.EXTRA_NAME, name);
        intent.putExtra(LoginActivity.EXTRA_PHONE_NUMBER, phoneNumber);
        bindService(intent, mSocketServiceConnection, BIND_AUTO_CREATE);
    }

    /**
     * 当监听到DriverOffline时执行的工作，工作内容是从msg获取driver的电话号，通过电话号在地图上移除driver
     *
     * @param msg
     */
    private void driverOffline(String msg) {
        Message message = new Message(msg);
        mMapManager.removeDriver(message.getDriverPhoneNumber());
    }

    /**
     * 当监听到UpdateDriver时执行的工作，工作内容是从msg获取driver的姓名、电话号、位置，并更新在地图上
     *
     * @param msg
     */
    private void updateDriver(String msg) {
        Message message = new Message(msg);
        Driver driver = new Driver(message.getDriverName(), message.getDriverPhoneNumber());
        driver.setLocation(message.getLocation());
        mMapManager.showDriversOnMap(driver);
    }

    private void initBaiduMap() {
        mMapManager = new BaiduMapManager(this, mMapView);//获取地图管理员
        mMapManager.registerLocationListener(this);//注册定位监听器
    }

    /**
     * 初始化视图
     */
    private void initViews() {
        mMapView = (MapView) findViewById(R.id.bMapView);//地图视图
        mBtnDestInPut = (Button) findViewById(R.id.btn_dest_input);//目的地输入按钮
        mBtnCallTaxi = (Button) findViewById(R.id.btn_call_taxi);//约车按钮

        mBtnDestInPut.setOnClickListener(this);
        mBtnCallTaxi.setOnClickListener(this);
    }

    private double mCurrentLat = -1;//初始化当前纬度
    private double mCurrentLng = -1;//初始化当前经度

    @Override
    public void onLocationUpdate(BDLocation bdLocation) {
        mCity = bdLocation.getCity();
//        List<Poi> poiList = bdLocation.getPoiList();
//        Poi poi = poiList.get(0);

        mCurrentLat = bdLocation.getLatitude();
        mCurrentLng = bdLocation.getLongitude();
        if (mServiceBinder != null) {
            LogHelper.d(TAG, "updateLocation");
            mServiceBinder.updateLocation(new double[]{mCurrentLat, mCurrentLng});
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapManager.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapManager.onResume();
        mMapManager.init();
        mMapManager.requestLocation();
        LogHelper.d(TAG, "requestLocation");
    }

    @Override
    protected void onDestroy() {
        unbindService(mSocketServiceConnection);
        mMapManager.onDestroy();
        mSocketServiceConnection = null;
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_dest_input:
                Intent intent = new Intent(this, DestSearchActivity.class);
                if (mCity != null) {
                    intent.putExtra(EXTRA_CITY, mCity);
                }
                startActivityForResult(intent, REQUEST_GET_DEST);
                break;
            case R.id.btn_call_taxi:
                //乘客未定位成功
                if (mCurrentLat <= 0 || mCurrentLng <= 0) {
                    ToastUtils.showToast(this, "等待定位...");
                    return;
                }
                //目的地未确定
                if (mDestAdress == null ||
                        TextUtils.isEmpty(mDestAdress) ||
                        mDestLocation[0] <= 0 ||
                        mDestLocation[1] <= 0) {
                    ToastUtils.showToast(this, "请先选择目的地");
                    return;
                }
                Destination destination = new Destination();
                destination.setDetailAdress(mDestAdress);//目的地地址
                destination.setLocation(mDestLocation);//目的地坐标
                double distance = DistanceUtil.getDistance(new LatLng(mCurrentLat, mCurrentLng),
                        new LatLng(mDestLocation[0], mDestLocation[1]));//当前坐标和目的地坐标距离
                destination.setDistance(distance);
                mServiceBinder.callTaxi(destination);
                startActivityForResult(new Intent(MapActivity.this, WaitingActivity.class), REQUEST_WAITING_ORDER);
                break;
        }
    }

    private double[] mDestLocation = new double[2];
    private String mDestAdress = null;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_GET_DEST:
                    if (data != null) {
                        mDestAdress = data.getStringExtra(EXTRA_DEST);
                        mBtnDestInPut.setText("目的地：" + mDestAdress);
                        mDestLocation[0] = data.getDoubleExtra(EXTRA_LATITUDE, -1);
                        mDestLocation[1] = data.getDoubleExtra(EXTRA_LONGITUDE, -1);
                    }
                    break;
                case REQUEST_WAITING_ORDER:
                    if (mServiceBinder != null) {
                        LogHelper.d(TAG, "passenger login again");
                        mServiceBinder.passengerLogin();
                    }
                    break;
            }

        }
    }

}
