package com.example.alertio

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast

import java.lang.Exception
import java.io.File
import java.io.FileOutputStream

class Operating : AppCompatActivity() {
    private val file = "mydata"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_operating)


        // + System.getProperty("line.separator")
        val data = ("12:23:29\n")
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