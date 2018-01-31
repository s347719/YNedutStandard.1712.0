package com.yineng.ynmessager.activity.app.internship;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Toast;

import com.yineng.ynmessager.R;
import com.yineng.ynmessager.activity.BaseFragment;
import com.yineng.ynmessager.bean.app.Internship.InternshipAct;
import com.yineng.ynmessager.bean.app.Internship.InternshipReport;
import com.yineng.ynmessager.sharedpreference.LastLoginUserSP;

/**
 * Created by 贺毅柳 on 2016/1/4 17:19.
 */
public abstract class ReportCalendarBaseFragment extends BaseFragment {
    protected InternshipAct mThisInternshipAct;
    private View mEmptyView;

    public static boolean hasLocalDraft(Context context, InternshipReport report) {
        return LastLoginUserSP.getInstance(context)
            .getSharedPreferences()
            .contains(InternshipReport.LOCAL_DRAFT_KEY_PREFIX + report.getId());
    }

    @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mEmptyView = view.findViewById(android.R.id.empty);

        mThisInternshipAct = getArguments().getParcelable("InternShipAct"); //接收Activity传入的参数“InternShipAct”
    }

    protected abstract void initData();

    @Override public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            initData();
        }
    }

    protected final void showEmptyView(boolean isShow) {
        if (mEmptyView == null) {
            return;
        }
        if (isShow) {
            mEmptyView.setVisibility(View.VISIBLE);

            Toast.makeText(mParentActivity, R.string.reportCalendar_loadingFailed2, Toast.LENGTH_SHORT).show();
        } else {
            mEmptyView.setVisibility(View.GONE);
        }
    }
}
