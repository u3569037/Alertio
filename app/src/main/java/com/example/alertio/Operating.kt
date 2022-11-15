package hk.hkucs.noisemonitoringapp

import android.content.pm.PackageManager
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import org.tensorflow.lite.support.audio.TensorAudio
import org.tensorflow.lite.task.audio.classifier.AudioClassifier
import org.tensorflow.lite.task.audio.classifier.Classifications
import java.io.IOException
import java.util.Locale.Category
import java.util.TimerTask
import kotlin.concurrent.timerTask


class MainActivity : AppCompatActivity() {
    //private lateinit var recorder: MediaRecorder

    private var isStopped = false
    private lateinit var audioRecord:AudioRecord
    private var model = "yamnet_classification.tflite"
    private lateinit var timerTask:TimerTask

    private lateinit var audioClassifier: AudioClassifier
    private lateinit var tensorAudio: TensorAudio


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnBack = findViewById<Button>(R.id.back)
        val btnStop = findViewById<Button>(R.id.stop)
        val visualizer = findViewById<com.gauravk.audiovisualizer.visualizer.BarVisualizer>(R.id.visualizer)

        btnBack.setOnClickListener{
            //check if recording is stopped
            if (isStopped) {
                // go back to the main page
            }
            else {
                Toast.makeText(this, "Recording in progress. Please stop the recording first", Toast.LENGTH_LONG).show()
            }
        }

        btnStop.setOnClickListener{
            stopRecording()
        }

        //loading model from asset folder
        try {
            audioClassifier = AudioClassifier.createFromFile(this, model)
        }catch (e: IOException){
            e.printStackTrace()
        }

        // create an audio recorder
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

    private fun startRecording() {
        if (!checkMicPresence()) {
            return
        }
        if (!checkPermission()) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.RECORD_AUDIO), 0)
            return
        }
        isStopped = false
        audioRecord = audioClassifier.createAudioRecord()
        audioRecord.startRecording()

        timerTask = object: TimerTask() {
            public override fun run() {
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

                runOnUiThread(Runnable{
                    fun run() {
                        outputTextView.setText(outputStr.toString())
                    }
                })

            }

        }
    }


    private fun stopRecording() {
        if (!isStopped) {
            timerTask.cancel()
            audioRecord.stop()
            isStopped = true
        }

    }


}
