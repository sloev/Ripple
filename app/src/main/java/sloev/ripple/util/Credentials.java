package sloev.ripple.util;

import com.quickblox.core.QBSettings;

/**
 * Created by johannes on 09/03/15.
 */
public class Credentials {
    private static final String QB_APP_ID = "20389";
    private static final String QB_AUTH_KEY = "ZjmkuL356ZRN7M-";
    private static final String QB_AUTH_SECRET = "caVnryyFwK-Y29V";

    private zstatic Credentials dataHolder;

    public static synchronized Credentials getDataHolder() {
        if (dataHolder == null) {
            dataHolder = new Credentials();
        }
        return dataHolder;
    }

    public void QBAuthorize() {
        QBSettings.getInstance().fastConfigInit(QB_APP_ID, QB_AUTH_KEY, QB_AUTH_SECRET);
    }

}
