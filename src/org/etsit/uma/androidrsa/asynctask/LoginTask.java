
package org.etsit.uma.androidrsa.asynctask;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.etsit.uma.androidrsa.R;
import org.etsit.uma.androidrsa.activities.ContactsActivity;
import org.etsit.uma.androidrsa.activities.LoginActivity;
import org.etsit.uma.androidrsa.activities.RegisterActivity;
import org.etsit.uma.androidrsa.rsa.RSA;
import org.etsit.uma.androidrsa.utils.AndroidRsaConstants;
import org.etsit.uma.androidrsa.xmpp.Conexion;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.XMPPException;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public class LoginTask extends AsyncTask<Object, Void, Boolean> {
	private static final String TAG = "LoginTask";
	private Connection con;
	private String service;
	private String user;
	private String password;
	private LoginActivity activity;
	private ProgressDialog pDialog;
	private Context ctx;

	public LoginTask(Context ctx, Connection c, String service, String user, String password, LoginActivity activity) {
		super();
		this.ctx = ctx;
		this.con = c;
		this.service = service;
		this.user = user;
		this.password = password;
		this.activity = activity;
	}

	public void onPostExecute(Boolean success) {
		if (success) {
			Log.i(TAG, "Conexión creada correctamente!");
			Log.i(TAG, "Conectado como " + con.getUser());

			SharedPreferences prefs = ctx.getSharedPreferences(AndroidRsaConstants.SHARED_PREFERENCE_FILE,
					Context.MODE_PRIVATE);

			if (prefs.getBoolean(AndroidRsaConstants.REGISTERED, false)) {
				try {
					if (RSA.verifyOwnPk(password)) {
						AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
						builder.setMessage(R.string.run_configuration_question).setCancelable(false).setPositiveButton(
								ctx.getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int id) {
										Intent i = new Intent(ctx.getApplicationContext(), RegisterActivity.class);
										i.putExtra(AndroidRsaConstants.PASSPHRASE, password);
										ctx.startActivity(i);
										dialog.dismiss();
									}
								}).setNegativeButton(ctx.getResources().getString(R.string.no),
										new DialogInterface.OnClickListener() {
											public void onClick(DialogInterface dialog, int id) {
												Intent i = new Intent(activity, ContactsActivity.class);
												i.putExtra(AndroidRsaConstants.PASSPHRASE, password);
												activity.startActivity(i);
												dialog.dismiss();
											}
										});
						AlertDialog alert = builder.create();
						alert.show();
					}
				} catch (Exception e) {
					AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
					builder.setMessage(R.string.invalid_passphrase).setCancelable(false).setPositiveButton(
							ctx.getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int id) {
									Intent i = new Intent(ctx.getApplicationContext(), RegisterActivity.class);
									i.putExtra(AndroidRsaConstants.PASSPHRASE, password);
									ctx.startActivity(i);
									dialog.dismiss();
								}
							});
					AlertDialog alert = builder.create();
					alert.show();
				}

			} else {
				Intent i = new Intent(activity, RegisterActivity.class);
				i.putExtra(AndroidRsaConstants.PASSPHRASE, password);
				activity.startActivity(i);
			}

			if (pDialog.isShowing()) {
				pDialog.dismiss();
			}
		} else {
			if (pDialog.isShowing()) {
				pDialog.dismiss();
			}
			Log.e(TAG, "ERROR al crear conexión.");
			Toast.makeText(activity, ctx.getResources().getString(R.string.connection_error), Toast.LENGTH_LONG).show();
			Conexion.disconnect();
		}
	}

	@Override
	protected Boolean doInBackground(Object... params) {
		try {
			con = Conexion.innit(service, user, password);
			return true;
		} catch (XMPPException e) {
			e.printStackTrace();
			Log.e(TAG, "Excepcion XMPP");
			con = null;
			return false;
		}
	}

	@Override
	protected void onPreExecute() {
		this.pDialog = new ProgressDialog(activity);
		this.pDialog.setMessage(" Login in... ");
		this.pDialog.show();
	}

	public void writeFile(byte[] data, String fileName) throws IOException {
		OutputStream out = new FileOutputStream(fileName);
		out.write(data);
		out.close();
	}
}
