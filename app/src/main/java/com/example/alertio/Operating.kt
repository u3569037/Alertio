package com.example.alertio

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.media.AudioFormat.CHANNEL_IN_MONO
import android.media.AudioFormat.ENCODING_PCM_16BIT
import android.media.AudioRecord
import android.media.AudioRecord.READ_NON_BLOCKING
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.View
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.visualizer.amplitude.AudioRecordView
import org.tensorflow.lite.support.audio.TensorAudio
import org.tensorflow.lite.task.audio.classifier.AudioClassifier
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.scheduleAtFixedRate
import kotlin.math.abs


class Operating : AppCompatActivity() {
    private val file = "identificationRecord"
    private var isStopped = true
    private var btnBack: ImageButton? = null
    private var btnStart: ImageButton? = null
    private var btnFile: ImageButton? = null
    private var toggle: Boolean = true
    private var isVibrating = false
    private var audioRecordView: AudioRecordView? = null
    private var resultText: TextView? = null
    //private var audioRecorder: MediaRecorder? = null
    private lateinit var outputTextView:TextView
    private lateinit var graphicText: ImageView


    private lateinit var audioRecord: AudioRecord
    private var modelPath = "lite-model_yamnet_classification_tflite_1.tflite"
    private lateinit var timerTask: TimerTask

    private lateinit var audioClassifier: AudioClassifier
    private lateinit var tensorAudio: TensorAudio



    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        //load model from assets folder
        try {
            audioClassifier = AudioClassifier.createFromFile(this, modelPath)
        } catch (e: IOException) {
            e.printStackTrace()

        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_operating)
        window.statusBarColor = 0   //set status bar color to white
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        btnBack = findViewById<ImageButton>(R.id.backBtn)
        btnStart = findViewById<ImageButton>(R.id.startbtn)
        btnFile = findViewById<ImageButton>(R.id.menuBtn)
        audioRecordView = findViewById(R.id.audioRecordView)
        resultText = findViewById(R.id.resultText)
        graphicText = findViewById(R.id.imageView9)
        //audioRecorder = MediaRecorder()
        val danger : List<String> = listOf("Speech","Shout","Yell","Vehicle horn, car horn, honking", "car alarm", "Train horn", "Alarm clock", "Buzzer","Smoke detector, smoke alarm","Fire alarm", "Explosion","Gunshot, gunfire","Machine gun", "Boiling")



        //check mic presence of not
        val pm = packageManager
        val micPresent = pm.hasSystemFeature(PackageManager.FEATURE_MICROPHONE)
        if (!micPresent) {
            Toast.makeText(this, "Your device doesn't have a microphone", Toast.LENGTH_LONG).show()
            finish()
        }

        val requestPermissionLauncher =
            registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                if (isGranted) {
                    // Permission is granted
                } else {
                    Toast.makeText(
                        this,
                        "Microphone is needed to perform AI analysis",
                        Toast.LENGTH_LONG
                    ).show()

                    finish()
                }
            }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)!= PackageManager.PERMISSION_GRANTED){
            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }



        btnBack!!.setOnClickListener {
            //check if recording is stopped
            if (isStopped) {
                // go back to the main page
                val intent = Intent(this@Operating, Homepage::class.java)
                startActivity(intent)
            } else {
                Toast.makeText(
                    this,
                    "AI is running. \nPlease stop the AI first",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        btnFile!!.setOnClickListener {
            //check if recording is stopped
            if (isStopped) {
                // go back to the main page
                val intent = Intent(this@Operating, PastRecord::class.java)
                startActivity(intent)
            } else {
                Toast.makeText(
                    this@Operating,
                    "AI is running. \nPlease stop the AI first",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        btnStart!!.setOnClickListener() {
            if (toggle){  //startRecording
                btnStart!!.setImageResource(R.drawable.stop_button_icon)
                toggle = false
                isStopped = false

                // Create the variable that will store the recording for inference and build the format specification for the recorder.
                tensorAudio = audioClassifier.createInputTensorAudio()

                //create audio recorder and start
                audioRecord = audioClassifier.createAudioRecord()
                audioRecord.startRecording()

                val bufferSize = AudioRecord.getMinBufferSize(44100, CHANNEL_IN_MONO, ENCODING_PCM_16BIT)
//                val bufferSize = 62400
//                var buffer : ByteBuffer = ByteBuffer.allocateDirect(bufferSize)
                //var buffer = ShortArray(bufferSize)

//                Thread {
//                    while (!isStopped){
//                        audioRecord.positionNotificationPeriod = bufferSize/2
//                        audioRecord.read(buffer,  bufferSize, READ_NON_BLOCKING)
//                    }
//                    return@Thread
//                }.start()

                Timer().scheduleAtFixedRate( 1, 500){
                    if (isStopped){
                        return@scheduleAtFixedRate
                    }
//                    var audioData = ShortArray(bufferSize/2)
//                    for (i in (0 until bufferSize/2)){
//                        audioData[i] = buffer.getShort(i*2)
//                    }
                    tensorAudio.load(audioRecord)

                    //tensorAudio.load(audioData)

                    val output = audioClassifier.classify(tensorAudio)
                    val filteredModelOutput = output[0].categories.filter {
                        it.score > 0.3f
                    }
                    val outputStr = filteredModelOutput.sortedBy { -it.score }
                        .joinToString(separator = "\n") { "${it.label} -> ${(it.score*100).toInt()}% " }

                    if (!isVibrating){
                        runOnUiThread {
                            resultText!!.text = outputStr
                        }
                    }

                    for (label:String in danger) {
                        if (output[0].categories[0].label == label && !isVibrating) {
                            alertUSER(label)
                        }
                    }
                 /*   if (output[0].categories[0].label == "Alarm clock") {
                        alertUSER("Alarm clock")

                    }*/

                }
/*
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


                    }*/


                //audioRecordView!!.recreate()   // For clearing all drawn pattern

//                val date = SimpleDateFormat("yyyy-MM-dd HH:mm").format(Date())
//                val filename = "audio_record_$date"
//                audioRecorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)
//                audioRecorder!!.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
//                audioRecorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
//                audioRecorder!!.setOutputFile("${externalCacheDir?.absolutePath}/$filename.mp3")
//                try{
//                    audioRecorder!!.prepare()
//                } catch(e:IOException){
//                    Toast.makeText(
//                        this,
//                        e.toString(),
//                        Toast.LENGTH_LONG
//                    ).show()
//                }

                //audioRecorder!!.start()

                //visualize audio
                Thread {
                    while(!isStopped){
                        //var buffer = ByteArray(1000)
                        //audioRecord.read(buffer, bufferSize, READ_NON_BLOCKING)
                        var buffer = tensorAudio.tensorBuffer.buffer.duplicate()

                        //var bytes : ByteArray = ByteArray(buffer.remaining())
                        //var shorts : ShortArray = ShortArray(bytes.size/2)
                        //ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shorts)
//                        var tempBuffer:ByteBuffer = ByteBuffer.allocateDirect(2)
//                        tempBuffer.order(ByteOrder.LITTLE_ENDIAN)
//                        tempBuffer.put(buffer.get(0))
//                        tempBuffer.put(buffer.get(1))
//                        var amplitude = 0
//                        for (i in (0 until 50)){
//                            if (abs(buffer.get(i) + buffer.get(i+1)*128)/2 > amplitude){
//                                amplitude = abs(buffer.get(i) + buffer.get(i+1)*128)/2
//                            }
//                        }

//                        var amplitude:Short = 0
//                        for (i in (0 until bufferSize/2)){
//                            if (buffer.getShort(i*2) > amplitude){
//                                amplitude = buffer.getShort(i*2)
//                            }
//                        }
                        //var amplitude = abs(buffer.get(0) + buffer.get(1)*256)/2
                        var amplitude = buffer.getShort(0).toInt()
                        if (amplitude<1000){
                            amplitude = 1000
                        }
                        //val amplitude = abs(tempBuffer.getShort(0).toInt())
                        //var amplitude = buffer.max()/2
                        //var amplitude = audioRecorder!!.getMaxAmplitude()

                        //var amplitude = Math.max(abs(buffer.max().toInt()),abs(buffer.min().toInt()))
                        audioRecordView!!.post { audioRecordView!!.update( amplitude.toInt() )}
                        //resultText!!.post { resultText!!.text = "Current amplitude: $amplitude" }
                        Thread.sleep(30)
                    }
                }.start()




            }else {   //stopRecording
                btnStart!!.setImageResource(R.drawable.start_button_icon)
                toggle = true
                isStopped = true
                audioRecord.stop()
                //audioRecorder!!.reset()
            }
        }
    }



    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            (requestCode) -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() &&
                            grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // Permission is granted.
                } else {
//                    Toast.makeText(
//                        this,
//                        "Microphone is needed to perform AI analysis",
//                        Toast.LENGTH_LONG
//                    ).show()

                    //finish()
                }
                return
            }
            else -> {
                // Ignore all other requests.
            }
        }
    }

    override fun onBackPressed() {
        if (isStopped) {
            super.onBackPressed()
        } else {
            Toast.makeText(
                this,
                "AI is running. \nPlease stop the AI first",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    //call this when detect danger
    private fun alertUSER(danger:String){
        if (!isVibrating){
            isVibrating = true

            //vibrate
            val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if (Build.VERSION.SDK_INT >= 26) {
                vibrator.vibrate(VibrationEffect.createOneShot(3000, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                vibrator.vibrate(3000)
            }

            //send notification
//            val timeStamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())
//            var builder = NotificationCompat.Builder(this, "i.apps.notifications")
//                .setSmallIcon(R.drawable.appicon)
//                .setContentTitle("Potential danger detected")
//                .setContentText("$danger detected at $timeStamp ")
//                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
//            builder.run {  }


            //change card color

            Thread {
                val card: View = findViewById<CardView>(R.id.view)
                if (danger == "Speech") {
                    runOnUiThread {
                        card.setBackgroundColor(Color.RED)
                        graphicText.setImageResource(R.drawable.alarm_clock)
                    }
                }
                Thread.sleep(3000)
                runOnUiThread {
                    card.setBackgroundColor(Color.TRANSPARENT)
                    graphicText.setImageResource(R.drawable.ai_preload_icon)
                }

                isVibrating = false
            }.start()

            //add the detected danger to record
            addRecord(danger)

            return
        } else{
            //add the detected danger to record
            addRecord(danger)

            return
        }
    }






    private fun addRecord(danger:String){

        /* ------------- code tried to change timezone ------------- */
        // var strDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date()) + "+0000"
        // var dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ssZ")
        // var resultOfParsing = OffsetDateTime.parse(strDate, dateFormatter)
        //
        // var timeZone = ZoneId.of("Asia/Shanghai")
        // var hkTime: ZonedDateTime = resultOfParsing.atZoneSameInstant(timeZone)
        // val timeStamp = hkTime.format(dateFormatter).removeSuffix("+0800")

        val timeStamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())


        val data = "$danger,$timeStamp\n"

        try {
            val fOut: FileOutputStream = openFileOutput(file, MODE_APPEND)
            fOut.write(data.toByteArray())
            fOut.close()
            //Toast.makeText(this, "file saved", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}