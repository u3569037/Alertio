package com.example.alertio

import android.content.Intent
import android.os.Bundle

import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.HandlerCompat.postDelayed
import com.visualizer.amplitude.AudioRecordView
import java.io.FileOutputStream
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*


class Operating : AppCompatActivity() {
    private val file = "identificationRecord"
    private var isStopped = true
    private var btnBack: ImageButton? = null
    private var btnStart: ImageButton? = null
    private var toggle: Boolean = true
    private var audioRecordView: AudioRecordView? = null
    //private var outputTextView: TextView? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_operating)
        window.statusBarColor = 0   //set status bar color to white

        btnBack = findViewById<ImageButton>(R.id.backBtn)
        btnStart = findViewById<ImageButton>(R.id.startbtn)
        audioRecordView = findViewById(R.id.audioRecordView)

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

        btnStart!!.setOnClickListener() {
            if (toggle){  //startRecording
                btnStart!!.setImageResource(R.drawable.stop_button_icon)
                toggle = false
                isStopped = false

                audioRecordView!!.recreate()
                Timer().schedule(object : TimerTask() {
                    override fun run() {
                        val currentMaxAmplitude = 8000
                        audioRecordView!!.update(currentMaxAmplitude)
                    }
                }, 10)

            } else{   //stopRecording
                btnStart!!.setImageResource(R.drawable.start_button_icon)
                toggle = true
                isStopped = true
            }
        }


        //addRecord("Bicycle ring")
    }

    override fun onBackPressed() {
        if (isStopped) {
            super.onBackPressed()
        } else {
            Toast.makeText(
                this,
                "Recording in progress. Please stop the recording first",
                Toast.LENGTH_LONG
            ).show()
        }
    }




    private fun addRecord(danger:String){

        // code tried to change timezone
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
            fOut.write(data!!.toByteArray())
            fOut.close()
            //Toast.makeText(this, "file saved", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}