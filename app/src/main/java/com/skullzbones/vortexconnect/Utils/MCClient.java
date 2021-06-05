package com.skullzbones.vortexconnect.Utils;

import android.content.Context;

import com.skullzbones.mcserverproxy.Exceptions.MinecraftNotFoundException;
import com.skullzbones.mcserverproxy.Exceptions.NoVPNException;
import com.skullzbones.mcserverproxy.Exceptions.ServerNotSetException;
import com.skullzbones.mcserverproxy.Exceptions.StoragePermissionNotGiven;
import com.skullzbones.mcserverproxy.MConnector;
import com.skullzbones.vortexconnect.MainActivity;
import com.skullzbones.vortexconnect.R;
import com.skullzbones.vortexconnect.model.Server;

public class MCClient {
  public static void joinAndPlay(Context context, String ip, Integer port) {
    try {

      MConnector.with(context)
          .setTargetServer(ip, port)
          .setInGameName(MainActivity.myAccount.getValue().name)
          .setSuppressStoragePermissions(false)
          .start();
    } catch (NoVPNException e) {
      e.printStackTrace();
      ToastUtils.out(context, R.string.vpn_not_permitted);
    } catch (MinecraftNotFoundException e) {
      e.printStackTrace();
      ToastUtils.out(context, R.string.minecraft_not_found);
    } catch (ServerNotSetException e) {
      e.printStackTrace();
    } catch (StoragePermissionNotGiven e){
      e.printStackTrace();
    }
  }

  public static void joinServerClick(Context context, Server s){
    if(!s.serverIp.isEmpty())
      joinAndPlay(context, s.serverIp, s.serverPort);
    else{
      // Logic for password protected servers, Need to ask for password then start server after
      // Querying main server.
    }
  }
}
