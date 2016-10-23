
package org.etsit.uma.androidrsa.steganography;

import java.util.Vector;

import org.etsit.uma.androidrsa.utils.handler.ProgressHandler;

import android.util.Log;

public class LSB2bit {

	private static int[] binary = { 16, 8, 0 };
	private static byte[] andByte = { (byte) 0xC0, 0x30, 0x0C, 0x03 };
	private static int[] toShift = { 6, 4, 2, 0 };
	public static String END_MESSAGE_COSTANT = "#!@";
	public static String START_MESSAGE_COSTANT = "@!#";

	public static byte[] encodeMessage(int[] oneDPix, int imgCols, int imgRows, String str, ProgressHandler hand) {
		str += END_MESSAGE_COSTANT;
		str = START_MESSAGE_COSTANT + str;
		byte[] msg = str.getBytes();
		int channels = 3;
		int shiftIndex = 4;
		// Array.newInstance(Byte.class, imgRows * imgCols * channels);
		byte[] result = new byte[imgRows * imgCols * channels];

		if (hand != null)
			hand.setTotal(imgRows * imgCols * channels);
		int msgIndex = 0;
		int resultIndex = 0;
		boolean msgEnded = false;
		for (int row = 0; row < imgRows; row++) {
			for (int col = 0; col < imgCols; col++) {
				int element = row * imgCols + col;
				byte tmp = 0;

				for (int channelIndex = 0; channelIndex < channels; channelIndex++) {
					if (!msgEnded) {
						tmp = (byte) ((((oneDPix[element] >> binary[channelIndex]) & 0xFF) & 0xFC)
								| ((msg[msgIndex] >> toShift[(shiftIndex++) % toShift.length]) & 0x3));// 6
						if (shiftIndex % toShift.length == 0) {
							msgIndex++;
						}
						if (msgIndex == msg.length) {
							msgEnded = true;
						}
					} else {
						tmp = (byte) ((((oneDPix[element] >> binary[channelIndex]) & 0xFF)));
					}
					result[resultIndex++] = tmp;
					if (hand != null)
						hand.increment(1);
				}

			}

		}
		return result;

	}

	public static String decodeMessage(byte[] oneDPix, int imgCols, int imgRows) {

		Vector<Byte> v = new Vector<Byte>();

		String builder = "";
		int shiftIndex = 4;
		byte tmp = 0x00;
		for (int i = 0; i < oneDPix.length; i++) {
			tmp = (byte) (tmp
					| ((oneDPix[i] << toShift[shiftIndex % toShift.length]) & andByte[shiftIndex++ % toShift.length]));
			if (shiftIndex % toShift.length == 0) {
				v.addElement(Byte.valueOf(tmp));
				byte[] nonso = { (v.elementAt(v.size() - 1)).byteValue() };
				String str = new String(nonso);
				// if (END_MESSAGE_COSTANT.equals(str)) {
				if (builder.endsWith(END_MESSAGE_COSTANT)) {
					break;
				} else {
					builder = builder + str;
					if (builder.length() == START_MESSAGE_COSTANT.length() && !START_MESSAGE_COSTANT.equals(builder)) {
						builder = null;
						break;
					}
				}

				tmp = 0x00;
			}

		}
		if (builder != null)
			builder = builder.substring(START_MESSAGE_COSTANT.length(),
					builder.length() - END_MESSAGE_COSTANT.length());
		return builder;

	}

	public static int[] byteArrayToIntArray(byte[] b) {
		Log.v("Size byte array", b.length + "");
		int size = b.length / 3;
		Log.v("Size Int array", size + "");
		System.runFinalization();
		System.gc();
		Log.v("FreeMemory", Runtime.getRuntime().freeMemory() + "");
		int[] result = new int[size];
		int off = 0;
		int index = 0;
		while (off < b.length) {
			result[index++] = byteArrayToInt(b, off);
			off = off + 3;
		}

		return result;
	}

	private static int byteArrayToInt(byte[] b, int offset) {
		int value = 0x00000000;
		for (int i = 0; i < 3; i++) {
			int shift = (3 - 1 - i) * 8;
			value |= (b[i + offset] & 0x000000FF) << shift;
		}
		value = value & 0x00FFFFFF;
		return value;
	}
}
