package sloev.ripple.chat;

import com.quickblox.chat.model.QBChatMessage;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;

public interface ChatManager {

    void sendMessage(int opponentID, QBChatMessage message) throws XMPPException, SmackException.NotConnectedException;

    void release(int opponentId) throws XMPPException;
}
