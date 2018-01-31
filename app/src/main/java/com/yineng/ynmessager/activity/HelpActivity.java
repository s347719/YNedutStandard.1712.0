package com.yineng.ynmessager.activity;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.Html;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yineng.ynmessager.R;

import org.xml.sax.XMLReader;


public class HelpActivity extends AppCompatActivity {

    private LinearLayout lin_phone_help,lin_server_help;
    public static final int HELP_TYPE_PHONE = 0; //手机验证码帮助文档
    public static final int HELP_TYPE_SERVER = 1;//服务器地址帮助文档
    private TextView help_title;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        initView();
        findViewById(R.id.dismiss).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void initView() {
        lin_server_help = (LinearLayout) findViewById(R.id.lin_server_help);
        lin_phone_help = (LinearLayout) findViewById(R.id.lin_phone_help);
        help_title = (TextView) findViewById(R.id.help_title);
        int helpType = getIntent().getIntExtra("helpType",HELP_TYPE_SERVER);
        switch (helpType){
            case HELP_TYPE_PHONE:
                help_title.setText("收不到动态码");
                lin_phone_help.setVisibility(View.VISIBLE);
                lin_server_help.setVisibility(View.GONE);
                break;
            case HELP_TYPE_SERVER:
                lin_phone_help.setVisibility(View.GONE);
                lin_server_help.setVisibility(View.VISIBLE);
                break;
        }
    }
}
