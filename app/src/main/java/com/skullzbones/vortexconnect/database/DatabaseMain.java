package com.skullzbones.vortexconnect.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.skullzbones.vortexconnect.converters.ArrayConverter;
import com.skullzbones.vortexconnect.converters.DateConverter;
import com.skullzbones.vortexconnect.converters.UserConverter;
import com.skullzbones.vortexconnect.interfaces.MessageDAO;
import com.skullzbones.vortexconnect.interfaces.UserDAO;
import com.skullzbones.vortexconnect.model.Message;
import com.skullzbones.vortexconnect.model.User;

@Database(entities = {Message.class, User.class}, version = 2)
@TypeConverters({DateConverter.class, UserConverter.class, ArrayConverter.class})
public abstract class DatabaseMain extends RoomDatabase {
    public abstract MessageDAO messageDAO();
    public abstract UserDAO userDAO();
}