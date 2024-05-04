package com.example.sa

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.firebase.Firebase
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class LeaderBoardActivity : AppCompatActivity() {
    lateinit var barChart: BarChart
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_leader_board)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val colecao = intent.getStringExtra("colecao")

        findViewById<TextView>(R.id.jogo).text = colecao

        barChart = findViewById(R.id.bar_chart)

        GlobalScope.launch(Dispatchers.Main) {
            configurarGrafico(colecao!!)
        }

    }

    private suspend fun configurarGrafico(colecao: String) {

        // Dados de exemplo (pontuações de jogadores)
        //val scores = listOf(100, 120, 90, 110, 80)

        val cor = "#D6D4CE"

        val scores = obterTop5Pontuacoes(colecao)

        // Entradas de dados
        val entries = ArrayList<BarEntry>()
        val labels = mutableListOf<String>()
        labels.add("")

        scores.forEachIndexed { index, (nomeUsuario, pontuacaoUsuario) ->
            println("Índice: $index, Usuário: $nomeUsuario, Pontuação: $pontuacaoUsuario")
            entries.add(BarEntry((index+1).toFloat(),pontuacaoUsuario.toFloat()))
            labels.add(nomeUsuario)
        }

        /*/ Entradas de dados
        val entries = ArrayList<BarEntry>()
        val labels = mutableListOf<String>()
        topPontuacoes.forEach() { index, pair ->
            val userId = index
            val pontuacao = pair.toFloat()
            entries.add(BarEntry(index.toFloat(), pontuacao))
            labels.add(userId)
        }*/

        // Associa os nomes às barras
        barChart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)

        // Conjunto de dados de barras
        val bardataSet = BarDataSet(entries, "Pontuações dos Jogadores")
        bardataSet.color = Color.parseColor(cor) // Cor das barras

        // Configuração dos dados do gráfico de barras
        val barData = BarData(bardataSet)
        barData.barWidth = 0.5f // Largura das barras

        // Configuração do gráfico
        barChart.data = barData
        barChart.data.setValueTextColor(Color.parseColor(cor))
        barChart.setFitBars(true) // Ajusta o tamanho das barras
        barChart.description.isEnabled = false // Desativa a descrição
        barChart.xAxis.labelRotationAngle = 45f // Rotação dos rótulos do eixo X
        barChart.xAxis.granularity = 1f // Espaçamento entre os rótulos do eixo X

        // abaixo está a linha para definir os dados
        // para o nosso gráfico de barras.
        barChart.data = barData

        // adicionando cor ao nosso conjunto de dados de barra.
        //bardataSet.colors = ColorTemplate.JOYFUL_COLORS

        // definindo a cor do texto.
        bardataSet.valueTextColor = Color.parseColor(cor)

        // definindo o tamanho do texto
        bardataSet.valueTextSize = 16f

        setupBarChart(cor)

        barChart.invalidate() // Atualiza o gráfico
    }


    private fun setupBarChart(cor:String) {
        barChart.description.isEnabled = false
        barChart.legend.isEnabled = false

        val xAxis: XAxis = barChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawLabels(true)
        xAxis.textColor = Color.parseColor(cor) // Define a cor do texto do eixo X
        xAxis.gridColor = Color.parseColor(cor) // Define a cor das linhas de grade do eixo X
        xAxis.axisLineColor = Color.parseColor(cor) // Define a cor da linha do eixo X

        val rightYAxis = barChart.axisRight
        rightYAxis.isEnabled = false

        val leftYAxis: YAxis = barChart.axisLeft
        leftYAxis.textColor = Color.parseColor(cor) // Define a cor do texto do eixo X
        leftYAxis.gridColor = Color.parseColor(cor) // Define a cor das linhas de grade do eixo X
        leftYAxis.axisLineColor = Color.parseColor(cor) // Define a cor da linha do eixo X

        barChart.xAxis.setDrawGridLines(false)
        barChart.axisLeft.setDrawGridLines(false)
        barChart.axisRight.setDrawGridLines(false)


        barChart.setDrawGridBackground(false)
        barChart.axisLeft.axisMinimum = 0f
        barChart.axisRight.axisMinimum = 0f
        barChart.xAxis.axisMinimum = 0f

    }

    private suspend fun obterTop5Pontuacoes(colecao: String): List<Pair<String, Long>> {
        return try {
            val query = db.collection(colecao)
                .orderBy("pontuação", Query.Direction.DESCENDING)
                .limit(5)

            val snapshot = query.get().await()

            val topPontuacoes = mutableListOf<Pair<String, Long>>()

            for (document in snapshot.documents) {
                val userId = document.getString("user_id") ?: ""
                val pontuacao = document.getLong("pontuação") ?: 0
                val nomeUsuario = obterNomeDoUsuario(userId) ?: ""

                // Adiciona o par (nome do usuário, pontuação) à lista
                topPontuacoes.add(nomeUsuario to pontuacao)
            }

            topPontuacoes
        } catch (e: Exception) {
            Log.e("Firestore", "Erro ao obter top 5 pontuações: $e")
            emptyList() // Retorna uma lista vazia em caso de erro
        }
    }


    suspend fun obterNomeDoUsuario(documentoId: String): String? {
        return suspendCoroutine { continuation ->
            db.collection("users").document(documentoId).get().addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val nome = documentSnapshot.getString("nome")
                    val apelido = documentSnapshot.getString("apelido")
                    // Faça algo com o nome, como exibir ou retornar
                    val nomeCompleto = nome + " "+ apelido
                    continuation.resume(nomeCompleto)
                } else {
                    // O documento não existe
                    continuation.resumeWith(Result.success("falhou"))
                }
            }.addOnFailureListener { exception ->
                // Tratar falha ao obter o documento
                continuation.resumeWithException(exception)
            }
        }
    }
}