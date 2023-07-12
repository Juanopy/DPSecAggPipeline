package com.example.database_test.motiondetection;

/**
 * Highly inspired by
 * https://github.com/android/location-samples/tree/master/ActivityRecognition/app/src/main/java/com/google/android/gms/location/sample/activityrecognition
 **/

import static com.example.database_test.MainActivity.getInstance;
import static com.example.database_test.MainActivity.lastmoitiondetection;
import static com.example.database_test.MainActivity.motiondetectioninterval;

import android.app.IntentService;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import java.io.IOException;
import java.util.ArrayList;

public class DetectedActivitiesIntentService extends IntentService {
    protected static final String TAG = "DetectedActivitiesIS";
    int motionfound;
    int conf;
    boolean foundStillorTilt;

    /**
     * This constructor is required, and calls the super IntentService(String)
     * constructor with the name for a worker thread.
     */
    public DetectedActivitiesIntentService() {
        // Use the TAG to name the worker thread.
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    /**
     * Handles incoming intents.
     *
     * @param intent The Intent is provided (inside a PendingIntent) when requestActivityUpdates()
     *               is called.
     */
    @SuppressWarnings("unchecked")
    @Override
    protected void onHandleIntent(Intent intent) {
        foundStillorTilt = false;
        motionfound = 4;
        conf = 0;
        ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);

        // Get the list of the probable activities associated with the current state of the
        // device. Each activity is associated with a confidence level, which is an int between
        // 0 and 100.
        ArrayList<DetectedActivity> detectedActivities = (ArrayList) result.getProbableActivities();

        PreferenceManager.getDefaultSharedPreferences(this)
                .edit()
                .putString(Constants.KEY_DETECTED_ACTIVITIES,
                        MDUtils.detectedActivitiesToJson(detectedActivities))
                .apply();


        // Log each activity and detect not relevant movement like Still or Tilt
        Log.i(TAG, "activities detected");
        for (DetectedActivity da : detectedActivities) {
            if (da.getConfidence() >= 75) {
                motionfound = da.getType();
                conf = da.getConfidence();
                if (da.getType() == 3 || da.getType() == 5) {
                    foundStillorTilt = true;
                    break;
                }
            }
            Log.i(TAG, MDUtils.getActivityString(
                    getApplicationContext(),
                    da.getType()) + " " + da.getConfidence() + "%"
            );
        }

        //Wait at least motiondetectioninterval Seconds before the next Database scan
        if (((System.currentTimeMillis() / 1000) - lastmoitiondetection) > motiondetectioninterval) {
            System.out.println("Last check was Seconds ago: " + ((System.currentTimeMillis() / 1000) - lastmoitiondetection));
            lastmoitiondetection = (System.currentTimeMillis() / 1000);
            if (foundStillorTilt) {
                System.out.println("foundStillorTilt");
            } else {
                try {
                    getInstance().getContactDbOnDisk().writeActivityData(motionfound);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }
}
