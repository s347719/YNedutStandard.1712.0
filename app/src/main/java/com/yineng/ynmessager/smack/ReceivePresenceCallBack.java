package com.yineng.ynmessager.smack;

import org.jivesoftware.smack.packet.Presence;

public interface ReceivePresenceCallBack {
	void receivedPresence(Presence packet);
}
