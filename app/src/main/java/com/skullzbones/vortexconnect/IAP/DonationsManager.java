package com.skullzbones.vortexconnect.IAP;

import android.app.Activity;
import android.content.Context;
import android.widget.Toast;
import com.limerse.iap.DataWrappers;
import com.limerse.iap.IapConnector;
import com.limerse.iap.PurchaseServiceListener;
import com.skullzbones.vortexconnect.R;
import com.skullzbones.vortexconnect.Utils.ToastUtils;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public class DonationsManager {
  Context context;
  IapConnector iapConnector;
  List<String> consumablesList = Arrays.asList(
      "vortex_donate_3",
      "vortex_donate_10",
      "vortex_donate_50",
      "vortex_donate_100"
  );

  public DonationsManager(Context context){
    this.context = context;

    iapConnector = new IapConnector(
        context,
        Collections.emptyList(),
        consumablesList,
        Collections.emptyList(),
        context.getString(R.string.google_play_app_license_key),
        true
    );

    iapConnector.addPurchaseListener(new PurchaseServiceListener() {
      public void onPricesUpdated(@NotNull Map iapKeyPrices) {

      }

      public void onProductPurchased(DataWrappers.PurchaseInfo purchaseInfo) {
        ToastUtils.out(context, R.string.thanks_for_donations);
      }

      public void onProductRestored(DataWrappers.PurchaseInfo purchaseInfo) {

      }
    });
  }

  public void donateIndex(Activity activity, int index){
    iapConnector.purchase(activity, consumablesList.get(index));
  }

}
