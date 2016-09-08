
package org.inftel.androidrsa.xmpp;

import java.util.ArrayList;
import java.util.HashMap;

import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.packet.VCard;
import org.jivesoftware.smackx.provider.VCardProvider;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public class AvatarsCache {
    private static final String TAG = "AvatarsCache";
    private static HashMap<String, Bitmap> avatarMap = null;

    public static HashMap<String, Bitmap> getInstance() {
        if (avatarMap == null) {
            return new HashMap<String, Bitmap>();
        }
        else {
            return avatarMap;
        }
    }

    public static Bitmap getMyAvatar() {
        VCard vCard = new VCard();
        try {
            ProviderManager.getInstance().addIQProvider("vCard",
                    "vcard-temp",
                    new VCardProvider());
            vCard.load(Conexion.getInstance());
            if (vCard.getAvatar() != null) {
                Log.d(TAG, "No es NULL");
                byte[] avatarRaw = vCard.getAvatar();
                Bitmap bm = BitmapFactory.decodeByteArray(avatarRaw, 0, avatarRaw.length);
                return bm;
            }
            else {
                Log.d(TAG, "Si es NULL");
                return null;
            }
        } catch (XMPPException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Bitmap getAvatar(String jid) {
        VCard vCard = new VCard();
        try {
            ProviderManager.getInstance().addIQProvider("vCard",
                    "vcard-temp",
                    new VCardProvider());
            vCard.load(Conexion.getInstance(), StringUtils.parseBareAddress(jid));
            if (vCard.getAvatar() != null) {
                byte[] avatarRaw = vCard.getAvatar();
                if (avatarRaw.length != 0) {
                    Bitmap bm = BitmapFactory.decodeByteArray(avatarRaw, 0, avatarRaw.length);
                    return bm;
                }
                else {
                    return null;
                }
            }
            else {
                return null;
            }
        } catch (XMPPException e) {
            e.printStackTrace();
            Log.e(TAG, "Error al recuperar el VCARD");
            return null;
        }
    }

    public static void populateFromList(ArrayList<Presence> list) {
        clear();
        for (Presence p : list) {
            avatarMap.put(p.getFrom(), getAvatar(p.getFrom()));
        }
    }

    public static void clear() {
        if (avatarMap != null) {
            avatarMap.clear();
        }
        avatarMap = new HashMap<String, Bitmap>();
    }

}
