package com.yineng.ynmessager.smack;

/**
 * IQ请求回执
 * 
 * @author yineng
 * 
 */
public interface ReceiveReqIQCallBack
{
	void receivedReqIQResult(ReqIQResult packet);
}
