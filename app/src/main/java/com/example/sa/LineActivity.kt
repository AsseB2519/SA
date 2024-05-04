package com.example.sa

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
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
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.properties.Delegates

class LineActivity : AppCompatActivity() {

    lateinit var linelist:ArrayList<Entry>
    lateinit var linelist2:ArrayList<Entry>
    lateinit var lineDataSet1: LineDataSet
    lateinit var lineDataSet2: LineDataSet
    lateinit var lineData: LineData
    var number by Delegates.notNull<Int>()

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
        number = 5
        val colecao = intent.getStringExtra("colecao")

        findViewById<TextView>(R.id.jogo).text = colecao

        // Initialize Firebase Auth
        auth = com.google.firebase.ktx.Firebase.auth

        val userId = auth.uid

        lineChart= findViewById(R.id.linechart)

        if (userId != null) {
            GlobalScope.launch(Dispatchers.Main) {
                val result = colecao?.let { obterPontuacoesPorUsuario(userId, it) }
                result?.let { configurarGrafico(it) }
            }
        } else {
            println("ID do usuário é nulo. Não é possível contar os documentos.")
        }

        //number = findViewById<TextView>(R.id.number).text.toString().toInt() // Supondo que você está dentro de uma atividade ou fragmento
        val editText = findViewById<EditText>(R.id.number)

        editText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                // Aqui você pode acessar o texto digitado no EditText
                val texto = s.toString()

                if (texto.isNotEmpty()){
                    number=texto.toInt()
                    if (number>5){
                        if (userId != null) {
                            GlobalScope.launch(Dispatchers.Main) {
                                val result = colecao?.let { obterPontuacoesPorUsuario(userId, it) }
                                result?.let { configurarGrafico(it) }
                            }
                        } else {
                            println("ID do usuário é nulo. Não é possível contar os documentos.")
                        }
                    }
                }else{
                    number=5

                    if (userId != null) {
                        GlobalScope.launch(Dispatchers.Main) {
                            val result = colecao?.let { obterPontuacoesPorUsuario(userId, it) }
                            result?.let { configurarGrafico(it) }
                        }
                    } else {
                        println("ID do usuário é nulo. Não é possível contar os documentos.")
                    }

                }

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Não é necessário implementar este método, mas é necessário tê-lo devido à interface TextWatcher
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Não é necessário implementar este método, mas é necessário tê-lo devido à interface TextWatcher
            }
        })

    }

    override fun onResume() {
        super.onResume()

        val userId = auth.uid
        val colecao = intent.getStringExtra("colecao")
        if (userId != null) {
            GlobalScope.launch(Dispatchers.Main) {
                val result = colecao?.let { obterPontuacoesPorUsuario(userId, it) }
                result?.let { configurarGrafico(it) }
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
                .limit(number.toLong())

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


    private suspend fun obterPontuacoesPorUsuario(userId: String,colecao: String): LinkedHashMap<Long, Long> {
        val maiorPontuacaoPorColecao = obterMaiorPontuacaoPorColecao(userId,colecao)

        return maiorPontuacaoPorColecao
    }

    private fun configurarGrafico(pontuação: LinkedHashMap<Long, Long>) {
        val cor = "#D6D4CE"
        linelist = ArrayList()
        var position = 0
        var value = 0f
        // Usando um loop for
        for ((timestamp, pontuacao) in pontuação.toList().reversed()) {
            Log.w("Firestore", "${timestamp.toFloat()} : ${pontuacao.toFloat()}")
            linelist.add(Entry(position.toFloat(),pontuacao.toFloat()))
            position++
            value = pontuacao.toFloat()
        }

        lineDataSet1 = LineDataSet(linelist,"Pontos")
        lineDataSet1.color=Color.parseColor("#E63C3A")
        lineDataSet1.setCircleColor(Color.parseColor("#E63C3A")) // Define a cor dos pontos
        lineDataSet1.valueTextColor= Color.parseColor(cor)
        lineDataSet1.color = Color.parseColor("#E63C3A") // Define a cor da linha
        lineDataSet1.valueTextSize=14f
        lineDataSet1.setDrawFilled(false)

        linelist2 = ArrayList()
        linelist2.add(Entry((position-1).toFloat(),value))
        linelist2.add(Entry((position).toFloat(),estimativaProximaPontuacao(pontuação)))

        lineDataSet2 = LineDataSet(linelist2,"Previsão")
        lineDataSet2.color=Color.BLUE
        lineDataSet2.setCircleColor(Color.BLUE) // Define a cor dos pontos
        lineDataSet2.valueTextColor= Color.parseColor(cor)
        lineDataSet2.color = Color.BLUE // Define a cor da linha
        lineDataSet2.valueTextSize=14f
        lineDataSet2.setDrawFilled(false)

        lineData = LineData(lineDataSet1,lineDataSet2)
        lineChart.data=lineData

        setupLineChart(cor)

        lineChart.invalidate() // Atualiza o gráfico


    }

    private fun setupLineChart(cor:String) {
        lineChart.description.isEnabled = true // Desativa a descrição
        lineChart.description.textColor=Color.parseColor(cor)
        lineChart.legend.textColor = Color.parseColor(cor)

        val xAxis: XAxis = lineChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.textColor = Color.parseColor(cor) // Define a cor do texto do eixo X
        xAxis.gridColor = Color.parseColor(cor) // Define a cor das linhas de grade do eixo X
        xAxis.axisLineColor = Color.parseColor(cor) // Define a cor da linha do eixo X
        xAxis.setDrawGridLines(false)
        xAxis.labelCount = lineDataSet1.entryCount
        xAxis.setDrawLabels(true)

        val leftYAxis: YAxis = lineChart.axisLeft
        leftYAxis.setDrawGridLines(false)
        leftYAxis.textColor = Color.parseColor(cor) // Define a cor do texto do eixo X
        leftYAxis.gridColor = Color.parseColor(cor) // Define a cor das linhas de grade do eixo X
        leftYAxis.axisLineColor = Color.parseColor(cor) // Define a cor da linha do eixo X

        val rightYAxis: YAxis = lineChart.axisRight
        rightYAxis.isEnabled = false

        lineChart.setDrawGridBackground(false)
        lineChart.description.isEnabled = false
        lineChart.axisLeft.axisMinimum = 0f
        lineChart.axisRight.axisMinimum = 0f
        lineChart.xAxis.axisMinimum = 0f
        lineChart.animateX(1000)
    }

    fun estimativaProximaPontuacao(pontuação: LinkedHashMap<Long, Long>): Float {

        val pontuacoes = pontuação.values.toList()

        // Se houver menos de 2 pontuações, não podemos fazer uma previsão
        if (pontuacoes.size < 2) {
            return 0f // Retorna 0 como estimativa padrão
        }
        // Calcula a média das pontuações
        val media = pontuacoes.average()

        // Calcula a soma dos quadrados das diferenças em relação à média
        val somaQuadradosDiferencas = pontuacoes.map { (it - media).pow(2) }.sum()

        // Calcula o desvio padrão das pontuações
        val desvioPadrao = sqrt(somaQuadradosDiferencas / pontuacoes.size)

        // Calcula o coeficiente de correlação
        val coeficienteCorrelacao = if (desvioPadrao == 0.0) {
            0.0 // Se o desvio padrão for zero, o coeficiente de correlação é zero
        } else {
            // Calcula a soma dos produtos das diferenças em relação à média
            val somaProdutosDiferencas = pontuacoes.map { (it - media) }.sum()

            // Calcula o coeficiente de correlação
            somaProdutosDiferencas / (pontuacoes.size * desvioPadrao)
        }

        // Calcula o coeficiente angular (slope) da reta de regressão
        val coeficienteAngular = coeficienteCorrelacao * (desvioPadrao / pontuacoes.size)

        // Calcula a interceptação (intercept) da reta de regressão
        val interceptacao = media - (coeficienteAngular * (pontuacoes.size / 2.0))

        // Calcula a próxima pontuação estimada
        val proximaPontuacao = interceptacao + (coeficienteAngular * (pontuacoes.size + 1))

        return proximaPontuacao.toFloat()
    }

}