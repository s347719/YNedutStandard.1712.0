//***************************************************************
//*    2015-9-17  上午11:27:32
//*    桌面产品部  贺毅柳
//*    TEL：18608044899
//*    Email：heyiliu@ynedut.cn
//*    成都依能科技有限公司
//*    Copyright© 2004-2015 All Rights Reserved
//*    version 1.0.0.0
//***************************************************************
package com.yineng.ynmessager.activity.settings;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.yineng.ynmessager.R;
import com.yineng.ynmessager.activity.BaseActivity;
import com.yineng.ynmessager.activity.TransmitActivity.TransMsgEntity;
import com.yineng.ynmessager.activity.TransmitActivity.TransmitActivity;
import com.yineng.ynmessager.adapter.DownloadedFilesListAdapter;
import com.yineng.ynmessager.bean.app.DownLoadFile;
import com.yineng.ynmessager.bean.imgpicker.ImageFile;
import com.yineng.ynmessager.bean.p2psession.BaseChatMsgEntity;
import com.yineng.ynmessager.bean.p2psession.MessageBodyEntity;
import com.yineng.ynmessager.bean.p2psession.MessageFileEntity;
import com.yineng.ynmessager.bean.p2psession.MessageImageEntity;
import com.yineng.ynmessager.bean.p2psession.MessageVoiceEntity;
import com.yineng.ynmessager.db.DownLoadFileTb;
import com.yineng.ynmessager.util.FileUtil;
import com.yineng.ynmessager.util.ImageUtil;
import com.yineng.ynmessager.util.SystemUtil;
import com.yineng.ynmessager.util.TimeUtil;
import com.yineng.ynmessager.util.ToastUtil;
import com.yineng.ynmessager.util.ViewHolder;
import com.yineng.ynmessager.util.audio.AudioPlayer;
import com.yineng.ynmessager.view.EmptySupportRecyclerView;
import com.yineng.ynmessager.view.recyclerview.OnItemClickListener;
import com.yineng.ynmessager.view.recyclerview.OnItemLongClickListener;

import org.jivesoftware.smack.packet.Message;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * @author 贺毅柳
 */
public class DownloadedFilesActivity extends BaseActivity implements OnClickListener, OnItemClickListener {
    private View downloadedFiles_top;
    private View downloadedFiles_editor;
    private TextView downloadedFiles_editor_tv;
    private View downloadedFiles_search;
    private View view_switch;
    private View downloadedFiles_select;
    private TextView downloadedFiles_select_tv;
    private TextView title;
    private View download_view_delete;
    private TextView tv_delete;

    private DownLoadFileTb downLoadFileTb;
    //查询出来的所有数据库数据
    private ArrayList<DownLoadFile> queryDatalist = new ArrayList<>();
    //用于展示的数据
    private ArrayList<DownLoadFile> dataList = new ArrayList<>();

    /**
     * 来源类型所属下的布局
     */
    private View view_source;
    private View view_source_alpha;
    private TextView source_all, source_img, source_txt, source_video, source_other, source_chat, source_groupshare;
    /**
     *
     -1 不限 0 图片 1 文档 2影音 3 其他 4 聊天 5 群共享
     */
    private HashMap<Integer, Integer> source_map = new HashMap<>();

    /**
     * 日期所属下面的布局
     */
    private View view_time;
    private View view_time_alpha;
    private TextView time_show, time_before, time_after, time_all;
    private GridView time_list;
    private TimeSelectAdapter timeSelectAdapter;
    /**
     *1 不限
     */
    private HashMap<Integer, Integer> time_map = new HashMap<>();

    //	顶部来源 日期  重置 排序 视图的对象
    private View downloadedFiles_source;
    private View downloadedFiles_time;
    private View downloadedFiles_reset;
    private View downloadedFiles_sort;
    private ImageView image_sort;
    // 是否是正序
    private boolean isDesc = true;
    //是否所有数据被选中
    private boolean isSelectAll = false;

    private View view_list;
    private EmptySupportRecyclerView mRecyclerView;
    private DownloadedFilesListAdapter mAdapter;

    /**
     * 搜索结果返回值
     */
    int REQUEST_CODE = 110;

    private LinearLayoutManager mLinearLayoutManager;
    /**
     * 弹出框
     */
    private View popView;
    private PopupWindow popWindows;
    private DownLoadFile popItem;
    private int top;
    private int topwithswitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_downloaded_files);
        //给来源和日期设置选中条件为默认不限
        source_map.put(-1, -1);
        time_map.put(-1, -1);
        top = SystemUtil.dp2px(this, 48);
        topwithswitch = top + SystemUtil.dp2px(this, 40);
        downLoadFileTb = new DownLoadFileTb(this);
        queryDatalist = downLoadFileTb.query("DESC");
        dataList.addAll(queryDatalist);
        initViews();
    }

    private void initViews() {
        downloadedFiles_top = findViewById(R.id.downloadedFiles_top);
        TextView mTxt_previous = (TextView) findViewById(R.id.downloadedFiles_txt_previous);
        mTxt_previous.setOnClickListener(this);
        title = (TextView) findViewById(R.id.title);
        download_view_delete = findViewById(R.id.download_view_delete);
        tv_delete = (TextView) findViewById(R.id.tv_delete);
        downloadedFiles_editor = findViewById(R.id.downloadedFiles_editor);
        downloadedFiles_editor_tv = (TextView) findViewById(R.id.downloadedFiles_editor_tv);
        downloadedFiles_search = findViewById(R.id.downloadedFiles_search);
        view_switch = findViewById(R.id.view_switch);
        downloadedFiles_select = findViewById(R.id.downloadedFiles_select);
        downloadedFiles_select_tv = (TextView) findViewById(R.id.downloadedFiles_select_tv);

        View mEmptyView = findViewById(R.id.text_empty);
        view_list = findViewById(R.id.view_list);
        mRecyclerView = (EmptySupportRecyclerView) findViewById(R.id.recyclerview);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mAdapter = new DownloadedFilesListAdapter(this, dataList);
        mAdapter.setOnItemClickListener(this);
        mAdapter.setOnLongClickListenet(new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(int position, RecyclerView.ViewHolder viewHolder) {
                popItem = dataList.get(position);
                showPopWindow(popItem);
                return true;
            }
        });
        mRecyclerView.setEmptyTextHint("没有文件");
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setEmptyView(mEmptyView);
        mRecyclerView.addOnScrollListener(new RecyclerViewListener());

        downloadedFiles_source = findViewById(R.id.downloadedFiles_source);
        downloadedFiles_time = findViewById(R.id.downloadedFiles_time);
        downloadedFiles_reset = findViewById(R.id.downloadedFiles_reset);

        downloadedFiles_sort = findViewById(R.id.downloadedFiles_sort);
        downloadedFiles_sort.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isDesc) {
                    getDataByOrder(true);
                    image_sort.setRotation(180);
                    isDesc = false;
                } else {
                    getDataByOrder(false);
                    image_sort.setRotation(360);
                    isDesc = true;
                }
            }
        });
        image_sort = (ImageView) findViewById(R.id.image_sort);

        view_source = findViewById(R.id.view_source);
        view_source_alpha = findViewById(R.id.view_source_alpha);
        view_source.setVisibility(View.GONE);
        source_all = (TextView) findViewById(R.id.source_all);
        setSourceTextBackground(source_all, -1, source_map);
        source_img = (TextView) findViewById(R.id.source_img);
        source_txt = (TextView) findViewById(R.id.source_txt);
        source_video = (TextView) findViewById(R.id.source_video);
        source_other = (TextView) findViewById(R.id.source_other);
        source_chat = (TextView) findViewById(R.id.source_chat);
        source_groupshare = (TextView) findViewById(R.id.source_groupshare);
//		source_promise = (TextView) findViewById(R.id.source_promise);
//		source_oa = (TextView) findViewById(R.id.source_oa);

        view_time = findViewById(R.id.view_time);
        view_time_alpha = findViewById(R.id.view_time_alpha);
        view_time.setVisibility(View.GONE);
        time_show = (TextView) findViewById(R.id.time_show);
        time_show.setText(getResources().getString(R.string.downloadFiles_select_time_show, TimeUtil.getCurrentYear() + ""));
        time_before = (TextView) findViewById(R.id.time_before);
        time_after = (TextView) findViewById(R.id.time_after);
        time_all = (TextView) findViewById(R.id.time_all);
        time_all.setTextColor(ContextCompat.getColor(this,R.color.white));
        time_all.setBackgroundResource(R.drawable.background_blue);
        time_list = (GridView) findViewById(R.id.time_list);
        String[] strings = getResources().getStringArray(R.array.downloadFiles_time_array);
        List<String> list = new ArrayList<>();
        Collections.addAll(list, strings);
        timeSelectAdapter = new TimeSelectAdapter(this, list);
        time_list.setAdapter(timeSelectAdapter);
        time_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // 日期选择的点击事件
                time_all.setTextColor(ContextCompat.getColor(DownloadedFilesActivity.this,R.color.actionBar_bg));
                time_all.setBackgroundResource(R.drawable.background_white_blue_cornor_square);
                time_map.clear();
                time_map.put(position, position);
                timeSelectAdapter.notifyDataSetChanged();
                view_time.setVisibility(View.GONE);
                dataList.clear();
                String time = time_show.getText().toString() + TimeUtil.betterMonthTimeDisplay(position + 1) + "月";
                for (int i = 0; i < queryDatalist.size(); i++) {
                    if (TimeUtil.getDateByMillisecond(Long.parseLong(queryDatalist.get(i).getDataTime()), TimeUtil.FORMAT_DATE3).equals(time)) {
                        dataList.add(queryDatalist.get(i));
                    }
                }
                dataList = sortBySource(dataList);
                mAdapter.setData(dataList);
                mAdapter.notifyDataSetChanged();
            }

        });
        downloadedFiles_editor.setOnClickListener(this);
        downloadedFiles_search.setOnClickListener(this);
        downloadedFiles_select.setOnClickListener(this);

        downloadedFiles_source.setOnClickListener(this);
        downloadedFiles_time.setOnClickListener(this);
        downloadedFiles_reset.setOnClickListener(this);
        source_all.setOnClickListener(this);
        source_img.setOnClickListener(this);
        source_txt.setOnClickListener(this);
        source_video.setOnClickListener(this);
        source_other.setOnClickListener(this);
        source_chat.setOnClickListener(this);
        source_groupshare.setOnClickListener(this);
        time_before.setOnClickListener(this);
        time_after.setOnClickListener(this);
        time_all.setOnClickListener(this);

        tv_delete.setOnClickListener(this);
        view_source_alpha.setOnClickListener(this);
        view_time_alpha.setOnClickListener(this);


    }

    //筛选来源
    private ArrayList<DownLoadFile> sortBySource(ArrayList<DownLoadFile> dataList) {
        int key = -1;
        Set set = source_map.keySet();
        for (Object aSet : set) {
            key = (Integer) aSet;
        }
        switch (key) {
            case -1:
                break;
            case 0:
                dataList = sortDataByType(dataList, 1);
                break;
            case 1:
                Iterator<DownLoadFile> iteratorTxt = dataList.iterator();
                while (iteratorTxt.hasNext()) {
                    DownLoadFile downLoadFile = iteratorTxt.next();
                    if (!(downLoadFile.getFileType() == 4 || downLoadFile.getFileType() == 5 || downLoadFile.getFileType() == 6 || downLoadFile.getFileType() == 7 || downLoadFile.getFileType() == 8)) {
                        iteratorTxt.remove();
                    }
                }
                break;
            case 2:
                Iterator<DownLoadFile> iteratorMoview = dataList.iterator();
                while (iteratorMoview.hasNext()) {
                    DownLoadFile downLoadFile = iteratorMoview.next();
                    if (!(downLoadFile.getFileType() == 2 || downLoadFile.getFileType() == 3)) {
                        iteratorMoview.remove();
                    }
                }
                break;
            case 3:
                dataList = sortDataByType(dataList, 9);
                break;
            case 4:
                dataList = sortDataByChat(dataList);
                break;
            case 5:
                dataList = sortDataByGroupShare(dataList);
                break;
//						case 6:
//							break;
            default:
                break;
        }

        return dataList;
    }

    //筛选根据类型
    private ArrayList<DownLoadFile> sortDataByType(ArrayList<DownLoadFile> dataList, int i) {
        Iterator<DownLoadFile> iterator = dataList.iterator();
        while (iterator.hasNext()) {
            DownLoadFile downLoadFile = iterator.next();
            if (downLoadFile.getFileType() != i) {
                iterator.remove();
            }
        }
        return dataList;
    }

    //筛选聊天
    private ArrayList<DownLoadFile> sortDataByChat(ArrayList<DownLoadFile> dataList) {
        Iterator<DownLoadFile> iterator = dataList.iterator();
        while (iterator.hasNext()) {
            DownLoadFile downLoadFile = iterator.next();
            if (!(downLoadFile.getFileSource() == 0 || downLoadFile.getFileSource() == 1 || downLoadFile.getFileSource() == 2)) {
                iterator.remove();
            }
        }
        return dataList;
    }

    //筛选群共享
    private ArrayList<DownLoadFile> sortDataByGroupShare(ArrayList<DownLoadFile> dataList) {
        Iterator<DownLoadFile> iterator = dataList.iterator();
        while (iterator.hasNext()) {
            DownLoadFile downLoadFile = iterator.next();
            if (downLoadFile.getFileSource() != 3) {
                iterator.remove();
            }
        }
        return dataList;
    }

    /**
     * 根据控件的选择状态改变控件显示状态
     *
     * @param layoutId 点击控件
     * @param position 所属map中的位置
     * @param map      所属map集合
     */
    private void setSourceTextBackground(TextView layoutId, int position, HashMap<Integer, Integer> map) {
        int key = -1;
        Set set = map.keySet();
        for (Object aSet : set) {
            key = (Integer) aSet;
        }
        if (key != position) {
            switch (key) {
                case -1:
                    source_all.setTextColor(ContextCompat.getColor(this,R.color.actionBar_bg));
                    source_all.setBackgroundResource(R.drawable.background_white_blue_cornor_square);
                    break;
                case 0:
                    source_img.setTextColor(ContextCompat.getColor(this,R.color.actionBar_bg));
                    source_img.setBackgroundResource(R.drawable.background_white_blue_cornor_square);
                    break;
                case 1:
                    source_txt.setTextColor(ContextCompat.getColor(this,R.color.actionBar_bg));
                    source_txt.setBackgroundResource(R.drawable.background_white_blue_cornor_square);
                    break;
                case 2:
                    source_video.setTextColor(ContextCompat.getColor(this,R.color.actionBar_bg));
                    source_video.setBackgroundResource(R.drawable.background_white_blue_cornor_square);
                    break;
                case 3:
                    source_other.setTextColor(ContextCompat.getColor(this,R.color.actionBar_bg));
                    source_other.setBackgroundResource(R.drawable.background_white_blue_cornor_square);
                    break;
                case 4:
                    source_chat.setTextColor(ContextCompat.getColor(this,R.color.actionBar_bg));
                    source_chat.setBackgroundResource(R.drawable.background_white_blue_cornor_square);
                    break;
                case 5:
                    source_groupshare.setTextColor(ContextCompat.getColor(this,R.color.actionBar_bg));
                    source_groupshare.setBackgroundResource(R.drawable.background_white_blue_cornor_square);
                    break;
                default:
                    break;
            }
        }

        map.clear();
        map.put(position, position);
        layoutId.setTextColor(ContextCompat.getColor(this,R.color.white));
        layoutId.setBackgroundResource(R.drawable.background_blue);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
//返回键
            case R.id.downloadedFiles_txt_previous:
                if (mAdapter.getSwitchMode()) {
                    RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    p.setMargins(0, topwithswitch, 0, 0);
                    view_list.setLayoutParams(p);
                    view_switch.setVisibility(View.VISIBLE);
                    title.setText(R.string.downloadedFiles_title);
                    downloadedFiles_editor.setVisibility(View.VISIBLE);
                    downloadedFiles_search.setVisibility(View.VISIBLE);
                    downloadedFiles_select.setVisibility(View.GONE);
                    download_view_delete.setVisibility(View.GONE);
                    downloadedFiles_editor_tv.setText(R.string.downloadedFiles_editor);
                    isSelectAll = false;
                    mAdapter.setIsSwitchMode(false);
                    changeDataCheck(false);
                } else {
                    finish();
                }
                break;
//点击编辑
            case R.id.downloadedFiles_editor:
                if (mAdapter.getData().size()>0) {
                    RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    p.setMargins(0, top, 0, SystemUtil.dp2px(this, 48));
                    view_list.setLayoutParams(p);
                    title.setText("已选0/" + dataList.size() + "");
                    view_switch.setVisibility(View.GONE);
                    downloadedFiles_editor.setVisibility(View.GONE);
                    downloadedFiles_search.setVisibility(View.GONE);
                    downloadedFiles_select.setVisibility(View.VISIBLE);
                    download_view_delete.setVisibility(View.VISIBLE);
                    changeDeleteDisplay(false);
                    downloadedFiles_select_tv.setText(R.string.downloadedFiles_editor_all);
                    mAdapter.setIsSwitchMode(true);
                    mAdapter.notifyDataSetChanged();
                }
                break;
//点击全选
            case R.id.downloadedFiles_select:
                if (isSelectAll) {
                    downloadedFiles_select_tv.setText(R.string.downloadedFiles_editor_all);
                    title.setText("已选0/" + dataList.size() + "");
                    isSelectAll = false;
                    changeDataCheck(false);
                    changeDeleteDisplay(false);
                } else {
                    title.setText("已选" + dataList.size() + "" + "/" + dataList.size() + "");
                    downloadedFiles_select_tv.setText(R.string.downloadedFiles_editor_cancle);
                    isSelectAll = true;
                    changeDataCheck(true);
                    changeDeleteDisplay(true);
                }
                break;
//点击删除
            case R.id.tv_delete:
                ArrayList<DownLoadFile> data = mAdapter.getData();
                Iterator<DownLoadFile> iterator = data.iterator();
                while (iterator.hasNext()) {
                    DownLoadFile downLoadFile = iterator.next();
                    if (downLoadFile.isCheck()) {
                        downLoadFileTb.deleteByFileName(downLoadFile.getFileName());
                        FileUtil.delFile(FileUtil.getUserSDPath(false, FileUtil.mFilePath) + "/" + downLoadFile.getFileName());
                        iterator.remove();
                    }
                }
//更新当前数据源
                if (isDesc) {
                    queryDatalist = null;
                    queryDatalist = downLoadFileTb.query("ASC");
                } else {
                    queryDatalist = null;
                    queryDatalist = downLoadFileTb.query("DESC");
                }
                mAdapter.setData(data);
                mAdapter.notifyDataSetChanged();

                break;
//点击搜索
            case R.id.downloadedFiles_search:
                startActivityForResult(new Intent(this, DownLoadFilesSearchActivity.class), REQUEST_CODE);
                reSetData();
                break;
//点击类型来源
            case R.id.downloadedFiles_source:
                view_time.setVisibility(View.GONE);
                view_source.setVisibility(view_source.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);

                break;
//点击来源下面的空白地方
            case R.id.view_source_alpha:
                view_source.setVisibility(View.GONE);
                break;
//点击日期
            case R.id.downloadedFiles_time:
                view_source.setVisibility(View.GONE);
                view_time.setVisibility(view_time.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
                break;
//点击日期视图下面的空白布局
            case R.id.view_time_alpha:
                view_time.setVisibility(View.GONE);
                break;
//点击重置
            case R.id.downloadedFiles_reset:
                //重新去数据库获取初始数据
                queryDatalist = null;
                queryDatalist = downLoadFileTb.query("DESC");
                reSetData();
                break;
//点击来源不限
            case R.id.source_all:
                setSourceTextBackground(source_all, -1, source_map);
                view_source.setVisibility(View.GONE);
                view_source.setVisibility(View.GONE);
                view_time.setVisibility(View.GONE);
                if (!isDesc) {
                    image_sort.setRotation(360);
                    isDesc = true;
                }
                dataList = new ArrayList<>();
                dataList.addAll(queryDatalist);
                sortDataByTimeKey(dataList);
                mAdapter.setData(dataList);
                mAdapter.notifyDataSetChanged();
                break;
//点击来源图片
            case R.id.source_img:
                setSourceTextBackground(source_img, 0, source_map);
                getDataBytype(DownLoadFile.FILETYPE_IMG);
                break;
//点击来源文档
            case R.id.source_txt:
                setSourceTextBackground(source_txt, 1, source_map);
                view_source.setVisibility(View.GONE);
                dataList = new ArrayList<>();
                for (int i = 0; i < queryDatalist.size(); i++) {
                    DownLoadFile file = queryDatalist.get(i);
                    if (file.getFileType() == DownLoadFile.FILETYPE_DOC || file.getFileType() == DownLoadFile.FILETYPE_EXCEL
                            || file.getFileType() == DownLoadFile.FILETYPE_PDF || file.getFileType() == DownLoadFile.FILETYPE_PPT
                            || file.getFileType() == DownLoadFile.FILETYPE_ZIP) {
                        dataList.add(file);
                    }
                }
                sortDataByTimeKey(dataList);
                mAdapter.setData(dataList);
                mAdapter.notifyDataSetChanged();
                break;
//点击来源影音
            case R.id.source_video:
                setSourceTextBackground(source_video, 2, source_map);
                view_source.setVisibility(View.GONE);
                ArrayList<DownLoadFile> data_movie = mAdapter.getData();
                dataList = new ArrayList<>();
                for (int i = 0; i < data_movie.size(); i++) {
                    DownLoadFile file = data_movie.get(i);
                    if (file.getFileType() == DownLoadFile.FILETYPE_MOIVE || file.getFileType() == DownLoadFile.FILETYPE_VOICE) {
                        dataList.add(file);
                    }
                }
                sortDataByTimeKey(dataList);
                mAdapter.setData(dataList);
                mAdapter.notifyDataSetChanged();
                break;
//点击来自其他
            case R.id.source_other:
                setSourceTextBackground(source_other, 3, source_map);
                getDataBytype(DownLoadFile.FILETYPE_OTHER);
                break;
//点击来源聊天
            case R.id.source_chat:
                setSourceTextBackground(source_chat, 4, source_map);
                view_source.setVisibility(View.GONE);
                dataList = new ArrayList<>();
                for (int i = 0; i < queryDatalist.size(); i++) {
                    DownLoadFile file = queryDatalist.get(i);
                    if (file.getFileSource() == DownLoadFile.FILE_SOURCE_PERSON || file.getFileSource() == DownLoadFile.FILE_SOURCE_DIS
                            || file.getFileSource() == DownLoadFile.FILE_SOURCE_GROUP) {
                        dataList.add(file);
                    }
                }
                sortDataByTimeKey(dataList);
                mAdapter.setData(dataList);
                mAdapter.notifyDataSetChanged();

                break;
//点击群共享
            case R.id.source_groupshare:
                setSourceTextBackground(source_groupshare, 5, source_map);
                view_source.setVisibility(View.GONE);
                dataList = new ArrayList<>();
                for (int i = 0; i < queryDatalist.size(); i++) {
                    DownLoadFile file = queryDatalist.get(i);
                    if (file.getFileSource() == DownLoadFile.FILE_SOURCE_SHARED_GROUP) {
                        dataList.add(file);
                    }
                }
                sortDataByTimeKey(dataList);
                mAdapter.setData(dataList);
                mAdapter.notifyDataSetChanged();
                break;
//点击日期往上一年
            case R.id.time_before:
                resetTimeAllVIewTextBack();
                int time1 = Integer.parseInt(time_show.getText().toString().replace("年", ""));
                time1 -= 1;
                time_show.setText(time1 + "年");

                break;
//点击日期往下一年
            case R.id.time_after:
                resetTimeAllVIewTextBack();
                int time2 = Integer.parseInt(time_show.getText().toString().replace("年", ""));
                time2 += 1;
                time_show.setText(time2 + "年");
                break;
//点击日期不限
            case R.id.time_all:
                resetTimeAllVIewTextBack();
                time_show.setText(TimeUtil.getCurrentYear() + "年");

                view_source.setVisibility(View.GONE);
                view_time.setVisibility(View.GONE);
                if (!isDesc) {
                    image_sort.setRotation(360);
                    isDesc = true;
                }
                dataList.clear();
                dataList.addAll(queryDatalist);

                dataList = sortBySource(dataList);

                mAdapter.setData(dataList);
                mAdapter.notifyDataSetChanged();
                break;
//点击弹框的删除
            case R.id.pop_delete:
                popWindows.dismiss();
                File file = new File(FileUtil.getFilePath("") + popItem.getFileName());
                boolean delete = file.delete();
                if (delete) {
                    downLoadFileTb.deleteByFileName(popItem.getFileName());
                    ToastUtil.toastAlerMessageCenter(this, R.string.downloadedFiles_del_success, 500);
                    ArrayList<DownLoadFile> data1 = mAdapter.getData();
                    data1.remove(popItem);
                    mAdapter.setData(data1);
                    mAdapter.notifyDataSetChanged();
                } else {
                    ToastUtil.toastAlerMessageCenter(this, R.string.downloadedFiles_del_failed, 500);
                }

                break;
//点击弹框的转发
            case R.id.pop_transmit:
                popWindows.dismiss();
                loadfileTransmit();

                break;
//点击弹框的预览
            case R.id.pop_scan:
                popWindows.dismiss();
                File item = new File(FileUtil.getFilePath(popItem.getFileName()));
                if (!SystemUtil.execLocalFile(this, item)) {
                    ToastUtil.toastAlerMessageCenter(this, R.string.downloadedFiles_noAppsForThisFile, 500);
                }
                break;
                default:
                    break;
        }
    }

    /**
     * 选择来源的时候同时筛选时间
     *
     * @param data2 筛选的数组
     */
    private void sortDataByTimeKey(ArrayList<DownLoadFile> data2) {
        //得到所选择的的时间下标
        Set<Integer> integers = time_map.keySet();
        Iterator<Integer> iterator1 = integers.iterator();
        int key = iterator1.next();
        Iterator<DownLoadFile> iterator = data2.iterator();
        while (iterator.hasNext()) {
            DownLoadFile downLoadFile = iterator.next();
            if (key == -1) {
                String time = time_show.getText().toString();
                if (!TimeUtil.getDateByMillisecond(Long.parseLong(downLoadFile.getDataTime()), TimeUtil.FORMAT_DATE4).equals(time)) {
                    iterator.remove();
                }
            } else {
                String time2 = time_show.getText().toString() + TimeUtil.betterMonthTimeDisplay(key + 1) + "月";
                if (!TimeUtil.getDateByMillisecond(Long.parseLong(downLoadFile.getDataTime()), TimeUtil.FORMAT_DATE3).equals(time2)) {
                    iterator.remove();
                }
            }
        }
    }

    /**
     * 将日期栏背景从新置为不限选中效果
     */
    private void resetTimeAllVIewTextBack() {
        time_all.setTextColor(ContextCompat.getColor(this,R.color.white));
        time_all.setBackgroundResource(R.drawable.background_blue);
        time_map.clear();
        time_map.put(-1, -1);
        timeSelectAdapter.notifyDataSetChanged();
    }

    /**
     * 点击转发当前文件
     */
    private void loadfileTransmit() {
        boolean canGo = true;
        Intent intent = new Intent(DownloadedFilesActivity.this, TransmitActivity.class);
        //生成传递的对象
        TransMsgEntity transEntity = new TransMsgEntity();
        Message msgs = new Message();
        MessageBodyEntity bodies = new MessageBodyEntity();
        if (popItem.getFileType() == DownLoadFile.FILETYPE_IMG) {
            MessageImageEntity tempImageBeans = new MessageImageEntity();
            ImageFile imageFile = new ImageFile(FileUtil.getFilePath("") + popItem.getFileName());
            String imagePath = imageFile.getPath();
            // 图片
            tempImageBeans.setSdcardPath(imagePath);
            tempImageBeans.setSize(popItem.getSize());
            tempImageBeans.setFileId(popItem.getPacketId());
            // 封装body
            bodies = new MessageBodyEntity();
            bodies.getImages().add(tempImageBeans);
            bodies.setSendName(mApplication.mSelfUser.getUserName());
            bodies.setContent("<img key=\"\">");
            msgs.setBody(JSON.toJSONString(bodies));
        } else if (popItem.getFileType() == DownLoadFile.FILETYPE_DOC || popItem.getFileType() == DownLoadFile.FILETYPE_EXCEL
                || popItem.getFileType() == DownLoadFile.FILETYPE_PDF || popItem.getFileType() == DownLoadFile.FILETYPE_PPT
                || popItem.getFileType() == DownLoadFile.FILETYPE_ZIP) {
            MessageFileEntity fileEntities = new MessageFileEntity();
            //保存文件本地路径
            File fileFile = new File(FileUtil.getFilePath("") + popItem.getFileName());
            fileEntities.setSdcardPath(fileFile.getPath());
            fileEntities.setSize(popItem.getSize());
            fileEntities.setFileId(popItem.getPacketId());
            fileEntities.setName(fileFile.getName());
            bodies.getFiles().add(fileEntities);
            bodies.setContent("<file key=\"\">");
            bodies.setSendName(mApplication.mSelfUser.getUserName());
            msgs.setBody(JSON.toJSONString(bodies));

        } else if (popItem.getFileType() == DownLoadFile.FILETYPE_VOICE) {
            MessageVoiceEntity voiceEntities = new MessageVoiceEntity();
            File fileVoice = new File(FileUtil.getFilePath("") + popItem.getFileName());
            voiceEntities.setId(fileVoice.getName());
            voiceEntities.setTime(AudioPlayer.getDuration(this, fileVoice));
            voiceEntities.setSentSuccess(false);
            bodies.setVoice(voiceEntities);
            bodies.setContent("[语音]");
            bodies.setSendName(mApplication.mSelfUser.getUserName());
            msgs.setBody(JSON.toJSONString(bodies));
        } else {
            canGo = false;
            ToastUtil.toastAlerMessageCenter(this, "文件类型不可转发", 500);
        }
        if (canGo) {
            //因为我们是复用的聊天会话中的转发流程，所以这个参数中发送成功字段统统设置成发送失败，重新发送一次文件到服务器
            transEntity.setIsSuccess(BaseChatMsgEntity.SEND_FAILED);
            transEntity.setMessage(msgs.getBody());
            transEntity.setIsSend(BaseChatMsgEntity.TO_MSG);
            Bundle bundle = new Bundle();
            bundle.putSerializable(TransmitActivity.TransEntity, transEntity);
            intent.putExtras(bundle);
            startActivity(intent);
        }
    }

    @Override
    public void onItemClick(int position, RecyclerView.ViewHolder viewHolder) {
        //   Item的点击事件
        if (mAdapter.getSwitchMode()) {
            DownLoadFile item = dataList.get(position);
            if (item.isCheck()) {
                item.setCheck(false);
            } else {
                item.setCheck(true);
            }
            int select = 0;
            for (int i = 0; i < dataList.size(); i++) {
                if (dataList.get(i).isCheck()) {
                    select += 1;
                }
            }
            changeDeleteDisplay(select > 0);
            isSelectAll = select > 0;
            title.setText("已选" + select + "" + "/" + dataList.size() + "");
            mAdapter.setData(dataList);
            mAdapter.notifyDataSetChanged();
        } else {
            popItem = dataList.get(position);
            showPopWindow(popItem);
        }
    }


    /**
     * 弹框选择
     *
     * @param downLoadFile 数据
     */
    private void showPopWindow(DownLoadFile downLoadFile) {
        popView = LayoutInflater.from(DownloadedFilesActivity.this).inflate(
                R.layout.item_downloaded_files_popwindow_item, null);

        initPopView(downLoadFile, popView);

        popWindows = new PopupWindow(popView,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT);
        popWindows.setFocusable(true);
        ColorDrawable dw = new ColorDrawable(0x00000000);
        popWindows.setBackgroundDrawable(dw);
        popWindows.showAsDropDown(downloadedFiles_top);
        popWindows.setAnimationStyle(R.style.new_popwindow_anim_style);
        popWindows.setClippingEnabled(false);
        popWindows.setOutsideTouchable(true);
        popWindows.setOnDismissListener(new PopupWindow.OnDismissListener() {

            @Override
            public void onDismiss() {
                popWindows.dismiss();
            }
        });

    }

    /**
     * 根据弹框的视图展示数据
     *
     * @param item    数据
     * @param popView 弹框
     */
    private void initPopView(DownLoadFile item, View popView) {
        ImageView downloadedFiles_img_listItem_thumb;
        TextView downloadedFiles_txt_listItem_fileName;
        TextView downloadedFiles_txt_listItem_timeStamp;
        TextView downloadedFiles_txt_listItem_fileSize;
        TextView downloadedFiles_txt_listItem_fileSource;
        TextView pop_delete;
        TextView pop_transmit;
        TextView pop_scan;
        downloadedFiles_img_listItem_thumb = (ImageView) popView.findViewById(R.id.downloadedFiles_img_listItem_thumb);
        downloadedFiles_txt_listItem_fileName = (TextView) popView.findViewById(R.id.downloadedFiles_txt_listItem_fileName);
        downloadedFiles_txt_listItem_timeStamp = (TextView) popView.findViewById(R.id.downloadedFiles_txt_listItem_timeStamp);
        downloadedFiles_txt_listItem_fileSize = (TextView) popView.findViewById(R.id.downloadedFiles_txt_listItem_fileSize);
        downloadedFiles_txt_listItem_fileSource = (TextView) popView.findViewById(R.id.downloadedFiles_txt_listItem_fileSource);
        pop_delete = (TextView) popView.findViewById(R.id.pop_delete);
        pop_transmit = (TextView) popView.findViewById(R.id.pop_transmit);
        pop_scan = (TextView) popView.findViewById(R.id.pop_scan);
        switch (item.getFileType()) {
            case 1:
                if (!TextUtils.isEmpty(item.getFileName())) {
                    String end = item.getFileName().substring(item.getFileName().lastIndexOf("."), item.getFileName().length());
                    if (end.endsWith("png") || end.endsWith("jpg") || end.endsWith("jpeg")) {
                        downloadedFiles_img_listItem_thumb.setImageBitmap(ImageUtil.getBitmapThumbnail(FileUtil.getFilePath("") + item.getFileName(), ImageUtil.mDefaultWidth, ImageUtil.mDefaultHeight));
                    } else {
                        downloadedFiles_img_listItem_thumb.setImageResource(R.mipmap.jpg);
                    }
                } else {
                    downloadedFiles_img_listItem_thumb.setImageResource(R.mipmap.jpg);
                }
                break;
            case 2:
                downloadedFiles_img_listItem_thumb.setImageResource(R.mipmap.video);
                break;
            case 3:
                downloadedFiles_img_listItem_thumb.setImageResource(R.mipmap.music);
                break;
            case 4:
                downloadedFiles_img_listItem_thumb.setImageResource(R.mipmap.word);
                break;
            case 5:
                downloadedFiles_img_listItem_thumb.setImageResource(R.mipmap.excel);
                break;
            case 6:
                downloadedFiles_img_listItem_thumb.setImageResource(R.mipmap.pdf);
                break;
            case 7:
                downloadedFiles_img_listItem_thumb.setImageResource(R.mipmap.ppt);
                break;
            case 8:
                downloadedFiles_img_listItem_thumb.setImageResource(R.mipmap.rar);
                break;
            case 9:
                downloadedFiles_img_listItem_thumb.setImageResource(R.mipmap.file);
                break;
            default:
                downloadedFiles_img_listItem_thumb.setImageResource(R.mipmap.file);
                break;

        }
        downloadedFiles_txt_listItem_fileName.setText(item.getFileName());
        downloadedFiles_txt_listItem_fileSize.setText(FileUtil.FormatFileSize(Long.parseLong(item.getSize())));
        downloadedFiles_txt_listItem_timeStamp.setText(TimeUtil.getDateByMillisecond(Long.parseLong(item.getDataTime()), TimeUtil.FORMAT_DATETIME_MM_DD2));
        // 012 代表聊天 4 流程审批  5  OA申请
        switch (item.getFileSource()) {
            case 0:
            case 1:
            case 2:
                downloadedFiles_txt_listItem_fileSource.setText(R.string.downloadFiles_select_source_adapterChat);
                break;
            case 3:
                downloadedFiles_txt_listItem_fileSource.setText(R.string.downloadFiles_select_source_groupshare);
                break;
            case 4:
                downloadedFiles_txt_listItem_fileSource.setText(R.string.downloadFiles_select_source_promise);
                break;
            case 5:
                downloadedFiles_txt_listItem_fileSource.setText(R.string.downloadFiles_select_source_oa);
                break;
            default:
                downloadedFiles_txt_listItem_fileSource.setText(R.string.downloadFiles_select_source_unkown);
                break;
        }
        pop_delete.setOnClickListener(this);
        pop_transmit.setOnClickListener(this);
        pop_scan.setOnClickListener(this);

    }


    /**
     * 改变删除字体颜色和背景
     *
     * @param color 颜色
     */
    private void changeDeleteDisplay(boolean color) {
        if (color) {
            tv_delete.setTextColor(0xFF12b5b0);
            tv_delete.setBackgroundResource(R.drawable.background_whitesolid_bluestroke_cornor);
        } else {
            tv_delete.setTextColor(0xFFa0a0a0);
            tv_delete.setBackgroundResource(R.drawable.background_whitesolid_graystroke_cornor);
        }
    }

    /**
     * 根据排序来显示数据
     */
    private void getDataByOrder(boolean order) {
        ArrayList<DownLoadFile> data = mAdapter.getData();
        if (order) {
            Collections.sort(data, new SortByDesc());
        } else {
            Collections.sort(data, new SortByAsc());
        }
        mAdapter.setData(data);
        mAdapter.notifyDataSetChanged();
    }


    /**
     * APP list通过seq排序
     */
    public static class SortByDesc implements Comparator<DownLoadFile> {
        @Override
        public int compare(DownLoadFile o1, DownLoadFile o2) {
            return (int) (Long.parseLong(o1.getDataTime()) - Long.parseLong(o2.getDataTime()));
        }
    }


    /**
     * APP list通过seq排序
     */
    public static class SortByAsc implements Comparator<DownLoadFile> {
        @Override
        public int compare(DownLoadFile o1, DownLoadFile o2) {
            return (int) (Long.parseLong(o2.getDataTime()) - Long.parseLong(o1.getDataTime()));
        }
    }

    /**
     * 根据文件类型筛选数据
     */
    private void getDataBytype(int type) {
        view_source.setVisibility(View.GONE);
        dataList = new ArrayList<>();
        for (int i = 0; i < queryDatalist.size(); i++) {
            DownLoadFile file = queryDatalist.get(i);
            if (file.getFileType() == type) {
                dataList.add(file);
            }
        }
        sortDataByTimeKey(dataList);
        mAdapter.setData(dataList);
        mAdapter.notifyDataSetChanged();
    }

    /**
     * 重置数据
     */
    private void reSetData() {
        setSourceTextBackground(source_all, -1, source_map);
        resetTimeAllVIewTextBack();
        time_show.setText(getResources().getString(R.string.downloadFiles_select_time_show, TimeUtil.getCurrentYear() + ""));
        view_source.setVisibility(View.GONE);
        view_time.setVisibility(View.GONE);
        if (!isDesc) {
            image_sort.setRotation(360);
            isDesc = true;
        }
        dataList.clear();
        dataList.addAll(queryDatalist);
        mAdapter.setData(dataList);
        mAdapter.notifyDataSetChanged();
    }

    /**
     * 更换当前数据的选中状态
     *
     * @param mode 状态
     */
    private void changeDataCheck(boolean mode) {
        dataList = mAdapter.getData();
        for (int i = 0; i < dataList.size(); i++) {
            dataList.get(i).setCheck(mode);
        }
        mAdapter.setData(dataList);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == REQUEST_CODE) {
            // 根据返回来的packetId 做接下来的操作
            String fileName = data.getStringExtra("fileName");
            int posi = 0;
            //如果再数据库中没有查到那么移动到第一个位置
            for (int i = 0; i < queryDatalist.size(); i++) {
                if (queryDatalist.get(i).getFileName().equals(fileName)) {
                    posi = i;
                }
            }
            move(posi);
            popItem = queryDatalist.get(posi);
            showPopWindow(popItem);
        }

    }

    class TimeSelectAdapter extends BaseAdapter {
        private LayoutInflater inflater;
        private List<String> data;

        TimeSelectAdapter(Context context, List<String> data) {
            this.data = data;
            this.inflater = LayoutInflater.from(context);
        }


        @Override
        public int getCount() {
            return data == null ? 0 : data.size();
        }

        @Override
        public String getItem(int position) {
            return data.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = this.inflater.inflate(R.layout.item_downloaded_files_timelist_item, parent, false);
            }
            TextView timeName = ViewHolder.get(convertView, R.id.time_tv);
            if (time_map.containsKey(position)) {
                timeName.setTextColor(ContextCompat.getColor(DownloadedFilesActivity.this,R.color.white));
                timeName.setBackgroundResource(R.drawable.background_blue);
            } else {
                timeName.setTextColor(ContextCompat.getColor(DownloadedFilesActivity.this,R.color.actionBar_bg));
                timeName.setBackgroundResource(R.drawable.background_white_blue_cornor_square);
            }
            timeName.setText(data.get(position) + "月");
            return convertView;
        }

    }

    /**
     * 移动到指定位置
     *
     * @param n 移动
     */
    private void move(int n) {
        if (n < 0 || n >= mAdapter.getItemCount()) {
            return;
        }
        mIndex = n;
        mRecyclerView.stopScroll();
        smoothMoveToPosition(n);
    }

    private void smoothMoveToPosition(int n) {

        int firstItem = mLinearLayoutManager.findFirstVisibleItemPosition();
        int lastItem = mLinearLayoutManager.findLastVisibleItemPosition();
        if (n <= firstItem) {
            mRecyclerView.smoothScrollToPosition(n);
        } else if (n <= lastItem) {
            int top = mRecyclerView.getChildAt(n - firstItem).getTop();
            mRecyclerView.smoothScrollBy(0, top);
        } else {
            mRecyclerView.smoothScrollToPosition(n);
            move = true;
        }

    }


    private boolean move = false;
    private int mIndex = 0;

    /**
     * recycleview 移动监听
     */
    class RecyclerViewListener extends RecyclerView.OnScrollListener {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            if (move && newState == RecyclerView.SCROLL_STATE_IDLE) {
                move = false;
                int n = mIndex - mLinearLayoutManager.findFirstVisibleItemPosition();
                if (0 <= n && n < mRecyclerView.getChildCount()) {
                    int top = mRecyclerView.getChildAt(n).getTop();
                    mRecyclerView.smoothScrollBy(0, top);
                }

            }
        }

    }


}
