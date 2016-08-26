package com.zyw.calltaxi.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;
import com.zyw.calltaxi.R;
import com.zyw.calltaxi.utils.LogHelper;
import com.zyw.calltaxi.utils.ToastUtils;

/**
 * Created by zyw on 2015/11/8.
 * 登陆
 */
public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String SP_USER_NAME = "sp_name";//给sharedpreferences的姓名

    public static final String SP_PHONE_NUMBER = "sp_phone_number";//给sharedpreferences的电话号

    public static final String EXTRA_NAME = "extra_name";//给intent的姓名

    public static final String EXTRA_PHONE_NUMBER = "extra_phone_number";//给intent的电话号
    /**
     * //{"passengerName":"zyw","location":{"lat":22.593371,"lng":114.279487}}
     handlePassengerUpdateLocation
      passengerName:zyw update_driver://{"driverName":"我是司机","location":{"lng":114.279458,"lat":22.593403}}
     //{"destination":{"detailAdress":"北京天安门","destLat":22.630564784549,"destLng":113.82012299723381,"distance":47333.79882022791}}
     distance=4.0
     */
    private EditText mEtName;//输入姓名

    private EditText mEtPhoneNumber;//输入电话

    private Button mBtnLogin;//登陆按钮

    private SharedPreferences mSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mSharedPreferences = getSharedPreferences("CallTaxi", Context.MODE_PRIVATE);//获取数据
        initViews();

        debug();

    }

    /**
     * 用一个特例检查代码是否正确
     */
    private void debug() {
        LatLng passenger = new LatLng(22.593371,114.279487);//乘客经纬度
        LatLng driver = new LatLng(22.593403,114.279458);//司机经纬度
        LatLng destination = new LatLng(22.630564784549,113.82012299723381);//目的地经纬度
        double p2d = DistanceUtil.getDistance(passenger, driver);//乘客和司机的距离
        double p2dest = DistanceUtil.getDistance(passenger,destination);//乘客和目的地的距离
        LogHelper.d("LoginActivity", "passenger to driver " + p2d + " passenger to destination " + p2dest);
    }

    /**
     * 初始化视图
     */
    private void initViews() {
        mEtName = (EditText) findViewById(R.id.et_user_name);
        mEtPhoneNumber = (EditText) findViewById(R.id.et_phone_number);

        String userName = mSharedPreferences.getString(SP_USER_NAME,"");//从sharedpreference中获取用户姓名
        String phoneNumber = mSharedPreferences.getString(SP_PHONE_NUMBER,"");//从sharedpreference中获取电话号
        if (!TextUtils.isEmpty(userName)){
            mEtName.setText(userName);//自动显示userName
        }
        if (!TextUtils.isEmpty(phoneNumber)){
            mEtPhoneNumber.setText(phoneNumber);//自动显示phoneNumber
        }
        mBtnLogin = (Button) findViewById(R.id.btn_login);//登陆按钮
        mBtnLogin.setOnClickListener(this);
    }

    /**
     * 登陆按钮点击事件
     * @param view
     */
    @Override
    public void onClick(View view){
        saveUserInfo();//保存用户的信息到SharedPreferences
        login();
        finish();
    }

    /**
     * 保存用户的信息到SharedPreferences
     */
    private void saveUserInfo() {
        SharedPreferences.Editor editor = mSharedPreferences.edit();//获得SharedPreferences编辑器
        editor.putString(SP_USER_NAME, mEtName.getText().toString());//添加用户名键值对
        editor.putString(SP_PHONE_NUMBER,mEtPhoneNumber.getText().toString());//添加电话号键值对
        editor.commit();//提交编辑
    }

    /**
     * 登陆
     */
    private void login() {
        String name = mEtName.getText().toString();
        String phoneNumber = mEtPhoneNumber.getText().toString();
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(phoneNumber)) {
            ToastUtils.showToast(this, "请输入用户名和手机号！");
            return;
        }
        Intent intent = new Intent(this,MapActivity.class);//跳转至MapActivity
        intent.putExtra(EXTRA_NAME,name);//添加姓名键值对
        intent.putExtra(EXTRA_PHONE_NUMBER,phoneNumber);//添加电话号键值对
        startActivity(intent);
    }
}
