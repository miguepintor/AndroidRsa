package org.etsit.uma.androidrsa.xmpp;

import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Presence.Mode;

public final class Status {

    public static final int CONTACT_STATUS_DISCONNECT = 100;
    public static final int CONTACT_STATUS_UNAVAILABLE = 200;
    public static final int CONTACT_STATUS_AWAY = 300;
    public static final int CONTACT_STATUS_BUSY = 400;
    public static final int CONTACT_STATUS_AVAILABLE = 500;
    public static final int CONTACT_STATUS_AVAILABLE_FOR_CHAT = 600;

    private Status() {}

    public static Presence.Mode getPresenceModeFromStatus(final int status) {
        Presence.Mode res;
        switch (status) {
            case CONTACT_STATUS_AVAILABLE:
                res = Presence.Mode.available;
                break;
            case CONTACT_STATUS_AVAILABLE_FOR_CHAT:
                res = Presence.Mode.chat;
                break;
            case CONTACT_STATUS_AWAY:
                res = Presence.Mode.away;
                break;
            case CONTACT_STATUS_BUSY:
                res = Presence.Mode.dnd;
                break;
            case CONTACT_STATUS_UNAVAILABLE:
                res = Presence.Mode.xa;
                break;
            default:
                return null;
        }
        return res;
    }

    public static int getStatusFromPresence(final Presence presence) {
        int res = Status.CONTACT_STATUS_DISCONNECT;
        if (presence.getType().equals(Presence.Type.unavailable)) {
            res = Status.CONTACT_STATUS_DISCONNECT;
        } else {
            Mode mode = presence.getMode();
            if (mode == null) {
                res = Status.CONTACT_STATUS_AVAILABLE;
            } else {
                switch (mode) {
                    case available:
                        res = Status.CONTACT_STATUS_AVAILABLE;
                        break;
                    case away:
                        res = Status.CONTACT_STATUS_AWAY;
                        break;
                    case chat:
                        res = Status.CONTACT_STATUS_AVAILABLE_FOR_CHAT;
                        break;
                    case dnd:
                        res = Status.CONTACT_STATUS_BUSY;
                        break;
                    case xa:
                        res = Status.CONTACT_STATUS_UNAVAILABLE;
                        break;
                    default:
                        res = Status.CONTACT_STATUS_DISCONNECT;
                        break;
                }
            }
        }
        return res;
    }

    public static boolean statusOnline(final int status) {
        return status != Status.CONTACT_STATUS_DISCONNECT;
    }

}
