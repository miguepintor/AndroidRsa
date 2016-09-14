
package org.etsit.uma.androidrsa.xmpp;

import org.etsit.uma.androidrsa.utils.AndroidRsaConstants;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.util.StringUtils;

public class RosterManager {
	private static Roster roster;

	public static Roster getRosterInstance() {
		if (roster == null) {
			roster = Conexion.getInstance().getRoster();
		}
		return roster;
	}

	public static boolean isSecure(String jid) {
		return StringUtils.parseResource(jid).startsWith(AndroidRsaConstants.ANDROIDRSA_APP_NAME);
	}
}
