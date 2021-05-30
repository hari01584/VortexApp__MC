package com.skullzbones.vortexconnect.ui.servers;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.VpnService;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.skullzbones.vortexconnect.API.MCQueryAPI;
import com.skullzbones.vortexconnect.API.MCServerAPI;
import com.skullzbones.vortexconnect.R;
import com.skullzbones.vortexconnect.Utils.NetworkUtils;
import com.skullzbones.vortexconnect.Utils.ToastUtils;

public class CreateActivity extends AppCompatActivity implements OnClickListener {

  private static final String MY_PREFS_NAME = "prefLastServ";
  private static final String TAG = "r/CreateActivity";
  ActivityResultLauncher<Intent> someActivityResultLauncher;

  Button button;
  EditText ipedit;
  EditText portedit;
  EditText nameedit;
  EditText passedit;
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_create);
    ipedit = findViewById(R.id.ed01);
    portedit = findViewById(R.id.ed02);
    nameedit = findViewById(R.id.ed03);
    passedit = findViewById(R.id.ed04);
    loadLastServer();
    someActivityResultLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> {
          if (result.getResultCode() == Activity.RESULT_OK) {
            // There are no request codes
            Intent data = result.getData();
//            doSomeOperations();
          }
        });


    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
    StrictMode.setThreadPolicy(policy);
    button = findViewById(R.id.start_button);
    button.setOnClickListener(this);
  }

  private void loadLastServer() {
    SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
    String i = prefs.getString("ip", "");
    int p = prefs.getInt("port", 19132);
    String n = prefs.getString("name", "Join my minecraft servers");
    String pas = prefs.getString("pass", "");
    ipedit.setText(i);
    nameedit.setText(n);
    passedit.setText(pas);
    portedit.setText(String.valueOf(p));
  }
  private void saveLastServer(String ipadrr, Integer port, String name, String pass) {
    SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
    editor.putString("ip", ipadrr);
    editor.putInt("port", port);
    editor.putString("name", name);
    editor.putString("pass", pass);
    editor.apply();
  }


  @Override
  public void onClick(View v) {
    String ipadrr = ipedit.getText().toString();
    Integer port = Integer.valueOf(portedit.getText().toString());
    String name = nameedit.getText().toString();
    String pass = passedit.getText().toString();
    if(ipadrr.isEmpty() || name.isEmpty()) {return;};

    if(!pass.isEmpty()){
      ToastUtils.make(this, "Sorry the feature to password protect server's are not here yet! Consider checking our open source repository for contribution to add this.");
      return;
    }

    saveLastServer(ipadrr, port, name, pass);

    Intent i = VpnService.prepare(this);
    if(i!=null){
      ToastUtils.out(this, R.string.give_vpn_perm);
      someActivityResultLauncher.launch(i);
    }

    if(ipadrr.equals("127.0.0.1") || ipadrr.equals("localhost")){
      try {
        ipadrr = NetworkUtils.getIp();
        //ipedit.setText(ipadrr);
      } catch (Exception e) {
        e.printStackTrace();
      }
      MCServerAPI.createServer(this, ipadrr, port, name, pass);
      return;
    }

    String finalIpadrr = ipadrr;
    MCQueryAPI.QueryServer(this, ipadrr, port, new FutureCallback<JsonObject>() {
      @Override
      public void onCompleted(Exception e, JsonObject result) {
        if(result==null) {
          ToastUtils.out(CreateActivity.this, R.string.offline_invalid_server);
          return;
        }
        boolean online = result.get("online").getAsBoolean();
        if(!online){
          ToastUtils.out(CreateActivity.this, R.string.offline_invalid_server);
        }
        else{
          MCServerAPI.createServer(CreateActivity.this, finalIpadrr, port, name, pass);
        }
      }
    });
  }
}