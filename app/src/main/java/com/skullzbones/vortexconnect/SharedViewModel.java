package com.skullzbones.vortexconnect;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.database.DatabaseReference;
import com.skullzbones.vortexconnect.API.FirebaseCreds;
import com.skullzbones.vortexconnect.database.DatabaseMain;
import com.skullzbones.vortexconnect.model.User;
import org.jetbrains.annotations.NotNull;
//import com.skullzbones.vortexconnect.prefs.Preference_BasicPrefs;

public class SharedViewModel extends ViewModel {
   // Preference_BasicPrefs basicPrefs;

    public FirebaseAuth mAuth;
    DatabaseReference mDatabase;
    private GoogleSignInClient mGoogleSignInClient;

    MutableLiveData<String> mUserId = new MutableLiveData<>();
    boolean isLogged;
    boolean mUserAnonymous;
    String mUserDisplayName;
    Uri mUserPhotoUrl;

    void setFirebaseUser(FirebaseUser user){
        FirebaseCreds.uid = user.getUid();
        user.getIdToken(false).addOnCompleteListener(
            new OnCompleteListener<GetTokenResult>() {
                @Override
                public void onComplete(@NonNull @NotNull Task<GetTokenResult> task) {
                    FirebaseCreds.token = task.getResult().getToken();
                    FirebaseCreds.refresh.postValue(true);
                }
            });
        isLogged = true;
        mUserDisplayName = user.getDisplayName();
        mUserPhotoUrl = user.getPhotoUrl();
        mUserAnonymous = user.isAnonymous();
        mUserId.setValue(user.getUid());
    }

    // Local Cached Stuff
}
