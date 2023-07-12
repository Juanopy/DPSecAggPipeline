package com.example.database_test.audiorecorder

/**
 * Highly inspired by
 * https://github.com/urbandroid-team/android-audio-recorder-foreground-service/tree/master/app
 * and https://github.com/MChehab94/Recording-Playing-Audio-Kotlin/blob/master/app/src/main/java/mchehab/com/recordingplayingaudio/MainActivity.kt
 **/

import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.database_test.MainActivity
import java.util.*


const val TAG: String = "RecordForeground"
const val NOTIFICATION_CHANNEL_FOREGROUND = "foreground"

class StartService : AppCompatActivity() {

    companion object {
        @JvmStatic
        fun startRec() {
            Log.i(TAG, "Foreground service will start at ${Date(System.currentTimeMillis())}")
            //Set Record Slot to 1
            MainActivity.recordslot1.set(true)
            RecordingService.startRecService(MainActivity.getInstance())
        }
    }
}
