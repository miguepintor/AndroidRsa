
package org.inftel.androidrsa.activities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;

import org.inftel.androidrsa.R;
import org.inftel.androidrsa.steganography.LSB2bit;
import org.inftel.androidrsa.steganography.MobiProgressBar;
import org.inftel.androidrsa.steganography.ProgressHandler;
import org.inftel.androidrsa.utils.AndroidRsaConstants;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

public class EncodeActivity extends Activity {
    private MobiProgressBar progressBar;
    private final Handler handler = new Handler();
    private Context context;
    private Bitmap mChosenImage;
    private String mChosenImagePath;
    private File mChosenFile;
    private String mChosenFilePath;
    private String passphrase;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        context = this;
        super.onCreate(savedInstanceState);

        // Obtaning intent information
        Bundle bundle = getIntent().getExtras();
        mChosenFilePath = bundle.getString(AndroidRsaConstants.FILE_PATH);
        mChosenImagePath = bundle.getString(AndroidRsaConstants.IMAGE_PATH);
        passphrase = bundle.getString(AndroidRsaConstants.PASSPHRASE);

        mChosenImage = BitmapFactory.decodeFile(mChosenImagePath);
        mChosenFile = new File(mChosenFilePath);

        // Encoding..
        progressBar = new MobiProgressBar(EncodeActivity.this);
        progressBar.setMax(100);
        progressBar.setMessage(getResources().getString(R.string.encoding));
        progressBar.show();
        Thread tt = new Thread(new Runnable() {
            public void run() {
                encode(converToString(mChosenFile), mChosenImage, mChosenImagePath);
                handler.post(mShowAlert);

                // Saving in prefs when run once
                SharedPreferences prefs = getSharedPreferences(
                        AndroidRsaConstants.SHARED_PREFERENCE_FILE,
                        Context.MODE_PRIVATE);
                boolean registered = prefs.getBoolean(AndroidRsaConstants.REGISTERED, false);
                if (!registered) {
                    Editor prefsEditor = prefs.edit();
                    prefsEditor.putBoolean(AndroidRsaConstants.REGISTERED, true);
                    prefsEditor.apply();
                }

                Intent i = new Intent(context, ContactsActivity.class);
                i.putExtra(AndroidRsaConstants.PASSPHRASE, passphrase);
                startActivity(i);
            }
        });
        tt.start();

    }

    private String converToString(File file) {
        FileInputStream fis;
        StringBuilder total = new StringBuilder();
        try {
            fis = new FileInputStream(file);
            BufferedReader r = new BufferedReader(new InputStreamReader(fis));
            String line;
            while ((line = r.readLine()) != null) {
                total.append(line);
            }
            fis.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return total.toString();
    }

    final Runnable mShowAlert = new Runnable() {
        public void run() {
            progressBar.dismiss();
            AlertDialog.Builder builder = new AlertDialog.Builder(
                    context);
            builder.setMessage(getResources().getString(R.string.saved))
                    .setCancelable(false).setPositiveButton(
                            context.getText(R.string.ok),
                            new DialogInterface.OnClickListener() {
                                public void onClick(
                                        DialogInterface dialog,
                                        int id) {
                                    EncodeActivity.this.finish();
                                }
                            });

            AlertDialog alert = builder.create();
            // alert.show();
        }
    };

    private Uri encode(String s, Bitmap sourceBitmap, String absoluteFilePathSource) {

        Uri result = null;

        int width = sourceBitmap.getWidth();
        int height = sourceBitmap.getHeight();

        int[] oneD = new int[width * height];
        sourceBitmap.getPixels(oneD, 0, width, 0, 0, width, height);
        int density = sourceBitmap.getDensity();
        sourceBitmap.recycle();
        byte[] byteImage = LSB2bit.encodeMessage(oneD, width, height, s,
                new ProgressHandler() {
                    private int mysize;
                    private int actualSize;

                    public void increment(final int inc) {
                        actualSize += inc;
                        if (actualSize % mysize == 0)
                            handler.post(mIncrementProgress);
                    }

                    public void setTotal(final int tot) {
                        mysize = tot / 50;
                        handler.post(mInitializeProgress);
                    }

                    public void finished() {

                    }
                });
        oneD = null;
        sourceBitmap = null;
        int[] oneDMod = LSB2bit.byteArrayToIntArray(byteImage);
        byteImage = null;
        Log.v("Encode", "" + oneDMod[0]);
        Log.v("Encode Alpha", "" + (oneDMod[0] >> 24 & 0xFF));
        Log.v("Encode Red", "" + (oneDMod[0] >> 16 & 0xFF));
        Log.v("Encode Green", "" + (oneDMod[0] >> 8 & 0xFF));
        Log.v("Encode Blue", "" + (oneDMod[0] & 0xFF));

        System.gc();
        Log.v("Free memory", Runtime.getRuntime().freeMemory() + "");
        Log.v("Image mesure", (width * height * 32 / 8) + "");

        Bitmap destBitmap = Bitmap.createBitmap(width, height,
                Config.ARGB_8888);

        destBitmap.setDensity(density);
        int partialProgr = height * width / 50;
        int masterIndex = 0;
        for (int j = 0; j < height; j++)
            for (int i = 0; i < width; i++) {
                // The unique way to write correctly the sourceBitmap, android
                // bug!!!
                destBitmap.setPixel(i, j, Color.argb(0xFF,
                        oneDMod[masterIndex] >> 16 & 0xFF,
                        oneDMod[masterIndex] >> 8 & 0xFF,
                        oneDMod[masterIndex++] & 0xFF));
                if (masterIndex % partialProgr == 0)
                    handler.post(mIncrementProgress);
            }
        handler.post(mSetInderminate);
        Log.v("Encode", "" + destBitmap.getPixel(0, 0));
        Log.v("Encode Alpha", "" + (destBitmap.getPixel(0, 0) >> 24 & 0xFF));
        Log.v("Encode Red", "" + (destBitmap.getPixel(0, 0) >> 16 & 0xFF));
        Log.v("Encode Green", "" + (destBitmap.getPixel(0, 0) >> 8 & 0xFF));
        Log.v("Encode Blue", "" + (destBitmap.getPixel(0, 0) & 0xFF));

        String sdcardState = android.os.Environment.getExternalStorageState();
        String destPath = null;
        int indexSepar = absoluteFilePathSource.lastIndexOf(File.separator);
        int indexPoint = absoluteFilePathSource.lastIndexOf(".");
        if (indexPoint <= 1)
            indexPoint = absoluteFilePathSource.length();
        String fileNameDest = absoluteFilePathSource.substring(indexSepar + 1, indexPoint);
        fileNameDest += AndroidRsaConstants.ENCODED_IMAGE_NAME;
        if (sdcardState.contentEquals(android.os.Environment.MEDIA_MOUNTED)) {

            destPath = android.os.Environment.getExternalStorageDirectory()
                    + File.separator + fileNameDest + ".png";
            SharedPreferences prefs = getSharedPreferences(
                    AndroidRsaConstants.SHARED_PREFERENCE_FILE,
                    Context.MODE_PRIVATE);
            Editor prefsEditor = prefs.edit();
            prefsEditor.putString(AndroidRsaConstants.ENCODED_IMAGE_PATH,
                    destPath);
            prefsEditor.apply();

        }
        OutputStream fout = null;
        try {

            Log.v("Path", destPath);
            fout = new FileOutputStream(destPath);
            destBitmap.compress(Bitmap.CompressFormat.PNG, 100, fout);
            // Media.insertImage(getContentResolver(),destPath, fileNameDest,
            // "MobiStego Encoded");
            result = Uri.parse("file://" + destPath);
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, result));
            fout.flush();
            fout.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        destBitmap.recycle();
        return result;
    }

    final Runnable mIncrementProgress = new Runnable() {
        public void run() {
            progressBar.incrementProgressBy(1);
        }
    };

    final Runnable mInitializeProgress = new Runnable() {
        public void run() {
            progressBar.setMax(100);
        }
    };

    final Runnable mSetInderminate = new Runnable() {
        public void run() {
            progressBar.setMessage(getResources().getString(R.string.saving));
            progressBar.setIndeterminate(true);
        }
    };
}
