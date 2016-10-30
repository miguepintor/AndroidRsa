
package org.etsit.uma.androidrsa.rsa;

import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;

import javax.security.cert.Certificate;

public class KeyStore {
    private static KeyStore Instance = new KeyStore();
    private Map<String, Certificate> data;
    private byte[] pk;
    private PublicKey caPb;

    private KeyStore() {
        data = new HashMap<String, Certificate>();
    }

    public static KeyStore getInstance() {
        return Instance;
    }

    public Certificate getCertificate(String alias) {
        return data.get(alias);
    }

    public void setCertificate(String alias, Certificate cert) {
        if (data.containsKey(alias)) {
            data.remove(alias);
            data.put(alias, cert);
        } else {
            data.put(alias, cert);
        }
    }

    public PublicKey getCaPb() {
        return caPb;
    }
    
    public void setCaPb(PublicKey caPb) {
        this.caPb = caPb;
    }

    public byte[] getPk() {
        return pk;
    }
    
    public void setPk(byte[] pk) {
        this.pk = pk;
    }
}
