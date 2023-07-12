package com.example.database_test.audiorecorder

/**
 * Highly inspired by
 * https://github.com/urbandroid-team/android-audio-recorder-foreground-service/tree/master/app
 **/


import android.content.BroadcastReceiver
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.util.Log

class RecordingReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.i(TAG, "Broadcast received")
        context?.apply {
            RecordingService.startRecService(this)
        }
    }
}