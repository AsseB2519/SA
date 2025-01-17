package com.example.sa

import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Vibrator
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import com.google.firebase.firestore.toObjects
import java.math.BigDecimal
import kotlin.math.abs
import kotlin.math.sqrt
import kotlin.properties.Delegates


class ShootActivity : AppCompatActivity() {
    private val aViewModel: AccelerometerViewModel by viewModels();
    private val aViewModel2: GyroscopeViewModel by viewModels();
    private lateinit var mediaPlayerbip: MediaPlayer
    private lateinit var auth: FirebaseAuth
    private val db = Firebase.firestore
    private var peso by Delegates.notNull<Float>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_shoot)

        mediaPlayerbip = MediaPlayer.create(this, R.raw.countdown)

        auth = com.google.firebase.ktx.Firebase.auth

        auth.uid?.let { lerpeso(it) }

        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        val builder = AlertDialog.Builder(this)
        builder.setTitle("How to play")
        builder.setMessage("After clicking the start button, place the smartphone upright in your pocket with the screen facing out.")

        findViewById<ImageView>(R.id.help).setOnClickListener{
            val alert = builder.create()
            alert.show()
        }

        findViewById<Button>(R.id.startshoot).setOnClickListener {
            object : CountDownTimer(5000, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    val secondsRemaining = millisUntilFinished / 1000
                    val minutes = secondsRemaining / 60
                    val seconds = secondsRemaining % 60
                    if (seconds.toInt() == 2) {
                        mediaPlayerbip.start()
                    }
                    val formattedTime = String.format("%02d:%02d", minutes, seconds + 1)
                    findViewById<TextView>(R.id.timerdisplay).text = formattedTime
                }

                override fun onFinish() {
                    vibrator.vibrate(1000)
                    val formattedTime = String.format("%02d:%02d", 0, 0)
                    findViewById<TextView>(R.id.timerdisplay).text = formattedTime
                    criarRemate()
                }
            }.start()

            findViewById<ImageView>(R.id.progression).setOnClickListener {
                val intent = Intent(this, LineActivity::class.java)
                intent.putExtra("colecao", "Shoot")
                startActivity(intent)
            }
            findViewById<ImageView>(R.id.top5).setOnClickListener {
                val intent = Intent(this, LeaderBoardActivity::class.java)
                intent.putExtra("colecao", "Shoot")
                startActivity(intent)
            }

        }

    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayerbip.release()
    }

    private fun criarRemate(){
        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val mAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val accelerometerSensorListener = AccelerometerSensorListener()
        val mGyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        val gyroscopeSensorListener = GyroscopeSensorListener()

        var novoRemateId:String= ""

        val handler = Handler()

        val stopSensorTask = Runnable {
            sensorManager.unregisterListener(accelerometerSensorListener)
            sensorManager.unregisterListener(gyroscopeSensorListener)
            ler2Docs(novoRemateId)
        }

        val currentUser = auth.currentUser

        val novoSaltoData  = hashMapOf(
            "user_id" to currentUser?.uid,
            "força" to 0,
            "pontuação" to 0,
            "timestamp" to Timestamp.now()
        )

        val novoRemateRef = db.collection("Shoot").add(novoSaltoData)
            .addOnSuccessListener { documentReference ->
                novoRemateId = documentReference.id
                Log.d("REMATE333", "Novo ID de Salto: $novoRemateId")
                mAccelerometer?.let { accelerometer ->
                    accelerometerSensorListener.setSensorManager(sensorManager, aViewModel,"Shoot",novoRemateId)
                    sensorManager.registerListener(
                        accelerometerSensorListener,
                        accelerometer,
                        SensorManager.SENSOR_DELAY_GAME
                    )

                    handler.postDelayed(stopSensorTask, 2000) // 1000 milissegundos = 1 segundo
                }
                mGyroscope?.let { gyroscope ->
                    gyroscopeSensorListener.setSensorManager(sensorManager, aViewModel2,"Shoot",novoRemateId)
                    sensorManager.registerListener(
                        gyroscopeSensorListener,
                        gyroscope,
                        SensorManager.SENSOR_DELAY_GAME
                    )

                    handler.postDelayed(stopSensorTask, 2000) // 1000 milissegundos = 1 segundo
                }

            }
            .addOnFailureListener { e ->
                Log.e("SHOOT333", "Erro ao adicionar o documento", e)
            }

    }

    private fun ler2Docs(novoRemateId:String) {
        db.collection("Shoot").document(novoRemateId)
            .collection("AccelerometerData")
            .orderBy("timestamp")
            .get()
            .addOnSuccessListener { result1 ->
                val listaDeDados1 = result1.toObjects<AccelerometerData>()

                db.collection("Shoot").document(novoRemateId)
                    .collection("GyroscopeData")
                    .orderBy("timestamp")
                    .get()
                    .addOnSuccessListener { result2 ->
                        val listaDeDados2 = result2.toObjects<GyroscopeData>()

                        val pontuacao = calcularpontuacao(listaDeDados1, listaDeDados2, novoRemateId)
                        animarPontuacao(pontuacao)

                    }
                    .addOnFailureListener { exception ->
                        Log.w("User555", "Error getting documents from second collection.", exception)
                    }
            }
            .addOnFailureListener { exception ->
                Log.w("User555", "Error getting documents from first collection.", exception)
            }
    }

    private fun lerpeso(novoSaltoId: String) {
        auth.uid?.let {
            db.collection("users").document(it)
                .get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        val usuario = documentSnapshot.toObject<User>()
                        peso = (usuario?.peso?: 0.0).toString().toFloat()

                        Log.d("Peso", "User $peso")
                    } else {
                        Log.d("User555", "User document not found for ID:")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.w("User555", "Error getting user document.", exception)
                }
        }
    }

    private fun maxValorAcelerometro(listaDeDados:List<AccelerometerData>):Int{

        var max: Float? = null
        var indice:Int=0

        for (i in listaDeDados.indices) {
            val valor = listaDeDados[i].accelerometerY
            if (valor > 0) {
                if (max == null || valor > max){
                    max = valor
                    indice=i
                }
            }
        }

        return indice
    }

    private fun maxValorGiroscopio(listaDeDados:List<GyroscopeData>):Int{

        var max: Float? = null
        var indice:Int=0

        for (i in listaDeDados.indices) {
            val valor = abs(listaDeDados[i].gyroscopeY)
            if (valor > 0) {
                if (max == null || valor > max){
                    max = valor
                    indice=i
                }
            }
        }

        return indice

    }

    private fun calcularpontuacao(listaDeDadosA:List<AccelerometerData>, listaDeDadosG:List<GyroscopeData>, novoRemateId:String):Int{
        var pontos  = 800

        val auxA = maxValorAcelerometro(listaDeDadosA)
        val auxG = maxValorGiroscopio(listaDeDadosG)
        val accelerometerData = listaDeDadosA[auxA]
        val gyroscopeValue = listaDeDadosG[auxG]

        var fA = sqrt(accelerometerData.accelerometerX*accelerometerData.accelerometerX+accelerometerData.accelerometerY
                *accelerometerData.accelerometerY+accelerometerData.accelerometerZ*accelerometerData.accelerometerZ)
        var fG = sqrt(gyroscopeValue.gyroscopeX *gyroscopeValue.gyroscopeX+gyroscopeValue.gyroscopeY
                *gyroscopeValue.gyroscopeY+gyroscopeValue.gyroscopeZ*gyroscopeValue.gyroscopeZ)
        val forca = (fA * fG * peso*0.17)
        pontos=calcularPontuacaocomF(forca)

        val docRef = db.collection("Shoot").document(novoRemateId)

        docRef
            .update("força", forca)
            .addOnSuccessListener {
            }
            .addOnFailureListener { e ->
            }
        docRef
            .update("pontuação", pontos)
            .addOnSuccessListener {
            }
            .addOnFailureListener { e ->
            }

        return pontos
    }

    fun calcularPontuacaocomF(forca: Double): Int {
        return when {
            forca <= 0 -> 0
            forca >= 3600-> 999
            else -> {
                val pontuacao = (forca*0.2775).toInt()
                pontuacao
            }
        }
    }

    private fun animarPontuacao(pontuacao:Int) {

        val animator = ValueAnimator.ofInt(0, pontuacao)
        animator.duration = 2000

        animator.addUpdateListener { animation ->
            val valorAtual = animation.animatedValue as Int
            findViewById<TextView>(R.id.pontos).text = "$valorAtual"
        }

        animator.start()
    }
}
