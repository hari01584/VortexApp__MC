package com.skullzbones.vortexconnect.API;

import android.content.Context;
import android.util.Log;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

public class MCQueryAPI {
  private static final String API_URL = "https://api.mcsrvstat.us/bedrock/2/%s:%d";
  private static final String TAG = "c/MCQueryAPI";

  public static void QueryServer(Context context, String url, Integer port, FutureCallback<JsonObject> callback){
    String callUrl = String.format(API_URL, url, port);
    if(context == null) return;
    Ion.with(context)
        .load(callUrl)
        .noCache()
        .asJsonObject()
        .setCallback(callback);
  }
}
