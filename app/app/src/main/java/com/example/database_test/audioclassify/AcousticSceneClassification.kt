package com.example.database_test.audioclassify


import android.util.Base64
import com.example.database_test.MainActivity.*
import com.example.database_test.audioclassify.converter.rawToWave
import java.io.File
import kotlin.concurrent.thread


class AcousticSceneClassification {


    /********************/
    /**   CLASSIFIER   **/
    /********************/


    private val LABELS: Array<String> = arrayOf(
        "airport", "bus", "metro", "metro_station", "park",
        "public_square", "shopping_mall", "street_pedestrian", "street_traffic", "tram"
    )


    /********************/
    /**   AUDIO DATA   **/
    /********************/

    /**
     *
     */
    private val SR = 44100

    /**
     *
     */
    private val NUM_FREQ_BIN = 128

    /**
     *
     */
    private val NUM_FFT = 2048

    /**
     *
     */
    private val HOP_LENGTH = (NUM_FFT / 2).toInt()

    /**
     *
     */
    private val appContext = getInstance()


    /**
     * Specifies the path of the audio file
     */
    private var output =
        getInstance().cacheDir.path + "/recording.wav"


    private var input: File? = null


    val file1 = File(getInstance().cacheDir.path + "/recording1.pcm")
    val file2 = File(getInstance().cacheDir.path + "/recording2.pcm")

    /**
     * Custom Audio Classifier
     */
    private var classifier = Classifier(appContext.assets.openFd("model.tflite"))

    /********************/
    /**   NATIVE CODE  **/
    /********************/

    external fun readWAVFile(path: String): FloatArray

    companion object {
        // Used to load the 'native-lib' library on application startup.
        init {
            System.loadLibrary("sndfile")
        }
    }


    fun start() {

        storeaudio.set(true)
        Utils.executeLater((recordinglength * 2 * 1000).toLong(), { stopRecording() })
    }


    private fun stopRecording() {

        input = if (!recordslot1.get()) {
            file1
        } else {
            file2
        }
        rawToWave(input, File(output))
        println("rawToWave")
        storeaudio.set(false)

        startClassification()
    }


    private fun startClassification() {

        var preds: FloatArray? = null
        System.out.println("Classification")


        val processThread = thread(isDaemon = true) {
            val input = readWAVFile(output)
            val features =
                Preprocessing.process(input, SR, NUM_FFT, HOP_LENGTH, NUM_FREQ_BIN, 0.0, SR / 2.0)
            val x = Array(1) {
                Array(1) {
                    features.map { ls ->
                        ls.map { e -> FloatArray(1) { e.toFloat() } }.toTypedArray()
                    }.toTypedArray()
                }
            }
            preds = classifier.classify(x).get(0)
        }

        processThread.join()
        endClassification(preds!!)


    }


    private fun endClassification(preds: FloatArray) {

        System.out.println("End Class")

        //get best Labels
        val labelIndices = Utils.getXMaximums(preds.clone(), 3)
        val probs = labelIndices.map { i -> (preds[i] * 100) }
        val labels = labelIndices.map { i -> LABELS[i] }

        //Label and Prob of Label which is saved in DB
        lastlabel = labels[0]
        lastlabelprob = probs[0].toDouble().toInt()


        //Generate 10 random float values
        val randomvectors = (0..9).map { (0..100).random().toFloat() }
        //print randomvectors
        println(randomvectors)
        //convert randomvectors to string
        val randomvectorsstring = randomvectors.joinToString(separator = ",")
        //convert randomvectorssstring to base 64
        val randomvectorsbase64 = Base64.encodeToString(randomvectorsstring.toByteArray(), Base64.DEFAULT)

        vectors = randomvectorsbase64

        //print vectors
        println("Vectors in Base 64"+vectors)

        //convert randomvectorsbase64 to string
        val randomvectorsstring2 = String(Base64.decode(randomvectorsbase64, Base64.DEFAULT))
        //convert randomvectorsstring2 to float array
        val randomvectorsfloat = randomvectorsstring2.split(",").map { it.toFloat() }.toFloatArray()
        //print randomvectorsfloat
        println("Vectors in Float Array"+randomvectorsfloat.contentToString())

        val file3 = File(getInstance().cacheDir.path + "/recording.wav")
        if (file3.exists()) {
            file3.delete()
        }

        println("label: " + lastlabel + "prob: " + lastlabelprob)

    }


}


