package com.example.sa

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MenuActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_menu)

        // Initialize Firebase Auth
        auth = Firebase.auth


        val sports = listOf("Box","Jump","LongJump","Shoot")
        
        val autoComplete : AutoCompleteTextView=findViewById(R.id.auto_complete)
        
        val adapter = ArrayAdapter(this,R.layout.list_item,sports)
        
        autoComplete.setAdapter(adapter)
        
        autoComplete.onItemClickListener = AdapterView.OnItemClickListener{
                adapterView: AdapterView<*>?, view: View?, position: Int, id: Long ->

            val itemSelected = adapterView?.getItemAtPosition(position)

            if (itemSelected=="Box") {
                val intent = Intent(this, BoxActivity::class.java)
                startActivity(intent)
            }
            else if(itemSelected=="Jump"){
                val intent = Intent(this, JumpActivity::class.java)
                startActivity(intent)
            }
        }

        val currentUser = auth.currentUser

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
            val intent = Intent(this, ShootActivity::class.java)
            startActivity(intent)
        }

        findViewById<ImageView>(R.id.stats).setOnClickListener{
            Log.d("BUTTON", "Siga saltar")
            val intent = Intent(this, LineActivity::class.java)
            startActivity(intent)
        }

        findViewById<Button>(R.id.button).setOnClickListener{
            Log.d("BUTTON", "Stop button pressed!")
            Firebase.auth.signOut()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

    }
}