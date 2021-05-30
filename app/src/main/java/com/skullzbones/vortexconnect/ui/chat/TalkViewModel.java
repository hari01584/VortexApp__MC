package com.skullzbones.vortexconnect.ui.chat;

import androidx.lifecycle.ViewModel;

import com.stfalcon.chatkit.commons.ImageLoader;
import com.stfalcon.chatkit.messages.MessagesListAdapter;

public class TalkViewModel extends ViewModel {
    String senderId = "null";
    ImageLoader imageLoader;
    MessagesListAdapter messagesAdapter;
}