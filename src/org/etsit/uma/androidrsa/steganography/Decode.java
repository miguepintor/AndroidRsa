
package org.etsit.uma.androidrsa.steganography;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.security.cert.Certificate;
import javax.security.cert.CertificateException;

import org.etsit.uma.androidrsa.rsa.RSA;
import org.etsit.uma.androidrsa.utils.AndroidRsaConstants;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public class Decode {
    private static final String TAG = "Decode";

    public static Certificate decode(String path) throws IOException, CertificateException {
        Bitmap image = null;
        try {
            BitmapFactory.Options opt = new BitmapFactory.Options();
            opt.inDither = false;
            opt.inScaled = false;
            opt.inDensity = 0;
            opt.inJustDecodeBounds = false;
            opt.inPurgeable = false;
            opt.inSampleSize = 1;
            opt.inScreenDensity = 0;
            opt.inTargetDensity = 0;
            image = BitmapFactory.decodeFile(path, opt);

        } catch (Exception e) {
            e.printStackTrace();
        }

        int[] pixels = new int[image.getWidth() * image.getHeight()];
        image.getPixels(pixels, 0, image.getWidth(), 0, 0, image.getWidth(),
                image.getHeight());
        Log.v("Decode", "" + pixels[0]);
        Log.v("Decode Alpha", "" + (pixels[0] >> 24 & 0xFF));
        Log.v("Decode Red", "" + (pixels[0] >> 16 & 0xFF));
        Log.v("Decode Green", "" + (pixels[0] >> 8 & 0xFF));
        Log.v("Decode Blue", "" + (pixels[0] & 0xFF));
        Log.v("Decode", "" + pixels[0]);
        Log.v("Decode", "" + image.getPixel(0, 0));
        byte[] b = null;
        try {
            b = LSB2bit.convertArray(pixels);
        } catch (OutOfMemoryError er) {
            Log.d(TAG, "OUT OF MEMORY");
        }
        final String vvv = LSB2bit.decodeMessage(b, image.getWidth(), image
                .getHeight());
        if (vvv == null) {
            Log.d(TAG, "NO STEGO IMAGE");
        } else {
            convertToFile(vvv);
            return RSA.getCertificate(AndroidRsaConstants.DECODED_CERT_PATH);
        }
        return null;
    }

    public static Certificate decode(Bitmap image) throws IOException, CertificateException {
        int[] pixels = new int[image.getWidth() * image.getHeight()];
        image.getPixels(pixels, 0, image.getWidth(), 0, 0, image.getWidth(),
                image.getHeight());
        Log.v("Decode", "" + pixels[0]);
        Log.v("Decode Alpha", "" + (pixels[0] >> 24 & 0xFF));
        Log.v("Decode Red", "" + (pixels[0] >> 16 & 0xFF));
        Log.v("Decode Green", "" + (pixels[0] >> 8 & 0xFF));
        Log.v("Decode Blue", "" + (pixels[0] & 0xFF));
        Log.v("Decode", "" + pixels[0]);
        Log.v("Decode", "" + image.getPixel(0, 0));
        byte[] b = null;
        try {
            b = LSB2bit.convertArray(pixels);
        } catch (OutOfMemoryError er) {
            Log.d(TAG, "OUT OF MEMORY");
        }
        final String vvv = LSB2bit.decodeMessage(b, image.getWidth(), image
                .getHeight());
        if (vvv == null) {
            Log.d(TAG, "NO STEGO IMAGE");
        } else {
            convertToFile(vvv);
            return RSA.getCertificate(AndroidRsaConstants.DECODED_CERT_PATH);
        }
        return null;
    }

    private static void convertToFile(String str) {
        try {
            File file = new File(AndroidRsaConstants.DECODED_CERT_PATH);
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write(str);

            // writer.flush();
            writer.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
}
