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


    fun setSensorManager(sensorMan: SensorManager,aViewModel: AccelerometerViewModel) {
        sensorManager = sensorMan
        ourAccelerometerViewModel = aViewModel
    }

    override fun onSensorChanged(event: SensorEvent) {

        val AccelerometerData = AccelerometerData(
            accelerometerX = event.values[0],
            accelerometerY = event.values[1],
            accelerometerZ = event.values[2],
            accuracy = event.accuracy,
            timestamp = event.timestamp
        )

        //sensorManager.unregisterListener(this)
        ourAccelerometerViewModel.currentAccelerometerData.value = AccelerometerData
        Log.d(TAG,"[SENSOR] - X=${AccelerometerData.accelerometerX},Y=${AccelerometerData.accelerometerY}," +
                "Z=${AccelerometerData.accelerometerZ}")

        val db = Firebase.firestore

        // Create a new user with a first and last name
        val accelerometer = hashMapOf(
            "accelerometerX" to AccelerometerData.accelerometerX,
            "accelerometerY" to AccelerometerData.accelerometerY,
            "accelerometerZ" to AccelerometerData.accelerometerZ,
            "timestamp" to AccelerometerData.timestamp
        )

        // Add a new document with a generated ID
        db.collection("accelerometro")
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