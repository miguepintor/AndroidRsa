
package org.etsit.uma.androidrsa.activities;

import java.io.File;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.SignatureException;
import java.util.Collection;

import javax.security.cert.CertificateException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
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
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

public class RegisterActivity extends Activity {

	private static final String TAG = "RegisterActivity";
	private static final int ACTIVITY_SELECT_IMAGE = 100;

	private File[] mFilesList;
	private File mChosenFile;

	private Bitmap mChosenImage;
	private String mChosenImagePath;

	private String passphrase;

	private static final int DIALOG_LOAD_FILE = 1000;

	private static final int DIALOG_NOT_CHOSEN = 1002;
	private static final int DIALOG_KEY_NOT_FOUND = 1003;
	private static final int DIALOG_INVALID_CERTIFICATE = 1004;
	private static final int DIALOG_INVALID_KEY = 1005;
	private static final int DIALOG_INVALID_SIGN_CERTIFICATE = 1006;
	private static final int DIALOG_IMAGE_TOO_LARGE = 1007;
	private static final int DIALOG_IMAGE_SIZE_FAIL = 1008;
	private static final int DIALOG_RUN_ONCE = 1009;
	private static final int DIALOG_RUN_OTHER = 1010;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.register);
		SharedPreferences prefs = getSharedPreferences(AndroidRsaConstants.SHARED_PREFERENCE_FILE,
				Context.MODE_PRIVATE);
		boolean registered = prefs.getBoolean(AndroidRsaConstants.REGISTERED, false);

		Bundle bundle = getIntent().getExtras();
		passphrase = bundle.getString(AndroidRsaConstants.PASSPHRASE);

		if (registered) {
			showDialog(DIALOG_RUN_OTHER);
		} else {
			showDialog(DIALOG_RUN_ONCE);
		}
	}

	private void moveRawResourcesToAndroidRsaFolder() {
		try {
			FileUtils.copyInputStreamToFile(getResources().openRawResource(R.raw.certificate),
					new File(AndroidRsaConstants.OWN_CERT_PATH));
			FileUtils.copyInputStreamToFile(getResources().openRawResource(R.raw.certificatekey),
					new File(AndroidRsaConstants.OWN_KEY_PATH));
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void onClickPickImage(View view) throws IOException {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("image/*");
		startActivityForResult(intent, ACTIVITY_SELECT_IMAGE);

	}

	public void onClickButtonPickCertificate(View view) throws IOException {
		loadFileList();
		showDialog(DIALOG_LOAD_FILE);
	}

	// Find *.crt in the sd
	private void loadFileList() {
		File extPath = AndroidRsaConstants.EXTERNAL_STORAGE_PATH;
		if (extPath.exists()) {
			Collection<File> fileList = FileUtils.listFiles(extPath,
					new RegexFileFilter("^(.*\\" + AndroidRsaConstants.CERT_EXTENSION + ")"),
					DirectoryFileFilter.DIRECTORY);
			mFilesList = (File[]) fileList.toArray();
		}
	}

	// Dialogs
	protected Dialog onCreateDialog(int id) {
		Dialog dialog = null;
		AlertDialog.Builder builder = new Builder(this);

		switch (id) {
		case DIALOG_RUN_ONCE:
			final EditText input = new EditText(this);
			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
					LinearLayout.LayoutParams.MATCH_PARENT);
			input.setLayoutParams(lp);
			builder.setView(input);
			builder.setMessage(R.string.first_time_configuration).setCancelable(false)
					.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							String password = input.getText().toString();
							if(password == null || password ==""){
								Toast.makeText(RegisterActivity.this, getResources().getString(R.string.info_empty_fields), Toast.LENGTH_LONG).show();
							} else {
								moveRawResourcesToAndroidRsaFolder();
								try {
									RSA.decryptEncryptPrivateKeyAndSave(AndroidRsaConstants.OWN_KEY_PATH, password, passphrase);
									dialog.dismiss();
								} catch (Exception e) {
									e.printStackTrace();
									Toast.makeText(RegisterActivity.this, getResources().getString(R.string.invalid_encryption_password), Toast.LENGTH_LONG).show();
								}
								
							}
						}
					});
			break;
		case DIALOG_RUN_OTHER:
			builder.setMessage(R.string.other_time_configuration).setCancelable(false)
					.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.dismiss();
						}
					});
			break;
		case DIALOG_LOAD_FILE:
			if (mFilesList == null) {
				builder.setMessage(R.string.not_found).setCancelable(false).setPositiveButton(
						getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.dismiss();
							}
						});
			} else {
				builder.setTitle(R.string.choose_file);

				builder.setSingleChoiceItems(extractFileNamesFromFilesList(), -1,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								mChosenFile = mFilesList[which];
							}
						}).setPositiveButton(getResources().getString(R.string.ok),
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int id) {
										dialog.dismiss();
									}
								});
			}
			break;
		case DIALOG_NOT_CHOSEN:
			builder.setMessage(R.string.not_chosen).setCancelable(false)
					.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.dismiss();
						}
					});
			break;
		case DIALOG_KEY_NOT_FOUND:
			builder.setMessage(R.string.key_not_found).setCancelable(false)
					.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.dismiss();
						}
					});
			break;

		case DIALOG_INVALID_CERTIFICATE:
			builder.setMessage(R.string.invalid_certificate).setCancelable(false)
					.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.dismiss();
						}
					});
			break;
		case DIALOG_INVALID_KEY:
			builder.setMessage(R.string.invalid_key).setCancelable(false)
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
		case DIALOG_IMAGE_TOO_LARGE:
			builder.setMessage(R.string.image_too_large).setCancelable(false)
					.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.dismiss();
						}
					});
			break;
		case DIALOG_IMAGE_SIZE_FAIL:
			builder.setMessage(R.string.image_size_fail).setCancelable(false)
					.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.dismiss();
						}
					});
			break;
		}
		dialog = builder.show();
		return dialog;
	}

	private String[] extractFileNamesFromFilesList() {
		String[] extractedFileNames = new String[mFilesList.length];
		for (int i = 0; i < mFilesList.length; i++) {
			extractedFileNames[i] = mFilesList[i].getName();
		}
		return extractedFileNames;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
		super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

		switch (requestCode) {
		case ACTIVITY_SELECT_IMAGE:
			if (resultCode == RESULT_OK) {
				Uri selectedImage = imageReturnedIntent.getData();
				String[] filePathColumn = { MediaStore.Images.Media.DATA };

				Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
				cursor.moveToFirst();

				int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
				// file path of selected image
				mChosenImagePath = cursor.getString(columnIndex);
				cursor.close();
				// Convert file path into bitmap image using below line.
				mChosenImage = BitmapFactory.decodeFile(mChosenImagePath);

				// put bitmapimage in imageview
				ImageView img = (ImageView) findViewById(R.id.image);
				img.setImageBitmap(mChosenImage);

				File file = new File(mChosenImagePath);
				Log.d(TAG, String.valueOf(file.length()));
				if (file.length() > 500000) {
					showDialog(DIALOG_IMAGE_TOO_LARGE);
				}

				if (mChosenImage.getHeight() != mChosenImage.getHeight()) {
					showDialog(DIALOG_IMAGE_SIZE_FAIL);
				}

			}
		}
	}

	public void onClickButtonDone(View view) throws IOException {

		if (mChosenFile != null && mChosenImage != null) {
			String mChosenFilePath = mChosenFile.toString();
			String mKeyPath = mChosenFilePath.substring(0, mChosenFilePath.indexOf(".")) + AndroidRsaConstants.KEY_NAME;

			File mKey = new File(mKeyPath);
			Log.d(TAG, mKeyPath);

			if (mKey.exists()) {

				// Stores my own certificate, my own private key and the public
				// key of the CA

				try {

					KeyStore.getInstance().setCertificate(AndroidRsaConstants.OWN_ALIAS,
							RSA.getCertificate(mChosenFilePath));
					
					// loading keystore
					KeyStore.getInstance().setPk(FileUtils.readFileToByteArray(mKey));

					KeyStore.getInstance().setPb(RSA.getCAPublicKey(getApplicationContext()));

					KeyStore.getInstance().getCertificate(AndroidRsaConstants.OWN_ALIAS)
							.verify(KeyStore.getInstance().getPb());

					// user have been registered
					SharedPreferences prefs = getSharedPreferences(AndroidRsaConstants.SHARED_PREFERENCE_FILE,
							Context.MODE_PRIVATE);
					Editor prefsEditor = prefs.edit();
					prefsEditor.putString(AndroidRsaConstants.CERT_PATH, mChosenFilePath);
					prefsEditor.apply();

					// Applying steganography
					Intent i = new Intent(getApplicationContext(), EncodeActivity.class);
					i.putExtra(AndroidRsaConstants.FILE_PATH, mChosenFilePath);
					i.putExtra(AndroidRsaConstants.IMAGE_PATH, mChosenImagePath);
					i.putExtra(AndroidRsaConstants.PASSPHRASE, passphrase);
					startActivity(i);

				} catch (CertificateException e) {
					showDialog(DIALOG_INVALID_CERTIFICATE);
					e.printStackTrace();
				} catch (InvalidKeyException e) {
					showDialog(DIALOG_INVALID_KEY);
					e.printStackTrace();
				} catch (SignatureException e) {
					showDialog(DIALOG_INVALID_SIGN_CERTIFICATE);
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}

			} else {
				showDialog(DIALOG_KEY_NOT_FOUND);
			}

		} else {
			showDialog(DIALOG_NOT_CHOSEN);
		}

	}
}
