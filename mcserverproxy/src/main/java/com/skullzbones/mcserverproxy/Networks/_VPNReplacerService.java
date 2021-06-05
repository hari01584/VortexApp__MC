/*
 ** Copyright 2015, Mohamed Naufal
 **
 ** Licensed under the Apache License, Version 2.0 (the "License");
 ** you may not use this file except in compliance with the License.
 ** You may obtain a copy of the License at
 **
 **     http://www.apache.org/licenses/LICENSE-2.0
 **
 ** Unless required by applicable law or agreed to in writing, software
 ** distributed under the License is distributed on an "AS IS" BASIS,
 ** WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 ** See the License for the specific language governing permissions and
 ** limitations under the License.
 */

package com.skullzbones.mcserverproxy.Networks;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.VpnService;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.skullzbones.mcserverproxy.R;

import java.io.Closeable;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.Selector;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class _VPNReplacerService extends VpnService {

  public static final String FLAG_SERVIP = "servip";
  public static final String FLAG_SERVPORT = "servport";
  public static final String STOP_VPN_FLAG = "stop_vpn";
  public static final String BROADCAST_VPN_STATE = "xyz.hexene.localvpn.VPN_STATE";

  private static final long INTERVAL_KILL = 60 * 1000;

  private static final String TAG = _VPNReplacerService.class.getSimpleName();
  private static final String VPN_ADDRESS = "10.0.0.2"; // Only IPv4 support for now
  private static final String VPN_ROUTE = "0.0.0.0"; // Intercept everything
  private static final String VPN_ALLOWED_PACKAGE_NAME = "com.mojang.minecraftpe";
  private static InetAddress GLOBAL_TARGET_IP = null;
  private static Integer GLOBAL_TARGET_PORT = null;
  private static boolean isRunning = false;
  private ParcelFileDescriptor vpnInterface = null;
  private PendingIntent pendingIntent;

  private ConcurrentLinkedQueue<Packet> deviceToNetworkUDPQueue;
  private ConcurrentLinkedQueue<Packet> deviceToNetworkTCPQueue;
  private ConcurrentLinkedQueue<ByteBuffer> networkToDeviceQueue;
  private ExecutorService executorService;

  private Selector udpSelector;
  private Selector tcpSelector;
  private static LocalBroadcastManager localbrodcastManager;

  @Override
  public void onCreate() {
    super.onCreate();
    isRunning = true;
    setupVPN();
    setupLocalBrodcastManager();
    try {
      udpSelector = Selector.open();
      tcpSelector = Selector.open();
      deviceToNetworkUDPQueue = new ConcurrentLinkedQueue<>();
      deviceToNetworkTCPQueue = new ConcurrentLinkedQueue<>();
      networkToDeviceQueue = new ConcurrentLinkedQueue<>();

      executorService = Executors.newFixedThreadPool(5);
      executorService.submit(new UDPInput(networkToDeviceQueue, udpSelector));
      executorService.submit(new UDPOutput(deviceToNetworkUDPQueue, udpSelector, this));
      executorService.submit(new TCPInput(networkToDeviceQueue, tcpSelector));
      executorService
          .submit(new TCPOutput(deviceToNetworkTCPQueue, networkToDeviceQueue, tcpSelector, this));
      executorService.submit(new VPNRunnable(vpnInterface.getFileDescriptor(),
          deviceToNetworkUDPQueue, deviceToNetworkTCPQueue, networkToDeviceQueue));
      localbrodcastManager.sendBroadcast(new Intent(BROADCAST_VPN_STATE).putExtra("running", true));
      Log.i(TAG, "Started");
    } catch (IOException e) {
      // TODO: Here and elsewhere, we should explicitly notify the user of any errors
      // and suggest that they stop the service, since we can't do it ourselves
      Log.e(TAG, "Error starting service", e);
      cleanup();
    }
  }

  private void setupLocalBrodcastManager() {
    localbrodcastManager = LocalBroadcastManager.getInstance(this);
    localbrodcastManager.registerReceiver(listener, new IntentFilter(STOP_VPN_FLAG));
  }

  private void setupVPN() {
    if (vpnInterface == null) {
      Builder builder = new Builder();
      builder.addAddress(VPN_ADDRESS, 32);
      builder.addRoute(VPN_ROUTE, 0);
      try {
        builder.addAllowedApplication(VPN_ALLOWED_PACKAGE_NAME);
      } catch (PackageManager.NameNotFoundException e) {
        e.printStackTrace();
      }
      vpnInterface = builder.setSession("libMcServerProxy")
          .setConfigureIntent(pendingIntent).establish();
    }
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    new Thread(() -> {
      try {
        GLOBAL_TARGET_IP = InetAddress.getByName(intent.getStringExtra(FLAG_SERVIP));
      } catch (UnknownHostException e) {
        e.printStackTrace();
      }
    }).start();

    GLOBAL_TARGET_PORT = intent.getIntExtra(FLAG_SERVPORT, 19132);
    killTimely();
    return START_STICKY;
  }

  private void killTimely() {
    TimerTask myTask = new TimerTask() {
      public void run() {
        if (isRunning) {
          cleanup();
        }
      }
    };
    Timer myTimer = new Timer();
    myTimer.schedule(myTask, INTERVAL_KILL);
  }

  public static boolean isRunning() {
    return isRunning;
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    isRunning = false;
    executorService.shutdownNow();
    cleanup();
    Log.i(TAG, "Stopped");
  }

  public void cleanup() {
    isRunning = false;
    deviceToNetworkTCPQueue = null;
    deviceToNetworkUDPQueue = null;
    networkToDeviceQueue = null;
    ByteBufferPool.clear();
    closeResources(udpSelector, tcpSelector, vpnInterface);
    stopSelf();
  }

  @Override
  public void onTaskRemoved(Intent rootIntent) {
    super.onTaskRemoved(rootIntent);
    Log.i(TAG, "Removed from recents");
    executorService.shutdownNow();
    cleanup();
    stopSelf();
  }


  private BroadcastReceiver listener = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      if (isRunning) {
        cleanup();
      }
    }
  };

  // TODO: Move this to a "utils" class for reuse
  private static void closeResources(Closeable... resources) {
    for (Closeable resource : resources) {
      try {
        resource.close();
      } catch (IOException e) {
        // Ignore
      }
    }
  }

  private static class VPNRunnable implements Runnable {

    private static final String TAG = VPNRunnable.class.getSimpleName();

    private FileDescriptor vpnFileDescriptor;

    private ConcurrentLinkedQueue<Packet> deviceToNetworkUDPQueue;
    private ConcurrentLinkedQueue<Packet> deviceToNetworkTCPQueue;
    private ConcurrentLinkedQueue<ByteBuffer> networkToDeviceQueue;

    public VPNRunnable(FileDescriptor vpnFileDescriptor,
        ConcurrentLinkedQueue<Packet> deviceToNetworkUDPQueue,
        ConcurrentLinkedQueue<Packet> deviceToNetworkTCPQueue,
        ConcurrentLinkedQueue<ByteBuffer> networkToDeviceQueue) {
      this.vpnFileDescriptor = vpnFileDescriptor;
      this.deviceToNetworkUDPQueue = deviceToNetworkUDPQueue;
      this.deviceToNetworkTCPQueue = deviceToNetworkTCPQueue;
      this.networkToDeviceQueue = networkToDeviceQueue;
    }

    @Override
    public void run() {
      Log.i(TAG, "Started");

      FileChannel vpnInput = new FileInputStream(vpnFileDescriptor).getChannel();
      FileChannel vpnOutput = new FileOutputStream(vpnFileDescriptor).getChannel();

      try {
        ByteBuffer bufferToNetwork = null;
        boolean dataSent = true;
        boolean dataReceived;
        while (!Thread.interrupted()) {
          if (dataSent) {
            bufferToNetwork = ByteBufferPool.acquire();
          } else {
            bufferToNetwork.clear();
          }

          // TODO: Block when not connected
          int readBytes = 0;
          try {
            readBytes = vpnInput.read(bufferToNetwork);
          } catch (Exception e) {
            Log.i(TAG, "Shouldnt Be HEre");
            e.printStackTrace();
          }
          if (readBytes > 0) {
            dataSent = true;
            bufferToNetwork.flip();
            Packet packet = new Packet(bufferToNetwork);
            if (packet.isUDP()) {
              packet = ForwardPacket(packet);
              deviceToNetworkUDPQueue.offer(packet);
            } else if (packet.isTCP()) {
              deviceToNetworkTCPQueue.offer(packet);
            } else {
              Log.w(TAG, "Unknown packet type");
              Log.w(TAG, packet.ip4Header.toString());
              dataSent = false;
            }
          } else {
            dataSent = false;
          }

          ByteBuffer bufferFromNetwork = networkToDeviceQueue.poll();
          if (bufferFromNetwork != null) {
            bufferFromNetwork.flip();
            while (bufferFromNetwork.hasRemaining()) {
              vpnOutput.write(bufferFromNetwork);
            }
            dataReceived = true;

            ByteBufferPool.release(bufferFromNetwork);
          } else {
            dataReceived = false;
          }

          // TODO: Sleep-looping is not very battery-friendly, consider blocking instead
          // Confirm if throughput with ConcurrentQueue is really higher compared to BlockingQueue
          if (!dataSent && !dataReceived) {
            Thread.sleep(10);
          }
        }
      } catch (InterruptedException e) {
        Log.i(TAG, "Stopping");
      } catch (IOException e) {
        Log.w(TAG, e.toString(), e);
      } finally {
        closeResources(vpnInput, vpnOutput);
      }
    }

    private Packet ForwardPacket(Packet packet) {
      if (packet.udpHeader.destinationPort == GLOBAL_TARGET_PORT
          && packet.ip4Header.destinationAddress.equals(GLOBAL_TARGET_IP)) {
        localbrodcastManager.sendBroadcast(new Intent(STOP_VPN_FLAG));
      }

      if (packet.udpHeader.destinationPort == 19132
          && packet.ip4Header.destinationAddress != GLOBAL_TARGET_IP) {
        packet.ip4Header.destinationAddress = GLOBAL_TARGET_IP;
        packet.udpHeader.destinationPort = GLOBAL_TARGET_PORT;
        Log.i(TAG, "Forwarded Packets To Local Client!");
      }
      return packet;
    }
  }
}
