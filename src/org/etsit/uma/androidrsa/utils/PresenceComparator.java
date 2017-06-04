package org.etsit.uma.androidrsa.utils;

import java.util.Comparator;

import org.etsit.uma.androidrsa.xmpp.Status;
import org.jivesoftware.smack.packet.Presence;

public class PresenceComparator implements Comparator<Presence> {

    public PresenceComparator() {
        super();
    }

    public int compare(Presence p1, Presence p2) {
        String name1 = (String) p1.getProperty("name");
        String name2 = (String) p2.getProperty("name");
        if (name1.equals(name2)) {
            return 0;
        }
        else if (Status.getStatusFromPresence(p1) < Status.getStatusFromPresence(p2)) {
            return 1;
        }
        else if (Status.getStatusFromPresence(p1) > Status.getStatusFromPresence(p2)) {
            return -1;
        }
        else {
            return 0;
        }
    }

}
