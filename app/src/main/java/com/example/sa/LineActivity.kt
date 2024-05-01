package com.example.sa

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.checkerframework.common.subtyping.qual.Bottom

class LineActivity : AppCompatActivity() {

    lateinit var linelist:ArrayList<Entry>
    lateinit var lineDataSet: LineDataSet
    lateinit var lineData: LineData

    private lateinit var auth: FirebaseAuth
    private val db = Firebase.firestore
    lateinit var lineChart: LineChart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_line)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize Firebase Auth
        auth = com.google.firebase.ktx.Firebase.auth

        val userId = auth.uid

        lineChart= findViewById(R.id.linechart)

        if (userId != null) {
            GlobalScope.launch(Dispatchers.Main) {
                val result = obterPontuacoesPorUsuario(userId)
                configurarGrafico(result)
            }
        } else {
            println("ID do usuário é nulo. Não é possível contar os documentos.")
        }

    }

    private suspend fun obterMaiorPontuacaoPorColecao(userId: String, colecao: String): LinkedHashMap<Long, Long> {
        return try {
            val query = db.collection(colecao)
                .whereEqualTo("user_id", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(5)

            val snapshot = query.get().await()

            val timestampPontuacaoMap = LinkedHashMap<Long, Long>()

            // Iterar sobre os documentos no snapshot
            for (document in snapshot.documents) {
                // Obter o valor do timestamp e da pontuação do documento
                val timestamp = document.getTimestamp("timestamp")?.seconds ?: 0
                val pontuacao = document.getLong("pontuação") ?: 0

                // Adicionar os dados ao mapa
                timestampPontuacaoMap[timestamp] = pontuacao
            }
            Log.w("Firestore", "$timestampPontuacaoMap")
            timestampPontuacaoMap
        } catch (e: Exception) {
            Log.e("Firestore", "Erro ao obter documentos: $e")
            LinkedHashMap<Long, Long>()
        }
    }


    private suspend fun obterPontuacoesPorUsuario(userId: String): LinkedHashMap<Long, Long> {
        val colecoes = "Jump"
        val maiorPontuacaoPorColecao = obterMaiorPontuacaoPorColecao(userId,colecoes)

        return maiorPontuacaoPorColecao
    }

    private fun configurarGrafico(pontuação: Map<Long, Long>) {

        linelist = ArrayList()
        var position = 0
        // Usando um loop for
        for ((timestamp, pontuacao) in pontuação) {
            Log.w("Firestore", "${timestamp.toFloat()} : ${pontuacao.toFloat()}")
            linelist.add(Entry(position.toFloat(),pontuacao.toFloat()))
            position++
        }

        lineDataSet = LineDataSet(linelist,"Count")
        lineData = LineData(lineDataSet)
        lineChart.data=lineData
        lineDataSet.color=Color.BLACK
        lineDataSet.valueTextColor= Color.BLACK
        lineDataSet.valueTextSize=14f

        lineDataSet.setDrawFilled(false)

        setupLineChart()

        lineChart.invalidate() // Atualiza o gráfico


    }

    private fun setupLineChart() {
        lineChart.description.isEnabled = true // Desativa a descrição

        val xAxis: XAxis = lineChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.labelCount = lineDataSet.entryCount
        xAxis.setDrawLabels(true)

        val leftYAxis: YAxis = lineChart.axisLeft
        leftYAxis.setDrawGridLines(false)

        val rightYAxis: YAxis = lineChart.axisRight
        rightYAxis.isEnabled = false

        lineChart.setDrawGridBackground(true)
        lineChart.setBackgroundColor(Color.WHITE)
        lineChart.description.isEnabled = false

        lineChart.axisLeft.axisMinimum = 0f
        lineChart.axisRight.axisMinimum = 0f
        lineChart.xAxis.axisMinimum = 0f

    }

}