package com.legacy07.x01bd_l;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class MainActivity extends AppCompatActivity {

    Button findfile, extr, nextp;
    TextView path, banner;
    ProgressDialog progressDialog;
    String filename, fpath = "";
    boolean success = false;
    private static final int FILE_SELECT_CODE = 0;
    assetmanager am = new assetmanager();
    Handler handler = new Handler();
    AlertDialog.Builder b;
    int action = 0;
    Animation aniBlink;
    private String backupfpath;
    public static final int PERMISSION_EXTERNAL_STORAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findfile = findViewById(R.id.button);
        path = findViewById(R.id.file);
        extr = findViewById(R.id.buttonext);
        nextp = findViewById(R.id.next);
        b = new AlertDialog.Builder(this, R.style.MyDialogTheme);
        banner = findViewById(R.id.legacy);

        aniBlink = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.blink);
        banner.startAnimation(aniBlink);
        if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Log.v("permission", "Permission is granted");
            //File write logic here
        }
        isStoragePermissionGranted();

        if (!isExternalStorageWritable())
        {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            intent.putExtra("android.content.extra.SHOW_ADVANCED", true);
            startActivityForResult(intent, 1);
        }



        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                PERMISSION_EXTERNAL_STORAGE);

        int ReadExternalStoragePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        Log.e("perm", "checkExternalStoragePermission() done");
        if (ReadExternalStoragePermission == PackageManager.PERMISSION_GRANTED) {
            Log.e("perm", "ReadExternalStoragePermission() granted");

            //read my file in  /sdcard/test.csv

        }
        runcommand("rm -rf storage/emulated/0/X01BD-StockRom-Patch");


        progressDialog = new ProgressDialog(MainActivity.this, R.style.MyDialogTheme);
        progressDialog.setCancelable(false);
        progressDialog.setMax(100);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);


        findfile.setOnClickListener(v -> {


            File folder = new File(Environment.getExternalStorageDirectory() +
                    File.separator + "X01BD-StockRom-Patch");
            if (!folder.exists())
                success = folder.mkdirs();

            if (!success) {
                path.setText("Something went wrong. Please check app permission ;(");
                path.setTextSize(20);
                findfile.setVisibility(View.INVISIBLE);
                extr.setVisibility(View.INVISIBLE);
            } else
                myOpenZipPicker();

            b.setTitle("Select an Action")
                    .setNeutralButton("Extract Firmware", (dialog, which) -> action = 2)
                    .setPositiveButton("Patch StockRom", (dialog, which) -> action = 1)
                    .setCancelable(false)
                    .setIcon(R.drawable.logo)
                    .create()
                    .show();

        });

        extr.setOnClickListener(v -> {

            progressDialog.setMessage("Getting root access");
            progressDialog.setProgress(10);
            progressDialog.show();

            handler.postDelayed(() -> checkroot(), 1500);


        });


        nextp.setOnClickListener(v -> {
            Intent intent1 = new Intent(getApplicationContext(), MainActivity.class);
            intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent1);
        });
    }

    void checkroot() {
        if (runcommand("su -c 'getenforce'").equals("")) {
            //progressDialog.dismiss();
            path.setText("Cant get root access. Process Failed to complete :(");
            findfile.setVisibility(View.INVISIBLE);
            extr.setVisibility(View.INVISIBLE);
        }

        progressDialog.setMessage("Extracting file");
        progressDialog.incrementProgressBy(14);

        handler.postDelayed(() -> extractfile(), 2000);
    }

    public void myOpenZipPicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/zip");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            startActivityForResult(
                    Intent.createChooser(intent, "Select a File to Upload"),
                    FILE_SELECT_CODE);
        } catch (android.content.ActivityNotFoundException ex) {
            // Potentially direct the user to the Market with a Dialog
            Toast.makeText(this, "Please install a File Manager.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    public boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v("perm", "Permission is granted");
                return true;
            } else {

                Log.v("perm", "Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v("perm", "Permission is granted");
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.v("perm", "Permission: " + permissions[0] + "was " + grantResults[0]);
            //resume tasks needing this permission
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode){
            case 0:
            {   if (resultCode == RESULT_OK) {
                    // Get the Uri of the selected file
                    Uri uri = data.getData();
                    assert uri != null;
                    Log.d("path", "File Uri: " + uri.toString());
                    // Get the path
                    fpath = uri.getPath();
                    assert fpath != null;
                    backupfpath = fpath;
                    backupfpath = backupfpath.replace("/document/", "storage/");
                    backupfpath = backupfpath.replace(":", "/");
                    Log.d("real file path", "File Path: " + fpath);
                    if (fpath.contains(":")) {
                        String currentpath = fpath;
                        String[] separated = currentpath.split(":");
                        fpath = separated[1];
                    }

                    if (!fpath.equals("")) {

                        filename = fpath.substring(fpath.lastIndexOf("/") + 1);
                        success = true;
                        path.setVisibility(View.VISIBLE);
                        path.setText(filename);
                        StringBuilder s1 = new StringBuilder(500);
                        s1.append("storage/emulated/0/");
                        s1.append(fpath);
                        fpath = s1.toString();

                        if (!fpath.contains(".zip")) {
                            StringBuilder s = new StringBuilder(500);
                            s.append(fpath);
                            s.append(".zip");
                            fpath = s.toString();
                        }
                        Log.d("file path", "File Path: " + fpath);

                        if (checkforfile(fpath)) {
                            findfile.setVisibility(View.INVISIBLE);
                            extr.setVisibility(View.VISIBLE);
                        } else if (checkforfile(backupfpath)) {

                            ///

                        } else {
                            Log.d("file path", "File Path: " + fpath);
                            Log.d("backupfile path", "File Path: " + backupfpath);
                            path.setText("Something went wrong. Unable to get the file path. Try a different location :(");
                            findfile.setVisibility(View.INVISIBLE);
                            nextp.setVisibility(View.VISIBLE);
                        }


                    } else {
                        path.setText("Something went wrong :(");
                    }

                    // Get the file instance
                    // File file = new File(path);
                    // Initiate the upload
                }
            }break;
            case 1:
            {
                if(resultCode==RESULT_OK){
                    //Take persistant permission. This way you can modify the selected folder ever after a device restart.
                    Uri treeUri = data.getData();
                    int takeFlags = data.getFlags()  & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    getContentResolver().takePersistableUriPermission(treeUri, takeFlags);
                }
            }

        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {

            return true;
        }
        return false;
    }


    private void extractfile() {
        runcommand("unzip -o " + fpath + " -d storage/emulated/0/X01BD-StockRom-Patch");
        progressDialog.incrementProgressBy(20);
        handler.postDelayed(this::patch, 1000);

    }

    private void patch() {
        progressDialog.incrementProgressBy(16);
        progressDialog.setMessage("Patching ( this might take a while )");
        runcommand("rm -f storage/emulated/0/X01BD-StockRom-Patch/META-INF/com/google/android/updater-script");
        am.copyAssets(getApplicationContext(), action);
        if (action == 2) {
            runcommand("mv storage/emulated/0/X01BD-StockRom-Patch/META-INF/com/google/android/updater-script_fw storage/emulated/0/X01BD-StockRom-Patch/META-INF/com/google/android/updater-script");
            runcommand("rm -f storage/emulated/0/X01BD-StockRom-Patch/system.new.dat.br");
            runcommand("rm -f storage/emulated/0/X01BD-StockRom-Patch/vendor.new.dat.br");
            runcommand("rm -f storage/emulated/0/X01BD-StockRom-Patch/system.transfer.list");
            runcommand("rm -f storage/emulated/0/X01BD-StockRom-Patch/vendor.transfer.list");
            runcommand("rm -f storage/emulated/0/X01BD-StockRom-Patch/system.patch.dat");
            runcommand("rm -f storage/emulated/0/X01BD-StockRom-Patch/vendor.patch.dat");
            runcommand("rm -f storage/emulated/0/X01BD-StockRom-Patch/boot.img");
            runcommand("rm -f storage/emulated/0/X01BD-StockRom-Patch/file_contexts.bin");
            runcommand("rm -f storage/emulated/0/X01BD-StockRom-Patch/compatibility.zip");
            runcommand("rm -f storage/emulated/0/X01BD-StockRom-Patch/compatibility_no_nfc.zip");

        }

        handler.postDelayed(this::compress, 1500);

    }

    private void compress() {

        progressDialog.incrementProgressBy(18);
        progressDialog.setMessage("Compressing");
        String comtoexec = "su" + " " + "-c" + " " + '"' + '"' + "cd" + " " + "storage" + "/" + "emulated" + "/" + "0" + "/" + "X01BD-StockRom-Patch" + " " + "&&" + " " + "zip" + " " + "-r" + " " + "-b" + " " + "META-INF" + " " + "zipfile" + " " + "*" + '"' + '"';

        Log.d("tag u r it!", comtoexec);

        runcommand(comtoexec);
        handler.postDelayed(() -> cleanup(), 1000);

    }

    private void cleanup() {
        progressDialog.setProgress(89);
        progressDialog.setMessage("Cleaning up");
        runcommand("rm -rf storage/emulated/0/X01BD-StockRom-Patch/META-INF");
        runcommand("rm -f storage/emulated/0/X01BD-StockRom-Patch/system.new.dat.br");
        runcommand("rm -f storage/emulated/0/X01BD-StockRom-Patch/vendor.new.dat.br");
        runcommand("rm -f storage/emulated/0/X01BD-StockRom-Patch/system.transfer.list");
        runcommand("rm -f storage/emulated/0/X01BD-StockRom-Patch/vendor.transfer.list");
        runcommand("rm -f storage/emulated/0/X01BD-StockRom-Patch/system.patch.dat");
        runcommand("rm -f storage/emulated/0/X01BD-StockRom-Patch/vendor.patch.dat");
        runcommand("rm -rf storage/emulated/0/X01BD-StockRom-Patch/firmware-update");
        runcommand("rm -f storage/emulated/0/X01BD-StockRom-Patch/boot.img");
        runcommand("rm -f storage/emulated/0/X01BD-StockRom-Patch/file_contexts.bin");
        runcommand("rm -f storage/emulated/0/X01BD-StockRom-Patch/compatibility.zip");
        runcommand("rm -f storage/emulated/0/X01BD-StockRom-Patch/compatibility_no_nfc.zip");

        handler.postDelayed(() -> finishing(), 1000);
    }

    private void finishing() {
        progressDialog.setProgress(95);
        progressDialog.setMessage("Finishing up");
        if (action == 1)
            runcommand("mv storage/emulated/0/X01BD-StockRom-Patch/zipfile.zip storage/emulated/0/X01BD-StockRom-Patch/Patched_" + filename);
        if (action == 2)
            runcommand("mv storage/emulated/0/X01BD-StockRom-Patch/zipfile.zip storage/emulated/0/X01BD-StockRom-Patch/Firmware_" + filename);

        progressDialog.setProgress(99);
        progressDialog.setMessage("DONE");
        handler.postDelayed(() -> progressDialog.dismiss(), 2000);

        Uri selectedUri = Uri.parse(Environment.getExternalStorageDirectory() + "/X01BD-StockRom-Patch/");
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(selectedUri, "resource/folder");

        if (checkforfile("storage/emulated/0/X01BD-StockRom-Patch/Patched_" + filename) || checkforfile("storage/emulated/0/X01BD-StockRom-Patch/Firmware_" + filename)) {


            if (intent.resolveActivityInfo(getPackageManager(), 0) != null) {
                startActivity(intent);
            } else {
                extr.setVisibility(View.INVISIBLE);
                handler.postDelayed(() -> path.setText("Default file explorer not set. Find the file from " + '"' + " X01BD-StockRom-Patch " + '"' + " folder in the internal storage"), 2000);
                nextp.setVisibility(View.VISIBLE);

            }
        } else {
            path.setText("Something went wrong, Please try again :(");
            nextp.setVisibility(View.VISIBLE);
            extr.setVisibility(View.INVISIBLE);
        }


    }

    private boolean checkforfile(String s) {

        File file = new File(s);
        if (file.exists())
            return true;
        else
            return false;

    }

    public String runcommand(String command) {
        StringBuilder log = new StringBuilder();
        try {
            Process process = Runtime.getRuntime().exec(command);
            process.waitFor();
            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                log.append(line).append("\n");
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return log.toString();
    }


}
