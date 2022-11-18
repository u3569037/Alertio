package com.example.alertio
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioRecord
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import org.tensorflow.lite.support.audio.TensorAudio
import org.tensorflow.lite.task.audio.classifier.AudioClassifier
import java.io.IOException
import java.util.*



class AudioClassificationActivity : Operating() {
    private lateinit var audioRecord: AudioRecord
    private var modelPath = "yamnet_classification.tflite"
    private lateinit var timerTask: TimerTask

    private lateinit var audioClassifier: AudioClassifier
    private lateinit var tensorAudio: TensorAudio



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        //loading model from assets folder
        try {
            audioClassifier = AudioClassifier.createFromFile(this, modelPath)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        // Create the variable that will store the recording for inference and build the format specification for the recorder.
        tensorAudio = audioClassifier.createInputTensorAudio()

    }

    fun checkMicPresence(): Boolean {
        //check if mic is present
        val pm = packageManager
        val micPresent = pm.hasSystemFeature(PackageManager.FEATURE_MICROPHONE)
        if (!micPresent) {
            Toast.makeText(this, "Your device doesn't have a microphone", Toast.LENGTH_LONG).show()
            return false
        }
        return true
    }

    fun checkPermission():Boolean {
        //check if audio recording permission is granted
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO)!= PackageManager.PERMISSION_GRANTED){
            return false
        }
        return true
    }

    override fun startRecording() {
        super.startRecording()
        if (!checkMicPresence()) {
            return
        }
        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.RECORD_AUDIO), 0)
        if (!checkPermission()) {
            return
        }
        isStopped = false
        //create audio recorder and start recording
        audioRecord = audioClassifier.createAudioRecord()
        audioRecord.startRecording()

        timerTask = object: TimerTask() {
             override fun run() {
                var output = audioClassifier.classify(tensorAudio)

                //filter out classifications with low probability
                var finalOutput = ArrayList<org.tensorflow.lite.support.label.Category>()
                for (classifications in output) {
                    for (category in classifications.categories){
                        if (category.score > 0.3f) {
                            finalOutput.add(category)
                        }
                    }
                }
                //create a multiline string with the filtered result
                var outputStr = StringBuilder()
                for (category in finalOutput) {
                    outputStr.append(category.label).append((": ")).append(category.score).append("\n")
                }



                 Thread {
                     runOnUiThread {
                         outputTextView?.setText(outputStr.toString())
                     }
                 }


                 /*
                 runOnUiThread(
                     outputTextView.setText(outputStr.toString())
                 )*/


            }

        }



    }
    override fun stopRecording() {
        super.stopRecording()
        if (!isStopped) {
            timerTask.cancel()
            audioRecord.stop()
            isStopped = true
        }
    }
}




