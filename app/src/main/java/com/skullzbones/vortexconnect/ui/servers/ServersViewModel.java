package com.skullzbones.vortexconnect.ui.servers;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.skullzbones.vortexconnect.model.Server;
import com.skullzbones.vortexconnect.model.ServersAdapter;
import java.util.ArrayList;
import java.util.List;

public class ServersViewModel extends ViewModel {
    MutableLiveData<Boolean> notifyUpdate = new MutableLiveData<>();
    ArrayList<Server> servers = new ArrayList<>();
    ServersAdapter adapter;
}