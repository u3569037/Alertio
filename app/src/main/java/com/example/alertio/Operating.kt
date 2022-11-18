package com.example.alertio

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_operating)
        window.statusBarColor = 0   //set status bar color to white

        addRecord("Bicycle ring")
    }

    private fun addRecord(danger:String){

          // code tried to change timezone
//        var strDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date()) + "+0000"
//        var dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ssZ")
//        var resultOfParsing = OffsetDateTime.parse(strDate, dateFormatter)
//
//        var timeZone = ZoneId.of("Asia/Shanghai")
//        var hkTime: ZonedDateTime = resultOfParsing.atZoneSameInstant(timeZone)
//        val timeStamp = hkTime.format(dateFormatter).removeSuffix("+0800")

        val timeStamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())


        val data = "$danger,$timeStamp\n"

        try {
            val fOut: FileOutputStream = openFileOutput(file, MODE_APPEND)
            fOut.write(data!!.toByteArray())
            fOut.close()
            Toast.makeText(getBaseContext(), "file saved", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}