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
import org.etsit.uma.androidrsa.xmpp.ConnectionManager;
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
	private LoginActivity activity;
	private ProgressDialog pDialog;

	public LoginTask(LoginActivity activity) {
		super();
		this.activity = activity;
	}

	public void onPostExecute(Boolean success) {
		if (success) {
			Log.i(TAG, "Conexión creada correctamente!");

			SharedPreferences prefs = activity.getSharedPreferences(AndroidRsaConstants.SHARED_PREFERENCE_FILE,
					Context.MODE_PRIVATE);

			if (prefs.getBoolean(AndroidRsaConstants.REGISTERED, false)) {
				try {
					if (RSA.verifyOwnPk(activity.password)) {
						AlertDialog.Builder builder = new AlertDialog.Builder(activity);
						builder.setMessage(R.string.run_configuration_question).setCancelable(false).setPositiveButton(
								activity.getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int id) {
										Intent i = new Intent(activity.getApplicationContext(), RegisterActivity.class);
										i.putExtra(AndroidRsaConstants.PASSPHRASE, activity.password);
										activity.startActivity(i);
										dialog.dismiss();
									}
								}).setNegativeButton(activity.getResources().getString(R.string.no),
										new DialogInterface.OnClickListener() {
											public void onClick(DialogInterface dialog, int id) {
												Intent i = new Intent(activity, ContactsActivity.class);
												i.putExtra(AndroidRsaConstants.PASSPHRASE, activity.password);
												activity.startActivity(i);
												dialog.dismiss();
											}
										});
						AlertDialog alert = builder.create();
						alert.show();
					}
				} catch (Exception e) {
					AlertDialog.Builder builder = new AlertDialog.Builder(activity);
					builder.setMessage(R.string.invalid_passphrase).setCancelable(false).setPositiveButton(
							activity.getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int id) {
									Intent i = new Intent(activity.getApplicationContext(), RegisterActivity.class);
									i.putExtra(AndroidRsaConstants.PASSPHRASE, activity.password);
									activity.startActivity(i);
									dialog.dismiss();
								}
							});
					AlertDialog alert = builder.create();
					alert.show();
				}

			} else {
				Intent i = new Intent(activity, RegisterActivity.class);
				i.putExtra(AndroidRsaConstants.PASSPHRASE, activity.password);
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
			Toast.makeText(activity, activity.getResources().getString(R.string.connection_error), Toast.LENGTH_LONG).show();
			ConnectionManager.disconnect();
		}
	}

	@Override
	protected Boolean doInBackground(Object... params) {
		try {
			if(activity.isCustomService){
				ConnectionManager.innitCustomService(activity.username, activity.password, activity.host, activity.port, activity.service);
			} else {
				ConnectionManager.innit(activity.getResources(), activity.service, activity.username, activity.password);
			}
			return true;
		} catch (XMPPException e) {
			e.printStackTrace();
			ConnectionManager.disconnect();
			return false;
		}
	}

	@Override
	protected void onPreExecute() {
		this.pDialog = new ProgressDialog(activity);
		this.pDialog.setMessage(activity.getResources().getString(R.string.logining));
		this.pDialog.show();
	}

	public void writeFile(byte[] data, String fileName) throws IOException {
		OutputStream out = new FileOutputStream(fileName);
		out.write(data);
		out.close();
	}
}
