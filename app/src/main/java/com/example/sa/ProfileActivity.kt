package com.example.sa

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.sa.databinding.ActivityProfileBinding
import com.example.sa.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.ktx.Firebase

class ProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private var binding: ActivityProfileBinding?=null
    private val db = com.google.firebase.Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        auth = Firebase.auth

        binding?.register?.setOnClickListener{
            val nome:String = binding?.nome?.text.toString()
            val apelido:String = binding?.apelido?.text.toString()
            val idade:String = binding?.idade?.text.toString()
            val peso:String = binding?.peso?.text.toString()

            setUser(nome,apelido,idade,peso)
        }
    }

    private fun setUser(nome:String,apelido:String,idade:String,peso:String){
        Log.d(TAG,"createUserWithEmailAndPassword:Sucesso")
        val user = auth.currentUser
        val dadosDoUsuario = hashMapOf(
            "nome" to nome,
            "idade" to idade,
            "apelido" to apelido,
            "peso" to peso
        )

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

    }

    companion object{
        private val TAG = "EmailAndPassword"
    }

    override fun onDestroy() {
        super.onDestroy()
        binding=null
    }
}
