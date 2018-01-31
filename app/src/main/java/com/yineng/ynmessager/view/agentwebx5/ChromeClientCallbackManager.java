package com.yineng.ynmessager.view.agentwebx5;


import com.tencent.smtt.export.external.interfaces.JsPromptResult;
import com.tencent.smtt.sdk.WebView;
import com.yineng.ynmessager.util.L;

/**
 * Created by cenxiaozhong on 2017/5/14.
 * source CODE  https://github.com/Justson/AgentWebX5
 */

public class ChromeClientCallbackManager {


    private ReceivedTitleCallback mReceivedTitleCallback;
    private GeoLocation mGeoLocation;

    public ReceivedTitleCallback getReceivedTitleCallback() {
        return mReceivedTitleCallback;
    }



    public ChromeClientCallbackManager setReceivedTitleCallback(ReceivedTitleCallback receivedTitleCallback) {
        mReceivedTitleCallback = receivedTitleCallback;
        return this;
    }
    public ChromeClientCallbackManager setGeoLocation(GeoLocation geoLocation){
       this.mGeoLocation=geoLocation;
        return this;
    }

    public interface ReceivedTitleCallback{
         void onReceivedTitle(WebView view, String title);
    }

    public AgentWebCompatInterface mAgentWebCompatInterface;
    public AgentWebCompatInterface getAgentWebCompatInterface(){
        return mAgentWebCompatInterface;
    }
    public void setAgentWebCompatInterface(AgentWebCompatInterface agentWebCompatInterface){
        this.mAgentWebCompatInterface=agentWebCompatInterface;
        L.i("Info","agent:"+agentWebCompatInterface);
    }

     interface AgentWebCompatInterface{
        public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result);
        public void onReceivedTitle(WebView view, String title);
        public void onProgressChanged(WebView view, int newProgress);
    }

    public static class GeoLocation {
        /*1 表示定位开启, 0 表示关闭*/
        public int tag=1;


    }
}
