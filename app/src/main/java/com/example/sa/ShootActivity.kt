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
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import com.google.firebase.firestore.toObjects
import java.math.BigDecimal
import kotlin.math.sqrt
import kotlin.properties.Delegates


class ShootActivity : AppCompatActivity() {
    private val aViewModel: AccelerometerViewModel by viewModels();
    private val aViewModel2: GyroscopeViewModel by viewModels();
    private lateinit var mediaPlayerbip: MediaPlayer
    private lateinit var auth: FirebaseAuth
    private val db = Firebase.firestore
    private var peso by Delegates.notNull<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_shoot)

        mediaPlayerbip = MediaPlayer.create(this, R.raw.countdown)

        // Initialize Firebase Auth
        auth = com.google.firebase.ktx.Firebase.auth

        auth.uid?.let { lerpeso(it) }

        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

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
                    //alertDialog.setMessage("Parabéns")
                    //alertDialog.show() // Mostrar a caixa de diálogo
                    //mediaPlayer.start()
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
        // Declare um Handler e um Runnable
        val handler = Handler()

        val stopSensorTask = Runnable {
            // Pare de coletar os dados do acelerômetro
            sensorManager.unregisterListener(accelerometerSensorListener)
            sensorManager.unregisterListener(gyroscopeSensorListener)
            //lerDocs(novoRemateId)
            ler2Docs(novoRemateId)
        }

        val currentUser = auth.currentUser

        // Create a new user with a first and last name
        val novoSaltoData  = hashMapOf(
            "user_id" to currentUser?.uid,
            "força" to 0,
            "pontuação" to 0,
            "timestamp" to Timestamp.now()
        )

        val novoRemateRef = db.collection("Shoot").add(novoSaltoData)
            .addOnSuccessListener { documentReference ->
                // O documento foi adicionado com sucesso, você pode acessar o ID aqui
                novoRemateId = documentReference.id
                Log.d("REMATE333", "Novo ID de Salto: $novoRemateId")
                // Inicie a coleta dos dados do acelerômetro
                mAccelerometer?.let { accelerometer ->
                    accelerometerSensorListener.setSensorManager(sensorManager, aViewModel,"Shoot",novoRemateId)
                    sensorManager.registerListener(
                        accelerometerSensorListener,
                        accelerometer,
                        SensorManager.SENSOR_DELAY_GAME
                    )

                    // Defina um tempo para parar de coletar os dados (por exemplo, 1 segundos)
                    handler.postDelayed(stopSensorTask, 2000) // 1000 milissegundos = 1 segundos
                }
                mGyroscope?.let { gyroscope ->
                    gyroscopeSensorListener.setSensorManager(sensorManager, aViewModel2,"Shoot",novoRemateId)
                    sensorManager.registerListener(
                        gyroscopeSensorListener,
                        gyroscope,
                        SensorManager.SENSOR_DELAY_GAME
                    )

                    // Defina um tempo para parar de coletar os dados (por exemplo, 1 segundos)
                    handler.postDelayed(stopSensorTask, 2000) // 1000 milissegundos = 1 segundos
                }

            }
            .addOnFailureListener { e ->
                // Ocorreu um erro ao adicionar o documento
                Log.e("SHOOT333", "Erro ao adicionar o documento", e)
            }

    }

    private fun ler2Docs(novoRemateId:String) {
        // Primeira coleção
        db.collection("Shoot").document(novoRemateId)
            .collection("AccelerometerData")
            .orderBy("timestamp") // Ordenar os documentos pelo campo "timestamp"
            .get()
            .addOnSuccessListener { result1 ->
                val listaDeDados1 = result1.toObjects<AccelerometerData>()

                // Segunda coleção
                db.collection("Shoot").document(novoRemateId)
                    .collection("GyroscopeData")
                    .orderBy("timestamp") // Ordenar os documentos pelo campo "timestamp"
                    .get()
                    .addOnSuccessListener { result2 ->
                        val listaDeDados2 = result2.toObjects<GyroscopeData>()

                        // Calcular pontuação com base nas duas listas de dados
                        val pontuacao = calcularpontuacao(listaDeDados1, listaDeDados2, novoRemateId)
                        animarPontuacao(pontuacao) // Assumindo que a função animarPontuacao espera um valor de pontuação

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
            db.collection("users").document(it) // Ordenar os documentos pelo campo "timestamp"
                .get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        val usuario = documentSnapshot.toObject<User>() // Classe Usuario
                        peso = (usuario?.peso?: 0.0).toString().toInt() // Peso do usuário (se existir)

                        // Use o pesoUsuario para o cálculo da pontuação ou outras ações
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
            val valor = Math.abs(listaDeDados[i].valueY)
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

        // Encontre o elemento com o menor valor de accelerometerX
        val auxA = maxValorAcelerometro(listaDeDadosA)
        val auxG = maxValorGiroscopio(listaDeDadosG)
        val accelerometerData = listaDeDadosA[auxA]
        val gyroscopeValue = listaDeDadosG[auxG]

        var fA = sqrt(accelerometerData.accelerometerX*accelerometerData.accelerometerX+accelerometerData.accelerometerY
                *accelerometerData.accelerometerY+accelerometerData.accelerometerZ*accelerometerData.accelerometerZ)
        var fG = sqrt(gyroscopeValue.valueX *gyroscopeValue.valueX+gyroscopeValue.valueY
                *gyroscopeValue.valueY+gyroscopeValue.valueZ*gyroscopeValue.valueZ)
        pontos = (fA * fG * 50).toInt() // *massa

        // Referência para o documento que você deseja atualizar
        val docRef = db.collection("Shoot").document(novoRemateId)

        // Atualiza o campo desejado
        docRef
            .update("força", fA*fG)
            .addOnSuccessListener {
                // Sucesso ao atualizar o campo
            }
            .addOnFailureListener { e ->
                // Tratamento de erro
            }
        docRef
            .update("pontuação", pontos)
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