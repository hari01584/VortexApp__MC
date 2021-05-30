package com.skullzbones.vortexconnect.ui.chat;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.skullzbones.vortexconnect.model.Dialog;
import com.stfalcon.chatkit.commons.ImageLoader;
import com.stfalcon.chatkit.dialogs.DialogsListAdapter;

import java.util.List;

public class ChatViewModel extends ViewModel {
    DialogsListAdapter dialogsAdapter;
    ImageLoader imageLoader;

    MutableLiveData<List<Dialog>> dialogs = new MutableLiveData<>();
}