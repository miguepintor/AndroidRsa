
package org.inftel.androidrsa.activities;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import org.inftel.androidrsa.R;
import org.inftel.androidrsa.adapters.ContactsAdapter;
import org.inftel.androidrsa.utils.AndroidRsaConstants;
import org.inftel.androidrsa.utils.PresenceComparator;
import org.inftel.androidrsa.utils.ReadFileAsByteArray;
import org.inftel.androidrsa.xmpp.AvatarsCache;
import org.inftel.androidrsa.xmpp.ChatMan;
import org.inftel.androidrsa.xmpp.Conexion;
import org.inftel.androidrsa.xmpp.RosterManager;
import org.inftel.androidrsa.xmpp.Status;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smackx.packet.VCard;
import org.jivesoftware.smackx.provider.VCardProvider;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
    public static Roster roster;
    private ArrayList<Presence> listaPresences = new ArrayList<Presence>();
    private boolean showAll = true;
    private ContactsAdapter adapter;
    private ListView myListView;
    public static ChatMan chatMan;
    public static String passPhrase;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        connection = Conexion.getInstance();

        VCard vCard = new VCard();
        SharedPreferences prefs = getSharedPreferences(
                AndroidRsaConstants.SHARED_PREFERENCE_FILE,
                Context.MODE_PRIVATE);
        String avatarPath = prefs.getString(AndroidRsaConstants.ENCODED_IMAGE_PATH,
                "default");
        ProviderManager.getInstance().addIQProvider("vCard",
                "vcard-temp",
                new VCardProvider());
        try {
            vCard.load(connection);

            if (!avatarPath.equals("default")) {
                byte[] bytes = ReadFileAsByteArray.getBytesFromFile(new File(avatarPath));
                vCard.setAvatar(bytes);
                Thread.sleep(10000);
                vCard.save(connection);
                Thread.sleep(1000);
            }

        } catch (XMPPException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        roster = RosterManager.getRosterInstance();
        Roster.setDefaultSubscriptionMode(Roster.SubscriptionMode.accept_all);
        roster.setSubscriptionMode(Roster.SubscriptionMode.accept_all);

        Bundle bundle = getIntent().getExtras();
        passPhrase = bundle.getString(AndroidRsaConstants.PASSPHRASE);

        pintarUI();
        chatMan = new ChatMan(this);
        chatMan.initListener();

    }

    private void loadContacts() {
        roster = RosterManager.getRosterInstance();
        Collection<RosterEntry> entries = roster.getEntries();
        listaPresences.clear();
        for (RosterEntry entry : entries) {
            if ((showAll) && (entry.getName() != null)) {
                Iterator<Presence> it = roster.getPresences(entry.getUser());
                while (it.hasNext()) {
                    Presence p = it.next();
                    p.setProperty("name", entry.getName());
                    listaPresences.add(p);
                    Log.d(TAG, "AÑADIDO from:" + p.getFrom() + " name:" +
                            entry.getName());
                }
            }
            else if (((!showAll) && (entry.getName() != null))) {
                Iterator<Presence> it = roster.getPresences(entry.getUser());
                while (it.hasNext()) {
                    Presence p = it.next();
                    int status = Status.getStatusFromPresence(p);
                    if ((status == Status.CONTACT_STATUS_AVAILABLE)
                            || (status == Status.CONTACT_STATUS_AVAILABLE_FOR_CHAT)
                            || (status == Status.CONTACT_STATUS_AWAY)
                            || (status == Status.CONTACT_STATUS_BUSY)) {
                        p.setProperty("name", entry.getName());
                        listaPresences.add(p);
                        Log.d(TAG, "AÑADIDO from:" + p.getFrom() + " name:" +
                                entry.getName());
                    }
                }
            }
        }
        Collections.sort(listaPresences, new PresenceComparator());
    }

    private void pintarUI() {
        loadContacts();
        myListView = getListView();
        View headerView = getLayoutInflater().inflate(R.layout.header_contacts, null);
        myListView.addHeaderView(headerView);
        myListView.setDivider(null);
        adapter = new ContactsAdapter(this, listaPresences);
        setListAdapter(adapter);

        myListView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                Intent i = new Intent(getApplicationContext(), ChatActivity.class);
                i.putExtra("destJid", listaPresences.get(position - 1).getFrom());
                i.putExtra(AndroidRsaConstants.PASSPHRASE, passPhrase);
                startActivity(i);
            }

        });

        roster.addRosterListener(new RosterListener() {
            public void entriesDeleted(Collection<String> addresses) {
                Log.d(TAG, "EntriesDeleted: " + addresses.toString());
                // loadContacts();
                refreshAdapter();
            }

            public void entriesUpdated(Collection<String> addresses) {
                Log.d(TAG, "EntriesUpdated: " + addresses.toString());
                // loadContacts();
                refreshAdapter();
            }

            public void presenceChanged(Presence presence) {
                Log.d(TAG, "Presence changed: " + presence.getFrom() + " " + presence.getMode());
                // loadContacts();
                removePresence(presence);
                presence.setProperty("name", roster.getEntry(presence.getFrom()).getName());
                listaPresences.add(presence);
                Collections.sort(listaPresences, new PresenceComparator());

                AvatarsCache.getInstance().put(presence.getFrom(),
                        AvatarsCache.getAvatar(presence.getFrom()));
                refreshAdapter();
            }

            public void entriesAdded(Collection<String> addresses) {
                Log.d(TAG, "EntriesAdded: " + addresses.toString());
                // loadContacts();
                refreshAdapter();
            }
        });

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
        super.onBackPressed();
    }

    public void removePresence(Presence presence) {
        for (Presence p : listaPresences) {
            if (p.getFrom().equals(presence.getFrom())) {
                listaPresences.remove(presence);
            }
        }
    }

}
