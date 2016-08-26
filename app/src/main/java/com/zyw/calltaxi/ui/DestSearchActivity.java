package com.zyw.calltaxi.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.baidu.mapapi.search.sug.OnGetSuggestionResultListener;
import com.baidu.mapapi.search.sug.SuggestionResult;
import com.baidu.mapapi.search.sug.SuggestionSearch;
import com.baidu.mapapi.search.sug.SuggestionSearchOption;
import com.zyw.calltaxi.R;
import com.zyw.calltaxi.adapter.SuggestionInfoAdapter;
import com.zyw.calltaxi.utils.ToastUtils;

import java.util.List;

/**
 * Created by zyw on 2015/11/8.
 * 目的地搜索
 */
public class DestSearchActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener, OnGetSuggestionResultListener {

    private EditText mEtInputDest;

    private Button mBtnSearchDest;

    private ListView mDestList;

    private SuggestionInfoAdapter mAdapter;

    private SuggestionSearch mSuggestionSearch;

    private String mCity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dest_search);
        initViews();//初始化视图
        mSuggestionSearch = SuggestionSearch.newInstance();//实例化建议搜索类
        mSuggestionSearch.setOnGetSuggestionResultListener(this);//设置获得建议结果的监听器
    }

    private void initViews() {
        mEtInputDest = (EditText) findViewById(R.id.et_input_dest);//输入目的地
        mBtnSearchDest = (Button) findViewById(R.id.btn_search_dest);//搜索目的地的按钮
        mDestList = (ListView) findViewById(R.id.lv_dest);//建议信息列表

        /**
         * 为EditText添加文字改变监听器,传入实现抽象方法的TextWatcher
         */
        mEtInputDest.addTextChangedListener(new TextWatcher() {
            /**
             * 在字符串s中，当从start开始的count个字符，替代了以前的before个字符时，触发的事件
             * @param s
             * @param start
             * @param before
             * @param count
             */
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mSuggestionSearch.requestSuggestion(new SuggestionSearchOption()
                        .keyword(s.toString())
                        .city(getIntent().getStringExtra(MapActivity.EXTRA_CITY)));//建议搜索类调用请求建议方法，设置建议搜索选项的关键字和城市，s就是输入框里的内容
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        mBtnSearchDest.setOnClickListener(this);//设置搜索目的地按钮点击事件
        mDestList.setOnItemClickListener(this);//设置建议信息listview的item点击事件
        mAdapter = new SuggestionInfoAdapter(this);//实例化建议信息适配器
        mDestList.setAdapter(mAdapter);//将建议信息适配器作为建议信息listview的适配器
    }

    /**
     * 设置搜索目的地按钮点击事件
     * @param v
     */
    @Override
    public void onClick(View v) {
        mSuggestionSearch.requestSuggestion(new SuggestionSearchOption()
                .keyword(mEtInputDest.getText().toString())
                .city(getIntent().getStringExtra(MapActivity.EXTRA_CITY)));//建议搜索类调用请求建议方法，配置建议搜索选项的关键字和城市
    }

    /**
     * 设置建议信息listview的item点击事件
     * @param parent
     * @param view
     * @param position
     * @param id
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        SuggestionResult.SuggestionInfo suggestionInfo = mAdapter.getItem(position);//获得点击条目SuggestionInfo
        if (suggestionInfo != null) {
            Intent result = new Intent();
            result.putExtra(MapActivity.EXTRA_DEST, suggestionInfo.key);//关键字
            result.putExtra(MapActivity.EXTRA_LATITUDE, suggestionInfo.pt.latitude);//纬度
            result.putExtra(MapActivity.EXTRA_LONGITUDE, suggestionInfo.pt.longitude);//经度
            setResult(Activity.RESULT_OK, result);//带着关键字和经纬度信息返回MapActivity
            finish();//结束DestSearchActivity
        }else {
            ToastUtils.showToast(this,"搜索出错了，选别的试试吧");
        }
    }

    /**
     * 当获取到建议结果时，将建议结果集合通过adapter添加到listview中
     * @param result
     */
    @Override
    public void onGetSuggestionResult(SuggestionResult result) {

        if (result == null || result.getAllSuggestions() == null) {
            return;
        }
        List<SuggestionResult.SuggestionInfo> suggestions = result.getAllSuggestions();//获得建议集合
        mAdapter.addAll(suggestions);//建议集合添加到listview中
    }
}
