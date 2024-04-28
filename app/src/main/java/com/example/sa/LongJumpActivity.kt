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
import kotlin.math.sqrt

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

        val documento = "YdYFeYrp8j3ie951TlTG"
        val collection = "Box"

        db.collection(collection).document(documento)
            .collection("AccelerometerData")
            .orderBy("timestamp") // Ordenar os documentos pelo campo "timestamp"
            .get()
            .addOnSuccessListener { result ->
                val listaDeDados=result.toObjects<AccelerometerData>()

                val dt = BigDecimal(listaDeDados[1].timestamp-listaDeDados[0].timestamp)
                    .divide(BigDecimal(1000000000)).toDouble()

                for (d in listaDeDados){
                    Log.w("Aceleração","${
                        sqrt(d.accelerometerX*d.accelerometerX+d.accelerometerY
                            *d.accelerometerY+d.accelerometerZ*d.accelerometerZ)
                    }")
                }

                Log.d("User5555","1")
            }
            .addOnFailureListener { exception ->
                Log.w("User555", "Error getting documents.", exception)
            }

    }

    // Função para integrar a aceleração vertical
    fun integrateAcceleration(listaDeDados: List<AccelerometerData>, dt: Double): List<Double> {
        // Inicializar lista de velocidades
        val velocities = mutableListOf<Double>()

        // Velocidade inicial (assumindo repouso inicial)
        var velocity = 0.0

        // Integrar a aceleração para cada ponto de dados
        for (acceleration in listaDeDados) {
            // Calcular a nova velocidade
            velocity += acceleration.accelerometerZ * dt

            // Adicionar a nova velocidade à lista
            velocities.add(velocity)
        }

        return velocities
    }

    // Função para integrar a velocidade vertical e calcular a posição vertical
    fun integrateVelocity(velocities: List<Double>, dt: Double): List<Double> {
        // Inicializar lista de posições
        val positions = mutableListOf<Double>()

        // Posição inicial (assumindo repouso inicial)
        var position = 0.0

        // Integrar a velocidade para cada ponto de dados
        for (velocity in velocities) {
            // Calcular a nova posição
            position += velocity * dt

            // Adicionar a nova posição à lista
            positions.add(position)
        }

        return positions
    }

    fun maxNegativoAntesDeSubir(listaDeDados: List<AccelerometerData>): Int {
        var maxNegativo: Float? = null
        var indice:Int=0

        for (i in listaDeDados.indices) {
            val valor = listaDeDados[i].accelerometerZ
            // Se o valor for negativo e ainda não encontramos um máximo negativo
            if (valor < 0) {
                if (maxNegativo == null || valor < maxNegativo){
                    maxNegativo = valor
                    indice=i
                }
                else if(valor>maxNegativo){
                    break
                }
            }
            // Se encontramos um valor positivo ou zero, paramos
            else if (valor >= 0 && maxNegativo != null) {
                break
            }
        }

        return indice
    }

}