package org.etsit.uma.androidrsa.steganography;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;

import org.etsit.uma.androidrsa.R;
import org.etsit.uma.androidrsa.utils.AndroidRsaConstants;
import org.etsit.uma.androidrsa.utils.handler.MobiProgressBar;
import org.etsit.uma.androidrsa.utils.handler.ProgressHandler;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.Config;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

public class Encoder {
	private final MobiProgressBar progressBar;
	private final Context ctx;
	private final Handler handler;

	public Encoder(Context ctx, MobiProgressBar progressBar, final Handler handler) {
		this.ctx = ctx;
		this.progressBar = progressBar;
		this.handler = handler;
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
			progressBar.setMessage(ctx.getResources().getString(R.string.saving));
			progressBar.setIndeterminate(true);
		}
	};

	public String encodeFileToImageAndSave(String filePath, String absoluteFilePathSource) {
		String s = converFileToString(filePath);
		Bitmap sourceBitmap = BitmapFactory.decodeFile(absoluteFilePathSource);
		int width = sourceBitmap.getWidth();
		int height = sourceBitmap.getHeight();

		int[] oneD = new int[width * height];
		sourceBitmap.getPixels(oneD, 0, width, 0, 0, width, height);
		int density = sourceBitmap.getDensity();
		sourceBitmap.recycle();
		byte[] byteImage = LSB2bit.encodeMessage(oneD, width, height, s, new ProgressHandler() {
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
		Log.v("Encode Rojo", "" + (oneDMod[0] >> 16 & 0xFF));
		Log.v("Encode Verde", "" + (oneDMod[0] >> 8 & 0xFF));
		Log.v("Encode Azul", "" + (oneDMod[0] & 0xFF));

		System.gc();
		Log.v("Memoria liberada", Runtime.getRuntime().freeMemory() + "");
		Log.v("Tamaño de la imagen", (width * height * 32 / 8) + "");

		Bitmap destBitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);

		destBitmap.setDensity(density);
		int partialProgr = height * width / 50;
		int masterIndex = 0;
		for (int j = 0; j < height; j++)
			for (int i = 0; i < width; i++) {
				// The unique way to write correctly the sourceBitmap, android. bug!!!
				destBitmap.setPixel(i, j, Color.argb(0xFF, oneDMod[masterIndex] >> 16 & 0xFF,
						oneDMod[masterIndex] >> 8 & 0xFF, oneDMod[masterIndex++] & 0xFF));
				if (masterIndex % partialProgr == 0)
					handler.post(mIncrementProgress);
			}
		handler.post(mSetInderminate);
		Log.v("Encode", "" + destBitmap.getPixel(0, 0));
		Log.v("Encode Alpha", "" + (destBitmap.getPixel(0, 0) >> 24 & 0xFF));
		Log.v("Encode Rojo", "" + (destBitmap.getPixel(0, 0) >> 16 & 0xFF));
		Log.v("Encode Verde", "" + (destBitmap.getPixel(0, 0) >> 8 & 0xFF));
		Log.v("Encode Azul", "" + (destBitmap.getPixel(0, 0) & 0xFF));
		
		String destPath = null;
		int indexSepar = absoluteFilePathSource.lastIndexOf(File.separator);
		int indexPoint = absoluteFilePathSource.lastIndexOf(".");
		
		if (indexPoint <= 1) {
			indexPoint = absoluteFilePathSource.length();
		}

		String fileNameDest = absoluteFilePathSource.substring(indexSepar + 1, indexPoint);

		destPath = AndroidRsaConstants.EXTERNAL_APP_FOLDER_PATH + File.separator + fileNameDest + AndroidRsaConstants.ENCODED_IMAGE_NAME;
		SharedPreferences prefs = ctx.getSharedPreferences(AndroidRsaConstants.SHARED_PREFERENCE_FILE,
				Context.MODE_PRIVATE);
		Editor prefsEditor = prefs.edit();
		prefsEditor.putString(AndroidRsaConstants.ENCODED_IMAGE_PATH, destPath);
		prefsEditor.apply();

		
		OutputStream fout = null;
		try {
			fout = new FileOutputStream(destPath);
			destBitmap.compress(Bitmap.CompressFormat.PNG, 100, fout);
			Uri result = Uri.parse("file://" + destPath);
			ctx.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, result));
			fout.flush();
			fout.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
		destBitmap.recycle();

		return destPath;
	}
	
	private String converFileToString(String path) {
		File file = new File(path);
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
		} catch (Exception e) {
			e.printStackTrace();
		}
		return total.toString();
	}
}
