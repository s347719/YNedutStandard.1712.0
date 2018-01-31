package com.yineng.ynmessager.activity.settings;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.yineng.ynmessager.R;
import com.yineng.ynmessager.activity.BaseActivity;
import com.yineng.ynmessager.adapter.DownloadedFilesSrarchAdapter;
import com.yineng.ynmessager.bean.app.DownLoadFile;
import com.yineng.ynmessager.db.DownLoadFileTb;
import com.yineng.ynmessager.util.InputUtil;
import com.yineng.ynmessager.view.EmptySupportRecyclerView;
import com.yineng.ynmessager.view.recyclerview.OnItemClickListener;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class DownLoadFilesSearchActivity extends BaseActivity implements View.OnClickListener, TextWatcher, OnItemClickListener {


    private View search_back;
    private EditText search_text;
    private EmptySupportRecyclerView recyclerview;
    private View view_search;
    private DownloadedFilesSrarchAdapter mAdapter;
    private ArrayList<DownLoadFile> dataList = new ArrayList<>();//用于展示的数据
    private DownLoadFileTb downLoadFileTb;
    private TextView search_result;
    private View view_result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_down_load_files_search);
        downLoadFileTb = new DownLoadFileTb(this);

        initView();


    }

    private void initView(){
        search_back = findViewById(R.id.search_back);
        search_text = (EditText) findViewById(R.id.search_text);
        view_search = findViewById(R.id.view_search);
        recyclerview = (EmptySupportRecyclerView) findViewById(R.id.recyclerview);
        recyclerview.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new DownloadedFilesSrarchAdapter(this,dataList);
        mAdapter.setOnItemClickListener(this);
        recyclerview.setAdapter(mAdapter);
        search_result = (TextView) findViewById(R.id.search_result);
        view_result = findViewById(R.id.view_result);
        view_result.setVisibility(View.GONE);
        search_back.setOnClickListener(this);
        view_search.setOnClickListener(this);
        search_text.addTextChangedListener(this);

        //延迟弹出键盘
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                search_text.setFocusable(true);
                search_text.setFocusableInTouchMode(true);
                search_text.requestFocus();
                InputUtil.ShowKeyboard(search_text);
            }
        }, 300);

    }


    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.search_back:
                InputUtil.HideKeyboard(search_text);
                finish();
                break;

            case R.id.view_search:
                String s = search_text.getText().toString();
                if (StringUtils.isEmpty(s)) {
                    return;
                }
                queryDataByString(s);
                break;

        }

    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        String name = s.toString();
        if (StringUtils.isEmpty(name)) {
            return;
        }
        queryDataByString(name);

    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    /**
     * 根据关键词查询数据库并展示结果
     * @param key
     */
    private void queryDataByString (String key){
        if (view_result.getVisibility()==View.GONE){
            view_result.setVisibility(View.VISIBLE);
        }
        List<DownLoadFile> loadFileList = downLoadFileTb.queryByKeyString(key);
        search_result.setText(getResources().getString(R.string.downloadFiles_serarch_data_size,loadFileList.size()+""));
        mAdapter.setData(loadFileList);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(int position, RecyclerView.ViewHolder viewHolder) {
        //  搜索结果的点击事件
        hideKeyBoard();
        DownLoadFile item = mAdapter.getItem(position);
        Intent bundle = new Intent();
        bundle.putExtra("fileName",item.getFileName());
        setResult(110,bundle);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    /**
     * 隐藏软键盘
     */
    private void hideKeyBoard(){
        //隐藏软键盘
        search_text.setText("");
        search_text.clearFocus();
        InputUtil.HideKeyboard(search_text);
    }
}
