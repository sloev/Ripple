package sloev.ripple.util;

import com.quickblox.core.QBSettings;

/**
 * Created by johannes on 09/03/15.
 */
public class Credentials {
    private static final String QB_APP_ID = "666";
    private static final String QB_AUTH_KEY = "lol";
    private static final String QB_AUTH_SECRET = "cat";

    private static Credentials dataHolder;

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
