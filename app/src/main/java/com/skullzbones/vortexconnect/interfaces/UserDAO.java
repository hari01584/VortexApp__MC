package com.skullzbones.vortexconnect.interfaces;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.skullzbones.vortexconnect.model.User;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

@Dao
public interface UserDAO {
    @Query("SELECT * FROM User WHERE id IS :uid")
    Single<User> getUserByUid(String uid);


    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(User user);

    @Query("UPDATE user SET lastChecked = strftime('%s','now') || substr(strftime('%f','now'),4) WHERE id IS :uid")
    Completable updateLastTimestamp(String uid);
}
