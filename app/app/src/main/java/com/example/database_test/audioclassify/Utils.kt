package com.example.database_test.audioclassify

import android.os.CountDownTimer

object Utils {

    @JvmStatic
    fun executeLater(duration: Long, task: () -> Unit) {

        val timer = object : CountDownTimer(duration, 1) {

            override fun onTick(millisUntilFinished: Long) {}

            override fun onFinish() {
                task()
            }
        }
        timer.start()
    }

    @JvmStatic
    fun getXMaximums(list: FloatArray, x: Int): IntArray {

        fun getMaxIndex(list: FloatArray): Int {
            return list.indices.maxByOrNull { list[it] }!!
        }

        val indices = IntArray(x)
        for (i in indices.indices) {
            indices[i] = getMaxIndex(list)
            list[indices[i]] = -1.0f
        }

        return indices

    }

}
