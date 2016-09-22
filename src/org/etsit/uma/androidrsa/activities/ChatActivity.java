
package org.etsit.uma.androidrsa.activities;

import java.security.PrivateKey;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.security.cert.Certificate;

import org.etsit.uma.androidrsa.R;
import org.etsit.uma.androidrsa.adapters.ChatAdapter;
import org.etsit.uma.androidrsa.rsa.KeyStore;
import org.etsit.uma.androidrsa.rsa.RSA;
import org.etsit.uma.androidrsa.steganography.Decode;
import org.etsit.uma.androidrsa.utils.AndroidRsaConstants;
import org.etsit.uma.androidrsa.xmpp.AvatarsCache;
import org.etsit.uma.androidrsa.xmpp.ChatMan;
import org.etsit.uma.androidrsa.xmpp.Conexion;
import org.etsit.uma.androidrsa.xmpp.RosterManager;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.util.StringUtils;

import android.app.ListActivity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class ChatActivity extends ListActivity {
	private static final String TAG = "ChatActivity";
	private ArrayList<Message> listMessages = new ArrayList<Message>();
	private ChatAdapter adapter;
	private ListView myListView;

	private String myJid;
	private String passPhrase;

	private Certificate destCert;
	private String destJid;
	private boolean cipher;

	private boolean backPressed = false;

	private MessageListener messageListener = new MessageListener() {
		public void processMessage(Chat chat, Message message) {
			if ((message.getBody() != null) && (!message.getType().equals(Message.Type.error))) {
				if (backPressed) {
					backPressed = false;
					Intent i = new Intent(ChatActivity.this, ChatActivity.class);
					i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
					i.putExtra(AndroidRsaConstants.DEST_JID, chat.getParticipant());
					startActivity(i);
				}
				setDestinationProperties(chat.getParticipant());

				Message copyMessage = new Message();
				copyMessage.setFrom(message.getFrom());
				copyMessage.setTo(message.getTo());
				SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
				copyMessage.setSubject(sdf.format(new Date()));

				if (cipher) {
					try {
						PrivateKey pk = RSA.getPrivateKeyDecryted(KeyStore.getInstance().getPk(), passPhrase);
						String decodedBody = RSA.decipher(message.getBody(), pk);
						Log.i(TAG, "Recibido mensaje cifrado: " + decodedBody);
						copyMessage.setBody(decodedBody);
					} catch (Exception e) {
						Log.e(TAG, "Error al descifrar");
						e.printStackTrace();
					}
				} else {
					copyMessage.setBody(message.getBody());
					Log.i(TAG, "Recibido mensaje plano: " + message.getBody());
				}

				listMessages.add(copyMessage);
				refreshAdapter();
				myListView.smoothScrollToPosition(adapter.getCount() - 1);
			}

		}

	};

	@Override
	public void onBackPressed() {
		backPressed = true;
		Intent i = new Intent(this, ContactsActivity.class);
		i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		startActivity(i);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.chat);

		adapter = new ChatAdapter(this, listMessages);
		setListAdapter(adapter);
		myListView = getListView();
		myListView.setDivider(null);

		myJid = Conexion.getInstance().getUser();
		passPhrase = getIntent().getStringExtra(AndroidRsaConstants.PASSPHRASE);

		setDestinationProperties(getIntent().getStringExtra(AndroidRsaConstants.DEST_JID));
		initializeMessageListener();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

		setDestinationProperties(intent.getStringExtra(AndroidRsaConstants.DEST_JID));
		initializeMessageListener();
	}

	private void setDestinationProperties(String newDestJid) {
		if (!newDestJid.equals(destJid)) {
			destJid = newDestJid;
			TextView title = (TextView) findViewById(R.id.chat_title);
			title.setText(StringUtils.parseName(destJid) + " " + getResources().getString(R.string.is_talking));
			cipher = RosterManager.isSecure(destJid);
			destCert = null;
			if (cipher) {
				Bitmap bm = AvatarsCache.getAvatar(destJid);
				try {
					destCert = Decode.decode(bm);
				} catch (Exception e) {
					Log.e(TAG, "Error decodificando el certificado del destinatario");
					e.printStackTrace();
				}
			}

			Log.i(TAG, "Creado chat con " + destJid + " cifrado=" + cipher);
		}
	}

	private void initializeMessageListener() {
		Chat destChat;
		if (ChatMan.openedChats.containsKey(destJid)) {
			destChat = ChatMan.openedChats.get(destJid);
			if (destChat.getListeners().isEmpty()) {
				destChat.addMessageListener(messageListener);
			}
		} else {
			destChat = ChatMan.createChat(destJid, messageListener);
			if (cipher) {
				Message m = new Message(destJid);
				try {
					destChat.sendMessage(m);
				} catch (XMPPException e) {
					e.printStackTrace();
				}
			}

		}
	}

	public void send(View view) {
		Message message = new Message(destJid);
		EditText editText = (EditText) findViewById(R.id.textInput);
		String plainText = editText.getText().toString();
		editText.setText("");

		message.setFrom(myJid);
		message.setTo(destJid);

		Message m = new Message();
		m.setFrom(myJid);
		m.setBody(plainText);
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		m.setSubject(sdf.format(new Date()));
		listMessages.add(m);
		refreshAdapter();
		myListView.smoothScrollToPosition(adapter.getCount() - 1);
		if (cipher) {
			try {
				String encodedMessage = RSA.cipher(plainText, destCert.getPublicKey());
				message.setBody(encodedMessage);
				Log.i(TAG, "Enviando cifrado: " + message.getBody() + " " + plainText);
			} catch (Exception e) {
				Log.e(TAG, "ERROR al cifrar");
				e.printStackTrace();
			}
		} else {
			message.setBody(plainText);
			Log.i(TAG, "Enviando mensaje plano: " + message.getBody());
		}
		try {
			ChatMan.openedChats.get(destJid).sendMessage(message);
		} catch (XMPPException e) {
			Log.e(TAG, "ERROR al enviar mensaje");
			e.printStackTrace();
		}
	}

	private void refreshAdapter() {
		runOnUiThread(new Runnable() {
			public void run() {
				adapter.notifyDataSetChanged();
			}
		});
	}

}
