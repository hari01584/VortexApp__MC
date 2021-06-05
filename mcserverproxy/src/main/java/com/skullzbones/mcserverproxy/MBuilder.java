package com.skullzbones.mcserverproxy;

import android.content.Context;
import com.skullzbones.mcserverproxy.Exceptions.MinecraftNotFoundException;
import com.skullzbones.mcserverproxy.Exceptions.NoVPNException;
import com.skullzbones.mcserverproxy.Exceptions.ServerNotSetException;
import com.skullzbones.mcserverproxy.Exceptions.StoragePermissionNotGiven;

public class MBuilder {

  private final Context mContext;
  private String serverIp;
  private int serverPort;

  private boolean supressStoragePermissions = false;
  private String inGameName;
  private String gameSkinUri;

  public MBuilder(Context context) {
    mContext = context;
  }

  /**
   * Set Address and port of connecting server, Address can be in form of domain as well (ex
   * abc.xyz.com), Port is also numerical
   *
   * @param ip   Address for target connecting server. (Can be in ip form or pure domain).
   * @param port Port for the target connecting server. (Only Integer)
   * @return Self build instance.
   */
  public MBuilder setTargetServer(String ip, int port) {
    this.serverIp = ip;
    this.serverPort = port;
    return this;
  }

  /**
   * Set Minecraft character name before launching
   * <p>
   * Uses storage permissions to work, Make sure you have read and write access to
   * /0/emulated/games/com.mojang/minecraftpe/.* or else it won't work.
   *
   * @param name Ingame name, Not to be very long.
   * @return Self builder instance.
   */
  public MBuilder setInGameName(String name) {
    this.inGameName = name;
    return this;
  }

  /**
   * Set ingame character skin before launching
   * <p>
   * Uses storage permission, same path as $setInGameName method, Make sure to convert your image
   * document intent to String (Using $toString() override) before passing.
   *
   * @param gameSkinUri String of image intent, Use intent.$toString(), where intent is your image
   *                    intent.
   * @ Self builder instance.
   */
  public MBuilder setGameSkin(String gameSkinUri) {
    this.gameSkinUri = gameSkinUri;
    return this;
  }

  /**
   * Suppress throwing of StoragePermissions Exception
   * <p>
   * If suppressed, the library will not throw the exception and will silently run minecraft without
   * changing ingame name or skin, If not suppressed then it will throw storage exception for user
   * to intervene. Default: False (Do not suppress)
   *
   * @param b pass true to suppress throwing of StoragePermission exception else false.
   * @return Self instance builder.
   */
  public MBuilder setSuppressStoragePermissions(boolean b) {
    this.supressStoragePermissions = b;
    return this;
  }

  /**
   * Build the connector
   * @return MConnector Instance built from self builder instance.
   */
  public MConnector build() {
    return new MConnector(this);
  }

  /**
   * Shortcut start the connector application directly from MBuilder instance.
   * @throws NoVPNException Vpn permission not given by user.
   * @throws ServerNotSetException Server ip addr or port is incorrect or empty.
   * @throws MinecraftNotFoundException Minecraft not found in device.
   * @throws StoragePermissionNotGiven Storage permission not given. (Cannot modify mc files)
   */
  public void start()
      throws NoVPNException, ServerNotSetException, MinecraftNotFoundException, StoragePermissionNotGiven {
    new MConnector(this).start();
  }

  public Context getContext() {
    return mContext;
  }

  public String getServerIp() {
    return this.serverIp;
  }

  public int getServerPort() {
    return this.serverPort;
  }

  public String getInGameName() {
    return this.inGameName;
  }

  public String getGameSkinUri() {
    return this.gameSkinUri;
  }

  public boolean getSuppressStoragePermissions() {
    return this.supressStoragePermissions;
  }

}