package com.skullzbones.vortexconnect.model;


import com.google.gson.annotations.SerializedName;

public class Server {
  @SerializedName("host_id")
  public String id;
  @SerializedName("server_name")
  public String title;
  public String shortdesc;
  @SerializedName("playerCount")
  public String player;
  public String image;
  @SerializedName("isPassword")
  public String isPasswordProtected;

  @SerializedName("host_extip")
  public String serverIp;
  @SerializedName("host_port")
  public Integer serverPort;

  public Server(String id, String title, String shortdesc, String player, String image) {
    this.id = id;
    this.title = title;
    this.shortdesc = shortdesc;
    this.player = player;
    this.image = image;
  }

  public String getId() {
    return id;
  }

  public String getTitle() {
    return title;
  }

  public String getShortdesc() {
    return shortdesc;
  }

  public String getPlayer() {
    return player;
  }
  
  public String getImage() {
    return image;
  }
}

