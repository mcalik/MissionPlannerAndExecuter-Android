package com.calikmustafa.common;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.calikmustafa.model.Soldier;

/**
 * Created by Mustafa on 19-Nov-14.
 */
public class Functions {
    private static Soldier user = new Soldier();
    public static String SERVER = "http://192.168.0.1";

    public static Soldier getUser() {
        return user;
    }

    public static void setUser(Soldier user) {
        Functions.user = user;
    }

    public static boolean hasInternet(Context ctx) {
        ConnectivityManager conMgr = (ConnectivityManager) ctx
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (conMgr != null) {
            NetworkInfo i = conMgr.getActiveNetworkInfo();
            if (i != null) {
                if (!i.isConnected())
                    return false;
                if (!i.isAvailable())
                    return false;
            }

            if (i == null)
                return false;

        } else
            return false;
        return true;
    }
 }
