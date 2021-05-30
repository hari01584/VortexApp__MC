package com.skullzbones.vortexconnect.fake;

import com.koushikdutta.ion.builder.Builders.Any.U;
import com.skullzbones.vortexconnect.model.User;

public class UserPersona {
    public static User getInvalidUserPersona(){
        return new User("0", "Invalid User", "https://cdn.pixabay.com/photo/2017/02/12/21/29/false-2061132_960_720.png", false);
    }

  public static User getAdminUserPersona(String uid) {
      return new User(uid, "Admin", "https://picsum.photos/seed/"+uid+"/400", false);
  }
}
