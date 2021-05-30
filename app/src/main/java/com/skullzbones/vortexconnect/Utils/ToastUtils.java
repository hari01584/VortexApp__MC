package com.skullzbones.vortexconnect.Utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public class ToastUtils {
    public static String TAG = "l/ToastUtils";
    public static void out(Context context, int resid){
        String mess = context.getResources().getString(resid);
        Log.i(TAG, mess);
        Toast.makeText(context, mess, Toast.LENGTH_SHORT).show();
    }

    public static void make(Context context, String message){
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }
}
