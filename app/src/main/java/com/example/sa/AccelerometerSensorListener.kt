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

        Accelerometerdata.accelerometerX = event.values[0]
        Accelerometerdata.accelerometerY = event.values[1]
        Accelerometerdata.accelerometerZ = event.values[2]
        // Obter o timestamp
        Accelerometerdata.timestamp = event.timestamp

        Accelerometerdata.accuracy = event.accuracy
        //sensorManager.unregisterListener(this)
        ourAccelerometerViewModel.currentAccelerometerData.value = Accelerometerdata
        Log.d(TAG,"[SENSOR] - X=${Accelerometerdata.accelerometerX},Y=${Accelerometerdata.accelerometerY}," +
                "Z=${Accelerometerdata.accelerometerZ}")

        val db = Firebase.firestore

        // Create a new user with a first and last name
        val accelerometer = hashMapOf(
            "accelerometerX" to Accelerometerdata.accelerometerX,
            "accelerometerY" to Accelerometerdata.accelerometerY,
            "accelerometerZ" to Accelerometerdata.accelerometerZ,
            "timestamp" to Accelerometerdata.timestamp
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