package com.skullzbones.vortexconnect.Utils;

import java.util.ArrayList;
import java.util.Random;

public class RandomPersona {

    static ArrayList<String> avatars = new ArrayList<String>() {
        {
            add("https://i.picsum.photos/id/916/400/400.jpg?hmac=Sbzcc2tBjFEutsmxtSCKFs5IhfDCpdYpyQ1-npRyHpY");
            add("https://i.picsum.photos/id/10/400/400.jpg?hmac=PIwVt0zDIDLjFhsKCUPaltt0400fYkh4vldbdFvqEz4");
            add("https://i.picsum.photos/id/29/400/400.jpg?hmac=JmRg5v6v6WI2S_QaQoVHTErlKvqRoDGMzzPtVN3EWc4");
            add("https://i.picsum.photos/id/598/400/400.jpg?hmac=MgKEjHwtxwfn3qPJsDjwczd_y_QHzzKBU41H31UTpbw");
            add("https://i.picsum.photos/id/94/400/400.jpg?hmac=cm9HLDaIBY1qXHxqa9kIzT1qWd-YjUdsEZxoL6TzKNs");
        }
    };

    public static String getRandImageAvatar(){
        Random randomizer = new Random();
        return avatars.get(randomizer.nextInt(avatars.size()));
    }
}
