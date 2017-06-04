package org.etsit.uma.androidrsa.xmpp;

import org.etsit.uma.androidrsa.utils.AndroidRsaConstants;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.util.StringUtils;

import android.util.Log;

public class RosterManager {
	private static final String TAG = "RosterManager";
	private static Roster roster;

	public static Roster getRosterInstance() {
		if (roster == null) {
			throw new RuntimeException("Error, roster no inicializado.");
		}
		Log.d(TAG, "Roster ya existe,devolviendo!");
		return roster;
	}

	public static boolean isSecure(String jid) {
		return StringUtils.parseResource(jid).startsWith(AndroidRsaConstants.ANDROIDRSA_APP_NAME);
	}
	
	public static void innit(){
		roster = ConnectionManager.getInstance().getRoster();
	}
}
