package com.example.alertio

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast

class Setting : AppCompatActivity() {
    private var notifBtn : Button? = null
    private var audioInputBtn : Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)
        window.statusBarColor = 0   //set status bar color to white

        notifBtn = findViewById(R.id.notifButton)

        notifBtn!!.setOnClickListener(){
            try{
                var intent = Intent("android.settings.APP_NOTIFICATION_SETTINGS")
                intent.putExtra("android.provider.extra.APP_PACKAGE", getPackageName());
                startActivity(intent)
            } catch(e:Exception){
                Toast.makeText(
                    this,
                    e.toString(),
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        audioInputBtn = findViewById(R.id.audioInputBtn)

        audioInputBtn!!.setOnClickListener(){
            try{
                var intent = Intent("android.settings.APPLICATION_DETAILS_SETTINGS")
                var uri :Uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri)
                startActivity(intent)
            } catch(e:Exception){
                Toast.makeText(
                    this,
                    e.toString(),
                    Toast.LENGTH_LONG
                ).show()
            }

        }

    }
}