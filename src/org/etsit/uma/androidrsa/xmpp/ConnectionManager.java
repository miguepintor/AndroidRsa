
package org.etsit.uma.androidrsa.xmpp;

import java.util.List;

import org.etsit.uma.androidrsa.R;
import org.etsit.uma.androidrsa.utils.AndroidRsaConstants;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;

import android.content.res.Resources;
import android.util.Log;

public class ConnectionManager {
	private static final String TAG = "Conexion";
	private static Connection con = null;

	public ConnectionManager() {
		super();
	}

	public static void innit(Resources resources, String service, String userid, String password) throws XMPPException {
		if (service.equals(resources.getString(R.string.openfire))) {
			SmackConfiguration.setPacketReplyTimeout(60000);
			Log.d(TAG, "Creando una conexión con " + AndroidRsaConstants.OPENFIRE_HOST + ":"
					+ AndroidRsaConstants.OPENFIRE_PORT);
			// Create the configuration for this new connection
			ConnectionConfiguration config = new ConnectionConfiguration(AndroidRsaConstants.OPENFIRE_HOST,
					AndroidRsaConstants.OPENFIRE_PORT, AndroidRsaConstants.OPENFIRE_SERVICE);
			config.setDebuggerEnabled(true);
			XMPPConnection.DEBUG_ENABLED = true;
			List<Class> clases = SASLAuthentication.getRegisterSASLMechanisms();
			SASLAuthentication.supportSASLMechanism("DIGEST-MD5", 0);
			
			config.setSASLAuthenticationEnabled(true);

			con = new XMPPConnection(config);
			con.connect();
			con.login(userid, password, AndroidRsaConstants.ANDROIDRSA_APP_NAME);
		} else if (service.equals(resources.getString(R.string.gmail))) {
			SmackConfiguration.setPacketReplyTimeout(60000);
			Log.d(TAG, "Creando una conexión con " + AndroidRsaConstants.GMAIL_HOST + ":"
					+ AndroidRsaConstants.GMAIL_PORT);
			// Create the configuration for this new connection
			ConnectionConfiguration config = new ConnectionConfiguration(AndroidRsaConstants.GMAIL_HOST,
					AndroidRsaConstants.GMAIL_PORT, AndroidRsaConstants.GMAIL_SERVICE);
			config.setDebuggerEnabled(true);
			XMPPConnection.DEBUG_ENABLED = true;
			SASLAuthentication.supportSASLMechanism("PLAIN", 0);
			config.setSASLAuthenticationEnabled(true);

			con = new XMPPConnection(config);
			con.connect();
			con.login(userid, password, AndroidRsaConstants.ANDROIDRSA_APP_NAME);
		} else {
			con = null;
		}

		RosterManager.innit();
	}

	public static void innitCustomService(String userid, String password, String host, int port, String xmppService)
			throws XMPPException {

		SmackConfiguration.setPacketReplyTimeout(60000);
		Log.d(TAG, "Cerando una conexión personalizada con " + host + ":" + port);
		ConnectionConfiguration config = new ConnectionConfiguration(host, port, xmppService);
		config.setDebuggerEnabled(true);
		XMPPConnection.DEBUG_ENABLED = true;
		SASLAuthentication.supportSASLMechanism("PLAIN", 0);
		config.setSASLAuthenticationEnabled(true);

		con = new XMPPConnection(config);
		con.connect();
		con.login(userid, password, AndroidRsaConstants.ANDROIDRSA_APP_NAME);

		RosterManager.innit();
	}

	public static Connection getInstance() {
		if (con == null) {
			throw new RuntimeException("Error, no esta logueado.");
		}

		Log.d(TAG, "La conexión ya existe,devolviendo!");
		return con;
	}

	public static void disconnect() {
		if ((con != null) && (con.isConnected())) {
			con.disconnect();
		}
		con = null;
	}

}
