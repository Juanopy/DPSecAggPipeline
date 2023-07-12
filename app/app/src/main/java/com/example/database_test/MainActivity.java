package com.example.database_test;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;

import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.example.database_test.audioclassify.AcousticSceneClassification;
import com.example.database_test.audioclassify.Utils;
import com.example.database_test.audiorecorder.StartService;
import com.example.database_test.clustering.DatabaseReader;
import com.example.database_test.clustering.KMeans;
import com.example.database_test.databinding.ActivityMainBinding;
import com.example.database_test.gmsreadout.ActivityContactDB;
import com.example.database_test.motiondetection.Constants;
import com.example.database_test.motiondetection.DetectedActivitiesIntentService;
import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.polidea.rxandroidble2.LogConstants;
import com.polidea.rxandroidble2.LogOptions;
import com.polidea.rxandroidble2.RxBleClient;
import com.polidea.rxandroidble2.exceptions.BleScanException;
import com.polidea.rxandroidble2.scan.ScanFilter;
import com.polidea.rxandroidble2.scan.ScanSettings;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

public class MainActivity extends AppCompatActivity {
    //Audio Recorder
    public static final int recordinglength = 10;
    //Motion Detection
    public static int motiondetectioninterval = 45;
    public static long lastmoitiondetection = (System.currentTimeMillis() / 1000);
    public static AtomicBoolean recordingInProgress = new AtomicBoolean();
    public static AtomicBoolean recordslot1 = new AtomicBoolean();
    public static AtomicBoolean storeaudio = new AtomicBoolean();
    public static AtomicBoolean endRecord = new AtomicBoolean();
    public static long recordStartTime;
    //ASC
    public static String lastlabel;
    public static int lastlabelprob = 0;
    private static MainActivity Instance;
    //GUI
    private static long Gifduration = 0;
    int motionfound;
    private Boolean permissionstate = false;
    //DB
    private ActivityContactDB contactDbOnDisk;
    private ActivityRecognitionClient mActivityRecognitionClient;
    //Vectors
    public static String vectors;

    public static RxBleClient rxBleClient;
    public static Disposable scanDisposable;
    public static ScanResultsAdapter resultsAdapter;
    public static int devices = 0;





    public static List<String> vectorlist = new ArrayList<String>();
    public static RequestQueue requestQueue;

    /**
     * Gets Instance of MainActivity
     */
    public static MainActivity getInstance() {
        return Instance;
    }

    public static void snackbar_show(String string) {
        View v = Instance.findViewById(android.R.id.content);
        Snackbar.make(v, string, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }

    /**
     * Return contactDbOnDisk
     */

    public ActivityContactDB getContactDbOnDisk() {
        return contactDbOnDisk;
    }

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        Instance = this;

        /**
         * motionfound is set to Value that DB Reader understand as init motion
         */
        motionfound = 99;

        /**
         * Permission Check
         */
        checkPermissionsGranted();
        while (permissionstate) {
            if (checkPermissionsGranted()) {
                System.out.println("break");
                break;
            }
        }

        requestQueue = Volley.newRequestQueue(this);
        requestQueue.start();

        /**
         * GUI init
         */
        setContentView(R.layout.fragment_first);

        GifImageView gifImageView = findViewById(R.id.connect);
        try {
            GifDrawable gifDrawable = new GifDrawable(getResources(), R.drawable.timgif1);
            gifImageView.setImageDrawable(gifDrawable);
            Gifduration = gifDrawable.getDuration();
        } catch (Resources.NotFoundException | IOException e) {
            e.printStackTrace();
        }



        /**
         * Start Audio Recorder Service
         */
        storeaudio.set(false);
        StartService.startRec();

        /**
         * Init DB if it is not set and open
         */
        if (contactDbOnDisk == null) {
            contactDbOnDisk = new ActivityContactDB(this);
        }
        try {
            contactDbOnDisk.open();
        } catch (IOException e) {
            e.printStackTrace();
        }


        /**
         * Fetch missing Data in new DB
         */
        try {
            contactDbOnDisk.fetchMissingData();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        /**
         * Init Activity Regonition
         */
        mActivityRecognitionClient = new ActivityRecognitionClient(this);
        MotionDetectionstart();


        /**
         * Bluetooth Scan
         */

        rxBleClient = RxBleClient.create(this);
        RxBleClient.updateLogOptions(new LogOptions.Builder()
                .setLogLevel(LogConstants.INFO)
                .setMacAddressLogSetting(LogConstants.MAC_ADDRESS_FULL)
                .setUuidsLogSetting(LogConstants.UUIDS_FULL)
                .setShouldLogAttributeValues(true)
                .build()
        );
        configureResultList();



        final Button button = findViewById(R.id.buttonConnect);
        MainActivity golbalcontext = this;
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Snackbar.make(v, "Sending request to Server", 9000)
                        .setAction("Action", null).show();
                try {
                    contactDbOnDisk.exportVectors();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // call the method readDatabase
                //DatabaseReader.readDatabase();

                try {
                    AcousticSceneClassification TestAccousticClassification = new AcousticSceneClassification();
                    TestAccousticClassification.start();
                    KMeans.Kmeansmain(golbalcontext);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        //Change Gif after first Loop
        Utils.executeLater(Gifduration, () -> {
            changeGif(R.drawable.timgif2);
            return null;
        });




    }

    public static void scanBleDevices() {
        scanDisposable = rxBleClient.scanBleDevices(
                        new ScanSettings.Builder()
                                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                                .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                                .build(),
                        new ScanFilter.Builder()
//                            .setDeviceAddress("B4:99:4C:34:DC:8B")
                                // add custom filters if needed
                                .build()
                )
                .observeOn(AndroidSchedulers.mainThread())
                .doFinally(MainActivity.getInstance()::dispose)
                .subscribe(resultsAdapter::addScanResult, MainActivity.getInstance()::onScanFailure);
    }

    private void onScanFailure(Throwable throwable) {
        if (throwable instanceof BleScanException) {
            System.out.println("bad");
        } else {
            Log.w("ScanActivity", "Scan failed", throwable);
        }
    }


    private void configureResultList() {
        resultsAdapter = new ScanResultsAdapter();

    }
    private void dispose() {
        scanDisposable = null;
        resultsAdapter.clearScanResults();
    }
    /**
     * @param gif in Drawable for R.id.connect
     */
    protected void changeGif(int gif) {
        GifImageView gifImageView = findViewById(R.id.connect);
        try {
            GifDrawable gifDrawable2 = new GifDrawable(getResources(), gif);
            gifImageView.setImageDrawable(gifDrawable2);
        } catch (Resources.NotFoundException | IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Permission check and request or allow execution
     */
    private Boolean checkPermissionsGranted() {
        if (checkPermission()) {
            System.out.println("All Permissions granted");
            permissionstate = true;
            return true;
        } else {
            requestmissingPermissions();
            return false;
        }
    }

    /**
     * Request Missing Permisiions
     */
    private void requestmissingPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SCHEDULE_EXACT_ALARM, Manifest.permission.FOREGROUND_SERVICE, Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACTIVITY_RECOGNITION}, 101);
    }

    /**
     * @return if Permissions are granted
     */
    private boolean checkPermission() {
        int permission1 = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
        int permission2 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int permission3 = ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION);
        int permission4 = ContextCompat.checkSelfPermission(this, Manifest.permission.FOREGROUND_SERVICE);
        int permission5 = ContextCompat.checkSelfPermission(this, Manifest.permission.SCHEDULE_EXACT_ALARM);

        return permission1 == PackageManager.PERMISSION_GRANTED && permission2 == PackageManager.PERMISSION_GRANTED && permission3 == PackageManager.PERMISSION_GRANTED && permission4 == PackageManager.PERMISSION_GRANTED && permission5 == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Start Motion Detection as a task and add on Success and Failure Listener
     */
    public void MotionDetectionstart() {
        Task<Void> task = mActivityRecognitionClient.requestActivityUpdates(
                Constants.DETECTION_INTERVAL_IN_MILLISECONDS,
                getActivityDetectionPendingIntent());

        task.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void result) {
                setUpdatesRequestedState(true);
            }
        });

        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                System.out.println("activity_updates_not_enabled");
            }
        });
    }

    /**
     * Gets a PendingIntent to be sent for each activity detection.
     */
    private PendingIntent getActivityDetectionPendingIntent() {
        Intent intent = new Intent(this, DetectedActivitiesIntentService.class);
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
    }


    /**
     * Sets the boolean in SharedPreferences that tracks whether we are requesting activity
     * updates.
     */
    private void setUpdatesRequestedState(boolean requesting) {
        PreferenceManager.getDefaultSharedPreferences(this)
                .edit()
                .putBoolean(Constants.KEY_ACTIVITY_UPDATES_REQUESTED, requesting)
                .apply();
    }

    protected void onDestroy() {
        super.onDestroy();
        endRecord.set(true);

        File file1 = new File(getCacheDir().getPath() + "/recording1.pcm");
        File file2 = new File(getCacheDir().getPath() + "/recording2.pcm");
        File file3 = new File(getCacheDir().getPath() + "/recording.wav");
        if (file1.exists()) {
            file1.delete();
        }
        if (file2.exists()) {
            file2.delete();
        }
        if (file3.exists()) {
            file3.delete();
        }
    }
}