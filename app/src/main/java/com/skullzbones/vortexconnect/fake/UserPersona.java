package com.skullzbones.vortexconnect.fake;

import com.koushikdutta.ion.builder.Builders.Any.U;
import com.skullzbones.vortexconnect.MainActivity;
import com.skullzbones.vortexconnect.model.Dialog;
import com.skullzbones.vortexconnect.model.Message;
import com.skullzbones.vortexconnect.model.User;
import java.util.ArrayList;

public class UserPersona {
    public static User getInvalidUserPersona(){
        return new User("0", "Invalid User", "https://cdn.pixabay.com/photo/2017/02/12/21/29/false-2061132_960_720.png", false);
    }

  public static User getAdminUserPersona(String uid) {
      return new User(uid, "Admin", "https://picsum.photos/seed/"+uid+"/400", false);
  }

  public static Dialog getAdminMessage() {
      ArrayList<User> e = new ArrayList<>();
      e.add(MainActivity.myAccount.getValue());
      e.add(getAdminUserPersona("rFxp2qfBnqdeKf4dyyV0fwTfJAq1"));
      Message lsm = new Message("abcdefgh", "rFxp2qfBnqdeKf4dyyV0fwTfJAq1", "Send your feedback here!");
      Dialog d = new Dialog("rFxp2qfBnqdeKf4dyyV0fwTfJAq1", "Admin", "https://picsum.photos/400",
          e, lsm, 0);
      return d;
  }
}
