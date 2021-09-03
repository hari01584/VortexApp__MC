package com.skullzbones.vortexconnect.IAP;

import android.content.Context;
import com.limerse.iap.IapConnector;
import com.skullzbones.vortexconnect.R;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class donationsManager {
  donationsManager(Context context){
    List<String> nonConsumablesList = Collections.singletonList("lifetime");
    List<String> consumablesList = Arrays.asList("base", "moderate", "quite", "plenty", "yearly");
    List<String> subsList = Collections.singletonList("subscription");

    IapConnector iapConnector = new IapConnector(
        context,
        nonConsumablesList,
        consumablesList,
        subsList,
        context.getString(R.string.google_play_app_license_key),
        true
    );
  }
}
