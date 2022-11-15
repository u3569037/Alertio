package com.example.alertio

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.ListView
import android.widget.SimpleAdapter
import android.widget.Toast
import java.io.FileInputStream
import java.io.File
import java.io.FileOutputStream


class PastRecord : AppCompatActivity() {
    private val file = "mydata"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_past_record)
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

        }
            catch (e: Exception) {
        }


        val list = arrayListOf<MutableMap<String, Any>>()
        for (i in recordList!!.indices) {
            val map: MutableMap<String, Any> = HashMap()
            val count = i+1
            map["Danger"] = "Danger $count"
            map["Time"] = recordList[i]
            list.add(map)
        }
        val adapter = SimpleAdapter(this, list, R.layout.activity_list_item,
            arrayOf("Danger", "Time"), intArrayOf(R.id.role, R.id.name))
        val list_view: ListView = findViewById(R.id.list_view)
        list_view.adapter = adapter }
    }

