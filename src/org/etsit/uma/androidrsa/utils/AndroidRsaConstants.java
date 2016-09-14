/*
 * Copyright 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.etsit.uma.androidrsa.utils;

import java.io.File;

import android.os.Environment;

public class AndroidRsaConstants {

    // Constants for directories, names, types...

    public static final File EXTERNAL_SD_PATH = new File(Environment.getExternalStorageDirectory()
            .getAbsolutePath());
    public static final String FTYPE = ".crt";
    public static final String ENCODED_IMAGE_NAME = "_mobistego";
    public static final String KEY_NAME = "key_";
    public static final String DECODED_CERT = "decoded_cert";
    public static final String DECODED_CERT_PATH = EXTERNAL_SD_PATH
            + File.separator + "decoded_cert.crt";
    public static final String SERVERS_FILE_PATH = EXTERNAL_SD_PATH
            + File.separator + "androidrsa_servers.srv";
    public static final String ANDROIDRSA_APP_NAME="androidRsa";

    // Constants for intents

    public static final String IMAGE_PATH = "img_path";
    public static final String FILE_PATH = "file_path";
    public static final String STEGO_IMAGE_PATH = "stego_img_path";
    public static final String PASSPHRASE = "passphrase";
    public static final String DEST_JID = "destJid";

    // Constants for preferences

    public static String SHARED_PREFERENCE_FILE = "SHARED_PREFERENCE_FILE";
    public static String KEY_IP_PREFERENCE = "ip_preference";
    public static String KEY_PORT_PREFERENCE = "port_preference";
    public static String KEY_PATH_PREFERENCE = "path_preference";
    public static String KEY_PHONE_NUMBER_PREFERENCE = "phone_number_preference";
    public static final String CERT_PATH = "cert_path";
    public static final String KEY_PATH = "key_path";
    public static final String ENCODED_IMAGE_PATH = "encoded_image_path";
    public static final String USERID = "pass_to_encrypt";
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
