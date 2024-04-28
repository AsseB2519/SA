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
        //setContentView(R.layout.activity_register)
        setContentView(binding?.root)

        auth = Firebase.auth

        binding?.register?.setOnClickListener{
            val email: String = binding?.email?.text.toString()
            val password: String = binding?.password?.text.toString()
            val confirmpassword: String = binding?.confirmpassword?.text.toString()
            val nome:String = binding?.nome?.text.toString()
            val apelido:String = binding?.apelido?.text.toString()

            if(email.isNotEmpty() && password.isNotEmpty() && confirmpassword.isNotEmpty()){
                if (password==confirmpassword){
                    createUserAndPassword(email,password,nome,apelido)
                }
                else{
                    Toast.makeText(this@RegisterActivity,"Passwords não compativeis.",Toast.LENGTH_SHORT).show()
                }
            }else{
                Toast.makeText(this@RegisterActivity,"Preencha os campos, por favor.",Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun createUserAndPassword(email:String,password:String,nome:String,apelido:String){
        auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener{task->
            if(task.isSuccessful){
                Log.d(TAG,"createUserWithEmailAndPassword:Sucesso")
                val user = auth.currentUser
                val dadosDoUsuario = hashMapOf(
                    "nome" to nome,
                    "idade" to 30,
                    "apelido" to apelido
                    // Adicione mais campos conforme necessário
                )

                // Add a new document with a generated ID
                db.collection("users").document(user!!.uid)
                    .set(dadosDoUsuario)
                    .addOnSuccessListener { documentReference ->
                        Log.d("User", "Novo user")
                        val intent = Intent(this, MenuActivity::class.java)
                        startActivity(intent)
                    }
                    .addOnFailureListener { e ->
                        Log.w("User", "Error adding document", e)
                    }
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