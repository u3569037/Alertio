package com.example.alertio

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.visualizer.amplitude.AudioRecordView
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs


class Operating : AppCompatActivity() {
    private val file = "identificationRecord"
    private var isStopped = true
    private var btnBack: ImageButton? = null
    private var btnStart: ImageButton? = null
    private var toggle: Boolean = true
    private var audioRecordView: AudioRecordView? = null
    private var resultText: TextView? = null
    private var audioRecorder: MediaRecorder? = null



    @SuppressLint("SimpleDateFormat")
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_operating)
        window.statusBarColor = 0   //set status bar color to white

        btnBack = findViewById<ImageButton>(R.id.backBtn)
        btnStart = findViewById<ImageButton>(R.id.startbtn)
        audioRecordView = findViewById(R.id.audioRecordView)
        resultText = findViewById(R.id.resultText)
        audioRecorder = MediaRecorder()

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

        btnStart!!.setOnClickListener() {
            if (toggle){  //startRecording
                btnStart!!.setImageResource(R.drawable.stop_button_icon)
                toggle = false
                isStopped = false




                //audioRecordView!!.recreate()   // For clearing all drawn pattern
                val date = SimpleDateFormat("yyyy-MM-dd HH:mm").format(Date())
                val filename = "audio_record_$date"
                audioRecorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)
                audioRecorder!!.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                audioRecorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                audioRecorder!!.setOutputFile("${externalCacheDir?.absolutePath}/$filename.mp3")
                try{
                    audioRecorder!!.prepare()
                } catch(e:IOException){
                    Toast.makeText(
                        this,
                        e.toString(),
                        Toast.LENGTH_LONG
                    ).show()
                }

                audioRecorder!!.start()

                //visualize audio
                Thread {
                    while(!isStopped){
                        audioRecordView!!.post { audioRecordView!!.update( audioRecorder!!.maxAmplitude)}
                        resultText!!.post { resultText!!.text = "Current amplitude: "+ audioRecorder!!.maxAmplitude.toString() }
                        Thread.sleep(30)
                    }
                }.start()


            } else{   //stopRecording
                btnStart!!.setImageResource(R.drawable.start_button_icon)
                toggle = true
                isStopped = true

                //audioRecord!!.stop()
                audioRecorder!!.pause()
            }
        }


        addRecord("Bicycle ring")
    }



    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults)
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