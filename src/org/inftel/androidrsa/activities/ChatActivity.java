
package org.inftel.androidrsa.activities;

import java.io.IOException;
import java.security.PrivateKey;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.security.cert.Certificate;
import javax.security.cert.CertificateException;

import org.inftel.androidrsa.R;
import org.inftel.androidrsa.adapters.ChatAdapter;
import org.inftel.androidrsa.rsa.KeyStore;
import org.inftel.androidrsa.rsa.RSA;
import org.inftel.androidrsa.steganography.Decode;
import org.inftel.androidrsa.utils.AndroidRsaConstants;
import org.inftel.androidrsa.xmpp.AvatarsCache;
import org.inftel.androidrsa.xmpp.ChatMan;
import org.inftel.androidrsa.xmpp.Conexion;
import org.inftel.androidrsa.xmpp.RosterManager;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.util.StringUtils;

import android.app.ListActivity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.EditText;
import android.widget.ListView;

public class ChatActivity extends ListActivity {
	private static final String TAG = "ChatActivity";
	private Connection connection;
	public ChatMan chatMan;
	private Chat chat = null;
	private Roster roster;
	private static ArrayList<Message> listMessages = new ArrayList<Message>();
	private ChatAdapter adapter;
	private static ListView myListView;
	private String destJid;
	private String myJid;
	private boolean cipher;
	private Certificate cert;
	private String passPhrase;
	private Context context;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		context = getApplicationContext();
		this.connection = Conexion.getInstance();
		this.roster = RosterManager.getRosterInstance();
		chatMan = ContactsActivity.chatMan;
		chat = chatMan.chat;
		super.onCreate(savedInstanceState);
		setContentView(R.layout.chat);
		destJid = getIntent().getStringExtra("destJid");
		myJid = this.connection.getUser();
		cipher = RosterManager.isSecure(destJid);
		Bundle bundle = getIntent().getExtras();
		passPhrase = bundle.getString(AndroidRsaConstants.PASSPHRASE);

		Log.d(TAG, "Creado chat con " + roster.getEntry(StringUtils.parseBareAddress(destJid)).getName() + " cifrado="
				+ cipher);

		if (chat == null) {
			chatMan.createChat(destJid, messageListener);
			chat = chatMan.getChat();
			if (cipher) {
				Message m = new Message(destJid);
				try {
					chat.sendMessage(m);
				} catch (XMPPException e) {
					e.printStackTrace();
				}
			}
		} else {
			chat.addMessageListener(messageListener);
		}

		adapter = new ChatAdapter(this, listMessages);
		setListAdapter(adapter);
		myListView = getListView();
		myListView.setDivider(null);

		if (cipher) {
			Bitmap bm = AvatarsCache.getAvatar(destJid);
			try {
				cert = Decode.decode(bm);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (CertificateException e) {
				e.printStackTrace();
			}

		}
	}

	public void animate(View view) {
		Animation animation = new AlphaAnimation(0.0f, 1.0f);
		animation.setDuration(1000);
		animation.setStartOffset(500);
		view.startAnimation(animation);
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

		if (!cipher) {
			try {
				message.setBody(plainText);
				chatMan.getChat().sendMessage(message);
				Log.d(TAG, "Enviando: " + message.getBody());

			} catch (XMPPException e) {
				Log.d(TAG, "ERROR al enviar mensaje");
			}
		} else {
			try {
				String encodedMessage = RSA.cipher(plainText, cert.getPublicKey());
				message.setBody(encodedMessage);
				chatMan.getChat().sendMessage(message);
				Log.d(TAG, "Enviando cifrado: " + message.getBody() + " " + plainText);

			} catch (Exception e) {
				Log.d(TAG, "PETO ENVIANDO CIFRADOOOO");
				e.printStackTrace();
			}
		}
	}

	private MessageListener messageListener = new MessageListener() {
		public void processMessage(Chat chat, Message message) {
			if ((message.getBody() != null) && (!message.getType().equals(Message.Type.error))) {
				if (!cipher) {

					Log.i(TAG, "Recibido mensaje plano: " + message.getBody());
					listMessages.add(message);
					refreshAdapter();
					myListView.smoothScrollToPosition(adapter.getCount() - 1);
				} else {

					try {
						PrivateKey pk = RSA.getPrivateKeyDecryted(KeyStore.getInstance().getPk(), passPhrase);
						String decodedMessage = RSA.decipher(message.getBody(), pk);
						Log.i(TAG, "Recibido mensaje cifrado: " + decodedMessage);

						Message m = new Message();
						m.setFrom(message.getFrom());
						m.setTo(message.getTo());

						m.setBody(decodedMessage);
						SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
						m.setSubject(sdf.format(new Date()));
						listMessages.add(m);
						refreshAdapter();
						myListView.smoothScrollToPosition(adapter.getCount() - 1);

					} catch (Exception e) {
						Log.d(TAG, "PETO AL DESCIFRAR");
						e.printStackTrace();

					}

				}
			}

		}

	};

	private void refreshAdapter() {
		runOnUiThread(new Runnable() {
			public void run() {
				adapter.notifyDataSetChanged();
			}
		});
	}

	@Override
	public void onBackPressed() {
		chatMan.chat = null;
		chat = null;
		super.onBackPressed();
	}

}
