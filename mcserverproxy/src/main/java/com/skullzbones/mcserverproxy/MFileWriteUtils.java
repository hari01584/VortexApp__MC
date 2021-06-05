package com.skullzbones.mcserverproxy;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import com.skullzbones.mcserverproxy.Exceptions.ServerNotSetException;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MFileWriteUtils {

  private static final String TAG = "r/MFileWriteUtils";

  public static void writeName(String name) {
    String base = Environment.getExternalStorageDirectory().getAbsolutePath();

    File mFolder = new File(base + "/games/com.mojang/minecraftpe");
    if (!mFolder.exists()) {
      if (!mFolder.mkdirs()) {
        return;
      }
    }
    File myFile = new File(mFolder, "options.txt");
    try {
      if (!myFile.exists()) {
        if (!myFile.createNewFile()) {
          return;
        }
      }

      StringBuilder text = new StringBuilder();
      BufferedReader br = new BufferedReader(new FileReader(myFile));
      String line;
      while ((line = br.readLine()) != null) {
        if (line.contains("mp_username:")) {
          line = "mp_username:" + name;
        }
        text.append(line);
        text.append('\n');
      }
      br.close();
      String result = text.toString();
      if(!result.contains("mp_username")){
        result+="mp_username:" + name + '\n';
      }

      FileOutputStream fstream = null;
      fstream = new FileOutputStream(myFile);
      fstream.write(result.getBytes());
      fstream.close();
    } catch (Exception e) {
      Log.i(TAG,
          "Perhaps, Some error with connector MFileWriteUtils, Basically means problem with reading/writing"
              + "to minecraft external storage directory!! It must be mainly due to scoped storage introduced in android 11"
              + "so i can't do much either, Hopefully someone port it to use scoped storage as well..");
      e.printStackTrace();
    }
  }

  // Dunno if these be used but here as a reference?
  private static boolean isExternalStorageReadOnly() {
    String extStorageState = Environment.getExternalStorageDirectory().getAbsolutePath();
    if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(extStorageState)) {
      return true;
    }
    return false;
  }

  // Dunno if these be used but here as a reference?
  private static boolean isExternalStorageAvailable() {
    String extStorageState = Environment.getExternalStorageDirectory().getAbsolutePath();
    if (Environment.MEDIA_MOUNTED.equals(extStorageState)) {
      return true;
    }
    return false;
  }

  public static void pasteSkin(Context context, String gameSkinUri) {
    String base = Environment.getExternalStorageDirectory().getAbsolutePath();

    File mFolder = new File(base + "/games/com.mojang/minecraftpe");
    if (!mFolder.exists()) {
      if (!mFolder.mkdirs()) {
        return;
      }
    }
    File myFile = new File(mFolder, "custom.png");
    try {
      if (!myFile.exists()) {
        if (!myFile.createNewFile()) {
          return;
        }
      }
      Uri u = Uri.parse(gameSkinUri);
      InputStream iStream = context.getContentResolver().openInputStream(u);
      byte[] inputData = getBytes(iStream);

      FileOutputStream fstream = null;
      fstream = new FileOutputStream(myFile);
      fstream.write(inputData);
      fstream.close();
    } catch (Exception e) {
      Log.i(TAG, "Error with imager as well, Are you running scoped storage apis?");
      e.printStackTrace();
    }
  }

  public static byte[] getBytes(InputStream inputStream) throws IOException {
    ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
    int bufferSize = 1024;
    byte[] buffer = new byte[bufferSize];

    int len = 0;
    while ((len = inputStream.read(buffer)) != -1) {
      byteBuffer.write(buffer, 0, len);
    }
    return byteBuffer.toByteArray();
  }

  public static void copyFile(String inputPath, String outputPath) {

    InputStream in = null;
    OutputStream out = null;
    try {
      in = new FileInputStream(inputPath);
      out = new FileOutputStream(outputPath);

      byte[] buffer = new byte[1024];
      int read;
      while ((read = in.read(buffer)) != -1) {
        out.write(buffer, 0, read);
      }
      in.close();
      in = null;

      // write the output file (You have now copied the file)
      out.flush();
      out.close();
      out = null;

    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
