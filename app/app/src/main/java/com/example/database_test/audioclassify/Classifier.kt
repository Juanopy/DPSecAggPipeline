package com.example.database_test.audioclassify

import android.content.res.AssetFileDescriptor
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel


class Classifier(var modelFileDescriptor: AssetFileDescriptor) {

    /**
     *
     */
    private val interpreter = Interpreter(loadModelFromFile(), Interpreter.Options())


    init {
        interpreter.resizeInput(0, intArrayOf(1, 1, 128, 431, 1))
        interpreter.allocateTensors()
    }


    /**
     *
     */
    fun classify(x: Array<Array<Array<Array<FloatArray>>>>): Array<FloatArray> {

        val preds = Array(1) { FloatArray(10) }

        /**
         * ERROR: E/libc: Access denied finding property "ro.hardware.chipname"
         * Tries to access privileged property of the underlying linux system.
         * On a physical device this access usually gets denied.
         */
        interpreter.run(x, preds)

        return preds
    }

    /**
     *
     */
    private fun loadModelFromFile(): MappedByteBuffer {

        val inputStream = FileInputStream(modelFileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = modelFileDescriptor.startOffset
        val declaredLength = modelFileDescriptor.declaredLength

        val res = fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)

        inputStream.close()

        return res
    }

}
