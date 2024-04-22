package com.example.sa

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.toObject
import com.google.firebase.firestore.toObjects
import com.google.firebase.ktx.Firebase

class LongJumpActivity : AppCompatActivity() {
    val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_long_jump)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        /*/ Create a new user with a first and last name
        val user = hashMapOf(
            "first" to "Rui",
            "last" to "Silva",
            "born" to 2002
        )

        // Add a new document with a generated ID
        db.collection("users")
            .add(user)
            .addOnSuccessListener { documentReference ->
                Log.d("User", "DocumentSnapshot added with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.w("User", "Error adding document", e)
            }
        */
        val listaDeDados = ArrayList<User>()

        val result = db.collection("users")
            .get()
            .addOnSuccessListener { result ->
                //val listaDeDados2=result.toObjects<User>()
                for (document in result) {
                    Log.d("User555", "${document.id} => ${document.data}")
                    val dado = document.toObject<User>()
                    listaDeDados.add(dado.copy())
                    //listaDeDados.add(User(dado.first, dado.last, dado.born))
                    //Log.w("User555","${dado.born}")
                }
                for(u in listaDeDados){
                    Log.w("User555","${u.first}, ${u.last}, ${u.born}")
                }
            }
            .addOnFailureListener { exception ->
                Log.w("User555", "Error getting documents.", exception)
            }

    }
}