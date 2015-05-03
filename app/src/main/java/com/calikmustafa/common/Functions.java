package com.calikmustafa.common;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.calikmustafa.model.Soldier;

import java.io.IOException;

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
        //if (executeCommand())
            return true;

    }

    private static boolean executeCommand(){
        System.out.println(" executeCammand");
        Runtime runtime = Runtime.getRuntime();
        try
        {
            Process  mIpAddrProcess = runtime.exec("/system/bin/ping -c 1 "+Functions.SERVER.substring(7));
            //Log.w("server ping",Functions.SERVER.substring(7));
            int mExitValue = mIpAddrProcess.waitFor();
            System.out.println(" mExitValue "+mExitValue);
            if(mExitValue==0){
                return true;
            }else{
                return false;
            }
        }
        catch (InterruptedException ignore)
        {
            ignore.printStackTrace();
            System.out.println(" Exception:"+ignore);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            System.out.println(" Exception:"+e);
        }
        return false;
    }
 }
