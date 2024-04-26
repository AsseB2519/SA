package com.example.sa

import android.animation.ValueAnimator
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObjects
import java.math.BigDecimal
import kotlin.math.sqrt

class BoxActivity : AppCompatActivity() {

    private val aViewModel: AccelerometerViewModel by viewModels();
    private val aViewModel2: GyroscopeViewModel by viewModels();
    private lateinit var mediaPlayerbip: MediaPlayer
    private lateinit var auth: FirebaseAuth
    private val db = Firebase.firestore

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
        val mGyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        val gyroscopeSensorListener = GyroscopeSensorListener()

        val novoSocoId ="salto1"

        // inside the onCreate method
        findViewById<Button>(R.id.register_button).setOnClickListener {
            Log.d("BUTTON", "Start button pressed!")

            //mediaPlayerbip.start()
            object : CountDownTimer(3000, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    val secondsRemaining = millisUntilFinished / 1000
                    val minutes = secondsRemaining / 60
                    val seconds = secondsRemaining % 60

                    val formattedTime = String.format("%02d:%02d", minutes, seconds+1)
                    findViewById<TextView>(R.id.timerdisplaybox).text = formattedTime
                }

                override fun onFinish() {
                    val formattedTime = String.format("%02d:%02d", 0, 0)
                    findViewById<TextView>(R.id.timerdisplaybox).text = formattedTime
                    //criarSoco()
                    mGyroscope?.let { gyroscope ->
                        gyroscopeSensorListener.setSensorManager(
                            sensorManager,
                            aViewModel2,
                            "Box",
                            novoSocoId
                        )
                        sensorManager.registerListener(
                            gyroscopeSensorListener,
                            gyroscope,
                            SensorManager.SENSOR_DELAY_NORMAL
                        )
                    }
                }
            }.start()
        }

        findViewById<Button>(R.id.unregister_button).setOnClickListener {
            Log.d("BUTTON", "Stop button pressed!")
            sensorManager.unregisterListener(gyroscopeSensorListener)

        }

        val gyroscopeObserver = Observer<GyroscopeData> { gyroSample ->
            findViewById<TextView>(R.id.value_for_x).text = gyroSample.valueX.toString()
            findViewById<TextView>(R.id.value_for_y).text = gyroSample.valueY.toString()
            findViewById<TextView>(R.id.value_for_z).text = gyroSample.valueZ.toString()
        }

        aViewModel2.currentGyroscopeData.observe(this, gyroscopeObserver)
    }
    /*
    private fun criarSoco(){
        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val mAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val accelerometerSensorListener = AccelerometerSensorListener()
        val mGyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        val gyroscopeSensorListener = GyroscopeSensorListener()

        var novoSaltoId:String= ""
        // Declare um Handler e um Runnable
        val handler = Handler()

        val stopSensorTask = Runnable {
            // Pare de coletar os dados do acelerômetro
            sensorManager.unregisterListener(accelerometerSensorListener)
            sensorManager.unregisterListener(gyroscopeSensorListener)
            lerDocs(novoSaltoId)
        }

        val currentUser = auth.currentUser

        // Create a new user with a first and last name
        val novoSaltoData  = hashMapOf(
            "user_id" to currentUser?.uid,
            "altura" to 0,
            "pontuação" to 0
        )

        val novoSaltoRef = db.collection("Box").add(novoSaltoData)
            .addOnSuccessListener { documentReference ->
                // O documento foi adicionado com sucesso, você pode acessar o ID aqui
                novoSaltoId = documentReference.id
                Log.d("JUMP333", "Novo ID de Salto: $novoSaltoId")
                // Inicie a coleta dos dados do acelerômetro
                mAccelerometer?.let { accelerometer ->
                    accelerometerSensorListener.setSensorManager(sensorManager, aViewModel,"Box",novoSaltoId)
                    sensorManager.registerListener(
                        accelerometerSensorListener,
                        accelerometer,
                        SensorManager.SENSOR_DELAY_NORMAL
                    )

                    // Defina um tempo para parar de coletar os dados (por exemplo, 1 segundos)
                    handler.postDelayed(stopSensorTask, 2000) // 1000 milissegundos = 1 segundos
                }
                mGyroscope?.let { gyroscope ->
                    gyroscopeSensorListener.setSensorManager(sensorManager, aViewModel2,"Box",novoSaltoId)
                    sensorManager.registerListener(
                        gyroscopeSensorListener,
                        gyroscope,
                        SensorManager.SENSOR_DELAY_NORMAL
                    )

                    // Defina um tempo para parar de coletar os dados (por exemplo, 1 segundos)
                    handler.postDelayed(stopSensorTask, 2000) // 1000 milissegundos = 1 segundos
                }

            }
            .addOnFailureListener { e ->
                // Ocorreu um erro ao adicionar o documento
                Log.e("JUMP333", "Erro ao adicionar o documento", e)
            }

    }

    private fun lerDocs(novoSaltoId:String){
        db.collection("Box").document(novoSaltoId)
            .collection("AccelerometerData")
            .orderBy("timestamp") // Ordenar os documentos pelo campo "timestamp"
            .get()
            .addOnSuccessListener { result ->
                val listaDeDados=result.toObjects<AccelerometerData>()

                val pontuacao = calcularpontuacao(listaDeDados)
                animarPontuacao(800)

            }
            .addOnFailureListener { exception ->
                Log.w("User555", "Error getting documents.", exception)
            }
    }

    private fun calcularpontuacao(listaDeDados:List<AccelerometerData>):Int{
        var res  = 800

        var lastTimeStamp = listaDeDados.first().timestamp
        var aux = listaDeDados.first()
        var f = sqrt(aux.accelerometerX*aux.accelerometerX+aux.accelerometerY
                *aux.accelerometerY+aux.accelerometerZ*aux.accelerometerZ)
        res= (f * 500).toInt()
        return res
    }

    private fun animarPontuacao(pontuacao:Int) {

        // Criar um ValueAnimator para animar a pontuação
        val animator = ValueAnimator.ofInt(0, pontuacao)
        animator.duration = 2000 // Duração da animação em milissegundos

        // Adicionar um listener de atualização de valor
        animator.addUpdateListener { animation ->
            // Atualizar o TextView com o valor animado
            val valorAtual = animation.animatedValue as Int
            findViewById<TextView>(R.id.pontos).text = "$valorAtual"
        }

        // Iniciar a animação
        animator.start()
    }*/
}