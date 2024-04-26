package com.example.sa

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Firebase Auth
        auth = Firebase.auth
        val currentUser = auth.currentUser
        Log.w("User666","${currentUser?.email}")

        if (currentUser != null){
            val intent = Intent(this, MenuActivity::class.java)
            startActivity(intent)
        }else{
            val intent = Intent(this, LogInActivity::class.java)
            startActivity(intent)
        }
    }
}