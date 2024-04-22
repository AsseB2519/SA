package com.example.sa

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        findViewById<ImageView>(R.id.imageView3).setOnClickListener{
            Log.d("BUTTON", "Stop button pressed!")
            val intent = Intent(this, BoxActivity::class.java)
            startActivity(intent)
        }

        findViewById<ImageView>(R.id.imageView5).setOnClickListener{
            Log.d("BUTTON", "Siga saltar")
            val intent = Intent(this, JumpActivity::class.java)
            startActivity(intent)
        }

        findViewById<ImageView>(R.id.imageView4).setOnClickListener{
            Log.d("BUTTON", "Siga saltar")
            val intent = Intent(this, LongJumpActivity::class.java)
            startActivity(intent)
        }

    }
}