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
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import androidx.core.app.NotificationManagerCompat


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
    private var mediaRecorder: MediaRecorder? = null
    private lateinit var outputTextView:TextView
    private lateinit var graphicText: ImageView


    private lateinit var audioRecord: AudioRecord
    private var modelPath = "lite-model_yamnet_classification_tflite_1.tflite"
    private lateinit var timerTask: TimerTask

    private lateinit var audioClassifier: AudioClassifier
    private lateinit var tensorAudio: TensorAudio

    private val CHANNEL_ID = "channelID"
    private val CHANNEL_NAME = "channelName"
    private val NOTIF_ID = 0

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
        mediaRecorder = MediaRecorder()
        val danger : List<String> = listOf("Shout","Yell","Vehicle horn, car horn, honking", "Car alarm", "Train horn", "Alarm clock", "Buzzer","Smoke detector, smoke alarm","Fire alarm", "Explosion","Gunshot, gunfire","Machine gun", "Boiling", "Bicycle bell")



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

        createNotifChannel()




    /*    binding.btnShowNotif.setOnClickListener {
            notifManger.notify(NOTIF_ID,notif)
        }*/


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

                val bufferSize = AudioRecord.getMinBufferSize(16000, CHANNEL_IN_MONO, ENCODING_PCM_16BIT)
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
                    try{
                        tensorAudio.load(audioRecord)
                    }  catch(e:Exception){
                        Toast.makeText(
                            this@Operating,
                            e.toString(),
                            Toast.LENGTH_LONG
                        ).show()
                    }


                    //tensorAudio.load(audioData)

                    val output = audioClassifier.classify(tensorAudio)
                    val filteredModelOutput = output[0].categories.filter {
                        it.score > 0.8f
                    }



                    val outputStr = filteredModelOutput.sortedBy { -it.score }
                        .joinToString(separator = "\n") { "${it.label} : ${(it.score*100).toInt()}% " }


                    val outputStr2 = outputStr.toString().replace("Vehicle horn, car horn, honking","Vehicle horn")

                    if (!isVibrating){
                        runOnUiThread {
                            resultText!!.text = outputStr2
                        }
                    }

                    if (filteredModelOutput.sortedBy { -it.score }.isNotEmpty()){
                        for (label:String in danger) {
                            if (filteredModelOutput.sortedBy { -it.score }[0].label == label && !isVibrating) {
                                alertUSER(label)
                                break
                            }
                        }
                    }
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

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val date = SimpleDateFormat("yyyy-MM-dd HH:mm").format(Date())
                    val filename = "audio_record_$date"
                    mediaRecorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)
                    mediaRecorder!!.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                    mediaRecorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                    mediaRecorder!!.setOutputFile("${externalCacheDir?.absolutePath}/$filename.mp3")
                    try {
                        mediaRecorder!!.prepare()
                    } catch (e: IOException) {
                        Toast.makeText(
                            this,
                            e.toString(),
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    mediaRecorder!!.start()
                }

                //visualize audio
                Thread {
                    while(!isStopped){
                        var amplitude = 0
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            amplitude = mediaRecorder!!.getMaxAmplitude()
                        } else{
                            var buffer = ByteBuffer.allocateDirect(10)
                            try{
                                audioRecord.read(buffer, 10)
                            }  catch(e:Exception){
                                Toast.makeText(
                                    this,
                                    e.toString(),
                                    Toast.LENGTH_LONG
                                ).show()
                            }

                            //var readBuffer = tensorAudio.tensorBuffer.buffer.duplicate()

                            //var amplitude = abs(buffer.getShort(1).toDouble())
    //                        for (i in (1..24)){
    //                            amplitude += buffer.getShort(i)
    //                        }
    //                        amplitude /= 12
                            //println(buffer.toString())
                            println(audioRecord.audioFormat)
                            println(audioRecord.format)
                            println("\n")
                            println(buffer.get(0))
                            println(buffer.get(1))
                            println(buffer.get(2))
                            println(buffer.get(3))
                            println(buffer.get(4))
                            println(buffer.get(5))
                            println(buffer.get(6))
                            println("\n")
                            println(buffer.getFloat(0))
                            println(buffer.getFloat(1))
                            println(buffer.getFloat(2))
                            println(buffer.getFloat(3))
                            println(buffer.getFloat(4))
                            println(buffer.getFloat(5))
                            println(buffer.getFloat(6))
                            println("\n")

                            //var amplitude = abs(buffer.get(0) + buffer.get(1)*256)/2
                            amplitude = (buffer.getFloat(3)*500000).toInt()
                            if (amplitude<500){
                                amplitude = 500
                            }
                        }

                        audioRecordView!!.post { audioRecordView!!.update( amplitude.toInt() )}
                        Thread.sleep(30)
                    }
                }.start()




            }else {   //stopRecording
                btnStart!!.setImageResource(R.drawable.start_button_icon)
                toggle = true
                isStopped = true
                audioRecord.stop()
                //tensorAudio.tensorBuffer.buffer.clear()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    mediaRecorder!!.reset()
                }
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


    private fun createNotifChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT).apply {
                lightColor = Color.BLUE
                enableLights(true)
            }
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
    /*
    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }*/

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


            // send notification

            val timeStamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())
            val intent=Intent(this,MainActivity::class.java)
            val pendingIntent = TaskStackBuilder.create(this).run {
                addNextIntentWithParentStack(intent)
                getPendingIntent(0,PendingIntent.FLAG_IMMUTABLE)
            }




            val notifManger = NotificationManagerCompat.from(this@Operating)

            val notif = NotificationCompat.Builder(this@Operating,CHANNEL_ID)
                .setContentTitle("Potential danger detected")
                .setContentText("$danger detected at $timeStamp ")
                .setSmallIcon(R.drawable.notifyicon)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .build()

            notifManger.notify(NOTIF_ID,notif)

            // string formatting
            var head = danger[0].lowercase()
            var tail = danger.substring(1)
            tail = tail.replace(" ","_")
            tail = tail.replace(",", "")
            val result = head + tail

            //change card color
            Thread {
                val card: View = findViewById<CardView>(R.id.view)

                if (danger == "Shout" || danger == "Yell") {
                    runOnUiThread {
                        card.setBackgroundColor(android.graphics.Color.RED)
                        graphicText.setImageResource(com.example.alertio.R.drawable.shout)
                    }
                }
                else if  (danger == "Vehicle horn, car horn, honking" || danger == "Train horn") {
                    runOnUiThread {
                        card.setBackgroundColor(android.graphics.Color.RED)
                        graphicText.setImageResource(com.example.alertio.R.drawable.vehicle_horn_car_horn_honking)
                    }
                }

                else if (danger == "Car alarm" || danger == "Buzzer") {
                    runOnUiThread {
                        card.setBackgroundColor(android.graphics.Color.RED)
                        graphicText.setImageResource(com.example.alertio.R.drawable.car_alarm)
                    }
                }

                else if (danger == "Alarm clock") {
                    runOnUiThread {
                        card.setBackgroundColor(android.graphics.Color.RED)
                        graphicText.setImageResource(com.example.alertio.R.drawable.alarm_clock)
                    }
                }

                else if (danger == "Smoke detector, smoke alarm") {
                    runOnUiThread {
                        card.setBackgroundColor(android.graphics.Color.RED)
                        graphicText.setImageResource(com.example.alertio.R.drawable.smoke_detector_smoke_alarm)
                    }
                }

                else if (danger == "Fire alarm" || danger == "Explosion") {
                    runOnUiThread {
                        card.setBackgroundColor(android.graphics.Color.RED)
                        graphicText.setImageResource(com.example.alertio.R.drawable.fire_alarm)
                    }
                }

                else if (danger == "Gunshot, gunfire" || danger == "Machine gun") {
                    runOnUiThread {
                        card.setBackgroundColor(android.graphics.Color.RED)
                        graphicText.setImageResource(com.example.alertio.R.drawable.gunshot_gunfire)
                    }
                }

                else if (danger == "Boiling") {
                    runOnUiThread {
                        card.setBackgroundColor(android.graphics.Color.RED)
                        graphicText.setImageResource(com.example.alertio.R.drawable.boiling)
                    }
                }

                else if (danger == "Bicycle bell") {
                    runOnUiThread {
                        card.setBackgroundColor(android.graphics.Color.RED)
                        graphicText.setImageResource(com.example.alertio.R.drawable.bicycle_bell)
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


        val data = "$danger | $timeStamp\n"

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




