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
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.collection.emptyLongSet
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

    private var contadorIncidencias = 0 //fichero con nombre MANTENIMIENTO
    private var contadorDirObra = 0 //fichero con nombre _chat.txt
    private var contadorCentroTrabajo = 0 //fichero con nombre Grupo 3 fichero pequeño
    //el fichero grupo 3 grande es cuando no es ninguno de los otros 3

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
            linea.startsWith("Telefono:") -> {
                return valor.ifEmpty { "SIN TELEFONO" }
            }
            linea.startsWith("Teléfono 1:") -> {
                return valor.ifEmpty { "SIN TELEFONO 1" }
            }
            linea.startsWith("Teléfono 2:") -> {
                return valor.ifEmpty { "SIN TELEFONO 2" }
            }
            linea.contains(" de obra:") -> {
                return valor.ifEmpty { "SIN DOMICILIO" }
            }
            linea.startsWith("Domicilio:") -> {
                return valor.ifEmpty { "SIN DOMICILIO" }
            }
            linea.startsWith("Centro de trabajo:") -> {
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
            linea.startsWith("Fecha:") -> {
                return valor.ifEmpty { "SIN FECHA" }
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
        //Añadimos mas logica para intentar reconocer los distintos tipos de fichero que podemos leer.
        //Primero leeremos el fichero para intentar averiguar que tipo de fichero es
        //Segundo en funcion del tipo de fichero buscaremos unos campos u otros.
        //Para esto, tendremos que leer minimo 2 veces el fichero

        //inicializamos los contadores a 0
        contadorIncidencias = 0
        contadorDirObra = 0
        contadorCentroTrabajo = 0
        for (line in lines) {
            if(line.startsWith("Nº Incidencia:"))
                contadorIncidencias++
            else if(line.contains("de obra:"))
                contadorDirObra++
            else if(line.startsWith("Centro de trabajo:"))
                contadorCentroTrabajo++
        }


        if(contadorIncidencias > 20) {
            for (line in lines) {
                when {
                    line.startsWith("Nº Incidencia:") -> {
                        if (currentMap.isNotEmpty()) {
                            incidents.add(createIncidentFromMap(currentMap))
                            currentMap.clear()
                        }
                        currentMap["numeroIncidencia"] =
                            establecerValor(line, line.substringAfter(":").trim())
                    }

                    line.startsWith("Proyecto:") -> currentMap["proyecto"] =
                        establecerValor(line, line.substringAfter(":").trim())

                    line.startsWith("Nombre cliente:") -> currentMap["nombreCliente"] =
                        establecerValor(line, line.substringAfter(":").trim())

                    line.startsWith("Teléfono 1:") -> currentMap["telefono1"] =
                        establecerValor(line, line.substringAfter(":").trim())

                    line.startsWith("Teléfono 2:") -> currentMap["telefono2"] =
                        establecerValor(line, line.substringAfter(":").trim())

                    line.startsWith("Domicilio:") -> currentMap["domicilio"] =
                        establecerValor(line, line.substringAfter(":").trim())

                    line.startsWith("Población:") -> currentMap["poblacion"] =
                        establecerValor(line, line.substringAfter(":").trim()).uppercase()

                    line.startsWith("Provincia:") -> currentMap["provincia"] =
                        establecerValor(line, line.substringAfter(":").trim()).uppercase()

                    line.startsWith("Zona O&M:") -> currentMap["zonaOM"] =
                        establecerValor(line, line.substringAfter(":").trim())

                    line.startsWith("Hora inicio:") -> currentMap["horaInicio"] =
                        establecerValor(line, line.substringAfter(":").trim())

                    line.startsWith("Duración:") -> currentMap["duracion"] =
                        establecerValor(line, line.substringAfter(":").trim())

                    line.startsWith("Comentarios:") -> currentMap["comentarios"] =
                        establecerValor(line, line.substringAfter(":").trim())

                    line.startsWith("Pedido:") -> currentMap["pedido"] =
                        establecerValor(line, line.substringAfter(":").trim())

                    line.startsWith("Planos:") -> currentMap["planos"] =
                        establecerValor(line, line.substringAfter(":").trim())

                    line.startsWith("Soporte:") -> {
                        currentMap["soporte"] =
                            establecerValor(line, line.substringAfter(":").trim())
                        incidents.add(createIncidentFromMap(currentMap))
                        currentMap.clear()
                    }
                }
            }

            if (currentMap.isNotEmpty()) {
                incidents.add(createIncidentFromMap(currentMap))
            }
        }
        else if(contadorDirObra > 10) {
            var auxDir = false
            var auxDirCentro = false
            var auxZona = false
            var poblacion = ""
            var provincia = ""
            //var poblacionYProvincia = ""
            for (line in lines) {
                if(auxDir)
                {
                    //poblacionYProvincia = line.split(" - ")[1].trim()
                    if(!line.isNullOrEmpty()) {
                        Log.d("PRUEBA", "la fila de error en el if es la: $line")
                        if(line.contains("(")) {
                            poblacion = line.split("(")[0].substringAfter("-").trim()
                            provincia = line.split("(")[1].substringBefore(")").trim()
                            currentMap["poblacion"] = poblacion.uppercase()
                            currentMap["provincia"] = provincia.uppercase()
                        }
                    }
                }
                else if (auxDirCentro && !line.startsWith("Hora:"))
                {
                    if(!line.isNullOrEmpty()) {
                        Log.d("PRUEBA", "la fila de error en el else if es la: $line")
                        if(line.contains("(")) {
                            poblacion = line.split("(")[0].substringAfter("-").trim()
                            provincia = line.split("(")[1].substringBefore(")").trim()
                            currentMap["poblacion"] = poblacion.uppercase()
                            currentMap["provincia"] = provincia.uppercase()
                        }
                    }
                }
                else
                {
                    poblacion = ""
                    provincia = ""
                }
                if(auxZona && line.startsWith("Centro de trabajo:"))
                {
                    var domi = ""
                    var prov = ""
                    var pobl = ""
                    if(establecerValor(line, line.substringAfter(":")).contains(" - "))
                    {

                        val tamaño = establecerValor(line, line.substringAfter(":")).split(",").size
                        domi = establecerValor(line, line.substringAfter(":")).split(",")[0].trim() // lo que hay despues de los 2 puntos y antes de la coma
                        if (tamaño > 1) { //si el valor de despues de la coma es nulo quiere decir que esta en la fila siguiente
                            pobl = establecerValor(line,line.substringAfter(":")).split(",")[1].split(" - ")[1].split("(")[0].trim()
                            prov = establecerValor(line,line.substringAfter(":")).split(",")[1].split(" - ")[1].split("(")[1].substringBefore(")").trim()

                            currentMap["poblacion"] = pobl.uppercase()
                            currentMap["provincia"] = prov.uppercase()
                        }
                        currentMap["domicilioAUX"] = domi
                    }
                    else
                    {
                        currentMap["domicilioAUX"] = establecerValor(line, line.substringAfter(":"))
                    }
                }


                auxDir = line.contains(" de obra:")
                auxDirCentro = line.startsWith("Centro de trabajo:")
                auxZona = line.startsWith("Zona de trabajo:")

                when {
                    line.startsWith("Telefono:") -> {
                        if (currentMap.isNotEmpty()) {
                            incidents.add(createIncidentFromMap(currentMap))
                            currentMap.clear()
                        }
                        currentMap["telefono"] =
                            establecerValor(line, line.substringAfter(":").trim())
                    }

                    line.startsWith("Nombre cliente:") -> currentMap["nombreCliente"] =
                        establecerValor(line, line.substringAfter(":"))

                    line.contains(" de obra:") -> currentMap["domicilio"] =
                        establecerValor(line, line.substringAfter(":"))
                }
            }

            if (currentMap.isNotEmpty()) {
                incidents.add(createIncidentFromMap(currentMap))
            }
        }
        else if(contadorCentroTrabajo > 10) {
            var auxZona = false
            var poblacion = ""
            var provincia = ""
            var poblacionYProvincia = ""
            for (line in lines) {
                if(auxZona && !line.startsWith("Centro de trabajo:"))
                {
                    poblacionYProvincia = line.split("-")[1].trim()
                    poblacion = poblacionYProvincia.split("(")[0].trim()
                    provincia = poblacionYProvincia.split("(")[1].substringBefore(")").trim()
                    currentMap["provincia"] = provincia.uppercase()
                    currentMap["poblacion"] = poblacion.uppercase()
                }
                else
                {
                    poblacion = ""
                    provincia = ""
                }

                if(!auxZona)
                    auxZona = line.startsWith("Zona de trabajo:")

                when {
                    line.startsWith("Telefono:") -> {
                        if (currentMap.isNotEmpty()) {
                            incidents.add(createIncidentFromMap(currentMap))
                            currentMap.clear()
                        }
                        currentMap["Telefono"] =
                            establecerValor(line, line.substringAfter(":").trim())
                    }

                    line.startsWith("Nombre cliente:") -> currentMap["nombreCliente"] =
                        establecerValor(line, line.substringAfter(":"))

                    line.startsWith("Centro de trabajo:") -> currentMap["domicilio"] =
                        establecerValor(line, line.substringAfter(":"))

                    /*
                    poblacion.isNotEmpty() -> currentMap["poblacion"] = poblacion.uppercase()
                    provincia.isNotEmpty() -> {
                        currentMap["provincia"] = provincia.uppercase()
                        incidents.add(createIncidentFromMap(currentMap))
                        currentMap.clear()
                    }
                    */

                }
            }

            if (currentMap.isNotEmpty()) {
                incidents.add(createIncidentFromMap(currentMap))
            }
        }
        else{
            var auxTelefono = false
            var auxCentroTrabajo = false
            var poblacion = ""
            var provincia = ""
            var poblacionYProvincia = ""
            for (line in lines) {
                if(auxTelefono && !line.startsWith("PLACAS:") || auxCentroTrabajo)
                {
                    poblacionYProvincia = line.split("-")[1].trim()
                    poblacion = poblacionYProvincia.split("(")[0].trim()
                    provincia = poblacionYProvincia.split("(")[1].substringBefore(")").trim()

                    currentMap["domicilioAUX"] = poblacionYProvincia.uppercase()
                    currentMap["poblacion"] = poblacion.uppercase()
                    currentMap["provincia"] = provincia.uppercase()
                }
                else
                {
                    poblacion = ""
                    provincia = ""
                    poblacionYProvincia = ""
                }

                auxTelefono = line.startsWith("Telefono:")
                auxCentroTrabajo = line.startsWith("Centro de trabajo:")

                when {
                    line.startsWith("Telefono:") -> {
                        if (currentMap.isNotEmpty()) {
                            incidents.add(createIncidentFromMap(currentMap))
                            currentMap.clear()
                        }
                        currentMap["Telefono"] =
                            establecerValor(line, line.substringAfter(":").trim())
                    }

                    line.startsWith("Nombre cliente:") -> currentMap["nombreCliente"] =
                        establecerValor(line, line.substringAfter(":"))

                    line.startsWith("Centro de trabajo:") -> currentMap["domicilio"] =
                        establecerValor(line, line.substringAfter(":"))
                    /*
                    poblacionYProvincia.isNotEmpty() -> currentMap["domicilioAUX"] = poblacionYProvincia.uppercase()

                    poblacion.isNotEmpty() -> currentMap["poblacion"] = poblacion.uppercase()
                    provincia.isNotEmpty() -> {
                        currentMap["provincia"] = provincia.uppercase()
                        incidents.add(createIncidentFromMap(currentMap))
                        currentMap.clear()
                    }

                     */
                }
            }

            if (currentMap.isNotEmpty()) {
                incidents.add(createIncidentFromMap(currentMap))
            }
        }

        return incidents
    }

    private fun createIncidentFromMap(map: Map<String, String>): IncidentEntity {
        if(contadorIncidencias > 20) {
            return IncidentEntity(
                //numeroIncidencia = map["numeroIncidencia"] ?: "",
                //proyecto = map["proyecto"] ?: "",
                nombreCliente = map["nombreCliente"] ?: "",
                telefonos = map["telefono1"] + "/" + map["telefono2"],
                //telefono1 = map["telefono1"] ?: "",
                //telefono2 = map["telefono2"] ?: "",
                domicilio = map["domicilio"] ?: "",
                poblacion = map["poblacion"] ?: "",
                provincia = map["provincia"] ?: "",
                fecha = "",
                //zonaOM = map["zonaOM"] ?: "",
                //horaInicio = map["horaInicio"] ?: "",
                //duracion = map["duracion"] ?: "",
                //comentarios = map["comentarios"] ?: "",
                //pedido = map["pedido"] ?: "",
                //planos = map["planos"] ?: "",
                //soporte = map["soporte"] ?: "",
                origen = "FICHERO",
                fechaCreacion = System.currentTimeMillis()
            )
        }
        else if (contadorDirObra > 10)
        {
            val dom = map["domicilio"]
            val domaux = map["domicilioAUX"]

            var domFinal = ""
            if (!dom.isNullOrEmpty())
                domFinal = dom.toString()
            else
                domFinal = domaux.toString()

            return IncidentEntity(
                nombreCliente = "",
                telefonos = map["telefono"] ?: "",
                domicilio = domFinal,
                poblacion = map["poblacion"] ?: "",
                provincia = map["provincia"] ?: "",
                fecha = "",
                origen = "FICHERO",
                fechaCreacion = System.currentTimeMillis()
            )
        }
        else if (contadorCentroTrabajo > 10)
        {
            return IncidentEntity(
                nombreCliente = "",
                telefonos = map["telefono"] ?: "",
                domicilio = map["domicilio"] ?: "",
                poblacion = map["poblacion"] ?: "",
                provincia = map["provincia"] ?: "",
                fecha = "",
                origen = "FICHERO",
                fechaCreacion = System.currentTimeMillis()
            )
        }
        else
        {
            val dom = map["domicilio"]
            val domaux = map["domicilioAUX"]
            var domFinal = ""
            if (dom != null) {
                domFinal = if(dom.isNotEmpty())
                    dom.toString()
                else
                    domaux.toString()
            }
            return IncidentEntity(
                nombreCliente = "",
                telefonos = map["telefono"] ?: "",
                domicilio = domFinal,
                poblacion = map["poblacion"] ?: "",
                provincia = map["provincia"] ?: "",
                fecha = "",
                origen = "FICHERO",
                fechaCreacion = System.currentTimeMillis()
            )
        }
    }

    private suspend fun insertIncidents(incidents: List<IncidentEntity>) {
        incidents.forEach { incident ->
            incidentDao.insert(incident)
        }
    }
}
