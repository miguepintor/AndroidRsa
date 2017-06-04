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
import android.util.Log;

public class Decoder {
    private static final String TAG = "Decoder";

    public Certificate decode(Bitmap image) throws IOException, CertificateException {
        int[] pixels = new int[image.getWidth() * image.getHeight()];
        image.getPixels(pixels, 0, image.getWidth(), 0, 0, image.getWidth(),
                image.getHeight());
        Log.v("Decode", "" + pixels[0]);
        Log.v("Decode Alpha", "" + (pixels[0] >> 24 & 0xFF));
        Log.v("Decode Rojo", "" + (pixels[0] >> 16 & 0xFF));
        Log.v("Decode Verde", "" + (pixels[0] >> 8 & 0xFF));
        Log.v("Decode Azul", "" + (pixels[0] & 0xFF));
        Log.v("Decode", "" + pixels[0]);
        Log.v("Decode", "" + image.getPixel(0, 0));
        byte[] b = null;
        try {
            b = convertArray(pixels);
        } catch (OutOfMemoryError er) {
            Log.e(TAG, "Sin memoria!!");
        }
        final String vvv = LSB2bit.decodeMessage(b, image.getWidth(), image
                .getHeight());
        if (vvv == null) {
            Log.e(TAG, "Imagen NO stego.");
        } else {
            convertToFile(vvv);
            return RSA.readCertificate(AndroidRsaConstants.DECODED_CERT_PATH);
        }
        return null;
    }

    private void convertToFile(String str) {
        try {
            File file = new File(AndroidRsaConstants.DECODED_CERT_PATH);
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write(str);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private static byte[] convertArray(int[] array) {
		byte[] newarray = new byte[array.length * 3];

		for (int i = 0; i < array.length; i++) {
			newarray[i * 3] = (byte) ((array[i] >> 16) & 0xFF);
			newarray[i * 3 + 1] = (byte) ((array[i] >> 8) & 0xFF);
			newarray[i * 3 + 2] = (byte) ((array[i]) & 0xFF);
		}
		return newarray;
	}
}
