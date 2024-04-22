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


class JumpActivity : AppCompatActivity() {

    private val accelerometerViewModel: AccelerometerViewModel by viewModels();
    private val gyroscopeViewModel: GyroscopeViewModel by viewModels();
    private lateinit var mediaPlayerbip: MediaPlayer
    private lateinit var mediaPlayer: MediaPlayer


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_jump)

        mediaPlayerbip = MediaPlayer.create(this, R.raw.countdown)
        mediaPlayer = MediaPlayer.create(this, R.raw.siuuuu) // Substitua "sound.mp3" pelo nome do seu arquivo de som

        val builder = AlertDialog.Builder(this)
            .setTitle("Parece que hoje é o dia")
            .setCancelable(false) // O usuário não pode cancelar a caixa de diálogo
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss() // Fechar a caixa de diálogo
            }

        val alertDialog = builder.create()

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
                    alertDialog.setMessage("Parabéns")
                    alertDialog.show() // Mostrar a caixa de diálogo
                    mediaPlayer.start()
                    animarPontuacao()
                }
            }.start()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
        mediaPlayerbip.release()
    }

    private fun animarPontuacao() {
        val pontuacao = 800
        // Criar um ValueAnimator para animar a pontuação
        val animator = ValueAnimator.ofInt(0, pontuacao)
        animator.duration = 1000 // Duração da animação em milissegundos

        // Adicionar um listener de atualização de valor
        animator.addUpdateListener { animation ->
            // Atualizar o TextView com o valor animado
            val valorAtual = animation.animatedValue as Int
            findViewById<TextView>(R.id.Pontuação).text = "Pontuação: $valorAtual"
        }

        // Iniciar a animação
        animator.start()
    }
}