package com.skullzbones.vortexconnect.converters;

import androidx.room.TypeConverter;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.skullzbones.vortexconnect.model.User;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;

public class UserConverter {
    @TypeConverter
    public static User fromEncoded(String value) {
        Gson gson = new Gson();
        return gson.fromJson(value, User.class);
    }
    @TypeConverter
    public static String userToEncoded(User user) {
        Gson gson = new Gson();
        String json = gson.toJson(user);
        return json;
    }
}