package sloev.ripple.chat;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBPrivateChat;
import com.quickblox.chat.QBPrivateChatManager;
import com.quickblox.chat.exception.QBChatException;
import com.quickblox.chat.listeners.QBMessageListenerImpl;
import com.quickblox.chat.listeners.QBPrivateChatManagerListener;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.users.model.QBUser;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;

/**
 * Created by johannes on 14/03/15.
 */

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

import sloev.ripple.util.ApplicationSingleton;


public class PrivateChatManager extends QBMessageListenerImpl<QBPrivateChat> implements ChatManager, QBPrivateChatManagerListener {

    private static final String TAG = "PrivateChatManagerImpl";
    private static final int AUTO_PRESENCE_INTERVAL_IN_SECONDS = 30;
    private Context context;
    ApplicationSingleton dataholder;

    private QBPrivateChatManager privateChatManager;
   // private QBPrivateChat privateChat;

    private List<ChatListener> listeners = new ArrayList<ChatListener>();
    private Map<Integer, QBPrivateChat> privateChats = new HashMap<Integer, QBPrivateChat>();

    private QBChatService chatService;

    public PrivateChatManager(Context context) {
        this.context = context;
        dataholder = ApplicationSingleton.getDataHolder();
        this.initChatService();

    }
    private void initChatService(){
        QBChatService.setDebugEnabled(false);
        if (!QBChatService.isInitialized()) {
            QBChatService.init(context);
        }
    }
    public QBChatService getChatService(){
        return QBChatService.getInstance();
    }


    public void initChatListener(){
        privateChatManager = this.getChatService().getPrivateChatManager();
        privateChatManager.addPrivateChatManagerListener(this);
        System.out.println("CHATINITIALIZED");
    }



    public void sendPresencesPeriodically() {
        try {
            chatService.startAutoSendPresence(30);
        } catch (SmackException.NotLoggedInException e) {
            e.printStackTrace();
        }
    }

    public void newChat(int opponentID){
        if(!privateChats.containsKey(opponentID)) {
            // init private chat
            //
            QBPrivateChat privateChat = privateChatManager.getChat(opponentID);
            if (privateChat == null) {
                privateChat = privateChatManager.createChat(opponentID, this);
            } else {
                privateChat.addMessageListener(this);
            }
            privateChats.put(opponentID, privateChat);
        }
    }
    public void removeChat(int opponentID){
        privateChats.remove(opponentID);

    }

    public void addListener(ChatListener toAdd) {
        listeners.add(toAdd);
    }

    public void removeListener(ChatListener toAdd) {
        listeners.remove(toAdd);
    }

    @Override
    public void sendMessage(int opponentId, QBChatMessage message) throws XMPPException, SmackException.NotConnectedException {
        privateChats.get(opponentId).sendMessage(message);
    }

    @Override
    public void sendLatLng(int opponentID, LatLng position) throws XMPPException, SmackException.NotConnectedException {
        QBChatMessage chatMessage = new QBChatMessage();
        String message = Double.toString(position.latitude) + " " + Double.toString(position.longitude);
        chatMessage.setBody(message);
        privateChats.get(opponentID).sendMessage(chatMessage);
    }

    @Override
    public void release(int opponentId) {
        Log.w(TAG, "release private chat");
        for (QBPrivateChat hl : privateChats.values())
            hl.removeMessageListener(this);
        privateChatManager.removePrivateChatManagerListener(this);
    }

    @Override
    public void processMessage(QBPrivateChat chat, QBChatMessage message) {
        //gets user id
        int userId =  chat.getParticipant();
        String[] positionStr = message.getBody().split(" ");
        double lat = Double.parseDouble(positionStr[0]);
        double lon = Double.parseDouble(positionStr[1]);
        LatLng position = new LatLng(lat, lon);
        // Notify everybody that may be interested.
        for (ChatListener hl : listeners)
            hl.gpsReceived(userId, position);
    }

    @Override
    public void processError(QBPrivateChat chat, QBChatException error, QBChatMessage originChatMessage) {

    }

    @Override
    public void chatCreated(QBPrivateChat incomingPrivateChat, boolean createdLocally) {
        if (!createdLocally) {
            int opponentID = incomingPrivateChat.getParticipant();
            if (dataholder.contactsContainsUser(opponentID)){
                System.out.println(String.format("USER %d ”IS IN CONTACTS", opponentID));
            }
            incomingPrivateChat.addMessageListener(PrivateChatManager.this);
            privateChats.put(opponentID, incomingPrivateChat);
        }

//        Log.w(TAG, "private chat created: " + incomingPrivateChat.getParticipant() + ", createdLocally:" + createdLocally);
    }
}
