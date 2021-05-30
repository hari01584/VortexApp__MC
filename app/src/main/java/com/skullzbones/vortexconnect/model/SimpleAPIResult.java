package com.skullzbones.vortexconnect.model;

import com.google.gson.annotations.SerializedName;

public class SimpleAPIResult {
  @SerializedName("code")
  public int code;

  @SerializedName("message")
  public String message;
}
