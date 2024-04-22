package com.example.sa

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class BoxActivity : AppCompatActivity() {

    private val aViewModel: AccelerometerViewModel by viewModels();

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_box)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val mAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val accelerometerSensorListener = AccelerometerSensorListener()



        // inside the onCreate method
        findViewById<Button>(R.id.register_button).setOnClickListener {
            Log.d("BUTTON", "Start button pressed!")

            if (mAccelerometer != null) {
                accelerometerSensorListener.setSensorManager(sensorManager, aViewModel)
                sensorManager.registerListener(
                    accelerometerSensorListener,
                    mAccelerometer,
                    SensorManager.SENSOR_DELAY_NORMAL
                )
            }
        }

        findViewById<Button>(R.id.unregister_button).setOnClickListener {
            Log.d("BUTTON", "Stop button pressed!")
            sensorManager.unregisterListener(accelerometerSensorListener)

            val db = FirebaseFirestore.getInstance()
            val listaDeDados = ArrayList<AccelerometerData>()

            db.collection("accelerometro")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener { result ->
                    for (document in result) {

                        listaDeDados.add(document.toObject(AccelerometerData::class.javaObjectType))

                        Log.d("Leitor", "${document.id} => ${document.data}")
                    }

                }
                .addOnFailureListener { exception ->
                    Log.w("Leitor", "Error getting documents.", exception)
                }

        }

        val accelerometerObserver = Observer<AccelerometerData> { accSample ->
            findViewById<TextView>(R.id.value_for_x).text = accSample.accelerometerX.toString()
            findViewById<TextView>(R.id.value_for_y).text = accSample.accelerometerY.toString()
            findViewById<TextView>(R.id.value_for_z).text = accSample.accelerometerZ.toString()
        }

        aViewModel.currentAccelerometerData.observe(this, accelerometerObserver)
    }
}