
package org.inftel.androidrsa.xmpp;

import org.inftel.androidrsa.utils.AndroidRsaConstants;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;

import android.util.Log;

public class Conexion {
	private static final String TAG = "Conexion";
	private static Connection con = null;

	public Conexion() {
		super();
	}

	// patron singleton, unica conexion para toda la aplicacion
	public static Connection getInstance(String service, String userid, String password) throws XMPPException {
		if (service.equals("Openfire")) {
			SmackConfiguration.setPacketReplyTimeout(60000);
			Log.d(TAG, "Creando una conexión con " + AndroidRsaConstants.OPENFIRE_HOST + ":"
					+ AndroidRsaConstants.OPENFIRE_PORT);
			// Create the configuration for this new connection
			ConnectionConfiguration config = new ConnectionConfiguration(AndroidRsaConstants.OPENFIRE_HOST,
					AndroidRsaConstants.OPENFIRE_PORT, AndroidRsaConstants.OPENFIRE_SERVICE);
			config.setDebuggerEnabled(true);
			XMPPConnection.DEBUG_ENABLED = true;
			SASLAuthentication.supportSASLMechanism("PLAIN", 0);
			config.setSASLAuthenticationEnabled(true);

			con = new XMPPConnection(config);
			con.connect();
			con.login(userid, password, AndroidRsaConstants.ANDROIDRSA_APP_NAME);
		} else if (service.equals("Gmail")) {
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
		} else if (service.equals("Facebook")) {
			ConnectionConfiguration config = new ConnectionConfiguration("chat.facebook.com", 5222);
			config.setSASLAuthenticationEnabled(true);

			SmackConfiguration.setPacketReplyTimeout(15000);
			XMPPConnection.DEBUG_ENABLED = true;
			SASLAuthentication.registerSASLMechanism("X-FACEBOOK-PLATFORM", SASLXFacebookPlatformMechanism.class);
			SASLAuthentication.supportSASLMechanism("X-FACEBOOK-PLATFORM", 0);
			con = new XMPPConnection(config);
			con.connect();
			String apiKey = "3290292390239";
			String accessToken = "ADSJDSJKDKSJKSJD0-43DKJSDJKSDKJSD094JJSDKJSDKJDSNDSKJNSDLkljdkjs";
			con.login(apiKey, accessToken);
		} else {
			con = null;
		}

		return con;

	}

	public static Connection getInstance() {
		if (con != null) {
			Log.d(TAG, "La conexión ya existe,devolviendo!");
			return con;
		} else {
			throw new RuntimeException("Error, no esta logueado.");
		}
	}

	public static void disconnect() {
		if ((con != null) && (con.isConnected())) {
			con.disconnect();
		}
		con = null;
	}

}
