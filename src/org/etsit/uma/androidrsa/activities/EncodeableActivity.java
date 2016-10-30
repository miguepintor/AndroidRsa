package org.etsit.uma.androidrsa.activities;

import java.io.File;
import java.security.SignatureException;

import javax.security.cert.CertificateException;

import org.apache.commons.io.FileUtils;
import org.etsit.uma.androidrsa.R;
import org.etsit.uma.androidrsa.rsa.KeyStore;
import org.etsit.uma.androidrsa.rsa.RSA;
import org.etsit.uma.androidrsa.utils.AndroidRsaConstants;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public abstract class EncodeableActivity extends Activity {
	
	protected String mChosenCertificatePath;
	protected String mChosenKeyPath;
	protected String mChosenImagePath;
	protected String passphrase;

	public static final int DIALOG_INVALID_CERTIFICATE = 1002;
	public static final int DIALOG_INVALID_SIGN_CERTIFICATE = 1003;

	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog = null;
		AlertDialog.Builder builder = new Builder(this);

		switch (id) {
		case DIALOG_INVALID_CERTIFICATE:
			builder.setMessage(R.string.invalid_certificate).setCancelable(false)
					.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.dismiss();
						}
					});
			break;
		case DIALOG_INVALID_SIGN_CERTIFICATE:
			builder.setMessage(R.string.invalid_sign_certificate).setCancelable(false)
					.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.dismiss();
						}
					});
			break;
		default:
			dialogBuild(builder, id);
			break;
		}
		dialog = builder.show();
		return dialog;
	}
	
	protected abstract void dialogBuild(AlertDialog.Builder builder, int id);
	
	protected void storeAndEncode() {
		// Stores my own certificate, my own private key and the public
		// key of the CA
		try {

			// loading keystore
			KeyStore.getInstance().setCertificate(AndroidRsaConstants.OWN_ALIAS,
					RSA.readCertificate(mChosenCertificatePath));
			KeyStore.getInstance().setPk(FileUtils.readFileToByteArray(new File(mChosenKeyPath)));
			KeyStore.getInstance().setCaPb(RSA.getCAPublicKey(getApplicationContext()));
			KeyStore.getInstance().getCertificate(AndroidRsaConstants.OWN_ALIAS).verify(KeyStore.getInstance().getCaPb());

			// user have been registered
			SharedPreferences prefs = getSharedPreferences(AndroidRsaConstants.SHARED_PREFERENCE_FILE,
					Context.MODE_PRIVATE);
			Editor prefsEditor = prefs.edit();
			prefsEditor.putString(AndroidRsaConstants.CERT_PATH, mChosenCertificatePath);
			prefsEditor.putString(AndroidRsaConstants.KEY_PATH, mChosenKeyPath);
			prefsEditor.apply();

			// Applying steganography
			Intent i = new Intent(this, EncodeActivity.class);
			i.putExtra(AndroidRsaConstants.FILE_PATH, mChosenCertificatePath);
			i.putExtra(AndroidRsaConstants.IMAGE_PATH, mChosenImagePath);
			i.putExtra(AndroidRsaConstants.PASSPHRASE, passphrase);
			startActivity(i);

		} catch (CertificateException e) {
			showDialog(DIALOG_INVALID_CERTIFICATE);
			e.printStackTrace();
		} catch (SignatureException e) {
			showDialog(DIALOG_INVALID_SIGN_CERTIFICATE);
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
