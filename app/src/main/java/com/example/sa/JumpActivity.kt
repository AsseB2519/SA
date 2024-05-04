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
import android.content.Intent
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
import android.widget.ImageView
import com.google.firebase.Timestamp
import com.google.firebase.firestore.toObjects
import java.math.BigDecimal

class JumpActivity : AppCompatActivity() {

    private val aViewModel: AccelerometerViewModel by viewModels();
    private val aViewModel2: GyroscopeViewModel by viewModels();
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
                    val formattedTime = String.format("%02d:%02d", 0, 0)
                    findViewById<TextView>(R.id.timerdisplay).text = formattedTime
                    //alertDialog.setMessage("Parabéns")
                    //alertDialog.show() // Mostrar a caixa de diálogo
                    //mediaPlayer.start()
                    criarSalto()
                }
            }.start()

        }
        findViewById<ImageView>(R.id.progression).setOnClickListener{
            val intent = Intent(this, LineActivity::class.java)
            intent.putExtra("colecao", "Jump")
            startActivity(intent)
        }
        findViewById<ImageView>(R.id.top5).setOnClickListener{
            val intent = Intent(this, LeaderBoardActivity::class.java)
            intent.putExtra("colecao", "Jump")
            startActivity(intent)
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
            "pontuação" to 0,
            "timestamp" to Timestamp.now()
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
                        SensorManager.SENSOR_DELAY_GAME
                    )

                    // Defina um tempo para parar de coletar os dados (por exemplo, 1 segundos)
                    handler.postDelayed(stopSensorTask, 2000) // 1000 milissegundos = 1 segundos
                }
                mGyroscope?.let { gyroscope ->
                    gyroscopeSensorListener.setSensorManager(sensorManager, aViewModel2,"Jump",novoSaltoId)
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
        db.collection("Jump").document(novoSaltoId)
            .collection("AccelerometerData")
            .orderBy("timestamp") // Ordenar os documentos pelo campo "timestamp"
            .get()
            .addOnSuccessListener { result ->
                val listaDeDados=result.toObjects<AccelerometerData>()

                val pontuacao = calcularpontuacao(listaDeDados,novoSaltoId)
                animarPontuacao(800)

            }
            .addOnFailureListener { exception ->
                Log.w("User555", "Error getting documents.", exception)
            }


    }

    fun maxNegativoAntesDeSubir(listaDeDados: List<AccelerometerData>): AccelerometerData? {
        var maxNegativo: Float? = null
        var indice:Int=0

        for (i in listaDeDados.indices) {
            val valor = listaDeDados[i].accelerometerZ
            // Se o valor for negativo e ainda não encontramos um máximo negativo
            if (valor < 0) {
                if (maxNegativo == null || valor < maxNegativo){
                    maxNegativo = valor
                    indice=i
                }
                else if(valor>maxNegativo){
                    break
                }
            }
            // Se encontramos um valor positivo ou zero, paramos
            else if (valor >= 0 && maxNegativo != null) {
                break
            }
        }

        return listaDeDados[indice]
    }

    private fun calcularpontuacao(listaDeDados:List<AccelerometerData>,novoSaltoId:String):Int{
        var pontos  = 800
        var jumpHeight = 0f
        var velocity = 0f
        var lastTimeStamp = listaDeDados.first().timestamp

        // Encontre o elemento com o menor valor de accelerometerZ
        val elementoMaisNegativo = maxNegativoAntesDeSubir(listaDeDados)
        Log.w("User555","minimo $elementoMaisNegativo")

        // Obtenha o timestamp do primeiro elemento da lista
        val primeiroTimestamp = listaDeDados.firstOrNull()?.timestamp ?: 0

        // Calcule a diferença de tempo se o elemento mais negativo existir
        val diferencaDeTempo: Long = if (elementoMaisNegativo != null) {
            val timestampMaisNegativo = elementoMaisNegativo.timestamp
            timestampMaisNegativo - primeiroTimestamp
        } else {
            0// Se não houver elemento mais negativo, retorne 0
        }
        var res = BigDecimal(diferencaDeTempo).divide(BigDecimal(1000000000))

        val h= 0.5 * res.toFloat()*res.toFloat()*9.81

        // Referência para o documento que você deseja atualizar
        val docRef = db.collection("Jump").document(novoSaltoId)

        // Atualiza o campo desejado
        docRef
            .update("altura", h)
            .addOnSuccessListener {
                // Sucesso ao atualizar o campo
            }
            .addOnFailureListener { e ->
                // Tratamento de erro
            }

        return pontos
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