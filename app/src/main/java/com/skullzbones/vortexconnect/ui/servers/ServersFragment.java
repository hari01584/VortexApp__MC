package com.skullzbones.vortexconnect.ui.servers;

import android.app.Activity;
import android.content.Intent;
import android.net.VpnService;
import android.util.Log;
import android.view.View.OnClickListener;
import android.widget.Button;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.skullzbones.mcserverproxy.Exceptions.MinecraftNotFoundException;
import com.skullzbones.mcserverproxy.Exceptions.NoVPNException;
import com.skullzbones.mcserverproxy.Exceptions.ServerNotSetException;
import com.skullzbones.mcserverproxy.MConnector;
import com.skullzbones.vortexconnect.API.ChatAPI;
import com.skullzbones.vortexconnect.API.FirebaseCreds;
import com.skullzbones.vortexconnect.API.MCQueryAPI;
import com.skullzbones.vortexconnect.API.MCServerAPI;
import com.skullzbones.vortexconnect.API.UserAPI;
import com.skullzbones.vortexconnect.R;

import com.skullzbones.vortexconnect.Utils.MCClient;
import com.skullzbones.vortexconnect.Utils.NetworkUtils;
import com.skullzbones.vortexconnect.Utils.ToastUtils;
import com.skullzbones.vortexconnect.interfaces.MyServerClickListener;
import com.skullzbones.vortexconnect.interfaces.UserAPIQuery;
import com.skullzbones.vortexconnect.model.ListAllAPIResult;
import com.skullzbones.vortexconnect.model.Server;
import com.skullzbones.vortexconnect.model.ServersAdapter;
import com.skullzbones.vortexconnect.model.SimpleAPIResult;
import com.skullzbones.vortexconnect.model.User;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;

import static android.app.Activity.RESULT_OK;

public class ServersFragment extends Fragment {

  private static final String TAG = "r/ServersFragment";
  private ServersViewModel mViewModel;
  private Button mButton;
  private FloatingActionButton fab;
  ActivityResultLauncher<Intent> someActivityResultLauncher;
  private RecyclerView recyclerView;
  private SwipeRefreshLayout swipeRefreshLayout;

  public static ServersFragment newInstance() {
    return new ServersFragment();
  }

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    View v = inflater.inflate(R.layout.servers_fragment, container, false);
    fab = v.findViewById(R.id.floating_action_button);
    recyclerView = (RecyclerView) v.findViewById(R.id.servers_recyclerview);
    swipeRefreshLayout = v.findViewById(R.id.swiperefresh);
    someActivityResultLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> {
          if (result.getResultCode() == Activity.RESULT_OK) {
            // There are no request codes
            Intent data = result.getData();
          }
        });
    return v;
  }

  @Override
  public void onViewCreated(@NonNull @NotNull View view,
      @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    mViewModel = new ViewModelProvider(this).get(ServersViewModel.class);

    if (mViewModel.servers.size() == 0) {
      MCServerAPI.displayAnnouncements(getContext());

      recyclerView.setHasFixedSize(true);
      recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
      mViewModel.adapter = new ServersAdapter(getContext(), mViewModel.servers,
          s -> {
            Intent i = VpnService.prepare(getContext());
            if(i!=null){
              ToastUtils.out(getContext(), R.string.give_vpn_perm);
              someActivityResultLauncher.launch(i);
            }
            else{
              MCClient.joinServerClick(getContext(), s);
            }
          });
      recyclerView.setAdapter(mViewModel.adapter);
      //swipeRefreshLayout.setRefreshing(true);
      FirebaseCreds.refresh.observe(getViewLifecycleOwner(), new Observer<Boolean>() {
        @Override
        public void onChanged(Boolean aBoolean) {
          callfillRefresh();
        }
      });

      swipeRefreshLayout.setOnRefreshListener(() -> {
        callfillRefresh();
        swipeRefreshLayout.setRefreshing(true);
      });

      fab.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
          Intent i = new Intent(getContext(), CreateActivity.class);
          startActivity(i);
        }
      });
    }
  }

  public void callfillRefresh() {
    mViewModel.servers.clear();
    MCServerAPI.listAllServers(getContext(), (e, result) -> {
      if(result==null){
        mViewModel.notifyUpdate.postValue(true);
        e.printStackTrace();
        return;
      }
      Gson gson = new Gson();
      ListAllAPIResult list = gson.fromJson(result, ListAllAPIResult.class);
      if(list.data == null) {
        mViewModel.notifyUpdate.postValue(true);
        return;
      }
      List<Server> lit = new ArrayList<>(list.data);
      for (Server s : lit) {
        UserAPI.getUser(s.getId(), user -> {
          s.image = user.avatar;
          s.shortdesc = "Hosted by " + user.getName();
          if (s.serverIp.isEmpty()) {
            mViewModel.servers.add(s);
            mViewModel.notifyUpdate.postValue(true);
          } else {
            MCQueryAPI.QueryServer(getContext(), s.serverIp, s.serverPort,
                (e1, r) -> {
                  if (r == null) {
                    e1.printStackTrace();
                    s.player = "0/0";
                    mViewModel.servers.add(s);
                    mViewModel.notifyUpdate.postValue(true);
                    MCServerAPI.reportOfflineServer(getContext(), s);
                    return;
                  }
                  boolean online = r.get("online").getAsBoolean();
                  if (online) {
                    int pmax = r.getAsJsonObject("players").get("max").getAsInt();
                    int onl = r.getAsJsonObject("players").get("online").getAsInt();
                    s.player = onl + "/" + pmax;
                    s.shortdesc+=String.format("(ver. %s)", r.get("version").getAsString());
                  }
                  else{
                    s.player = "0/0";
                    s.shortdesc+=" (Probable Offline)";
                  }
                  mViewModel.servers.add(s);
                  mViewModel.notifyUpdate.postValue(true);
                });
          }
        });
      }
    });
    mViewModel.notifyUpdate.observe(getViewLifecycleOwner(), new Observer<Boolean>() {
      @Override
      public void onChanged(Boolean aBoolean) {
        aBoolean = false;
        mViewModel.adapter.notifyDataSetChanged();
        swipeRefreshLayout.setRefreshing(false);
      }
    });
//        mViewModel.servers.add(new Server("ads",
//            "Welcome to minecraft server of agorange",
//            "Hosted by playerksUi0",
//            "3/10","https://picsum.photos/400"));

  }

}