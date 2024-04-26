package com.example.sa

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class AccelerometerSensorListener: SensorEventListener {
    companion object{
        private const val TAG : String = "AccelerometerSensorListener"
    }

    private lateinit var sensorManager: SensorManager
    private lateinit var ourAccelerometerViewModel: AccelerometerViewModel
    private lateinit var novoSaltoId:String
    private lateinit var colecao:String

    fun setSensorManager(sensorMan: SensorManager,aViewModel: AccelerometerViewModel, colecao:String,novoSaltoId: String) {
        sensorManager = sensorMan
        ourAccelerometerViewModel = aViewModel
        this.novoSaltoId = novoSaltoId
        this.colecao = colecao
    }

    override fun onSensorChanged(event: SensorEvent) {

        val accelerometerData = AccelerometerData(
            accelerometerX = event.values[0],
            accelerometerY = event.values[1],
            accelerometerZ = event.values[2],
            accuracy = event.accuracy,
            timestamp = event.timestamp
        )

        //sensorManager.unregisterListener(this)
        ourAccelerometerViewModel.currentAccelerometerData.value = accelerometerData
        Log.d(TAG,"[SENSOR] - X=${accelerometerData.accelerometerX},Y=${accelerometerData.accelerometerY}," +
                "Z=${accelerometerData.accelerometerZ}")

        val db = Firebase.firestore

        // Create a new user with a first and last name
        val accelerometer = hashMapOf(
            "accelerometerX" to accelerometerData.accelerometerX,
            "accelerometerY" to accelerometerData.accelerometerY,
            "accelerometerZ" to accelerometerData.accelerometerZ,
            "timestamp" to accelerometerData.timestamp
        )

        // Add a new document with a generated ID
        db.collection(colecao).document(novoSaltoId)
            .collection("AccelerometerData")
            .add(accelerometer)
            .addOnSuccessListener { documentReference ->
                Log.d(TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding document", e)
            }

    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

}