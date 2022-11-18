package com.example.alertio

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.ListView
import android.widget.SimpleAdapter
import android.widget.Toast
import java.io.FileInputStream


class PastRecord : AppCompatActivity() {
    private val file = "identificationRecord"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_past_record)
        window.statusBarColor = 0   //set status bar color to white


        // add record to content view
        var recordList = arrayOf<String>()
        try {
            val fin: FileInputStream = openFileInput(file)
            var c: Int
            var temp = ""
            while (fin.read().also { c = it } != -1) {
                temp = temp + Character.toString(c.toChar())
                if(c.toChar() == '\n'){
                    recordList += temp
                    temp = ""
                }
            }
            //Toast.makeText(getBaseContext(), "file read", Toast.LENGTH_SHORT).show()

        }
            catch (e: Exception) {
                //exe
        }


        val list = arrayListOf<MutableMap<String, Any>>()
        for (i in recordList!!.indices) {
            val map: MutableMap<String, Any> = HashMap()
            val splitData = recordList[i].split(",").toTypedArray()
            map["Danger"] = splitData[0]
            map["Time"] = splitData[1]
            list.add(map)
        }
        val adapter = SimpleAdapter(this, list, R.layout.activity_list_item,
            arrayOf("Danger", "Time"), intArrayOf(R.id.danger, R.id.dtime))
        val list_view: ListView = findViewById(R.id.list_view)
        list_view.adapter = adapter }
    }

