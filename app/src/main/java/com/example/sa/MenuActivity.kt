package com.example.sa

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MenuActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_menu)

        // Initialize Firebase Auth
        auth = Firebase.auth


        val currentUser = auth.currentUser
        Log.w("User666","${currentUser?.email}")

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

        findViewById<ImageView>(R.id.imageView6).setOnClickListener{
            Log.d("BUTTON", "Siga saltar")
        }


        findViewById<Button>(R.id.button).setOnClickListener{
            Log.d("BUTTON", "Stop button pressed!")
            Firebase.auth.signOut()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

    }
}