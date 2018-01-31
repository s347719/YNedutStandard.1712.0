package com.yineng.ynmessager.activity.session;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.yineng.ynmessager.R;
import com.yineng.ynmessager.activity.BaseActivity;
import com.yineng.ynmessager.activity.event.NoticeFragmentAll;
import com.yineng.ynmessager.activity.event.NoticeFragmentPlatNotice;
import com.yineng.ynmessager.activity.event.NoticeFragmentSystem;
import com.yineng.ynmessager.bean.event.NoticeEvent;
import com.yineng.ynmessager.view.HorizontalListView;
import com.zhy.http.okhttp.OkHttpUtils;

import org.greenrobot.eventbus.EventBus;

public class PlatformNoticeActivity extends BaseActivity {
    public RelativeLayout mRel_actionBar;
    public HorizontalListView scan_listview;
    private MyAdapter myAdapter;
    private String strings[] ={"全部","通知公告","系统消息"};
    private View top_view;
    private String mTag = PlatformNoticeActivity.class.getSimpleName();

    private FragmentTransaction fragmentTransaction;
    private NoticeFragmentAll noticeFragmentAll;
    private NoticeFragmentPlatNotice noticeFragmentPlatNotice;
    private NoticeFragmentSystem noticeFragmentSystem;
    private FragmentManager supportFragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        setContentView(R.layout.activity_platform_notice);
        noticeFragmentAll = new NoticeFragmentAll();
        noticeFragmentPlatNotice = new NoticeFragmentPlatNotice();
        noticeFragmentSystem = new NoticeFragmentSystem();
        supportFragmentManager  = getSupportFragmentManager();
        fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.contain,noticeFragmentAll,"2")
                .add(R.id.contain,noticeFragmentPlatNotice,"1")
                .add(R.id.contain,noticeFragmentSystem,"0").commit();
        supportFragmentManager.beginTransaction()
                .hide(noticeFragmentPlatNotice)
                .hide(noticeFragmentSystem)
                .show(noticeFragmentAll).commit();
        findViews();
    }

    private void findViews() {
        mRel_actionBar = (RelativeLayout) findViewById(R.id.main_rel_event_ActionBar);
        top_view = findViewById(R.id.top_view);
        scan_listview = (HorizontalListView) findViewById(R.id.scan_listview);
        myAdapter = new MyAdapter();
        scan_listview.setAdapter(myAdapter);
        scan_listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position!=myAdapter.getChange()){
                    //点击不同于当前item的时间筛选不同的信息，将消息发送到NoticeFragment 进行操作
                    myAdapter.change(position);
                    if (position==0){
                        supportFragmentManager.beginTransaction()
                                .hide(noticeFragmentPlatNotice)
                                .hide(noticeFragmentSystem)
                                .show(noticeFragmentAll).commit();
                    }else if (position==1){
                        supportFragmentManager.beginTransaction()
                                .hide(noticeFragmentAll)
                                .hide(noticeFragmentSystem)
                                .show(noticeFragmentPlatNotice).commit();
                    }else if (position==2){
                        supportFragmentManager.beginTransaction()
                                .hide(noticeFragmentPlatNotice)
                                .hide(noticeFragmentAll)
                                .show(noticeFragmentSystem).commit();
                    }
                }
            }
        });
    }

    @Override
    protected void onResume() {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        OkHttpUtils.getInstance().cancelTag(this);
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(NoticeEvent.ACTION_REFRESH_UNREAD));
    }


    class MyAdapter extends BaseAdapter {
        private int posi = 0;
        @Override
        public int getCount() {
            return strings.length;
        }

        @Override
        public Object getItem(int position) {
            return strings[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = LayoutInflater.from(PlatformNoticeActivity.this).inflate(R.layout.platform_list_item,parent,false);
            RelativeLayout rl_img = (RelativeLayout) convertView.findViewById(R.id.rl_img);
            View view_bg = convertView.findViewById(R.id.view_bg);
            TextView tab_text = (TextView) convertView.findViewById(R.id.tab_text);
            ViewGroup.LayoutParams layoutParams = (ViewGroup.LayoutParams) rl_img.getLayoutParams();
            layoutParams.width = getScreenWidth(PlatformNoticeActivity.this)/getCount();
            tab_text.setText(strings[position]);
            if (posi==position){
                tab_text.setTextColor(getResources().getColor(R.color.actionBar_bg_blue));
                view_bg.setVisibility(View.VISIBLE);
            }else {
                tab_text.setTextColor(getResources().getColor(R.color.black));
            }
            return convertView;
        }
        public void change(int pos){
            this.posi = pos;
            notifyDataSetChanged();
        }
        public int getChange(){
            return posi;
        }
    }

    //获取屏宽
    public static int getScreenWidth(Context context){
        return context.getResources().getDisplayMetrics().widthPixels;
    }

    public void backKeyEvent(View v) {
        PlatformNoticeActivity.this.finish();

    }

    @Override
    public void onBackPressed() {
           PlatformNoticeActivity.this.finish();

    }
}
