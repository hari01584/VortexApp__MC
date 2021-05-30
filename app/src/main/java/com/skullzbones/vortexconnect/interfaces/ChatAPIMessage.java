package com.skullzbones.vortexconnect.interfaces;

import com.skullzbones.vortexconnect.model.Message;

public interface ChatAPIMessage {
    void receivedMessage(Message recv);
}
