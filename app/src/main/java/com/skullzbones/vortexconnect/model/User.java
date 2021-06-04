package com.skullzbones.vortexconnect.model;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.firestore.DocumentSnapshot;
import com.skullzbones.vortexconnect.converters.ArrayConverter;
import com.skullzbones.vortexconnect.converters.UserConverter;
import com.stfalcon.chatkit.commons.models.IUser;


import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

/*
 * Created by troy379 on 04.04.17.
 */
@Entity
public class User implements IUser {
    @PrimaryKey
    @NonNull
    public final String id;
    public final String name;
    public final String avatar;
    public long lastChecked;
    public final boolean online;

    @TypeConverters(ArrayConverter.class)
    public ArrayList<String> groups;

    public User(String id, String name, String avatar, boolean online) {
        this.id = id;
        this.name = name;
        this.avatar = avatar;
        this.online = online;
    }

    @Ignore
    public User(DocumentSnapshot documentSnapshot) {
        this.id = documentSnapshot.get("uid", String.class);
        this.name = documentSnapshot.get("displayName", String.class);
        this.avatar = documentSnapshot.get("imageUrl", String.class);
        this.online = false;
        this.groups = (ArrayList<String>) documentSnapshot.get("groups");

    }

    @Override
    @Ignore
    public String getId() {
        return id;
    }

    @Override
    @Ignore
    public String getName() {
        return name;
    }

    @Override
    @Ignore
    public String getAvatar() {
        return avatar;
    }

    @Ignore
    public boolean isOnline() {
        return online;
    }

    @NonNull
    @NotNull
    @Override
    @Ignore
    public String toString() {
        return String.format("%s[%s]=>%s %s", id, name, avatar, groups);
    }
}
