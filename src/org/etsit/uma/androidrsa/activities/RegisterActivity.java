package org.etsit.uma.androidrsa.activities;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.security.cert.CertificateException;

import org.apache.commons.io.FileUtils;
import org.etsit.uma.androidrsa.R;
import org.etsit.uma.androidrsa.rsa.RSA;
import org.etsit.uma.androidrsa.utils.AndroidRsaConstants;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.Toast;

public class RegisterActivity extends EncodeableActivity {

	private static final String TAG = "RegisterActivity";

	private static final int ACTIVITY_SELECT_IMAGE = 100;

	private List<File> mCertificatesList;
	private Bitmap mChosenImage;

	private static final int DIALOG_LOAD_FILE = 1000;

	private static final int DIALOG_KEY_NOT_FOUND = 1005;
	private static final int DIALOG_INVALID_KEY = 1006;
	private static final int DIALOG_IMAGE_TOO_LARGE = 1007;
	private static final int DIALOG_IMAGE_SIZE_FAIL = 1008;
	private static final int DIALOG_RUN_OTHER = 1009;

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
			moveRawResourcesToAndroidRsaFolder();
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
		if (AndroidRsaConstants.EXTERNAL_STORAGE_PATH.exists()) {
			mCertificatesList = (List<File>) FileUtils.listFiles(AndroidRsaConstants.EXTERNAL_STORAGE_PATH,
					new String[] { "crt" }, true);
		}
	}

	// Dialogs
	protected void dialogBuild(AlertDialog.Builder builder, int id) {
		switch (id) {
		case DIALOG_RUN_OTHER:
			builder.setMessage(R.string.other_time_configuration).setCancelable(false)
					.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.dismiss();
						}
					});
			break;
		case DIALOG_LOAD_FILE:
			if (mCertificatesList == null) {
				builder.setMessage(R.string.not_found).setCancelable(false).setPositiveButton(
						getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.dismiss();
							}
						});
			} else {
				builder.setTitle(R.string.choose_file);

				builder.setSingleChoiceItems(extractFileNamesFromCertificatesList(), -1,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								File mChosenCertificate = mCertificatesList.get(which);
								mChosenCertificatePath = mChosenCertificate == null ? null
										: mChosenCertificate.toString();
							}
						}).setPositiveButton(getResources().getString(R.string.ok),
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int id) {
										dialog.dismiss();
									}
								});
			}
			break;
		case DIALOG_KEY_NOT_FOUND:
			builder.setMessage(R.string.key_not_found).setCancelable(false)
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
	}

	private String[] extractFileNamesFromCertificatesList() {
		String[] extractedFileNames = new String[mCertificatesList.size()];
		for (int i = 0; i < mCertificatesList.size(); i++) {
			extractedFileNames[i] = mCertificatesList.get(i).getName();
		}
		return extractedFileNames;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent returnedIntent) {
		super.onActivityResult(requestCode, resultCode, returnedIntent);

		switch (requestCode) {
		case ACTIVITY_SELECT_IMAGE:
			if (resultCode == RESULT_OK) {
				Uri selectedImage = returnedIntent.getData();
				String[] filePathColumn = { MediaStore.Images.Media.DATA };

				Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
				cursor.moveToFirst();

				int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
				// File path of selected image
				mChosenImagePath = cursor.getString(columnIndex);
				cursor.close();
				// Convert file path into bitmap image using below line.
				mChosenImage = BitmapFactory.decodeFile(mChosenImagePath);

				// Put bitmapimage in imageview
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
			break;
		}
	}

	public void onClickButtonDone(View view) {
		if (mChosenCertificatePath != null && mChosenImage != null) {
			mChosenKeyPath = mChosenCertificatePath.substring(0, mChosenCertificatePath.indexOf("."))
					+ AndroidRsaConstants.KEY_NAME;
			File mChosenKey = new File(mChosenKeyPath);

			if (mChosenKey.exists()) {
				try {
					if (RSA.testPrivateKeyFile(mChosenKey)) {
						Intent i = new Intent(this, EncryptationPasswordActivity.class);
						i.putExtra(AndroidRsaConstants.FILE_PATH, mChosenCertificatePath);
						i.putExtra(AndroidRsaConstants.KEY_FILE_PATH, mChosenKeyPath);
						i.putExtra(AndroidRsaConstants.IMAGE_PATH, mChosenImagePath);
						i.putExtra(AndroidRsaConstants.PASSPHRASE, passphrase);
						startActivity(i);
					} else {
						RSA.testKeys(RSA.getPrivateKeyDecryted(FileUtils.readFileToByteArray(new File(mChosenKeyPath)),
								passphrase), RSA.readCertificate(mChosenCertificatePath).getPublicKey());
						storeAndEncode();
					}
				} catch (CertificateException ex) {
					showDialog(DIALOG_INVALID_CERTIFICATE);
				} catch (Exception ex) {
					showDialog(DIALOG_INVALID_KEY);
				}

			} else {
				showDialog(DIALOG_KEY_NOT_FOUND);
			}

		} else {
			Toast.makeText(this, getResources().getString(R.string.not_chosen), Toast.LENGTH_LONG).show();
		}

	}

}
