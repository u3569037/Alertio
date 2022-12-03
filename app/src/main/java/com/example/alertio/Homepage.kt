package com.example.alertio

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.cardview.widget.CardView
import java.util.*
import kotlin.concurrent.schedule

class Homepage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_homepage)
        window.statusBarColor = 0   //set status bar color to white

        val card: CardView = findViewById(R.id.cardView)
        card.setOnClickListener {
            val intent = Intent(this@Homepage, Setting::class.java)
            startActivity(intent)
        }


        val guidebtn: Button = findViewById(R.id.guidebutton)
        guidebtn.setOnClickListener {
            val intent = Intent(this@Homepage, UserGuide::class.java)
            startActivity(intent)
        }

        val recordbtn: Button = findViewById(R.id.recordbutton)
        recordbtn.setOnClickListener {
            val intent = Intent(this@Homepage, PastRecord::class.java)
            startActivity(intent)
        }

        val startbtn: Button = findViewById(R.id.startbutton)
        startbtn.setOnClickListener {
            val intent = Intent(this@Homepage, Operating::class.java)
            startActivity(intent)
        }
    }


}