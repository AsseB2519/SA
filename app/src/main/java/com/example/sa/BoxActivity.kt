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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
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

        setContentView(R.layout.activity_box)

        mediaPlayerbip = MediaPlayer.create(this, R.raw.countdown) // Substitua "sound.mp3" pelo nome do seu arquivo de som

        // Initialize Firebase Auth
        auth = com.google.firebase.ktx.Firebase.auth

        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        findViewById<Button>(R.id.startbox).setOnClickListener {
            object : CountDownTimer(5000, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    val secondsRemaining = millisUntilFinished / 1000
                    val minutes = secondsRemaining / 60
                    val seconds = secondsRemaining % 60
                    if (seconds.toInt()==2){mediaPlayerbip.start()}
                    val formattedTime = String.format("%02d:%02d", minutes, seconds+1)
                    findViewById<TextView>(R.id.timerdisplay).text = formattedTime
                }

                override fun onFinish() {
                    vibrator.vibrate(1000)
                    val formattedTime = String.format("%02d:%02d", 0, 0)
                    findViewById<TextView>(R.id.timerdisplay).text = formattedTime
                    //alertDialog.setMessage("Parabéns")
                    //alertDialog.show() // Mostrar a caixa de diálogo
                    //mediaPlayer.start()
                    criarSoco()
                }
            }.start()

        }
        findViewById<ImageView>(R.id.progression).setOnClickListener{
            val intent = Intent(this, LineActivity::class.java)
            intent.putExtra("colecao", "Box")
            startActivity(intent)
        }
        findViewById<ImageView>(R.id.top5).setOnClickListener{
            val intent = Intent(this, LeaderBoardActivity::class.java)
            intent.putExtra("colecao", "Box")
            startActivity(intent)
        }

    }

    private fun criarSoco(){
        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val mAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val accelerometerSensorListener = AccelerometerSensorListener()
        val mGyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        val gyroscopeSensorListener = GyroscopeSensorListener()


        var novoSocoId:String= ""
        // Declare um Handler e um Runnable
        val handler = Handler()

        val stopSensorTask = Runnable {
            // Pare de coletar os dados do acelerômetro
            sensorManager.unregisterListener(accelerometerSensorListener)
            sensorManager.unregisterListener(gyroscopeSensorListener)
            lerDocs(novoSocoId)
        }

        val currentUser = auth.currentUser

        // Create a new user with a first and last name
        val novoSocoData  = hashMapOf(
            "user_id" to currentUser?.uid,
            "força" to 0,
            "pontuação" to 0,
            "timestamp" to Timestamp.now()
        )

        Log.w("User555","$novoSocoData")

        db.collection("Box").add(novoSocoData)
            .addOnSuccessListener { documentReference ->
                // O documento foi adicionado com sucesso, você pode acessar o ID aqui
                novoSocoId = documentReference.id
                Log.d("Box333", "Novo ID de Box: $novoSocoId")
                // Inicie a coleta dos dados do acelerômetro
                mAccelerometer?.let { accelerometer ->
                    accelerometerSensorListener.setSensorManager(sensorManager, aViewModel,"Box",novoSocoId)
                    sensorManager.registerListener(
                        accelerometerSensorListener,
                        accelerometer,
                        SensorManager.SENSOR_DELAY_GAME
                    )

                    // Defina um tempo para parar de coletar os dados (por exemplo, 1 segundos)
                    handler.postDelayed(stopSensorTask, 1000) // 1000 milissegundos = 1 segundos
                }
                mGyroscope?.let { gyroscope ->
                    gyroscopeSensorListener.setSensorManager(sensorManager, aViewModel2,"Box",novoSocoId)
                    sensorManager.registerListener(
                        gyroscopeSensorListener,
                        gyroscope,
                        SensorManager.SENSOR_DELAY_GAME
                    )

                    // Defina um tempo para parar de coletar os dados (por exemplo, 1 segundos)
                    handler.postDelayed(stopSensorTask, 1000) // 1000 milissegundos = 1 segundos
                }

            }
            .addOnFailureListener { e ->
                // Ocorreu um erro ao adicionar o documento
                Log.e("Box333", "Erro ao adicionar o documento", e)
            }

    }

    private fun lerDocs(novoSocoId:String){
        db.collection("Box").document(novoSocoId)
            .collection("AccelerometerData")
            .orderBy("timestamp") // Ordenar os documentos pelo campo "timestamp"
            .get()
            .addOnSuccessListener { result ->
                val listaDeDados=result.toObjects<AccelerometerData>()
                Log.w("User555", "Cheguei aqui")
                val pontuacao = calcularpontuacao(listaDeDados,novoSocoId)
                animarPontuacao(800)

            }
            .addOnFailureListener { exception ->
                Log.w("User555", "Error getting documents.", exception)
            }
    }

    private fun calcularpontuacao(listaDeDados:List<AccelerometerData>,novoSocoId:String):Int{
        var res  = 800
        var maxF = 0.0f
        for (aux in listaDeDados){
            var f = sqrt(aux.accelerometerX*aux.accelerometerX+aux.accelerometerY
                    *aux.accelerometerY+aux.accelerometerZ*aux.accelerometerZ)
            if (f>maxF){maxF=f}
        }

        res= (maxF * 30).toInt()

        // Atualiza o campo desejado
        // Referência para o documento que você deseja atualizar
        val docRef = db.collection("Box").document(novoSocoId)
        docRef
            .update("força", maxF)
            .addOnSuccessListener {
                // Sucesso ao atualizar o campo
            }
            .addOnFailureListener { e ->
                // Tratamento de erro
            }
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