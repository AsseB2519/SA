package com.example.sa

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.data.RadarDataSet
import com.github.mikephil.charting.data.RadarEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.material.internal.NavigationMenuItemView
import com.google.android.material.navigation.NavigationBarItemView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Calendar

class MenuActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var buttonDrawer: ImageButton
    private lateinit var spinner: Spinner
    private lateinit var pieChart: PieChart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu2)

        drawerLayout=findViewById(R.id.drawerLayout)
        buttonDrawer=findViewById(R.id.buttonDrawer)
        navView=findViewById(R.id.navigation)

        buttonDrawer.setOnClickListener{
            Log.d("BUTTON", "Stop button pressed!")
            drawerLayout.open()
        }


        // Initialize Firebase Auth
        auth = Firebase.auth
        pieChart = findViewById(R.id.pieChart)
        configurarGrafico()

        val sports = listOf("Line","stats","Radar","Leader")
        
        val autoComplete : AutoCompleteTextView=findViewById(R.id.auto_complete)
        
        val adapter = ArrayAdapter(this,R.layout.list_item,sports)
        
        autoComplete.setAdapter(adapter)
        
        autoComplete.onItemClickListener = AdapterView.OnItemClickListener{
                adapterView: AdapterView<*>?, view: View?, position: Int, id: Long ->

            val itemSelected = adapterView?.getItemAtPosition(position)

            if (itemSelected=="Line") {
                val intent = Intent(this, LineActivity::class.java)
                startActivity(intent)
            }
            else if(itemSelected=="stats"){
                val intent = Intent(this, StatsActivity::class.java)
                startActivity(intent)
            }
            else if(itemSelected=="Radar"){
                val intent = Intent(this, RadarActivity::class.java)
                startActivity(intent)
            }
            else if(itemSelected=="Leader"){
                val intent = Intent(this, LeaderBoardActivity::class.java)
                startActivity(intent)
            }
        }


        val currentUser = auth.currentUser

        findViewById<ImageView>(R.id.imageView3).setOnClickListener{
            Log.d("BUTTON", "Stop button pressed!")
            val intent = Intent(this, BoxActivity::class.java)
            startActivity(intent)
        }

        findViewById<ImageView>(R.id.imageView5).setOnClickListener{
            Log.d("BUTTON", "Siga saltar")
            val intent = Intent(this, JumpActivity::class.java)
            startActivity(intent)
        }

        findViewById<ImageView>(R.id.imageView4).setOnClickListener{
            Log.d("BUTTON", "Siga saltar")
            val intent = Intent(this, LongJumpActivity::class.java)
            startActivity(intent)
        }

        findViewById<ImageView>(R.id.imageView6).setOnClickListener{
            Log.d("BUTTON", "Siga saltar")
            val intent = Intent(this, ShootActivity::class.java)
            startActivity(intent)
        }

        // Configurar o listener de seleção de itens de navegação
        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    //supportFragmentManager.beginTransaction().replace(R.id.fragment_container, HomeFragment()).commit()
                }
                R.id.nav_settings -> {
                    //supportFragmentManager.beginTransaction().replace(R.id.fragment_container, SettingsFragment()).commit()
                }
                R.id.nav_logout -> {
                    Toast.makeText(this, "Logout!", Toast.LENGTH_SHORT).show()
                    // Coloque aqui a lógica para realizar o logout
                    Firebase.auth.signOut()
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                }
            }
            // Fechar o DrawerLayout após selecionar um item
            drawerLayout.closeDrawer(GravityCompat.START)
            true // Indica que o evento de clique foi consumido
        }

    }

    suspend fun contarDocumentosDoDia(colecao: String): Int {
        val db = FirebaseFirestore.getInstance()
        val hoje = Calendar.getInstance()
        hoje.set(Calendar.HOUR_OF_DAY, 0)
        hoje.set(Calendar.MINUTE, 0)
        hoje.set(Calendar.SECOND, 0)
        hoje.set(Calendar.MILLISECOND, 0)

        val inicioDoDia = Timestamp(hoje.time)

        val amanha = Calendar.getInstance()
        amanha.add(Calendar.DAY_OF_MONTH, 1)
        amanha.set(Calendar.HOUR_OF_DAY, 0)
        amanha.set(Calendar.MINUTE, 0)
        amanha.set(Calendar.SECOND, 0)
        amanha.set(Calendar.MILLISECOND, 0)

        val inicioDoDiaAmanha = Timestamp(amanha.time)

        val query = db.collection(colecao)
            .whereGreaterThanOrEqualTo("timestamp", inicioDoDia)
            .whereLessThan("timestamp", inicioDoDiaAmanha)

        val snapshot = query.get().await()
        return snapshot.size()
    }

    private fun configurarGrafico() {

        // Porcentagem atingida e objetivo (valores de exemplo)
        val percentReached = 75f // Porcentagem atingida
        val goal = 100f // Objetivo

        // Calcula a porcentagem restante
        val percentRemaining = goal - percentReached

        // Entradas de dados para o PieChart
        val entries = ArrayList<PieEntry>()
        entries.add(PieEntry(percentReached, "Alcançado")) // Adiciona a porcentagem atingida como uma fatia
        entries.add(PieEntry(percentRemaining, "Restante")) // Adiciona a porcentagem restante como outra fatia

        // Conjunto de dados do PieChart
        val dataSet = PieDataSet(entries, "Progresso")

        // Cores para as fatias do PieChart
        dataSet.colors = listOf(Color.parseColor("#F70316"), Color.parseColor("#5E5D83"))
        dataSet.valueTextColor = Color.WHITE
        dataSet.setDrawValues(false)

        // Configuração dos dados do PieChart
        val pieData = PieData(dataSet)

        // Configuração do PieChart
        pieChart.data = pieData
        pieChart.description.isEnabled = false // Desativa a descrição
        pieChart.legend.isEnabled = false // Desativa a legenda
        pieChart.centerText = "$percentReached%" // Texto central exibindo a porcentagem atingida
        pieChart.setHoleColor(Color.TRANSPARENT)
        pieChart.setDrawEntryLabels(false)
        pieChart.setTransparentCircleColor(Color.TRANSPARENT) // Cor da borda do buraco central (transparente para não mostrar)
        pieChart.setCenterTextColor(Color.WHITE)

        pieChart.invalidate() // Atualiza o gráfico
    }

}