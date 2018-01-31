package com.yineng.ynmessager.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.yineng.ynmessager.app.AppController;
import com.yineng.ynmessager.util.AppUtils;
import com.yineng.ynmessager.util.L;

public abstract class BaseFragment extends Fragment
{
	protected FragmentActivity mParentActivity;
	protected AppController mApplication;
	protected final String mTag = this.getClass().getSimpleName();
	private static final boolean DEBUG = false;

	@Override
	public void onAttach(Context context)
	{
		super.onAttach(context);
		if(DEBUG) {
            L.v(mTag, mTag + " -> onAttach");
        }

		mParentActivity = getActivity();
		mApplication = AppController.getInstance();
	}

	@Override
	public void onStart()
	{
		super.onStart();
		if(DEBUG) {
            L.v(mTag, mTag + " -> onStart");
        }
	}

	@Override
	public void onResume()
	{
		super.onResume();
		if(DEBUG) {
            L.v(mTag, mTag + " -> onResume");
        }
	}

	@Override
	public void onPause()
	{
		super.onPause();
		if(DEBUG) {
            L.v(mTag, mTag + " -> onPause");
        }
	}

	@Override
	public void onStop()
	{
		super.onStop();
		if(DEBUG) {
            L.v(mTag, mTag + " -> onStop");
        }
	}

	@Override
	public void onDestroyView()
	{
		super.onDestroyView();
		if(DEBUG) {
            L.v(mTag, mTag + " -> onDestroyView");
        }
	}

	@Override
	public void onDetach()
	{
		super.onDetach();
		if(DEBUG) {
            L.v(mTag, mTag + " -> onDetach");
        }
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		if(DEBUG) {
            L.v(mTag, mTag + " -> onDestroy");
        }
	}

	@Override
	@Nullable
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
	{
		if(DEBUG) {
            L.v(mTag, mTag + " -> onCreateView");
        }
		return super.onCreateView(inflater,container,savedInstanceState);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
	{
		super.onViewCreated(view,savedInstanceState);
		if(DEBUG) {
            L.v(mTag, mTag + " -> onViewCreated");
        }
	}

	@Override
	public void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		if(DEBUG) {
            L.v(mTag, mTag + " -> onSaveInstanceState");
        }
	}

	@Override
	public void onViewStateRestored(@Nullable Bundle savedInstanceState)
	{
		super.onViewStateRestored(savedInstanceState);
		if(DEBUG) {
            L.v(mTag, mTag + " -> onViewStateRestored");
        }
	}

	@Override
	public void onHiddenChanged(boolean hidden)
	{
		super.onHiddenChanged(hidden);
		if(DEBUG) {
            L.v(mTag, mTag + " -> onHiddenChanged");
        }
	}

	public Context getApplicationContext()
	{
		return super.getContext();
	}

	public void hideProgressDialog() {
		AppUtils.hideProgressDialog();
	}

	public void showProgressDialog(String title) {
		AppUtils.showProgressDialog(getActivity(), title + " ");
	}

	public int getStatusBarHeight() {
		int result = 0;
		int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
		if (resourceId > 0) {
			result = getResources().getDimensionPixelSize(resourceId);
		}
		return result;
	}
}
