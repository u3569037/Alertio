package com.example.alertio

import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import org.tensorflow.lite.support.audio.TensorAudio
import org.tensorflow.lite.task.audio.classifier.AudioClassifier
import org.tensorflow.lite.task.audio.classifier.AudioClassifier.AudioClassifierOptions
import org.tensorflow.lite.task.audio.classifier.Classifications
import java.io.IOException
import java.util.Locale.Category
import java.util.TimerTask
import kotlin.concurrent.timerTask


open class Operating : AppCompatActivity() {
    //private lateinit var recorder: MediaRecorder

     protected var isStopped = false
     protected var btnBack: Button? = null
     protected var btnStart: Button? = null
     protected var btnStop: Button? = null
     protected var outputTextView: TextView? = null

    //   private lateinit var audioRecord:AudioRecord
 //   var modelPath = "yamnet_classification.tflite"
 //   private lateinit var timerTask:TimerTask

//    private lateinit var audioClassifier: AudioClassifier
//    private lateinit var tensorAudio: TensorAudio


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_operating)

        btnBack = findViewById<Button>(R.id.back)
        btnStart = findViewById<Button>(R.id.start)
        btnStop = findViewById<Button>(R.id.stop)
        //outputTextView = findViewById<TextView>(R.id.outputTextView)
        // val visualizer = findViewById<com.gauravk.audiovisualizer.visualizer.BarVisualizer>(R.id.visualizer)

        btnBack!!.setOnClickListener {
            //check if recording is stopped
            if (isStopped) {
                // go back to the main page
                val intent = Intent(this@Operating, Homepage::class.java)
                startActivity(intent)
            } else {
                Toast.makeText(
                    this,
                    "Recording in progress. Please stop the recording first",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        btnStop!!.setOnClickListener {
            stopRecording()
        }

        btnStart!!.setOnClickListener() {
            startRecording()
        }


/*
        //loading model from assets folder
        try {
            audioClassifier = AudioClassifier.createFromFile(this, modelPath)
        } catch (e: IOException) {
            e.printStackTrace()
        }


        // create an audio recorder
        tensorAudio = audioClassifier.createInputTensorAudio()*/


    }
/*
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
*/
    open fun startRecording() {
        //to be implemented in AudioClassificationActivity.kt
    }


    open fun stopRecording() {
        //to be implemented in AudioClassificationActivity.kt
    }

}



