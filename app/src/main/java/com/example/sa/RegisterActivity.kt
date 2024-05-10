package com.example.sa

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.sa.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.ktx.Firebase
import org.w3c.dom.Text

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth:FirebaseAuth
    private var binding: ActivityRegisterBinding?=null
    private val db = com.google.firebase.Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        auth = Firebase.auth

        binding?.register?.setOnClickListener{
            val email: String = binding?.email?.text.toString()
            val password: String = binding?.password?.text.toString()
            val confirmpassword: String = binding?.confirmpassword?.text.toString()

            if(email.isNotEmpty() && password.isNotEmpty() && confirmpassword.isNotEmpty()){
                if (password==confirmpassword){
                    createUserAndPassword(email,password)
                }
                else{
                    Toast.makeText(this@RegisterActivity,"Passwords não compativeis.",Toast.LENGTH_SHORT).show()
                }
            }else{
                Toast.makeText(this@RegisterActivity,"Preencha os campos, por favor.",Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun createUserAndPassword(email:String,password:String){
        auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener{task->
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }
    }

    companion object{
        private val TAG = "EmailAndPassword"
    }

    override fun onDestroy() {
        super.onDestroy()
        binding=null
    }
}
