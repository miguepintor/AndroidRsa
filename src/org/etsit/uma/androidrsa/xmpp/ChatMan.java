
package org.etsit.uma.androidrsa.xmpp;

import org.etsit.uma.androidrsa.activities.ChatActivity;
import org.etsit.uma.androidrsa.activities.ContactsActivity;
import org.etsit.uma.androidrsa.utils.AndroidRsaConstants;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.MessageListener;

import android.content.Intent;
import android.util.Log;

public class ChatMan {
    private final static String TAG = "ChatActivity";
    private Connection connection;
    public static Chat chat = null;
    private ContactsActivity activity;

    public ChatMan(ContactsActivity cActivity) {
        this.activity = cActivity;
        this.connection = Conexion.getInstance();
    }

    public void initListener() {
        // Listener para detectar si el chat lo crea el otro
        org.jivesoftware.smack.ChatManager chatmanager = connection.getChatManager();
        ChatManagerListener chatManagerListener = new ChatManagerListener() {
            public void chatCreated(Chat chat, boolean createdLocally)
            {
                if (!createdLocally) {
                    Log.d(TAG, "Chat Creado localmente por " + chat.getParticipant());
                    ChatMan.chat = chat;
                    Intent i = new Intent(activity, ChatActivity.class);
                    i.putExtra("destJid", chat.getParticipant());
                    i.putExtra(AndroidRsaConstants.PASSPHRASE, activity.passPhrase);
                    activity.startActivity(i);
                }
            }

        };

        chatmanager.addChatListener(chatManagerListener);

    }

    public void createChat(String jidDest, MessageListener messageListener) {
        ChatManager chatmanager = connection.getChatManager();
        chat = chatmanager.createChat(jidDest, messageListener);

    }

    public Chat getChat() {
        return chat;
    }

    public void setChat(Chat chat) {
        this.chat = chat;
    }

}
