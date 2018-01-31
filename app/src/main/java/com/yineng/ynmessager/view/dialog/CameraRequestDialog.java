package com.yineng.ynmessager.view.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.yineng.ynmessager.R;

public class CameraRequestDialog extends Dialog implements View.OnClickListener
{
	private View.OnClickListener onclick;
	private TextView tv_camera_cancle,tv_camera_ok;


	public CameraRequestDialog(Context context, int theme, View.OnClickListener onclick)
	{
		super(context, theme);
		this.onclick = onclick;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.cancle_dialog);
		setCanceledOnTouchOutside(false);
		tv_camera_cancle = (TextView) findViewById(R.id.tv_camera_cancle);
		tv_camera_cancle.setOnClickListener(this);
		tv_camera_ok = (TextView) findViewById(R.id.tv_camera_ok);
		tv_camera_ok.setOnClickListener(this);
	}

	@Override
	public void onClick(View v)
	{
		switch (v.getId())
		{
			case R.id.tv_camera_cancle:
				onclick.onClick(v);
				break;
			case R.id.tv_camera_ok:
				onclick.onClick(v);
				break;
		}
	}
}
