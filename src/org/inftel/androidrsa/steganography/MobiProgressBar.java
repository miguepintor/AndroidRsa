
package org.inftel.androidrsa.steganography;

import android.app.ProgressDialog;
import android.content.Context;

public class MobiProgressBar extends ProgressDialog {

    public MobiProgressBar(Context context) {
        super(context);
        setProgressStyle(STYLE_HORIZONTAL);
    }

}
