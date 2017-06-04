package org.etsit.uma.androidrsa.activities;

import org.etsit.uma.androidrsa.R;
import org.etsit.uma.androidrsa.rsa.RSA;
import org.etsit.uma.androidrsa.utils.AndroidRsaConstants;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;

public class EncryptationPasswordActivity extends EncodeableActivity {
	private static final int DIALOG_INVALID_ENCRYPTATION_PASSWORD = 1010;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mChosenCertificatePath = getIntent().getStringExtra(AndroidRsaConstants.FILE_PATH);
		mChosenKeyPath = getIntent().getStringExtra(AndroidRsaConstants.KEY_FILE_PATH);
		mChosenImagePath = getIntent().getStringExtra(AndroidRsaConstants.IMAGE_PATH);
		passphrase = getIntent().getStringExtra(AndroidRsaConstants.PASSPHRASE);

		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.encryptation_password);
	}

	protected void dialogBuild(AlertDialog.Builder builder, int id) {
		switch (id) {
		case DIALOG_INVALID_ENCRYPTATION_PASSWORD:
			builder.setMessage(R.string.invalid_encryption_password).setCancelable(false)
					.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.dismiss();
						}
					});
			break;
		}
	}

	public void onClickButtonOk(View view) {
		String encryptationPassword = ((EditText) findViewById(R.id.encryptation_password)).getText().toString();
		if (encryptationPassword != null && !encryptationPassword.equals("")) {
			try {
				RSA.decryptEncryptPrivateKeyAndSave(AndroidRsaConstants.OWN_KEY_PATH, encryptationPassword, passphrase);
				storeAndEncode();
			} catch (Exception e) {
				showDialog(DIALOG_INVALID_ENCRYPTATION_PASSWORD);
			}
		} else {
			Toast.makeText(this, getResources().getString(R.string.info_empty_fields), Toast.LENGTH_LONG).show();
		}
	}
}
