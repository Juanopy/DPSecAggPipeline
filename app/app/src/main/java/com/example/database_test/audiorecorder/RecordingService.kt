package com.example.database_test.audiorecorder

/**
 * Highly inspired by
 * https://github.com/urbandroid-team/android-audio-recorder-foreground-service/tree/master/app
 **/

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.database_test.MainActivity
import com.example.database_test.MainActivity.*
import java.io.File
import java.io.FileOutputStream
import java.lang.System.*
import java.nio.ByteBuffer
import java.util.*

/**
 * Sample that demonstrates how to record a device's microphone using {@link AudioRecord}.
 */
const val SAMPLING_RATE_IN_HZ = 44100
const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT

/**
 * Factor by that the minimum buffer size is multiplied. The bigger the factor is the less
 * likely it is that samples will be dropped, but more memory will be used. The minimum buffer
 * size is determined by [AudioRecord.getMinBufferSize] and depends on the
 * recording settings.
 */
const val BUFFER_SIZE_FACTOR = 2


/**
 * Size of the buffer where the audio data is stored by Android
 */
private val BUFFER_SIZE = AudioRecord.getMinBufferSize(
    SAMPLING_RATE_IN_HZ,
    CHANNEL_CONFIG, AUDIO_FORMAT
) * BUFFER_SIZE_FACTOR


class RecordingService : Service() {
    var recordingThread: Thread? = null
    var recorder: AudioRecord? = null

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

        Log.i(TAG, "Foreground service created")

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channelName = "Foreground"
        val importance = NotificationManager.IMPORTANCE_LOW
        val notificationChannel =
            NotificationChannel(NOTIFICATION_CHANNEL_FOREGROUND, channelName, importance)
        notificationChannel.setShowBadge(false)
        notificationManager.createNotificationChannel(notificationChannel)

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val i = Intent(this, MainActivity::class.java)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        val pi = PendingIntent.getActivity(
            this,
            4242,
            i,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE
        )

        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_FOREGROUND)
            .setChannelId(NOTIFICATION_CHANNEL_FOREGROUND)
            .setContentIntent(pi)
            .setShowWhen(false)
            .setContentText("Running")

        startForeground(5632, notificationBuilder.build())

        startRecordinginService()

        return START_NOT_STICKY

    }


    @SuppressLint("MissingPermission")
    fun startRecordinginService() {
        println("Recorder start")


        recorder = AudioRecord(
            MediaRecorder.AudioSource.DEFAULT, SAMPLING_RATE_IN_HZ,
            CHANNEL_CONFIG, AUDIO_FORMAT, BUFFER_SIZE
        )

        if (recorder!!.state != AudioRecord.STATE_INITIALIZED) {
            println("recorder state" + recorder!!.state)
            Log.e(TAG, "Invalid state: " + recorder!!.state)
            recorder!!.release()
            recorder = null

            return
        }
        endRecord.set(false)


        println("recorder state" + recorder!!.state)
        recorder!!.startRecording()
        recordingInProgress.set(true)
        recordingThread = Thread(RecordingRunnable(recorder!!), "Recording Thread")
        recordingThread!!.start()

    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "Service onDestroy()")
        recordingInProgress.set(false)
        endRecord.set(true)
        recorder!!.stop()
        recorder!!.release()
        recorder = null
        recordingThread = null
    }

    companion object {
        fun startRecService(context: Context) {
            context.startForegroundService(Intent(context, RecordingService::class.java))
        }
    }
}

private class RecordingRunnable(val recorder: AudioRecord) : Runnable {

    override fun run() {
        var file: File? = null
        val file1 = File(getInstance().cacheDir.path + "/recording1.pcm")
        val file2 = File(getInstance().cacheDir.path + "/recording2.pcm")
        if (!file2.exists()) {
            file2.createNewFile()
        }
        if (!file1.exists()) {
            file1.createNewFile()
        }

        recordStartTime = currentTimeMillis() / 1000
        val buffer =
            ByteBuffer.allocateDirect(BUFFER_SIZE)
        while (!endRecord.get()) {
            file = if (recordslot1.get()) {
                file1
            } else {
                file2
            }

            try {
                FileOutputStream(file).use { outStream ->
                    while (recordingInProgress.get()) {
                        val result: Int = recorder.read(buffer, BUFFER_SIZE)
                        if (result < 0) {
                            throw RuntimeException(
                                "Reading of audio buffer failed: " +
                                        getBufferReadFailureReason(result)
                            )
                        }
                        if ((currentTimeMillis() / 1000) - recordStartTime > recordinglength) {
                            recordStartTime = currentTimeMillis() / 1000
                            recordingInProgress.set(false)
                            break
                        }
                        if (storeaudio.get()) {
                            outStream.write(
                                buffer.array(),
                                0,
                                BUFFER_SIZE
                            )
                        }
                        buffer.clear()
                    }
                }
            } catch (ex: Exception) {
                out.println(ex)
            }
            out.println("Audio Saved" + file)
            recordslot1.set(!recordslot1.get())
            recordingInProgress.set(true)
        }
    }

    private fun getBufferReadFailureReason(errorCode: Int): String {
        return when (errorCode) {
            AudioRecord.ERROR_INVALID_OPERATION -> "ERROR_INVALID_OPERATION"
            AudioRecord.ERROR_BAD_VALUE -> "ERROR_BAD_VALUE"
            AudioRecord.ERROR_DEAD_OBJECT -> "ERROR_DEAD_OBJECT"
            AudioRecord.ERROR -> "ERROR"
            else -> "Unknown ($errorCode)"
        }

    }
}
