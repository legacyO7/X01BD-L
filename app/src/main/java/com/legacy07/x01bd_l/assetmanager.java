package com.legacy07.x01bd_l;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class assetmanager {
    String out = Environment.getExternalStorageDirectory().getAbsolutePath() + "/X01BD-StockRom-Patch/META-INF/com/google/android/";
    File outFile;
    String filename="";

    public void copyAssets(Context context, int action) {


        if (action == 1) {
            filename = "updater-script";
            outFile = new File(out, "updater-script");
        }
        if (action == 2){
            outFile = new File(out, "updater-script_fw");
            filename = "updater-script_fw";
        }



        AssetManager assetManager = context.getAssets();
        String[] files = null;
        try {
            files = assetManager.list("");
        } catch (IOException e) {
            Log.e("tag", "Failed to get asset file list.", e);
        }
        InputStream in = null;
        OutputStream out = null;
        try {

            in = assetManager.open(filename);

            String outDir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/X01BD-StockRom-Patch/META-INF/com/google/android/";

            File outFile = new File(outDir, filename);

            out = new FileOutputStream(outFile);
            copyFile(in, out);
            in.close();
            in = null;
            out.flush();
            out.close();
            out = null;
        } catch (IOException e) {
            Log.e("tag", "Failed to copy asset file: " + filename, e);
        }
    }

    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }


}
