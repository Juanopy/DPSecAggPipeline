/**
 * Highly inspired by
 * https://github.com/mh-/corona-warn-companion-android
 **/

package com.example.database_test.gmsreadout;

import static com.example.database_test.gmsreadout.Sudo.sudo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.IOException;

public class GMSContactDB extends ContactDB {
    private static final String TAG = "GMSContactDB";


    @SuppressLint("SdCardPath")
    private static final String gmsPathStr = "/data/data/com.google.android.gms";
    private static final String dbName = "app_contact-tracing-contact-record-db";
    private static final String dbNameModifier = "_old";
    private static final String dbNameModified = dbName + dbNameModifier;

    private final Context context;

    public GMSContactDB(Context context) {
        super(new File(getCacheDir(context).getPath() + "/" + dbNameModified));
        this.context = context;
    }

    @Override
    public void open() throws IOException {
        copyFromGMS();

        super.open();
    }

    public void copyFromGMS() {
        // Copy the GMS LevelDB to local app cache
        Log.d(TAG, "Trying to copy LevelDB");

        System.out.println("External Cache Dir" + getCacheDir(context).getPath());
        String cachePathStr = getCacheDir(context).getPath();

        // First rename the LevelDB directory, then copy it, then rename to the original name
        String result = sudo(
                "rm -rf " + cachePathStr + "/" + dbNameModified,
                "mv " + gmsPathStr + "/" + dbName + " " + gmsPathStr + "/" + dbNameModified,
                "cp -R " + gmsPathStr + "/" + dbNameModified + " " + cachePathStr + "/",
                "mv " + gmsPathStr + "/" + dbNameModified + " " + gmsPathStr + "/" + dbName,
                "chmod -R 707 " + cachePathStr + "/" + dbNameModified,
                "ls -la " + cachePathStr + "/" + dbNameModified
        );
        Log.d(TAG, "Result from trying to copy LevelDB: " + result);
        if (result.length() < 10) {
            Log.e(TAG, "ERROR: Super User rights not granted!");
        }
    }

}
