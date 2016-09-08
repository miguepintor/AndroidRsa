
package org.inftel.androidrsa.xmpp;

import org.inftel.androidrsa.utils.AndroidRsaConstants;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.util.StringUtils;

public class RosterManager {
	private static Roster roster = Conexion.getInstance().getRoster();

	public static Roster getRosterInstance() {
		return Conexion.getInstance().getRoster();
	}

	public static RosterEntry findByName(String name) {
		for (RosterEntry entry : roster.getEntries()) {
			if ((entry.getName() != null) && (entry.getName().equals(name))) {
				return entry;
			}
		}
		return null;
	}

	public static RosterEntry findByJid(String jid) {
		for (RosterEntry entry : roster.getEntries()) {
			if ((entry.getName() != null) && (roster.getPresence(entry.getUser()).getFrom().equals(jid))) {
				return entry;
			}
		}
		return null;
	}

	public static boolean isSecure(String jid) {
		return StringUtils.parseResource(jid).startsWith(AndroidRsaConstants.ANDROIDRSA_APP_NAME);
	}
}
