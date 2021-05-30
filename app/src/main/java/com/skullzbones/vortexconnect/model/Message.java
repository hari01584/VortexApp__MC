package com.skullzbones.vortexconnect.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.google.firebase.database.DataSnapshot;
import com.skullzbones.vortexconnect.API.UserAPI;
import com.skullzbones.vortexconnect.MainActivity;
import com.skullzbones.vortexconnect.converters.DateConverter;
import com.skullzbones.vortexconnect.fake.UserPersona;
import com.stfalcon.chatkit.commons.models.IMessage;

import org.jetbrains.annotations.NotNull;

import java.util.Date;

/*
 * Created by troy379 on 04.04.17.
 */
@Entity
public class Message implements IMessage {
    @PrimaryKey
    @NonNull
    public final String id;

    public String text;

    @TypeConverters(DateConverter.class)
    public Date createdAt;

    public final String user;
//    private Image image;

    @Ignore
    public Message(String id, String user, String text) {
        this(id, user, text, new Date());
    }


    public Message(String id, String user, String text, Date createdAt) {
        this.id = id;
        this.text = text;
        this.user = user;
        this.createdAt = createdAt;
    }

    @Ignore
    public Message(DataSnapshot dataSnapshot, String user) {
        this.id = dataSnapshot.getKey();
        this.text = dataSnapshot.child("content").getValue(String.class);
        this.user = user;
        this.createdAt = new Date(dataSnapshot.child("t").getValue(Long.class));
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public Date getCreatedAt() {
        return createdAt;
    }

    @Override
    public @NonNull
    User getUser() {
        if(!id.startsWith("mine")){
            User u = MainActivity.myAccount.getValue();
            if(u == null)
                return UserPersona.getInvalidUserPersona();
            return MainActivity.myAccount.getValue();
        }
        return getSenderUser();
    }

    @Ignore
    public User getSenderUser(){
        User u = UserAPI.getUserFromDBSync(user);
        if(u == null)
            return UserPersona.getInvalidUserPersona();
        return u;
    }

    //    @Override
//    public String getImageUrl() {
//        return image == null ? null : image.url;
//    }


    public String getStatus() {
        return "Sent";
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

//    public void setImage(Image image) {
//        this.image = image;
//    }

//    public static class Image {
//
//        private final String url;
//
//        public Image(String url) {
//            this.url = url;
//        }
//    }
//

    @NonNull
    @NotNull
    @Override
    public String toString() {
        return String.format("Key %s Says %s With user=> {%s} At %s",id, text, user, createdAt);
    }
}
