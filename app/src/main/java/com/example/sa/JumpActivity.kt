package com.example.sa

import android.animation.ValueAnimator
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.content.Context
import android.util.Log
import android.widget.Button
import android.media.MediaPlayer
import android.os.CountDownTimer
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.firestore
import java.util.concurrent.ExecutionException
import android.os.Handler
import com.google.firebase.firestore.toObjects
import java.math.BigDecimal

class JumpActivity : AppCompatActivity() {

    private val accelerometerViewModel: AccelerometerViewModel by viewModels();
    private val aViewModel: AccelerometerViewModel by viewModels();
    private val gyroscopeViewModel: GyroscopeViewModel by viewModels();
    private lateinit var mediaPlayerbip: MediaPlayer
    private lateinit var auth: FirebaseAuth
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_jump)

        mediaPlayerbip = MediaPlayer.create(this, R.raw.countdown) // Substitua "sound.mp3" pelo nome do seu arquivo de som

        // Initialize Firebase Auth
        auth = com.google.firebase.ktx.Firebase.auth

        findViewById<Button>(R.id.startjump).setOnClickListener {
            mediaPlayerbip.start()
            object : CountDownTimer(3000, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    val secondsRemaining = millisUntilFinished / 1000
                    val minutes = secondsRemaining / 60
                    val seconds = secondsRemaining % 60

                    val formattedTime = String.format("%02d:%02d", minutes, seconds+1)
                    findViewById<TextView>(R.id.timerdisplay).text = formattedTime
                }

                override fun onFinish() {
                    val formattedTime = String.format("%02d:%02d", 0, 0)
                    findViewById<TextView>(R.id.timerdisplay).text = formattedTime
                    //alertDialog.setMessage("Parabéns")
                    //alertDialog.show() // Mostrar a caixa de diálogo
                    //mediaPlayer.start()
                    criarSalto()
                }
            }.start()

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayerbip.release()
    }

    private fun criarSalto(){
        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val mAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val accelerometerSensorListener = AccelerometerSensorListener()
        var novoSaltoId:String= ""
        // Declare um Handler e um Runnable
        val handler = Handler()

        val stopSensorTask = Runnable {
            // Pare de coletar os dados do acelerômetro
            sensorManager.unregisterListener(accelerometerSensorListener)
            lerDocs(novoSaltoId)
        }

        val currentUser = auth.currentUser

        // Create a new user with a first and last name
        val novoSaltoData  = hashMapOf(
            "user_id" to currentUser?.uid,
            "altura" to 0,
            "pontuação" to 0
        )

        val novoSaltoRef = db.collection("Jump").add(novoSaltoData)
            .addOnSuccessListener { documentReference ->
                // O documento foi adicionado com sucesso, você pode acessar o ID aqui
                novoSaltoId = documentReference.id
                Log.d("JUMP333", "Novo ID de Salto: $novoSaltoId")
                // Inicie a coleta dos dados do acelerômetro
                mAccelerometer?.let { accelerometer ->
                    accelerometerSensorListener.setSensorManager(sensorManager, aViewModel,"Jump",novoSaltoId)
                    sensorManager.registerListener(
                        accelerometerSensorListener,
                        accelerometer,
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
        db.collection("Jump").document(novoSaltoId)
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
        var jumpHeight = 0f
        var velocity = 0f
        var lastTimeStamp = listaDeDados.first().timestamp

        // Iterar sobre os dados de aceleração e timestamps
        for (i in 1 until listaDeDados.size) {
            val acceleration = listaDeDados[i].accelerometerZ
            val currentTimeStamp = listaDeDados[i].timestamp

            // Calcular o intervalo de tempo desde a última amostra
            val timeInterval = BigDecimal(currentTimeStamp - lastTimeStamp).divide(BigDecimal(1000000000)).toFloat()// Convertendo para segundos

            // Integração da aceleração para obter a velocidade
            velocity += acceleration * timeInterval

            // Integração da velocidade para obter a altura
            jumpHeight += velocity * timeInterval + 0.5f * acceleration * timeInterval *timeInterval

            // Atualizar o timestamp da última amostra
            lastTimeStamp = currentTimeStamp
        }
        Log.w("Altura","$jumpHeight")

        /*/ Referência para o documento que você deseja atualizar
        val docRef = db.collection("Jump").document("$novoSaltoId")

        // Atualiza o campo desejado
        docRef
            .update("altura", jumpHeight)
            .addOnSuccessListener {
                // Sucesso ao atualizar o campo
            }
            .addOnFailureListener { e ->
                // Tratamento de erro
            }
        */
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
    }
}