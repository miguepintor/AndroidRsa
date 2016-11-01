
package org.etsit.uma.androidrsa.xmpp;

import java.util.HashMap;

import org.etsit.uma.androidrsa.activities.ChatActivity;
import org.etsit.uma.androidrsa.activities.ContactsActivity;
import org.etsit.uma.androidrsa.utils.AndroidRsaConstants;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.MessageListener;

import android.content.Intent;
import android.util.Log;

public class ConversationManager {
	private final static String TAG = "ChatMan";
	public final static HashMap<String, Chat> openedChats = new HashMap<String, Chat>(); 

	public static void initListener(final ContactsActivity activity) {
		// Listener para detectar si el chat lo crea el otro
		ChatManager chatmanager = ConnectionManager.getInstance().getChatManager();
		ChatManagerListener chatManagerListener = new ChatManagerListener() {
			public void chatCreated(Chat chat, boolean createdLocally) {
				if (!createdLocally) {
					Log.i(TAG, "Chat creado por " + chat.getParticipant());
					
					if(!openedChats.containsKey(chat.getParticipant())){
						openedChats.put(chat.getParticipant(), chat);
					}
					
					Intent i = new Intent(activity, ChatActivity.class);
					i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
					i.putExtra(AndroidRsaConstants.DEST_JID, chat.getParticipant());
					i.putExtra(AndroidRsaConstants.PASSPHRASE, activity.passPhrase);
					activity.startActivity(i);
				}
			}

		};

		chatmanager.addChatListener(chatManagerListener);
	}

	public static Chat createChat(String jidDest, MessageListener messageListener) {
		ChatManager chatmanager = ConnectionManager.getInstance().getChatManager();
		Chat chat = chatmanager.createChat(jidDest, messageListener);
		openedChats.put(jidDest, chat);
		return chat;
	}

}
