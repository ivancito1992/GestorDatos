package com.gestordatos.pantallas

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.gestordatos.BBDD.AppDatabase
import com.gestordatos.BBDD.IncidentDao
import com.gestordatos.BBDD.IncidentEntity
import com.gestordatos.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import kotlin.coroutines.resume

class FormActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var incidentDao: IncidentDao

    private val PERMISSION_REQUEST_CODE = 1001

    private fun isEntryValid(
        numIncidencia: String, proyecto: String, nombreCliente: String,
        telf1: String, telf2: String, domicilio: String, poblacion: String,
        provincia: String, zona: String, horaInicio: String, duracion: String,
        comentario: String, pedido: String, planos: String, soporte: String): Boolean {
        return !(numIncidencia.isBlank() || proyecto.isBlank() || nombreCliente.isBlank() ||
                telf1.toString().isBlank() || telf2.toString().isBlank() || domicilio.isBlank() ||
                poblacion.isBlank() || provincia.isBlank() || zona.isBlank() ||
                horaInicio.isBlank() || duracion.isBlank() || comentario.isBlank() ||
                pedido.isBlank() || planos.isBlank() || soporte.isBlank())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_form)

        db = AppDatabase.getDatabase(applicationContext)
        incidentDao = db.incidentDao()

        val etNumIncidencia: EditText = findViewById(R.id.etNumIncidencia)
        val etProyecto: EditText = findViewById(R.id.etProyecto)
        val etNombreCliente: EditText = findViewById(R.id.etNombreCliente)
        val etTelefono1: EditText = findViewById(R.id.etTelefono1)
        val etTelefono2: EditText = findViewById(R.id.etTelefono2)
        val etDomicilio: EditText = findViewById(R.id.etDomicilio)
        val etPoblacion: EditText = findViewById(R.id.etPoblacion)
        val etProvincia: EditText = findViewById(R.id.etProvincia)
        val etZonaOyM: EditText = findViewById(R.id.etZonaOyM)
        val etHoraInicio: EditText = findViewById(R.id.etHoraInicio)
        val etDuracion: EditText = findViewById(R.id.etDuracion)
        val etComentarios: EditText = findViewById(R.id.etComentarios)
        val etPedido: EditText = findViewById(R.id.etPedido)
        val etPlanos: EditText = findViewById(R.id.etPlanos)
        val etSoporte: EditText = findViewById(R.id.etSoporte)

        val btnGuardar: Button = findViewById(R.id.btnGuardar)
        val btnGenerar: Button = findViewById(R.id.btnGenerar)

        btnGenerar.setOnClickListener {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P && !checkPermission()) {
                requestPermission()
            } else {
                generateCSV()
            }
        }

        btnGuardar.setOnClickListener {

            val numIncidencia: String = etNumIncidencia.text.toString()
            val proyecto: String = etProyecto.text.toString()
            val nombreCliente: String = etNombreCliente.text.toString()
            val telefono1: String = etTelefono1.text.toString()
            val telefono2: String = etTelefono2.text.toString()
            val domicilio: String = etDomicilio.text.toString()
            val poblacion: String = etPoblacion.text.toString()
            val provincia: String = etProvincia.text.toString()
            val zonaOyM: String = etZonaOyM.text.toString()
            val horaInicio: String = etHoraInicio.text.toString()
            val duracion: String = etDuracion.text.toString()
            val comentarios: String = etComentarios.text.toString()
            val pedido: String = etPedido.text.toString()
            val planos: String = etPlanos.text.toString()
            val soporte: String = etSoporte.text.toString()

            if (isEntryValid(
                    numIncidencia,
                    proyecto,
                    nombreCliente,
                    telefono1,
                    telefono2,
                    domicilio,
                    poblacion,
                    provincia,
                    zonaOyM,
                    horaInicio,
                    duracion,
                    comentarios,
                    pedido,
                    planos,
                    soporte
                )
            ) {
                insertIncident(
                    numIncidencia,
                    proyecto,
                    nombreCliente,
                    telefono1,
                    telefono2,
                    domicilio,
                    poblacion,
                    provincia,
                    zonaOyM,
                    horaInicio,
                    duracion,
                    comentarios,
                    pedido,
                    planos,
                    soporte
                )
            } else {
                Toast.makeText(this, "Por favor, rellena todos los campos", Toast.LENGTH_SHORT)
                    .show()
            }
        }

    }
    //Funciones para el insert
    private fun insertIncident(numIncidencia: String, proyecto: String, nombreCliente: String,
                               telefono1: String, telefono2: String, domicilio: String, poblacion: String,
                               provincia: String, zonaOyM: String, horaInicio: String, duracion: String,
                               comentarios: String, pedido: String, planos: String, soporte: String) {
        val incident = IncidentEntity(
            numeroIncidencia = numIncidencia,
            proyecto = proyecto,
            nombreCliente = nombreCliente,
            telefono1 = telefono1,
            telefono2 = telefono2,
            domicilio = domicilio,
            poblacion = poblacion,
            provincia = provincia,
            zonaOM = zonaOyM,
            horaInicio = horaInicio,
            duracion = duracion,
            comentarios = comentarios,
            pedido = pedido,
            planos = planos,
            soporte = soporte,
            origen = "FORMULARIO",
            fechaCreacion = System.currentTimeMillis()
        )

        lifecycleScope.launch(Dispatchers.IO) {
            incidentDao.insert(incident)
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    this@FormActivity,
                    "Incidencia guardada con éxito",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    //Funciones para los permisos
    private fun checkPermission(): Boolean {
        val result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        return result == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    generateCSV()
                } else {
                    Toast.makeText(this, "Permiso denegado. No se puede generar el CSV.", Toast.LENGTH_LONG).show()
                }
                return
            }
        }
    }



    //Funciones para generar el CSV
    private fun generateCSV() {
        lifecycleScope.launch(Dispatchers.IO) {
            val countFormulario = incidentDao.getCountByOrigin("FORMULARIO")
            val countFichero = incidentDao.getCountByOrigin("FICHERO")

            val message = "¿Desea generar el CSV?\n" +
                    "Registros de Formulario: $countFormulario\n" +
                    "Registros de Fichero: $countFichero"

            val shouldGenerate = withContext(Dispatchers.Main) {
                AlertDialog.Builder(this@FormActivity)
                    .setTitle("Generar CSV")
                    .setMessage(message)
                    .setPositiveButton("Sí") { _, _ -> }
                    .setNegativeButton("No") { _, _ -> }
                    .create()
                    .let { dialog ->
                        suspendCancellableCoroutine<Boolean> { continuation ->
                            dialog.setOnDismissListener {
                                continuation.resume(false)
                            }
                            dialog.show()
                            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                                dialog.dismiss()
                                continuation.resume(true)
                            }
                            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener {
                                dialog.dismiss()
                                continuation.resume(false)
                            }
                        }
                    }
                }

            if (shouldGenerate) {
                val incidents = incidentDao.getAllIncidents()
                val csvContent = buildCSVContent(incidents)

                when {
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> saveFileWithMediaStore(csvContent)
                    else -> saveFileToExternalFilesDir(csvContent)
                }
            }
        }
    }

    private fun buildCSVContent(incidents: List<IncidentEntity>): String {
        return buildString {
            appendLine("ID,Num_Incidencia,Proyecto,Nombre_Cliente,Telefono_1,Telefono_2,Domicilio,Poblacion,Provincia,Zona_OM,Hora_Inicio,Duracion,Comentarios,Pedido,Planos,Soporte,Origen")
            incidents.forEach { incident ->
                appendLine("${incident.id},${incident.numeroIncidencia},${incident.proyecto},${incident.nombreCliente},${incident.telefono1},${incident.telefono2},${incident.domicilio},${incident.poblacion},${incident.provincia},${incident.zonaOM},${incident.horaInicio},${incident.duracion},${incident.comentarios},${incident.pedido},${incident.planos},${incident.soporte},${incident.origen}")
            }
        }
    }

    private suspend fun saveFileWithMediaStore(csvContent: String) {
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "incidencias.csv")
            put(MediaStore.MediaColumns.MIME_TYPE, "text/csv")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS)
        }

        val uri = contentResolver.insert(MediaStore.Files.getContentUri("external"), contentValues)
        uri?.let {
            contentResolver.openOutputStream(it)?.use { outputStream ->
                OutputStreamWriter(outputStream).use { writer ->
                    writer.write(csvContent)
                }
            }
            withContext(Dispatchers.Main) {
                Toast.makeText(this@FormActivity, "CSV generado en Documentos", Toast.LENGTH_LONG).show()
            }
        }
    }

    private suspend fun saveFileToExternalFilesDir(csvContent: String) {
        withContext(Dispatchers.IO) {
            val file = File(getExternalFilesDir(null), "incidencias.csv")
            file.outputStream().use {
                it.write(csvContent.toByteArray())
            }
        }
        withContext(Dispatchers.Main) {
            Toast.makeText(this@FormActivity, "CSV generado en los archivos de la aplicación", Toast.LENGTH_LONG).show()
        }
    }
}