package com.example.sa

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class GyroscopeSensorListener: SensorEventListener {
    companion object {
        private const val TAG = "GyroscopeSensorListener"
    }

    private lateinit var sensorManager: SensorManager
    private lateinit var ourGyroscopeViewModel: GyroscopeViewModel
    private lateinit var novoSaltoId:String
    private lateinit var colecao:String

    fun setSensorManager(sensorMan: SensorManager, aViewModel: GyroscopeViewModel,colecao:String,novoSaltoId: String) {
        sensorManager = sensorMan
        ourGyroscopeViewModel = aViewModel
        this.novoSaltoId = novoSaltoId
        this.colecao = colecao
    }

    override fun onSensorChanged(event: SensorEvent) {

        val GyroscopeData = GyroscopeData(
            valueX = event.values[0],
            valueY = event.values[1],
            valueZ = event.values[2],
            accuracy = event.accuracy,
            timestamp = event.timestamp
        )

        //sensorManager.unregisterListener(this)
        ourGyroscopeViewModel.currentGyroscopeData.value = GyroscopeData
        Log.d(TAG,"[SENSOR] - X=${GyroscopeData.valueX},Y=${GyroscopeData.valueY}," +
                "Z=${GyroscopeData.valueZ}")

        val db = Firebase.firestore

        // Create a new user with a first and last name
        val gyroscope = hashMapOf(
            "gyroscopeX" to GyroscopeData.valueX,
            "gyroscopeY" to GyroscopeData.valueY,
            "gyroscopeZ" to GyroscopeData.valueZ,
            "timestamp" to GyroscopeData.timestamp
        )

        // Add a new document with a generated ID
        db.collection(colecao).document(novoSaltoId)
            .collection("GyroscopeData")
            .add(gyroscope)
            .addOnSuccessListener { documentReference ->
                Log.d(TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding document", e)
            }

    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

}