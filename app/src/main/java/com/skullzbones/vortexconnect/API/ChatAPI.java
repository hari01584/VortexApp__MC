package com.skullzbones.vortexconnect.API;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Logger;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.skullzbones.vortexconnect.MainActivity;
import com.skullzbones.vortexconnect.Utils.ToastUtils;
import com.skullzbones.vortexconnect.interfaces.ChatAPIMessage;
import com.skullzbones.vortexconnect.interfaces.UserAPIQuery;
import com.skullzbones.vortexconnect.model.Message;
import com.skullzbones.vortexconnect.model.User;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatAPI {
    private static final String TAG = "r/ChatAPI";
    private static final FirebaseDatabase database = FirebaseDatabase.getInstance();
    static boolean isInit = false;
    private static ChatAPIMessage MyChatrecv;

    public static void selfInit(){
        if(!isInit) {
            //database.useEmulator("192.168.18.3", 9000);
            isInit = true;
        }
    }

    static ValueEventListener myChatListener;
    static List<ValueEventListener> myGroupListeners;

    public static void attachAllListener(User myuser, ChatAPIMessage recv){
        attachMyChatListener(myuser, recv);

        //List<String> groups = myuser.groups; #TODO: Someday I'be worthy
        //attachGroupListener(myuser,recv,groups);
    }

    private static void detachMyGroupsListener(User myuser) {
        List<String> groups = myuser.groups;

        for(int i=0;i<groups.size();i++) {
            final String group = groups.get(i);
            DatabaseReference gpref = database.getReference("box").child(group).child("messages");
            gpref.removeEventListener(myGroupListeners.get(i));
        }
    }


    private static void attachGroupListener(User myuser, ChatAPIMessage recv, List<String> groups) {
        for(int i=0;i<groups.size();i++){
            final String group = groups.get(i);
            myGroupListeners.add(i, new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {

                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {

                }
            });

            DatabaseReference gpref = database.getReference("box").child(group).child("messages");
            gpref.addValueEventListener(myGroupListeners.get(i));
        }
    }

    public static void detachAllListener(User myuser){
        detachMyChatListener(myuser);
        //detachMyGroupsListener(myuser);
    }



    private static void attachMyChatListener(User myuser, ChatAPIMessage recv) {
        DatabaseReference myRef = database.getReference("box").child(myuser.getId()).child("messages");

        myChatListener =  new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists())
                {
                    for(DataSnapshot ds : dataSnapshot.getChildren()){
                        UserAPI.getUser(ds.child("from").getValue(String.class), user -> {
                            Message m = new Message(ds, user.id);
                            recv.receivedMessage(m);
                        });
                    }
                    myRef.setValue(null);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //
            }
        };

//        Map<String, Object> ob = new HashMap<>();
//        ob.put("from",myuser.getId());
//        ob.put("content","Hii");
//        Timestamp stamp = new Timestamp(System.currentTimeMillis());
//        ob.put("t", stamp.getTime());
//
//        myRef.child("gibbeiadad").setValue(ob);
        myRef.addValueEventListener(myChatListener);
    }

    private static void detachMyChatListener(User myuser) {
        if(myChatListener == null) return;
        DatabaseReference myRef = database.getReference("box").child(myuser.getId()).child("messages");
        myRef.removeEventListener(myChatListener);
    }

    public static void sendMessage(String senderId, String toString) {
        DatabaseReference myRef = database.getReference("box").child(senderId).child("messages");

        HashMap<String, Object> mess = new HashMap<String, Object>();
        mess.put("from", MainActivity.myAccount.getValue().getId());
        mess.put("content",toString);
        Timestamp stamp = new Timestamp(System.currentTimeMillis());
        mess.put("t", stamp.getTime());

        myRef.push().setValue(mess);

    }
}
