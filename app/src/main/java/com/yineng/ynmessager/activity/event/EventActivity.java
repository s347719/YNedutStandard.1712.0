package com.yineng.ynmessager.activity.event;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.viewpagerindicator.TabPageIndicator;
import com.yineng.ynmessager.R;
import com.yineng.ynmessager.activity.BaseActivity;
import com.yineng.ynmessager.bean.CommonEvent;
import com.yineng.ynmessager.okHttp.OKHttpCustomUtils;
import com.yineng.ynmessager.sharedpreference.LastLoginUserSP;
import com.yineng.ynmessager.util.L;
import com.yineng.ynmessager.util.V8TokenManager;
import com.yineng.ynmessager.util.address.URLs;
import com.yineng.ynmessager.view.ViewPagerCompat;
import com.yineng.ynmessager.yninterface.TokenLoadedListener;
import com.zhy.http.okhttp.callback.StringCallback;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.MessageFormat;

import okhttp3.Call;

/**
 * Created by 舒欢
 * Created time: 2017/8/2
 * Descreption：待办activity包括我的待办、我的申请、我的已办
 */

public class EventActivity extends BaseActivity implements RadioGroup.OnCheckedChangeListener {

    private String[] mTabTitle;
    public Fragment[] mContent;
    private TabPageIndicator mIdc_indicator;
    private ViewPagerCompat mPager_content;
    private EventActivity.EventPagerAdapter mContentAdapter;
    private RadioGroup mRadG_switcher;
    public RadioButton mRad_switchMyTodo;
    public RadioButton mRad_switchMySend;
    public RadioButton mRad_switchMyDemand;
    public RadioButton mRad_switchMyDone;
    private DoneFragment mDoneFragment;
    private DemandFragment mDemandFragment;
    private SendRequestFragment mSendRequestFragment;
    private TodoFragment mTodoFragment;
    private V8TokenManager mV8TokenManager;
    private ImageView img_close;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);
        img_close = (ImageView) findViewById(R.id.img_close);
        img_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mIdc_indicator = (TabPageIndicator)findViewById(R.id.main_idc_eventIndicator);
        mPager_content = (ViewPagerCompat)findViewById(R.id.main_pager_eventContent);
        mRadG_switcher = (RadioGroup)findViewById(R.id.main_radG_event_switcher);
        mRad_switchMyTodo = (RadioButton) findViewById(R.id.main_rad_event_myTodo);
        mRad_switchMySend = (RadioButton) findViewById(R.id.main_rad_event_send_request);
        mRad_switchMyDemand = (RadioButton) findViewById(R.id.main_rad_event_myDemand);
        mRad_switchMyDone = (RadioButton) findViewById(R.id.main_rad_event_myDone);
        mTodoFragment = new TodoFragment();
        mSendRequestFragment = new SendRequestFragment();
        mDoneFragment = new DoneFragment();
        mDemandFragment = new DemandFragment();
        mContent = new Fragment[]{mTodoFragment,mSendRequestFragment, mDemandFragment, mDoneFragment};

        mRadG_switcher.setOnCheckedChangeListener(this);
        mTabTitle = getResources().getStringArray(R.array.main_eventTabTitle);
        mContentAdapter = new EventPagerAdapter(getSupportFragmentManager());
        mPager_content.setAdapter(mContentAdapter);
        mPager_content.setScrollable(false);
        mPager_content.setOffscreenPageLimit(mContent.length); // 设置ViewPager缓存所有Fragment
        mIdc_indicator.setViewPager(mPager_content);

        mPager_content.setCurrentItem(0);
        mTodoFragment.refreshTodoList(true,true);

        mV8TokenManager = new V8TokenManager();
        mV8TokenManager.setTokenLoadedListener(new TokenLoadedListener() {
            @Override
            public void tokenLoaded() {
                //V8OA接口还未发布，所以暂不调用计数接口
                refreshUnreadEventCount();
            }
        });
    }

    // 注册EventBus 必须要实现的方法，不可删除
    @Subscribe(threadMode  = ThreadMode.MAIN)
    public void onEventMainThread(CommonEvent event) {
        String countStr = (String) event.getObj();
        switch (event.getWhat()){
            case BaseEventFragment.EVENT_TODO_TYPE:
                //更改事件中我的待办选项卡中的计数
                mRad_switchMyTodo.setText(countStr);
                break;
            case BaseEventFragment.EVENT_DEMAND_TYPE:
                //更改事件中我的申请选项卡中的计数
                mRad_switchMyDemand.setText(countStr);
                break;
            case BaseEventFragment.EVENT_DONE_TYPE:
                //更改事件中我的申请选项卡中的计数
                mRad_switchMyDone.setText(countStr);
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().register(this);
        }
        if (mV8TokenManager.isMustUpdateToken()) {
            mV8TokenManager.initAppTokenData(true);
        } else {
            //V8OA接口还未发布，所以暂不调用计数接口
            refreshUnreadEventCount();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId)
        {
            case R.id.main_rad_event_myTodo:
                mPager_content.setCurrentItem(0);
                mTodoFragment.refreshTodoList(true,true);
                break;

            case R.id.main_rad_event_send_request:
                mPager_content.setCurrentItem(1);
                mSendRequestFragment.refreshSendList(true,true);
                break;
            case R.id.main_rad_event_myDemand:
                mPager_content.setCurrentItem(2);
                mDemandFragment.refreshDemandList(true,true);
                break;
            case R.id.main_rad_event_myDone:
                mPager_content.setCurrentItem(3);
                mDoneFragment.refreshDoneList(true,true);
                break;
        }
    }
    private class EventPagerAdapter extends FragmentPagerAdapter
    {
        EventPagerAdapter(FragmentManager fm)
        {
            super(fm);
        }

        @Override
        public Fragment getItem(int arg0)
        {
            return mContent[arg0];
        }

        @Override
        public CharSequence getPageTitle(int position)
        {
            return mTabTitle[position];
        }

        @Override
        public int getCount()
        {
            return mContent.length;
        }
    }

    /**
     * 更新待办各个子模块的统计计数
     * @param todoStr 待办数量
     * @param demandStr 申请数量
     * @param doneStr 已办数量
     */
    public void refreshEventCount(String todoStr, String demandStr, String doneStr) {
        if (Integer.parseInt(todoStr)>0) {
            todoStr =getString(R.string.main_event_my_todo)+"("+todoStr+")";
        } else {
            todoStr = getString(R.string.main_event_my_todo);
        }
        mRad_switchMyTodo.setText(todoStr);
        if (Integer.parseInt(demandStr)>0) {
            demandStr =getString(R.string.main_event_my_demand)+"("+demandStr+")";
        } else {
            demandStr = getString(R.string.main_event_my_demand);
        }
        mRad_switchMyDemand.setText(demandStr);
        if (Integer.parseInt(doneStr)>0) {
            doneStr =getString(R.string.main_event_my_done)+"("+doneStr+")";
        } else {
            doneStr = getString(R.string.main_event_my_done);
        }
        mRad_switchMyDone.setText(doneStr);
    }

    /**
     * 访问服务器接口获取待办、申请、已办的数量
     */
    private void refreshUnreadEventCount() {
        String url =  URLs.TODO_COUNT_URL;
        //给url赋上参数
        url = MessageFormat.format(url, V8TokenManager.sToken,
                LastLoginUserSP.getInstance(this).getUserAccount(),0);
        OKHttpCustomUtils.get(url, new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {

            }

            @Override
            public void onResponse(String response, int id) {
                if (TextUtils.isEmpty(response)) {
                    return;
                }
                try {
                    JSONObject jsonResp = new JSONObject(response);
                    String status = jsonResp.optString("status");
                    if ("0".equals(status)) {//成功
                        JSONObject result = jsonResp.optJSONObject("result");
                        if (result != null) {
                            String todoCount = result.optString("todoCount");
                            String demandCount = result.optString("demandCount");
                            String doneCount = result.optString("doneCount");
                            refreshEventCount(todoCount,demandCount,doneCount);
                        }
                    }
                } catch (JSONException e) {
                    L.w(mTag, e.getMessage(), e);
                }
            }
        });

    }

}
