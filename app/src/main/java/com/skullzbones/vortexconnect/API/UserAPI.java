package com.skullzbones.vortexconnect.API;

import android.content.Context;
import android.nfc.Tag;
import android.util.Log;
import android.webkit.URLUtil;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.room.rxjava3.EmptyResultSetException;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.skullzbones.vortexconnect.MainActivity;
import com.skullzbones.vortexconnect.R;
import com.skullzbones.vortexconnect.Utils.RandomPersona;
import com.skullzbones.vortexconnect.Utils.ToastUtils;
import com.skullzbones.vortexconnect.fake.UserPersona;
import com.skullzbones.vortexconnect.interfaces.UserAPIExchange;
import com.skullzbones.vortexconnect.interfaces.UserAPIQuery;
import com.skullzbones.vortexconnect.model.User;

import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class UserAPI {

    private static final String TAG = "r/UserAPI";
    private static final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    static boolean alreadyMadeSnap = false;
    private final CountDownLatch doneSignal = new CountDownLatch(1);

    public static void userEntryInit(FirebaseUser user, UserAPIExchange exchange){
        firestore.collection("users").document(user.getUid()).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if(!task.getResult().exists()) {
                            createUserEntry(user, exchange);
                        }
                        else{
                            exchange.returnUserData(task.getResult());
                        }
                    }
                    else{
                        Log.e(TAG, task.getException().getMessage());
                    }
                });
    }

    protected static void createUserEntry(FirebaseUser user, UserAPIExchange exchange) {
        Map<String, Object> record = new HashMap<>();
        record.put("uid", user.getUid());
        record.put("displayName", "player"+user.getUid().substring(0,4));
        record.put("imageUrl", RandomPersona.getRandImageAvatar());
        record.put("groups", new ArrayList<String>());

        firestore.collection("users").document(user.getUid())
                .set(record)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully written!");
                        userEntryInit(user, exchange);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                    }
                });
    }

    public static void updateImage(Context context, String targUrl, FirebaseUser user){
      if(!URLUtil.isValidUrl(targUrl)){
        ToastUtils.out(context, R.string.invalid_url);
        return;
      }
      firestore.collection("users").document(user.getUid())
          .update("imageUrl", targUrl).addOnSuccessListener(new OnSuccessListener<Void>() {
        @Override
        public void onSuccess(Void unused) {
          ToastUtils.out(context, R.string.updated_image);
        }
      });
    }

  public static void updateDisplayName(Context context, String name, FirebaseUser user){
    if(name.length() > 20){
      ToastUtils.out(context, R.string.too_long_name);
      return;
    }
    firestore.collection("users").document(user.getUid())
        .update("displayName", name).addOnSuccessListener(new OnSuccessListener<Void>() {
      @Override
      public void onSuccess(Void unused) {
        ToastUtils.out(context, R.string.updated_name);
      }
    });
  }


  public static void getUser(String uid, UserAPIQuery query){
        if(uid.startsWith("admin")){
          query.returnsUser(UserPersona.getAdminUserPersona(uid));
          return;
        }
        MainActivity.databaseMain.userDAO().getUserByUid(uid).observeOn(Schedulers.io()).subscribe(new SingleObserver<User>() {
            @Override
            public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {

            }

            @Override
            public void onSuccess(@io.reactivex.rxjava3.annotations.NonNull User user) {
                query.returnsUser(user);
            }

            @Override
            public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
                if(e instanceof EmptyResultSetException){
                    queryFirebaseUser(uid, query);
                }
                else{ e.printStackTrace(); }
            }

        });
    }

    public static User getUserFromDBSync(String uid){
        try{
            return MainActivity.databaseMain.userDAO().getUserByUid(uid).blockingGet();
        }
        catch (EmptyResultSetException ex){
            queryFirebaseUser(uid, user -> { });
            return UserPersona.getInvalidUserPersona();
        }
    }

    private static void queryFirebaseUser(String uid, UserAPIQuery query){
        firestore.collection("users").document(uid).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if(task.getResult().exists()) {
                            final User user = new User(task.getResult());
                            MainActivity.databaseMain.userDAO().insert(user);
                            query.returnsUser(user);
                        }
                    }
                    else{
                        Log.e(TAG, task.getException().getMessage());
                    }
                });
    }

    public static void searchUserByName(String name, UserAPIQuery query){
        firestore.collection("users").whereEqualTo("displayName", name).get()
                .addOnSuccessListener(data -> {
                    for(DocumentSnapshot q:data){
                        User u = new User(q);
                        query.returnsUser(u);
                        return;
                    }
                    query.returnsUser(null);
                });
    }
}
