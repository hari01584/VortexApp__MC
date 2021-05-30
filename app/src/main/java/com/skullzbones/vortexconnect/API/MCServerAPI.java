package com.skullzbones.vortexconnect.API;

import android.content.Context;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.async.http.NameValuePair;
import com.koushikdutta.ion.Ion;
import com.skullzbones.vortexconnect.MainActivity;
import com.skullzbones.vortexconnect.R;
import com.skullzbones.vortexconnect.Utils.MCClient;
import com.skullzbones.vortexconnect.Utils.ToastUtils;
import com.skullzbones.vortexconnect.model.Server;
import com.skullzbones.vortexconnect.model.SimpleAPIResult;
import java.util.Timer;
import java.util.TimerTask;
import javax.annotation.Nullable;

public class MCServerAPI {

  private static final String TAG = "t/MCServerCreater";
  private static String API_ANNOUNCEMENTS = "http://vortex.skullzbones.com/announcement.php";

  private static String API_CREATE_SERVER_URL = "http://vortex.skullzbones.com/api2/registerServer.php";
  private static String API_GET_SERVERS = "http://vortex.skullzbones.com/api2/listServers.php";
  private static String API_REPORTS_SERVERS = "http://vortex.skullzbones.com/api2/reportOfflineServer.php";

  public static void displayAnnouncements(Context context){
    Ion.with(context)
        .load(API_ANNOUNCEMENTS)
        .asJsonObject()
        .setCallback(new FutureCallback<JsonObject>() {
          @Override
          public void onCompleted(Exception e, JsonObject result) {
            if(result==null){
              e.printStackTrace();
              return;
            }
            Gson gson = new Gson();
            SimpleAPIResult res= gson.fromJson(result, SimpleAPIResult.class);
            if(res.code==1)
              ToastUtils.make(context, res.message);
          }
        });
  }

  public static void listAllServers(Context context, FutureCallback<JsonObject> callback){
    if(FirebaseCreds.token==null || FirebaseCreds.token.isEmpty()){
      ToastUtils.out(context, R.string.delay_servers);
      new Timer().schedule(new TimerTask() {
        @Override
        public void run() {
          listAllServers(context, callback);
        }
      }, 3000);
      return;
    }
    Ion.with(context)
        .load(API_GET_SERVERS)
        .setHeader("uid", FirebaseCreds.uid)
        .setHeader("token", FirebaseCreds.token)
        .asJsonObject()
        .setCallback(callback);
  }

  public static void createServer(Context context, String ipadrr, Integer port, String servname,@Nullable String password){
    JsonObject json = new JsonObject();
    json.addProperty("server_name", servname);
    json.addProperty("mc_ver", "0");
    json.addProperty("server_pass", password);
    json.addProperty("ext_ip", ipadrr);
    json.addProperty("ext_port", port);

    Ion.with(context)
        .load(API_CREATE_SERVER_URL)
        .setHeader("uid", FirebaseCreds.uid)
        .setHeader("token", FirebaseCreds.token)
        .setJsonObjectBody(json)
        .asJsonObject()
        .setCallback(new FutureCallback<JsonObject>() {
          @Override
          public void onCompleted(Exception e, JsonObject result) {
            Gson gson = new Gson();
            SimpleAPIResult res= gson.fromJson(result, SimpleAPIResult.class);
            ToastUtils.make(context, res.message);
            if(res.code==1){
              Log.i(TAG, "Starting MC");
              MCClient.joinAndPlay(context, ipadrr, port);
            }
          }
        });
  }

  public static void reportOfflineServer(Context context, Server s) {
    JsonObject json = new JsonObject();
    json.addProperty("server_user", s.id);
    json.addProperty("targetIp", s.serverIp);
    json.addProperty("targetPort", s.serverPort);

    if(context==null) return;
    Ion.with(context)
        .load(API_REPORTS_SERVERS)
        .setHeader("uid", FirebaseCreds.uid)
        .setHeader("token", FirebaseCreds.token)
        .setJsonObjectBody(json)
        .asJsonObject()
        .setCallback(new FutureCallback<JsonObject>() {

          @Override
          public void onCompleted(Exception e, JsonObject result) {

          }
        });
  }
}
