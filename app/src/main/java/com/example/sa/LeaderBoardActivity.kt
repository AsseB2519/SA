package com.example.sa

import android.graphics.Color
import android.os.Bundle
import android.util.Log
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
import com.github.mikephil.charting.utils.ColorTemplate

class LeaderBoardActivity : AppCompatActivity() {
    lateinit var barChart: BarChart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_leader_board)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        barChart = findViewById(R.id.bar_chart)

        configurarGrafico()

    }

    private fun configurarGrafico() {

        // Dados de exemplo (pontuações de jogadores)
        val scores = listOf(100, 120, 90, 110, 80)

        // Entradas de dados
        val entries = ArrayList<BarEntry>()
        scores.forEachIndexed { index, score ->
            entries.add(BarEntry((index+1).toFloat(), score.toFloat()))
        }

        // Conjunto de dados de barras
        val bardataSet = BarDataSet(entries, "Pontuações dos Jogadores")
        bardataSet.color = Color.BLUE // Cor das barras

        // Configuração dos dados do gráfico de barras
        val barData = BarData(bardataSet)
        barData.barWidth = 0.5f // Largura das barras

        // Configuração do gráfico
        barChart.data = barData
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
        bardataSet.valueTextColor = Color.BLACK

        // definindo o tamanho do texto
        bardataSet.valueTextSize = 16f

        setupBarChart()

        barChart.invalidate() // Atualiza o gráfico


    }


    private fun setupBarChart() {
        barChart.description.isEnabled = false
        barChart.legend.isEnabled = false

        val xAxis: XAxis = barChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.setDrawLabels(true)

        val rightYAxis = barChart.axisRight

        barChart.xAxis.setDrawGridLines(false)
        barChart.axisLeft.setDrawGridLines(false)
        barChart.axisRight.setDrawGridLines(false)

        rightYAxis.isEnabled = false
        barChart.setDrawGridBackground(false)

        barChart.axisLeft.axisMinimum = 0f
        barChart.axisRight.axisMinimum = 0f
        barChart.xAxis.axisMinimum = 0f

    }
}