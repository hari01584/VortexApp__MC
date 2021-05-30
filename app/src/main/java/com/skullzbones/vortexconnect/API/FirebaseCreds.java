package com.skullzbones.vortexconnect.API;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class FirebaseCreds {
  public static MutableLiveData<Boolean> refresh = new MutableLiveData<>();
  public static String uid;
  public static String token;
}
