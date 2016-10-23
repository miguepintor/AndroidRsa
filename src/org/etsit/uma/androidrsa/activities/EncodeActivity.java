
package org.etsit.uma.androidrsa.activities;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.etsit.uma.androidrsa.R;
import org.etsit.uma.androidrsa.steganography.Encoder;
import org.etsit.uma.androidrsa.utils.AndroidRsaConstants;
import org.etsit.uma.androidrsa.utils.handler.MobiProgressBar;
import org.etsit.uma.androidrsa.xmpp.Conexion;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smackx.packet.VCard;
import org.jivesoftware.smackx.provider.VCardProvider;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;

public class EncodeActivity extends Activity {
	private final MobiProgressBar progressBar = new MobiProgressBar(EncodeActivity.this);

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Obtaning intent information
		Bundle bundle = getIntent().getExtras();
		final String mChosenFilePath = bundle.getString(AndroidRsaConstants.FILE_PATH);
		final String mChosenImagePath = bundle.getString(AndroidRsaConstants.IMAGE_PATH);
		final String passphrase = bundle.getString(AndroidRsaConstants.PASSPHRASE);

		final Handler handler = new Handler();

		// Encoding..
		progressBar.setMax(100);
		progressBar.setMessage(getResources().getString(R.string.encoding));
		progressBar.show();
		Thread tt = new Thread(new Runnable() {
			public void run() {
				Encoder enconder = new Encoder(EncodeActivity.this, progressBar, handler);

				String encodedImagePath = enconder.encodeFileToImageAndSave(mChosenFilePath, mChosenImagePath);

				notifyAvatarUpdated(encodedImagePath);

				handler.post(mShowAlert);

				// Saving in prefs when run once
				SharedPreferences prefs = getSharedPreferences(AndroidRsaConstants.SHARED_PREFERENCE_FILE,
						Context.MODE_PRIVATE);
				boolean registered = prefs.getBoolean(AndroidRsaConstants.REGISTERED, false);
				if (!registered) {
					Editor prefsEditor = prefs.edit();
					prefsEditor.putBoolean(AndroidRsaConstants.REGISTERED, true);
					prefsEditor.apply();
				}

				Intent i = new Intent(EncodeActivity.this, ContactsActivity.class);
				i.putExtra(AndroidRsaConstants.PASSPHRASE, passphrase);
				startActivity(i);
			}
		});
		tt.start();

	}

	protected void notifyAvatarUpdated(String avatarPath) {
		VCard vCard = new VCard();
		ProviderManager.getInstance().addIQProvider("vCard", "vcard-temp", new VCardProvider());
		Connection connection = Conexion.getInstance();
		try {
			vCard.load(connection);

			if (!avatarPath.equals("default")) {
				byte[] bytes = getBytesFromFile(new File(avatarPath));
				vCard.setAvatar(bytes);
				Thread.sleep(1000);
				vCard.save(connection);
				Thread.sleep(1000);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private byte[] getBytesFromFile(File file) throws IOException {
		InputStream is = new FileInputStream(file);

		long length = file.length();
		byte[] bytes = new byte[(int) length];

		// Read in the bytes
		int offset = 0;
		int numRead = 0;
		while (offset < bytes.length && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
			offset += numRead;
		}

		if (offset < bytes.length) {
			is.close();
			throw new IOException("Could not completely read file " + file.getName());
		}

		is.close();
		return bytes;
	}

	final Runnable mShowAlert = new Runnable() {
		public void run() {
			progressBar.dismiss();
			AlertDialog.Builder builder = new AlertDialog.Builder(EncodeActivity.this);
			builder.setMessage(getResources().getString(R.string.saved)).setCancelable(false)
					.setPositiveButton(EncodeActivity.this.getText(R.string.ok), new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							EncodeActivity.this.finish();
						}
					});

			builder.create();
		}
	};
}
