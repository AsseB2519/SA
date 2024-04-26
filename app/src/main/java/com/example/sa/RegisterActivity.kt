package com.example.sa

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
import com.google.firebase.ktx.Firebase
import org.w3c.dom.Text

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth:FirebaseAuth
    private var binding: ActivityRegisterBinding?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        //setContentView(R.layout.activity_register)
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
            if(task.isSuccessful){
                Log.d(TAG,"createUserWithEmailAndPassword:Sucesso")
                val user = auth.currentUser
            }else{
                Log.w(TAG,"createUserWithEmailAndPassword:Failure",task.exception)
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