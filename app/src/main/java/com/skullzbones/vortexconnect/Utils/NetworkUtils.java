package com.skullzbones.vortexconnect.Utils;

import android.content.Context;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;

public class NetworkUtils {
  public static boolean isThisMyIpAddress(InetAddress addr) {
    // Check if the address is a valid special local or loop back
    if (addr.isAnyLocalAddress() || addr.isLoopbackAddress())
      return true;

    // Check if the address is defined on any interface
    try {
      return NetworkInterface.getByInetAddress(addr) != null;
    } catch (SocketException e) {
      return false;
    }
  }

  public static String getIp() throws Exception {
    URL whatismyip = new URL("http://checkip.amazonaws.com");
    BufferedReader in = null;
    try {
      in = new BufferedReader(new InputStreamReader(
          whatismyip.openStream()));
      String ip = in.readLine();
      return ip;
    } finally {
      if (in != null) {
        try {
          in.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  public static void showServerAnnouncements(Context context) {


  }
}
