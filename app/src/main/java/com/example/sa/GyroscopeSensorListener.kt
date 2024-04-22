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


    fun setSensorManager(sensorMan: SensorManager, aViewModel: GyroscopeViewModel) {
        sensorManager = sensorMan
        ourGyroscopeViewModel = aViewModel
    }

    override fun onSensorChanged(event: SensorEvent) {

        val GyroscopeData = GyroscopeData(
            valueX = event.values[0],
            valueY = event.values[1],
            valueZ = event.values[2],
            accuracy = event.accuracy,
        )

        //Gyroscopedata.valueX = event.values[0]
        //Gyroscopedata.valueY = event.values[1]
        //Gyroscopedata.valueZ = event.values[2]
        //Gyroscopedata.accuracy = event.accuracy

        //sensorManager.unregisterListener(this)
        ourGyroscopeViewModel.currentGyroscopeData.value = GyroscopeData
        Log.d(TAG,"[SENSOR] - X=${GyroscopeData.valueX},Y=${GyroscopeData.valueY}," +
                "Z=${GyroscopeData.valueZ}")

        val db = Firebase.firestore

        // Create a new user with a first and last name
        val accelerometer = hashMapOf(
            "x" to GyroscopeData.valueX,
            "y" to GyroscopeData.valueY,
            "z" to GyroscopeData.valueZ
        )

        // Add a new document with a generated ID
        db.collection("gyroscopedata")
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