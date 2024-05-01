package com.example.sa

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.github.mikephil.charting.charts.RadarChart
import com.github.mikephil.charting.data.RadarData
import com.github.mikephil.charting.data.RadarDataSet
import com.github.mikephil.charting.data.RadarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class RadarActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val db = Firebase.firestore
    private lateinit var radarChart:RadarChart
    private lateinit var spinner: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_radar)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // Initialize Firebase Auth
        auth = com.google.firebase.ktx.Firebase.auth

        val userId = auth.uid

        radarChart = findViewById(R.id.radar_chart)
        spinner = findViewById(R.id.spinner)

        // Lista de opções para o Spinner
        val opcoes = listOf("Z3J7ndiZR5TzVddgICaassEXDvm1", "Z3J7ndiZR5TzVddgICaassEXDvm1", "Z3J7ndiZR5TzVddgICaassEXDvm1")

        // Criar um adaptador para o Spinner
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, opcoes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // Definir o adaptador para o Spinner
        spinner.adapter = adapter

        if (userId != null) {
            GlobalScope.launch(Dispatchers.Main) {
                val result = obterMaiorPontuacaoPorUsuario(userId)
                configurarGrafico(result)
            }
        } else {
            println("ID do usuário é nulo. Não é possível contar os documentos.")
        }

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                // Faça algo com o item selecionado, por exemplo:
                val itemSelecionado = opcoes[position]

                val entries1 = ArrayList<RadarEntry>()
                entries1.add(RadarEntry(20f))
                entries1.add(RadarEntry(0f))
                entries1.add(RadarEntry(30f))

                val dataSet2 = RadarDataSet(entries1, "222")
                dataSet2.color = Color.BLUE
                dataSet2.valueTextColor = Color.WHITE
                dataSet2.valueTextSize = 12f
                /*
                radarChart.data.addDataSet(dataSet2)

                radarChart.data.notifyDataChanged()
                */
                // Recrie o gráfico
                radarChart.notifyDataSetChanged()

            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Método necessário, mas pode ser deixado em branco
            }
        }

    }

    private suspend fun obterMaiorPontuacaoPorColecao(userId: String, colecao: String): Long {
        return try {
            val query = db.collection(colecao)
                .whereEqualTo("user_id", userId)
                .orderBy("pontuação", Query.Direction.DESCENDING)
                .limit(1)

            val snapshot = query.get().await()
            val documento = snapshot.documents.firstOrNull()

            documento?.getLong("pontuação") ?: 0 // Se não houver pontuação, retorna 0
        } catch (e: Exception) {
            Log.e("Firestore", "Erro ao obter documentos: $e")
            0
        }
    }

    private suspend fun obterMaiorPontuacaoPorUsuario(userId: String): Map<String, Long> {
        val colecoes = listOf("Box", "Jump", "Shoot") // Substitua pelos nomes reais das suas coleções
        val maiorPontuacaoPorColecao = mutableMapOf<String, Long>()

        for (colecao in colecoes) {
            val maiorPontuacao = obterMaiorPontuacaoPorColecao(userId, colecao)
            maiorPontuacaoPorColecao[colecao] = maiorPontuacao
        }

        return maiorPontuacaoPorColecao
    }

    private fun configurarGrafico(pontuação: Map<String, Long>) {

        // Sample data
        val entries1 = ArrayList<RadarEntry>()
        entries1.add(RadarEntry(pontuação["Box"]?.toFloat()?:0f))
        entries1.add(RadarEntry(pontuação["Jump"]?.toFloat()?:0f))
        entries1.add(RadarEntry(pontuação["Shoot"]?.toFloat()?:0f))

        val dataSet1 = RadarDataSet(entries1, "${auth.currentUser?.email}")
        dataSet1.color = Color.RED
        dataSet1.valueTextColor = Color.WHITE
        dataSet1.valueTextSize = 12f

        setupRadarChart()

        val data = RadarData(dataSet1)

        radarChart.data = data

        radarChart.invalidate() // Atualiza o gráfico
    }

    private fun setupRadarChart() {
        val labels = arrayOf("Box", "Jump", "Shoot")
        radarChart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        radarChart.xAxis.textColor = Color.WHITE
        radarChart.xAxis.textSize = 16f
        radarChart.description.isEnabled = false // Desativa a descrição
        radarChart.webLineWidth = 1f // Largura das linhas do gráfico
        radarChart.webColor = Color.WHITE // Cor das linhas do gráfico
        radarChart.webLineWidthInner = 1f // Largura das linhas internas do gráfico
        radarChart.webColorInner = Color.WHITE // Cor das linhas internas do gráfico
        radarChart.webAlpha = 100 // Transparência das linhas do gráfico
        radarChart.yAxis.isEnabled=false

    }
}