package org.etsit.uma.androidrsa.utils;

import java.io.File;

import android.os.Environment;

public class AndroidRsaConstants {

	// Constants for directories, names, types...
	public static final File EXTERNAL_STORAGE_PATH = new File(
			Environment.getExternalStorageDirectory().getAbsolutePath());
	public static final File EXTERNAL_APP_FOLDER_PATH = new File(EXTERNAL_STORAGE_PATH + File.separator + "androidRsa");
	
	public static final String CERT_EXTENSION = ".crt";
	public static final String KEY_EXTENSION = ".pem";
	public static final String IMAGE_EXTENSION = ".png";
	
	public static final String DECODED_CERT_PATH = EXTERNAL_APP_FOLDER_PATH + File.separator + "decoded_cert" + CERT_EXTENSION;
	public static final String OWN_CERT_PATH = EXTERNAL_APP_FOLDER_PATH + File.separator + "certificate" + CERT_EXTENSION;
	public static final String OWN_KEY_PATH = EXTERNAL_APP_FOLDER_PATH + File.separator + "certificatekey" + KEY_EXTENSION;
	
	public static final String KEY_NAME = "key" + AndroidRsaConstants.KEY_EXTENSION;
	public static final String ENCODED_IMAGE_NAME = "_mobistego" +  AndroidRsaConstants.IMAGE_EXTENSION;
	
	public static final String ANDROIDRSA_APP_NAME = "androidRsa";

	// Constants for intents
	public static final String IMAGE_PATH = "img_path";
	public static final String FILE_PATH = "file_path";
	public static final String PASSPHRASE = "passphrase";
	public static final String DEST_JID = "destJid";
	public static final String KEY_FILE_PATH = "key_file_path";

	// Constants for preferences
	public static final String SHARED_PREFERENCE_FILE = "SHARED_PREFERENCE_FILE";
	public static final String CERT_PATH = "cert_path";
	public static final String KEY_PATH = "key_path";
	public static final String ENCODED_IMAGE_PATH = "encoded_image_path";
	public static final String USERID = "user_id";
	public static final String SERVICE = "service";
	public static final String REGISTERED = "registered";

	// Constants for KeyStore
	public static String FRIEND_ALIAS = "fr";
	public static String OWN_ALIAS = "own";

	// Services
	public static String GMAIL_HOST = "talk.google.com";
	public static int GMAIL_PORT = 5222;
	public static String GMAIL_SERVICE = "gmail.com";

	public static String OPENFIRE_HOST = "192.168.1.3";
	public static int OPENFIRE_PORT = 5222;
	public static String OPENFIRE_SERVICE = "localhost";

}
