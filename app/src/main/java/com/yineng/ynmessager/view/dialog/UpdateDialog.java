package com.yineng.ynmessager.view.dialog;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.yineng.ynmessager.R;
import com.yineng.ynmessager.util.DensityUtil;

/**
 * Created by 舒欢
 * Created time: 2017/7/24
 * 作用：
 */

public class UpdateDialog extends AlertDialog implements
        View.OnClickListener {

    private String mess;
    private Button update_update;
    private ImageView update_rocekt;
    private Button update_calcle;
    private TextView update_info;
    private TextView multe_update_hint;
    private OnclickListener onclick;
    private Context context;
    private boolean isMustUpdate;

    public static UpdateDialog updateDialog = null;

    public  static UpdateDialog getInstance(Context context, int theme, OnclickListener onclick, String message,boolean isMustUpdate) {
        if (updateDialog == null) {
                    updateDialog = new UpdateDialog( context,  theme,  onclick,  message, isMustUpdate);
        }
        return updateDialog;
    }

    public UpdateDialog(Context context, int theme, OnclickListener onclick, String message,boolean isMustUpdate) {
        super(context, theme);
        this.context = context;
        this.mess = message;
        this.onclick = onclick;
        this.isMustUpdate = isMustUpdate;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.update_dialog_layout);
        DisplayMetrics dm = new DisplayMetrics();
        WindowManager mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        mWindowManager.getDefaultDisplay().getMetrics(dm);
        android.view.WindowManager.LayoutParams p = this.getWindow().getAttributes();  //获取对话框当前的参数值
        p.width = dm.widthPixels;   //高度设置为屏幕
        p.height = dm.heightPixels;    //宽度设置为全屏
        p.gravity = Gravity.CENTER;
        this.getWindow().setAttributes(p);     //设置生效

        update_update = (Button) findViewById(R.id.update_update);
        update_rocekt = (ImageView) findViewById(R.id.update_rocekt);
        update_calcle = (Button) findViewById(R.id.update_calcle);
        update_info = (TextView) findViewById(R.id.update_info);
        multe_update_hint = (TextView) findViewById(R.id.multe_update_hint);
//        如果是强制更新，显示提示词
        if (isMustUpdate){
            multe_update_hint.setVisibility(View.VISIBLE);
            update_calcle.setText("退出应用");
        }
        else {
            multe_update_hint.setVisibility(View.GONE);
            update_calcle.setText("暂不更新");
        }
        update_info.setText(mess);
        update_calcle.setOnClickListener(this);
        update_update.setOnClickListener(this);

    }


    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.update_calcle:
                onclick.clicklistener(v);
                break;
            case R.id.update_update:
                runAnim(v);
                break;
        }
    }

    /**
     * 动画
     */
    private void runAnim(final View v) {
        //获取屏幕高度，飞出屏幕一半的距离
        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        final int height = wm.getDefaultDisplay().getHeight();

        //获取小火箭的位置
        int[] viewLocation = new int[2];
        update_rocekt.getLocationInWindow(viewLocation);
        int viewX = viewLocation[0] - DensityUtil.dip2px(context, 35); // x 坐标
        ObjectAnimator objAnim = ObjectAnimator.ofFloat(update_rocekt, "x", viewX + 8, viewX - 8);
        objAnim.setInterpolator(new DecelerateInterpolator());
        objAnim.setDuration(100);
        objAnim.setRepeatCount(10);
        objAnim.start();
        //晃动完就飞走
       objAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                ObjectAnimator upAnim = ObjectAnimator.ofFloat(update_rocekt, "y", -(height / 3));
                upAnim.setInterpolator(new DecelerateInterpolator());
                upAnim.setDuration(200);
                upAnim.start();
                upAnim.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        onclick.clicklistener(v);
                    }
                });
            }
        });
    }

    //外部接口接收点击确定事件
    public interface OnclickListener {

        void clicklistener(View view);
    }


}
