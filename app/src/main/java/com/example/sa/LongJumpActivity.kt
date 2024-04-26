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
import java.math.BigDecimal

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


        db.collection("Jump").document("salto1")
            .collection("AccelerometerData")
            .orderBy("timestamp") // Ordenar os documentos pelo campo "timestamp"
            .get()
            .addOnSuccessListener { result ->
                val listaDeDados=result.toObjects<AccelerometerData>()

                for (d in listaDeDados){
                    Log.w("User5555","${d.timestamp} : ${d.accelerometerZ}")
                }
                // Encontre o elemento com o menor valor de accelerometerZ
                val elementoMaisNegativo = listaDeDados.minByOrNull { it.accelerometerZ }

                // Obtenha o timestamp do primeiro elemento da lista
                val primeiroTimestamp = listaDeDados.firstOrNull()?.timestamp ?: 0

                // Calcule a diferença de tempo se o elemento mais negativo existir
                val diferencaDeTempo: Long = if (elementoMaisNegativo != null) {
                    val timestampMaisNegativo = elementoMaisNegativo.timestamp
                    timestampMaisNegativo - primeiroTimestamp
                } else {
                    0// Se não houver elemento mais negativo, retorne 0
                }
                var res = BigDecimal(diferencaDeTempo).divide(BigDecimal(1000000000))

                val h= 0.5 * res.toFloat()*res.toFloat()*9.81
                Log.d("User5555","$h")
            }
            .addOnFailureListener { exception ->
                Log.w("User555", "Error getting documents.", exception)
            }

    }
}