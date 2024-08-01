package com.gestordatos.pantallas

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.gestordatos.R
import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.gestordatos.BBDD.AppDatabase
import com.gestordatos.BBDD.IncidentDao
import com.gestordatos.BBDD.IncidentEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import kotlin.coroutines.resume

class ReadFileActivity : AppCompatActivity() {

    private lateinit var textView: TextView
    private lateinit var db: AppDatabase
    private lateinit var incidentDao: IncidentDao

    private val pickFile = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { readTextFile(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_read_file)

        db = AppDatabase.getDatabase(applicationContext)
        incidentDao = db.incidentDao()

        textView = findViewById(R.id.textView)
        val buttonPickFile: Button = findViewById(R.id.buttonPickFile)

        buttonPickFile.setOnClickListener {
            pickFile.launch("text/plain")
        }
    }

    private fun readTextFile(uri: Uri) {
        lifecycleScope.launch(Dispatchers.IO) {
            val content = contentResolver.openInputStream(uri)?.bufferedReader().use { it?.readText() } ?: ""
            val incidents = parseIncidents(content)
            insertIncidents(incidents)
        }
        Toast.makeText(this@ReadFileActivity, "La información ha sido almacenada", Toast.LENGTH_LONG).show()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun limpiezaTexto(texto: String): String {
        var limpio = texto.replace("\t"," ")
        limpio = limpio.replace("&nbsp"," ")
        limpio = limpio.replace(",","")
        return limpio
    }

    private fun establecerValor(linea: String, valor: String): String{
        when{
            linea.startsWith("Nº Incidencia:") -> {
                return valor.ifEmpty { "SIN Nº INCIDENCIA" }
            }
            linea.startsWith("Proyecto:") -> {
                return valor.ifEmpty { "SIN PROYECTO" }
            }
            linea.startsWith("Nombre cliente:") -> {
                return valor.ifEmpty { "SIN NOMBRE CLIENTE" }
            }
            linea.startsWith("Teléfono 1:") -> {
                return valor.ifEmpty { "SIN TELEFONO 1" }
            }
            linea.startsWith("Teléfono 2:") -> {
                return valor.ifEmpty { "SIN TELEFONO 2" }
            }
            linea.startsWith("Domicilio:") -> {
                return valor.ifEmpty { "SIN DOMICILIO" }
            }
            linea.startsWith("Población:") -> {
                return valor.ifEmpty { "SIN POBLACION" }
            }
            linea.startsWith("Provincia:") -> {
                return valor.ifEmpty { "SIN PROVINCIA" }
            }
            linea.startsWith("Zona O&M:") -> {
                return valor.ifEmpty { "SIN ZONA O&M" }
            }
            linea.startsWith("Hora inicio:") -> {
                return valor.ifEmpty { "SIN HORA INICIO" }
            }
            linea.startsWith("Duración:") -> {
                return valor.ifEmpty { "SIN DURACION" }
            }
            linea.startsWith("Comentarios:") -> {
                return valor.ifEmpty { "SIN COMENTARIOS" }
            }
            linea.startsWith("Pedido:") -> {
                return valor.ifEmpty { "SIN PEDIDO" }
            }
            linea.startsWith("Planos:") -> {
                return valor.ifEmpty { "SIN PLANOS" }
            }
            linea.startsWith("Soporte:") -> {
                return valor.ifEmpty { "SIN SOPORTE" }
            }
        }
        return "NADA"
    }

    private fun parseIncidents(content: String): List<IncidentEntity> {
        val textoLimpio = limpiezaTexto(content)
        val incidents = mutableListOf<IncidentEntity>()
        val lines = textoLimpio.split("\n")
        val currentMap = mutableMapOf<String, String>()

        for (line in lines) {
            when {
                line.startsWith("Nº Incidencia:") -> {
                    if (currentMap.isNotEmpty()) {
                        incidents.add(createIncidentFromMap(currentMap))
                        currentMap.clear()
                    }
                    currentMap["numeroIncidencia"] = establecerValor(line, line.substringAfter(":").trim())
                }
                line.startsWith("Proyecto:") -> currentMap["proyecto"] = establecerValor(line, line.substringAfter(":").trim())
                line.startsWith("Nombre cliente:") -> currentMap["nombreCliente"] = establecerValor(line, line.substringAfter(":").trim())
                line.startsWith("Teléfono 1:") -> currentMap["telefono1"] = establecerValor(line, line.substringAfter(":").trim())
                line.startsWith("Teléfono 2:") -> currentMap["telefono2"] = establecerValor(line, line.substringAfter(":").trim())
                line.startsWith("Domicilio:") -> currentMap["domicilio"] = establecerValor(line, line.substringAfter(":").trim())
                line.startsWith("Población:") -> currentMap["poblacion"] = establecerValor(line, line.substringAfter(":").trim()).uppercase()
                line.startsWith("Provincia:") -> currentMap["provincia"] = establecerValor(line, line.substringAfter(":").trim()).uppercase()
                line.startsWith("Zona O&M:") -> currentMap["zonaOM"] = establecerValor(line, line.substringAfter(":").trim())
                line.startsWith("Hora inicio:") -> currentMap["horaInicio"] = establecerValor(line, line.substringAfter(":").trim())
                line.startsWith("Duración:") -> currentMap["duracion"] = establecerValor(line, line.substringAfter(":").trim())
                line.startsWith("Comentarios:") -> currentMap["comentarios"] = establecerValor(line, line.substringAfter(":").trim())
                line.startsWith("Pedido:") -> currentMap["pedido"] = establecerValor(line, line.substringAfter(":").trim())
                line.startsWith("Planos:") -> currentMap["planos"] = establecerValor(line, line.substringAfter(":").trim())
                line.startsWith("Soporte:") -> {currentMap["soporte"] = establecerValor(line, line.substringAfter(":").trim())
                    incidents.add(createIncidentFromMap(currentMap))
                    currentMap.clear()
                }
            }
        }

        if (currentMap.isNotEmpty()) {
            incidents.add(createIncidentFromMap(currentMap))
        }

        return incidents
    }

    private fun createIncidentFromMap(map: Map<String, String>): IncidentEntity {
        return IncidentEntity(
            numeroIncidencia = map["numeroIncidencia"] ?: "",
            proyecto = map["proyecto"] ?: "",
            nombreCliente = map["nombreCliente"] ?: "",
            telefono1 = map["telefono1"] ?: "",
            telefono2 = map["telefono2"] ?: "",
            domicilio = map["domicilio"] ?: "",
            poblacion = map["poblacion"] ?: "",
            provincia = map["provincia"] ?: "",
            zonaOM = map["zonaOM"] ?: "",
            horaInicio = map["horaInicio"] ?: "",
            duracion = map["duracion"] ?: "",
            comentarios = map["comentarios"] ?: "",
            pedido = map["pedido"] ?: "",
            planos = map["planos"] ?: "",
            soporte = map["soporte"] ?: "",
            origen = "FICHERO",
            fechaCreacion = System.currentTimeMillis()
        )
    }

    private suspend fun insertIncidents(incidents: List<IncidentEntity>) {
        incidents.forEach { incident ->
            incidentDao.insert(incident)
        }
    }
}
