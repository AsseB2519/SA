package com.example.sa

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.sa.databinding.ActivityLogInBinding
import com.example.sa.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LogInActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private var binding: ActivityLogInBinding?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLogInBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        auth = Firebase.auth

        binding?.login?.setOnClickListener{
            val email: String = binding?.email?.text.toString()
            val password: String = binding?.password?.text.toString()

            if(email.isNotEmpty() && password.isNotEmpty()){
                signInUserAndPassword(email,password)
            }else{
                Toast.makeText(this@LogInActivity,"Preencha os campos, por favor.",Toast.LENGTH_SHORT).show()
            }
        }

        binding?.registernow?.setOnClickListener{
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }
    private fun signInUserAndPassword(email:String,password:String){
        auth.signInWithEmailAndPassword(email,password).addOnCompleteListener {task->
            if(task.isSuccessful){
                Log.d(TAG,"signInUserWithEmailAndPassword:Sucesso")
                val user = auth.currentUser
                val intent = Intent(this, MenuActivity::class.java)
                intent.putExtra("user", user)
                startActivity(intent)
            }else{
                Log.w(TAG,"signInUserWithEmailAndPassword:Failure",task.exception)
                Toast.makeText(baseContext,"Authentication Failure", Toast.LENGTH_SHORT).show()
            }
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
