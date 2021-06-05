package com.skullzbones.vortexconnect.interfaces;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.skullzbones.vortexconnect.model.Message;

import java.util.List;


import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

@Dao
public interface MessageDAO {
    @Query("SELECT * FROM message")
    Flowable<List<Message>> getAll();

    @Query("SELECT *, MAX(createdAt) FROM message GROUP BY user")
    LiveData<List<Message>> getDialogMakerMessages();

    @Query("SELECT * FROM message WHERE user IS :userId ORDER BY createdAt DESC LIMIT :limit")
    LiveData<List<Message>> loadAllById(String userId, int limit);

    @Query("SELECT * FROM message WHERE user IS :userId AND createdAt <= :timestamp ORDER BY createdAt DESC LIMIT :limit")
    Single<List<Message>> loadMoreMessageById(String userId, long timestamp,int limit);

    @Query("SELECT COUNT(*) FROM Message WHERE user IS :userid")
    int getTotalMessageUser(String userid);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insert(final Message mess);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertSync(final Message mess);

    @Insert
    void insertAll(Message... messes);
}