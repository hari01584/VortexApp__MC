package com.skullzbones.vortexconnect.interfaces;

import com.google.firebase.firestore.DocumentSnapshot;

public interface UserAPIExchange {
    void returnUserData(DocumentSnapshot documentSnapshot);
}

