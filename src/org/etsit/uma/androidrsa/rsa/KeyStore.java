
package org.etsit.uma.androidrsa.rsa;

import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;

import javax.security.cert.Certificate;

public class KeyStore {
    private static KeyStore Instance = new KeyStore();
    private Map<String, Certificate> data;
    private byte[] pk;
    private PublicKey pb;

    private KeyStore() {
        data = new HashMap<String, Certificate>();
    }

    public static KeyStore getInstance() {
        return Instance;
    }

    public void setPk(byte[] pk) {
        this.pk = pk;
    }

    public void setPb(PublicKey pb) {
        this.pb = pb;
    }

    public void setCertificate(String alias, Certificate cert) {
        if (data.containsKey(alias)) {
            data.remove(alias);
            data.put(alias, cert);
        } else {
            data.put(alias, cert);
        }
    }

    public Certificate getCertificate(String alias) {
        return data.get(alias);
    }

    public PublicKey getPb() {
        return pb;
    }

    public byte[] getPk() {
        return pk;
    }
}
