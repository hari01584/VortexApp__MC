package com.skullzbones.vortexconnect.Utils;

import java.lang.Math;
import java.util.UUID;

public class RandomGens {
    public static String generateString() {
        String uuid = UUID.randomUUID().toString();
        return "uuid_" + uuid;
    }

}
