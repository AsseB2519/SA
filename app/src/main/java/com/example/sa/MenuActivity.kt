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
import android.widget.TextView
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
import com.google.firebase.firestore.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class MenuActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var buttonDrawer: ImageButton
    private lateinit var spinner: Spinner
    private lateinit var pieChart: PieChart
    private val db = com.google.firebase.Firebase.firestore

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

        auth = Firebase.auth

        val head = navView.getHeaderView(0)
        head.findViewById<TextView>(R.id.email).text=auth.currentUser?.email

        val sports = listOf("Line","stats","Radar","Leader")
        
        val currentUser = auth.currentUser

        findViewById<ImageView>(R.id.imageView3).setOnClickListener{
            val intent = Intent(this, BoxActivity::class.java)
            startActivity(intent)
        }

        findViewById<ImageView>(R.id.imageView5).setOnClickListener{
            val intent = Intent(this, JumpActivity::class.java)
            startActivity(intent)
        }

        findViewById<ImageView>(R.id.imageView4).setOnClickListener{
            val intent = Intent(this, ShootActivity::class.java)
            startActivity(intent)
        }

        findViewById<ImageView>(R.id.radar_button).setOnClickListener{
            val intent = Intent(this, RadarActivity::class.java)
            startActivity(intent)
        }

        findViewById<ImageView>(R.id.stats_button).setOnClickListener{
            val intent = Intent(this, StatsActivity::class.java)
            startActivity(intent)
        }

        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {}
                R.id.nav_settings -> {}
                R.id.nav_profile->{
                    val intent = Intent(this, ProfileActivity::class.java)
                    startActivity(intent)
                }
                R.id.nav_logout -> {
                    Toast.makeText(this, "Logout!", Toast.LENGTH_SHORT).show()
                    Firebase.auth.signOut()
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true 
        }

    }
    override fun onResume() {
        super.onResume()

        val head = navView.getHeaderView(0)
        var activity :Int
        val userId = auth.uid

        if (userId != null) {
            GlobalScope.launch(Dispatchers.Main) {
                activity = contarDocumentosPorUsuario(userId)
                findViewById<TextView>(R.id.activities).text=activity.toString()
                val aux = auth.uid
                head.findViewById<TextView>(R.id.nome).text = aux?.let { obterNomeDoUsuario(it) }
                pieChart = findViewById(R.id.pieChart)
                configurarGrafico()
            }
        } else {
            println("ID do usuário é nulo. Não é possível contar os documentos.")
        }

    }


    private suspend fun contarDocumentosCriadosHojeParaUsuario(colecao: String, userId: String): Int {
        return try {
            val hoje = Calendar.getInstance()
            hoje.set(Calendar.HOUR_OF_DAY, 0)
            hoje.set(Calendar.MINUTE, 0)
            hoje.set(Calendar.SECOND, 0)
            hoje.set(Calendar.MILLISECOND, 0)
            val inicioDoDia = hoje.time

            val query = db.collection(colecao)
                .whereEqualTo("user_id", userId)
                .whereGreaterThanOrEqualTo("timestamp", inicioDoDia)

            val snapshot = query.get().await()

            for (document in snapshot.documents) {
                Log.d("Firestore", "ID do documento: ${document.id} Coleção:$colecao")
            }

            snapshot.size()
        } catch (e: Exception) {
            Log.e("Firestore", "Erro ao contar documentos: $e")
            0
        }
    }

    private suspend fun contarDocumentosPorUserHoje(userId: String): Int{
        val colecoes = listOf("Box", "Jump", "Shoot")
        var res = 0
        for (colecao in colecoes) {
            res += contarDocumentosCriadosHojeParaUsuario(colecao, userId).toInt()
        }

        return res
    }


    private suspend fun configurarGrafico() {

        var percentReached = 0f
        percentReached = auth.uid?.let { contarDocumentosPorUserHoje(it).toFloat() }!!
        val goal = 10f // Objetivo

        if (percentReached>goal) {
            findViewById<TextView>(R.id.textView6).text =
                "Congratulations, you achieved your daily goal"
            percentReached=goal
        }else{
            findViewById<TextView>(R.id.textView6).text =
                "You did ${percentReached.toInt()} of your 10 activities goal"
        }

        val percentRemaining = goal - percentReached

        val entries = ArrayList<PieEntry>()
        entries.add(PieEntry(percentReached, "Alcançado"))
        entries.add(PieEntry(percentRemaining, "Restante"))

        val dataSet = PieDataSet(entries, "Progresso")

        dataSet.colors = listOf(Color.parseColor("#E63C3A"), Color.parseColor("#696969"))
        dataSet.valueTextColor = Color.WHITE
        dataSet.setDrawValues(false)

        val pieData = PieData(dataSet)

        pieChart.data = pieData
        pieChart.description.isEnabled = false
        pieChart.legend.isEnabled = false
        pieChart.centerText = "${percentReached*100/goal}%"
        pieChart.setHoleColor(Color.TRANSPARENT)
        pieChart.setDrawEntryLabels(false)
        pieChart.setTransparentCircleColor(Color.TRANSPARENT)
        pieChart.setCenterTextColor(Color.WHITE)
        pieChart.invalidate()
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

    private suspend fun contarDocumentosPorUsuario(userId: String): Int{
        val colecoes = listOf("Box", "Jump", "Shoot")
        val contagens = mutableMapOf<String, Long>()
        var res = 0
        for (colecao in colecoes) {
            contagens[colecao] = contarDocumentosPorColecao(userId, colecao)
            res += contarDocumentosPorColecao(userId, colecao).toInt()
        }

        return res
    }

    suspend fun obterNomeDoUsuario(documentoId: String): String? {
        return suspendCoroutine { continuation ->
            db.collection("users").document(documentoId).get().addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val nome = documentSnapshot.getString("nome")
                    val apelido = documentSnapshot.getString("apelido")
                    val nomeCompleto = nome + " "+ apelido
                    continuation.resume(nomeCompleto)
                } else {
                    continuation.resumeWith(Result.success("falhou"))
                }
            }.addOnFailureListener { exception ->
                continuation.resumeWithException(exception)
            }
        }
    }
}
