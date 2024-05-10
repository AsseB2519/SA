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
import androidx.core.content.ContextCompat
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
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

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

        auth = com.google.firebase.ktx.Firebase.auth

        val userId = auth.uid

        radarChart = findViewById(R.id.radar_chart)
        spinner = findViewById(R.id.spinner)


        if (userId != null) {
            GlobalScope.launch(Dispatchers.Main) {
                val result = obterMaiorPontuacaoPorUsuario(userId)
                configurarGrafico(result)
                aux()
            }
        } else {
            println("ID do usuário é nulo. Não é possível contar os documentos.")
        }


    }

    private suspend fun aux(){

        val documentos = buscarDocumentosUsuarios()

        val mapNomeApelidoParaId = documentos.associate { (id, info) ->
            val nome = info["nome"] ?: ""
            val apelido = info["apelido"] ?: ""
            "$nome $apelido" to id
        }

        var opcoes = mutableListOf("None").apply {
            addAll(mapNomeApelidoParaId.keys)
        }

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, opcoes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spinner.adapter = adapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

                val itemSelecionado = opcoes[position]

                if (itemSelecionado != "None") {

                    GlobalScope.launch(Dispatchers.Main) {
                        val cor = "#91908D"

                        var pontuação = mapNomeApelidoParaId[itemSelecionado]?.let {
                            obterMaiorPontuacaoPorUsuario(
                                it
                            )
                        }

                        val entries1 = ArrayList<RadarEntry>()
                        entries1.add(RadarEntry(pontuação?.get("Box")?.toFloat()?:0f))
                        entries1.add(RadarEntry(pontuação?.get("Jump")?.toFloat()?:0f))
                        entries1.add(RadarEntry(pontuação?.get("Shoot")?.toFloat()?:0f))

                        val dataSet2 = RadarDataSet(entries1, itemSelecionado)
                        dataSet2.color = Color.BLUE
                        dataSet2.valueTextColor = Color.parseColor(cor)
                        dataSet2.valueTextSize = 12f
                        dataSet2.setDrawFilled(true)
                        dataSet2.setFillColor(Color.BLUE)

                        radarChart.data.addDataSet(dataSet2)
                        radarChart.data.notifyDataChanged()
                        radarChart.notifyDataSetChanged()
                        radarChart.invalidate()
                    }
                }
                else{
                    for (i in  radarChart.data.dataSetCount - 1 downTo 1) {
                        val dataSetToRemove =  radarChart.data.getDataSetByIndex(i)
                        radarChart.data.removeDataSet(dataSetToRemove)
                    }
                    radarChart.data.notifyDataChanged()
                    radarChart.notifyDataSetChanged()
                    setupRadarChart("#91908D")

                    radarChart.invalidate()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
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

            documento?.getLong("pontuação") ?: 0
        } catch (e: Exception) {
            Log.e("Firestore", "Erro ao obter documentos: $e")
            0
        }
    }

    private suspend fun obterMaiorPontuacaoPorUsuario(userId: String): Map<String, Long> {
        val colecoes = listOf("Box", "Jump", "Shoot")
        val maiorPontuacaoPorColecao = mutableMapOf<String, Long>()

        for (colecao in colecoes) {
            val maiorPontuacao = obterMaiorPontuacaoPorColecao(userId, colecao)
            maiorPontuacaoPorColecao[colecao] = maiorPontuacao
        }

        return maiorPontuacaoPorColecao
    }

    private fun configurarGrafico(pontuação: Map<String, Long>) {

        val cor = "#91908D"
        val entries1 = ArrayList<RadarEntry>()
        entries1.add(RadarEntry(pontuação["Box"]?.toFloat()?:0f))
        entries1.add(RadarEntry(pontuação["Jump"]?.toFloat()?:0f))
        entries1.add(RadarEntry(pontuação["Shoot"]?.toFloat()?:0f))

        val dataSet1 = RadarDataSet(entries1, "You")
        dataSet1.color = Color.RED
        dataSet1.valueTextColor = Color.parseColor(cor)
        dataSet1.valueTextSize = 12f
        dataSet1.setDrawFilled(true)
        dataSet1.setFillColor(Color.RED)

        radarChart.data = RadarData(dataSet1)

        setupRadarChart(cor)

        radarChart.invalidate()
    }

    private fun setupRadarChart(cor:String) {
        val labels = arrayOf("Box", "Jump", "Shoot")

        radarChart.description.textColor=Color.parseColor(cor)
        radarChart.legend.textColor = Color.parseColor(cor)
        radarChart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        radarChart.xAxis.textColor = Color.parseColor(cor)
        radarChart.xAxis.textSize = 16f
        radarChart.description.isEnabled = false
        radarChart.webLineWidth = 2f
        radarChart.webColor = Color.parseColor(cor)
        radarChart.webLineWidthInner = 2f
        radarChart.webColorInner = Color.parseColor(cor)
        radarChart.webAlpha = 100
        radarChart.yAxis.isEnabled=false
        radarChart.animateXY(1000,1000)
        radarChart.yAxis.setLabelCount(10, true);

    }

    suspend fun buscarDocumentosUsuarios(): List<Pair<String, Map<String, Any>>> {
        val query = db.collection("users")

        val snapshot = query.get().await()

        val documentosUsuarios = mutableListOf<Pair<String, Map<String, Any>>>()

        for (document in snapshot.documents) {
            val idDocumento = document.id
            val dadosDocumento = document.data ?: emptyMap()
            documentosUsuarios.add(Pair(idDocumento, dadosDocumento))
        }

        return documentosUsuarios
    }

}
