package com.example.sa

import android.R.attr
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.anychart.AnyChart
import com.anychart.AnyChartView
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await



class StatsActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val db = Firebase.firestore
    private lateinit var pieChart: PieChart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_stats)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = com.google.firebase.ktx.Firebase.auth

        val userId = auth.uid

        val contagens = mutableMapOf<String, Long>()
        pieChart = findViewById(R.id.pieChart);
        
        if (userId != null) {
            GlobalScope.launch(Dispatchers.Main) {
                val result = contarDocumentosPorUsuario(userId)
                contagens.putAll(result)
                configurarGrafico(contagens)
            }
        } else {
            println("ID do usuário é nulo. Não é possível contar os documentos.")
        }

    }

    private suspend fun contarDocumentosPorColecao(userId: String, colecao: String): Long {
        return try {
            val query = db.collection(colecao)
                .whereEqualTo("user_id", userId)
            val snapshot = query.get().await()
            snapshot.size().toLong()
        } catch (e: Exception) {
            println("Erro ao contar documentos: ${e.message}")
            0
        }
    }

    private suspend fun contarDocumentosPorUsuario(userId: String): Map<String, Long> {
        val colecoes = listOf("Box", "Jump", "Shoot")
        val contagens = mutableMapOf<String, Long>()

        for (colecao in colecoes) {
            contagens[colecao] = contarDocumentosPorColecao(userId, colecao)
        }

        return contagens
    }

    private fun configurarGrafico(contagens: Map<String, Long>) {

        val dados = ArrayList<PieEntry>()
        contagens.forEach { (colecao, quantidade) ->
            dados.add(PieEntry(quantidade.toFloat(), colecao))
        }

        val dataSet = PieDataSet(dados, "Pie Chart")
        dataSet.setDrawValues(true)
        dataSet.setValueTextSize(18f)
        val customColors = listOf(Color.parseColor("#F70316"), Color.parseColor("#91908D"),
            Color.parseColor("#5E5D83"))
        dataSet.colors = customColors
        setupPieChart();

        val pieData = PieData(dataSet)
        pieChart.data = pieData

        pieChart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry?, h: Highlight?) {
                if (e == null) return
                val valorAbsoluto = e.y.toInt()
                val label = (e as PieEntry).label
                val mensagem = "You did : $valorAbsoluto $label"
                Toast.makeText(applicationContext, mensagem, Toast.LENGTH_SHORT).show()
            }

            override fun onNothingSelected() {}
        })

        pieChart.invalidate()
    }

    private fun setupPieChart() {
        pieChart.setDrawHoleEnabled(true)
        pieChart.setHoleColor(Color.TRANSPARENT)
        pieChart.setCenterTextColor(Color.WHITE)
        pieChart.setTransparentCircleColor(Color.TRANSPARENT)
        pieChart.setUsePercentValues(true)
        pieChart.setCenterTextSize(18f);
        pieChart.setEntryLabelColor(Color.BLACK)
        pieChart.setEntryLabelTextSize(24f)
        pieChart.setCenterText("Sports Statistics")
        pieChart.getDescription().setEnabled(false)
        pieChart.getLegend().setEnabled(false)
        pieChart.animateY(1000)
    }

}
