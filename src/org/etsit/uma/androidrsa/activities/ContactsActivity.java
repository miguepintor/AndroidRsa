
package org.etsit.uma.androidrsa.activities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import org.etsit.uma.androidrsa.R;
import org.etsit.uma.androidrsa.adapters.ContactsAdapter;
import org.etsit.uma.androidrsa.utils.AndroidRsaConstants;
import org.etsit.uma.androidrsa.utils.PresenceComparator;
import org.etsit.uma.androidrsa.xmpp.AvatarsCache;
import org.etsit.uma.androidrsa.xmpp.ChatMan;
import org.etsit.uma.androidrsa.xmpp.Conexion;
import org.etsit.uma.androidrsa.xmpp.RosterManager;
import org.etsit.uma.androidrsa.xmpp.Status;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.util.StringUtils;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class ContactsActivity extends ListActivity {
	private static final String TAG = "ContactsActivity";
	private Connection connection;
	private ArrayList<Presence> listaPresences = new ArrayList<Presence>();
	private boolean showAll = true;
	private ContactsAdapter adapter;
	private ListView myListView;
	public String passPhrase;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		connection = Conexion.getInstance();

		Roster.setDefaultSubscriptionMode(Roster.SubscriptionMode.accept_all);
		RosterManager.getRosterInstance().setSubscriptionMode(Roster.SubscriptionMode.accept_all);

		passPhrase = getIntent().getStringExtra(AndroidRsaConstants.PASSPHRASE);

		loadUI();

		ChatMan.initListener(this);

		RosterManager.getRosterInstance().addRosterListener(new RosterListener() {
			public void entriesDeleted(Collection<String> addresses) {
				Log.d(TAG, "EntriesDeleted: " + addresses.toString());
				refreshAdapter();
			}

			public void entriesUpdated(Collection<String> addresses) {
				Log.d(TAG, "EntriesUpdated: " + addresses.toString());
				refreshAdapter();
			}

			public void presenceChanged(Presence presence) {
				Log.d(TAG, "Presence changed: " + presence.getFrom() + " " + presence.getMode());
				removePresence(presence);
				String name = StringUtils.parseName(presence.getFrom());
				presence.setProperty("name", name);
				listaPresences.add(presence);
				Collections.sort(listaPresences, new PresenceComparator());

				AvatarsCache.getInstance().put(presence.getFrom(), AvatarsCache.getAvatar(presence.getFrom()));
				refreshAdapter();
			}

			public void entriesAdded(Collection<String> addresses) {
				Log.d(TAG, "EntriesAdded: " + addresses.toString());
				refreshAdapter();
			}
		});
	}

	private void loadUI() {
		loadContacts();
		setContentView(R.layout.contacts);
		myListView = getListView();
		View headerView = getLayoutInflater().inflate(R.layout.header_contacts, null);
		headerView.setClickable(false);
		myListView.addHeaderView(headerView);
		myListView.setDivider(null);
		adapter = new ContactsAdapter(this, listaPresences);
		setListAdapter(adapter);

		myListView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (position > 0) {
					Intent i = new Intent(ContactsActivity.this, ChatActivity.class);
					i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
					i.putExtra(AndroidRsaConstants.DEST_JID, listaPresences.get(position - 1).getFrom());
					i.putExtra(AndroidRsaConstants.PASSPHRASE, passPhrase);
					startActivity(i);
				}
			}

		});
	}

	private void loadContacts() {
		Collection<RosterEntry> entries = RosterManager.getRosterInstance().getEntries();
		listaPresences.clear();
		for (RosterEntry entry : entries) {
			if (showAll) {
				Iterator<Presence> it = RosterManager.getRosterInstance().getPresences(entry.getUser());
				while (it.hasNext()) {
					Presence p = it.next();
					p.setProperty("name", StringUtils.parseName(p.getFrom()));
					listaPresences.add(p);
					Log.d(TAG, "AÑADIDO contacto from:" + p.getFrom() + " name:" + p.getProperty("name"));
				}
			} else if (!showAll) {
				Iterator<Presence> it = RosterManager.getRosterInstance().getPresences(entry.getUser());
				while (it.hasNext()) {
					Presence p = it.next();
					int status = Status.getStatusFromPresence(p);
					if ((status == Status.CONTACT_STATUS_AVAILABLE)
							|| (status == Status.CONTACT_STATUS_AVAILABLE_FOR_CHAT)
							|| (status == Status.CONTACT_STATUS_AWAY) || (status == Status.CONTACT_STATUS_BUSY)) {
						p.setProperty("name", StringUtils.parseName(p.getFrom()));
						listaPresences.add(p);
						Log.d(TAG, "AÑADIDO contacto from:" + p.getFrom() + " name:" + p.getProperty("name"));
					}
				}
			}
		}
		Collections.sort(listaPresences, new PresenceComparator());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_contacts, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.MenuToggle:
			showAll = !showAll;
			loadContacts();
			refreshAdapter();
			return true;
		case R.id.MenuChangeState:
			return true;
		case R.id.available:
			Presence presence = new Presence(Presence.Type.available);
			presence.setMode(Presence.Mode.available);
			presence.setStatus("aqui estamos ya!");
			connection.sendPacket(presence);
			return true;
		case R.id.away:
			Presence presence2 = new Presence(Presence.Type.available);
			presence2.setStatus("De parranda!");
			presence2.setMode(Presence.Mode.away);
			connection.sendPacket(presence2);
			return true;
		case R.id.busy:
			Presence presence3 = new Presence(Presence.Type.available);
			presence3.setStatus("Trabajando!");
			presence3.setMode(Presence.Mode.dnd);
			connection.sendPacket(presence3);
			return true;
		case R.id.unavailable:
			Presence presence4 = new Presence(Presence.Type.unavailable);
			presence4.setStatus("Invisible!");
			connection.sendPacket(presence4);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void refreshAdapter() {
		runOnUiThread(new Runnable() {
			public void run() {
				adapter.notifyDataSetChanged();
			}
		});
	}

	@Override
	public void onBackPressed() {
		Conexion.disconnect();
		Intent i = new Intent(this, LoginActivity.class);
		i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(i);
	}

	private void removePresence(Presence presence) {
		for (Iterator<Presence> it = listaPresences.iterator(); it.hasNext();) {
			Presence p = it.next();
			if (p.getFrom().equals(presence.getFrom())) {
				it.remove();
			}
		}
	}

}
