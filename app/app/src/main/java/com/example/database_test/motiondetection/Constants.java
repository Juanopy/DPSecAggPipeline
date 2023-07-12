package com.example.database_test.motiondetection;

/**
 * Highly inspired by
 * https://github.com/android/location-samples/tree/master/ActivityRecognition/app/src/main/java/com/google/android/gms/location/sample/activityrecognition
 **/

public class Constants {

    public static final String PACKAGE_NAME =
            "com.google.android.gms.location.activityrecognition";

    public static final String KEY_ACTIVITY_UPDATES_REQUESTED = PACKAGE_NAME +
            ".ACTIVITY_UPDATES_REQUESTED";

    public static final String KEY_DETECTED_ACTIVITIES = PACKAGE_NAME + ".DETECTED_ACTIVITIES";

    /**
     * The desired time between activity detections. Larger values result in fewer activity
     * detections while improving battery life. A value of 0 results in activity detections at the
     * fastest possible rate.
     */
    public static final long DETECTION_INTERVAL_IN_MILLISECONDS = 30 * 1000; // 30 seconds

}
