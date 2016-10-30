package org.etsit.uma.androidrsa.rsa;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import javax.security.cert.Certificate;
import javax.security.cert.CertificateException;
import javax.security.cert.X509Certificate;

import org.apache.commons.io.FileUtils;
import org.etsit.uma.androidrsa.R;
import org.etsit.uma.androidrsa.utils.AndroidRsaConstants;
import org.jivesoftware.smack.util.Base64;
import org.jivesoftware.smack.util.StringUtils;
import org.spongycastle.openssl.PEMDecryptorProvider;
import org.spongycastle.openssl.PEMEncryptedKeyPair;
import org.spongycastle.openssl.PEMParser;
import org.spongycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.spongycastle.openssl.jcajce.JcePEMDecryptorProviderBuilder;

import android.annotation.SuppressLint;
import android.content.Context;

public class RSA {

	static {
		Security.insertProviderAt(new org.spongycastle.jce.provider.BouncyCastleProvider(), 1);
	}

	public static void decryptEncryptPrivateKeyAndSave(String privKeyFilePath, String encryptationPassword,
			String passphrase) throws IOException, InvalidKeyException, NoSuchAlgorithmException,
			NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, NoSuchProviderException {

		File privKeyFile = new File(privKeyFilePath);

		PrivateKey privateKey = readEncryptedPrivateKeyFile(privKeyFile, encryptationPassword);
		byte[] encryptedBytes = encrytpKey(privateKey.getEncoded(), passphrase);
		FileUtils.writeByteArrayToFile(privKeyFile, encryptedBytes, false);
	}

	private static PrivateKey readEncryptedPrivateKeyFile(File file, String encryptionPassword) throws IOException {
		PrivateKey privKey = null;
		PEMParser pemParser = new PEMParser(new FileReader(file));
		Object object = pemParser.readObject();
		JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
		PEMEncryptedKeyPair ckp = (PEMEncryptedKeyPair) object;
		PEMDecryptorProvider decProv = new JcePEMDecryptorProviderBuilder().build(encryptionPassword.toCharArray());
		KeyPair kp = converter.getKeyPair(ckp.decryptKeyPair(decProv));
		privKey = kp.getPrivate();
		pemParser.close();
		return privKey;
	}

	public static PrivateKey getPrivateKeyDecryted(byte[] pk, String passphrase)
			throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException,
			BadPaddingException, NoSuchProviderException, InvalidKeySpecException {

		byte[] privKeyDecrypted = decrytpKey(pk, passphrase);

		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		KeySpec ks = new PKCS8EncodedKeySpec(privKeyDecrypted);
		PrivateKey privKey = keyFactory.generatePrivate(ks);
		return privKey;
	}

	private static byte[] decrytpKey(byte[] input, String passphrase)
			throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException,
			BadPaddingException, NoSuchProviderException {

		SecretKeySpec key = generateKey(passphrase);
		Cipher cipher;
		cipher = Cipher.getInstance("AES/ECB/PKCS7Padding", "BC");
		cipher.init(Cipher.DECRYPT_MODE, key);
		byte[] plainText = cipher.doFinal(input);

		return plainText;
	}

	@SuppressLint("TrulyRandom")
	private static byte[] encrytpKey(byte[] input, String passphrase)
			throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException,
			BadPaddingException, NoSuchProviderException {

		SecretKeySpec key = generateKey(passphrase);
		Cipher cipher = Cipher.getInstance("AES/ECB/PKCS7Padding", "BC");
		cipher.init(Cipher.ENCRYPT_MODE, key);
		byte[] cipherText = cipher.doFinal(input);

		return cipherText;
	}

	private static SecretKeySpec generateKey(String passphrase) throws NoSuchAlgorithmException {

		byte[] key = Base64.decode(passphrase);

		MessageDigest sha = MessageDigest.getInstance("SHA-1");
		key = sha.digest(key);
		key = Arrays.copyOf(key, 16); // use only first 128 bit

		SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
		return secretKeySpec;
	}

	public static boolean testPrivateKeyFile(File privKeyFile) throws IOException {
		boolean headerMatchs = false;
		BufferedReader in = new BufferedReader(new FileReader(privKeyFile));
		String line = in.readLine();
		if (line.contains("-----BEGIN") && line.contains("PRIVATE KEY-----")) {
			headerMatchs = true;
		}
		in.close();
		return headerMatchs;
	}

	public static Certificate readCertificate(String path) throws IOException, CertificateException {
		BufferedReader in = new BufferedReader(new FileReader(path));
		return readCertificate(in);
	}

	public static PublicKey getCAPublicKey(Context ctx) throws IOException, CertificateException {
		InputStream inputStream = ctx.getResources().openRawResource(R.raw.ca);
		BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
		return readCertificate(in).getPublicKey();
	}

	private static Certificate readCertificate(BufferedReader in) throws IOException, CertificateException {
		String line = in.readLine();
		if (line.contains("-----BEGIN CERTIFICATE-----") == false) {
			in.close();
			throw new CertificateException("Invalid Certificate format: header does not contain -----BEGIN CERTIFICATE-----");
		}
		line = line.substring(27);

		String base64 = new String();
		boolean trucking = true;
		while (trucking) {

			if (line.contains("-----")) {
				trucking = false;
				base64 += line.substring(0, line.indexOf("-----"));
			} else {
				base64 += line;
				line = in.readLine();
			}
		}
		in.close();
		byte[] certifacteData = Base64.decode(base64);

		X509Certificate c = X509Certificate.getInstance(certifacteData);
		return c;
	}

	public static String cipher(String text, PublicKey key) throws NoSuchAlgorithmException, NoSuchPaddingException,
			InvalidKeyException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException {

		byte[] bytes = text.getBytes();
		Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.ENCRYPT_MODE, key);
		byte[] encryptedBytes = cipher.doFinal(bytes);
		return StringUtils.encodeBase64(encryptedBytes);
	}

	public static String decipher(String text, PrivateKey key) throws InvalidKeyException, NoSuchAlgorithmException,
			NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException {

		byte[] bytes = StringUtils.decodeBase64(text);
		Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.DECRYPT_MODE, key);
		byte[] decryptedBytes = cipher.doFinal(bytes);
		return new String(decryptedBytes);
	}

	public static boolean verifyOwnPk(String passphrase)
			throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException,
			BadPaddingException, UnsupportedEncodingException, NoSuchProviderException, InvalidKeySpecException {

		return testKeys(RSA.getPrivateKeyDecryted(KeyStore.getInstance().getPk(), passphrase),
				KeyStore.getInstance().getCertificate(AndroidRsaConstants.OWN_ALIAS).getPublicKey());
	}

	public static boolean testKeys(PrivateKey pk, PublicKey pb) throws InvalidKeyException, NoSuchAlgorithmException,
			NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException {

		String test = "TEST";
		return decipher(cipher(test, pb), pk).equals(test);
	}

}
