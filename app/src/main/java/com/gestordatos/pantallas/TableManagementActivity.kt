package com.gestordatos.pantallas

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.gestordatos.BBDD.AppDatabase
import com.gestordatos.BBDD.IncidentBackupEntity
import com.gestordatos.BBDD.IncidentDao
import com.gestordatos.BBDD.IncidentEntity
import com.gestordatos.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import java.io.OutputStreamWriter
import java.util.Date
import kotlin.coroutines.resume

class TableManagementActivity : AppCompatActivity() {
    private lateinit var incidentDao: IncidentDao
    private val PERMISSION_REQUEST_CODE = 1001
    private var countMain: Int = 0

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_table_management)

        incidentDao = AppDatabase.getDatabase(applicationContext).incidentDao()

        updateInfo()
        findViewById<Button>(R.id.btnRefrescar).setOnClickListener{
            updateInfo()
        }

        findViewById<Button>(R.id.btnGenerarCSV).setOnClickListener {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P && !checkPermission()) {
                requestPermission()
            }
            else {
                generateCSV()
            }
        }

        findViewById<Button>(R.id.btnResetTables).setOnClickListener {
            showResetConfirmation()
        }

        findViewById<Button>(R.id.btnBackupData).setOnClickListener {
            performBackup()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateInfo() {
        lifecycleScope.launch {
            countMain = incidentDao.getTotalIncidentes()
            val lastBackupDate = incidentDao.getLastBackupDate()

            findViewById<TextView>(R.id.tvMainTableInfo).text =
                "Tabla principal: $countMain registros\n" +
                        "Último cambio: ${getLastIncidentDate()}"

            findViewById<TextView>(R.id.tvBackupTableInfo).text =
                "Último backup: ${lastBackupDate?.let { Date(it).toString() } ?: "Nunca"}"
        }
    }

    private suspend fun getLastIncidentDate(): String {
        return incidentDao.getLastBackupDate()?.let {
            // Asumiendo que tienes un campo de fecha en tu entidad
            val resultdate: Date = Date(it)
            resultdate.toString()
        } ?: "No hay registros"
    }

    private fun showResetConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Resetear Tablas")
            .setMessage("¿Desea realizar una copia de seguridad antes de resetear?")
            .setPositiveButton("Sí") { _, _ -> resetWithBackup() }
            .setNegativeButton("No") { _, _ -> resetWithoutBackup() }
            .setNeutralButton("Cancelar", null)
            .show()
    }

    private fun resetWithBackup() {
        lifecycleScope.launch {
            val incidents = incidentDao.getAllIncidents()
            val backups = incidents.map {
                IncidentBackupEntity(
                    //numeroIncidencia = it.numeroIncidencia,
                    //proyecto = it.proyecto,
                    nombreCliente = it.nombreCliente,
                    telefonos = it.telefonos,
                    //telefono1 = it.telefono1,
                    //telefono2 = it.telefono2,
                    domicilio = it.domicilio,
                    poblacion = it.poblacion,
                    provincia = it.provincia,
                    fecha = it.fecha,
                    //zonaOM = it.zonaOM,
                    //horaInicio = it.horaInicio,
                    //duracion = it.duracion,
                    //comentarios = it.comentarios,
                    //pedido = it.pedido,
                    //planos = it.planos,
                    //soporte = it.soporte,
                    origen = it.origen,
                    fechaBackup = System.currentTimeMillis()
                )
            }
            incidentDao.insertBackup(backups)
            incidentDao.deleteAllIncidents()
            updateInfo()
        }
    }

    private fun resetWithoutBackup() {
        lifecycleScope.launch {
            incidentDao.deleteAllIncidents()
            updateInfo()
        }
    }

    private fun performBackup() {
        lifecycleScope.launch {
            val incidents = incidentDao.getAllIncidents()
            val backups = incidents.map {
                IncidentBackupEntity(
                    //numeroIncidencia = it.numeroIncidencia,
                    //proyecto = it.proyecto,
                    nombreCliente = it.nombreCliente,
                    telefonos = it.telefonos,
                    //telefono1 = it.telefono1,
                    //telefono2 = it.telefono2,
                    domicilio = it.domicilio,
                    poblacion = it.poblacion,
                    provincia = it.provincia,
                    fecha = it.fecha,
                    //zonaOM = it.zonaOM,
                    //horaInicio = it.horaInicio,
                    //duracion = it.duracion,
                    //comentarios = it.comentarios,
                    //pedido = it.pedido,
                    //planos = it.planos,
                    //soporte = it.soporte,
                    origen = it.origen,
                    fechaBackup = System.currentTimeMillis()
                )
            }
            incidentDao.insertBackup(backups)
            updateInfo()
            Toast.makeText(this@TableManagementActivity, "Backup realizado con éxito", Toast.LENGTH_SHORT).show()
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
        if(countMain > 0)
        {
            lifecycleScope.launch(Dispatchers.IO) {
                val incidents = incidentDao.getAllIncidents()
                val csvContent = buildCSVContent(incidents)

                when {
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> saveFileWithMediaStore(csvContent)
                    else -> saveFileToExternalFilesDir(csvContent)
                }
            }
        }
        else {
            Toast.makeText(this@TableManagementActivity, "No hay registros que generar", Toast.LENGTH_LONG).show()
        }
    }

    private fun buildCSVContent(incidents: List<IncidentEntity>): String {
        return buildString {
            //appendLine("ID,Num_Incidencia,Proyecto,Nombre_Cliente,Telefono_1,Telefono_2,Domicilio,Poblacion,Provincia,Zona_OM,Hora_Inicio,Duracion,Comentarios,Pedido,Planos,Soporte,Origen")
            appendLine("ID,Nombre_Cliente,Telefonos,Domicilio,Poblacion,Provincia,fecha,Origen")
            incidents.forEach { incident ->
                //appendLine("${incident.id},${incident.numeroIncidencia},${incident.proyecto},${incident.nombreCliente},${incident.telefono1},${incident.telefono2},${incident.domicilio},${incident.poblacion},${incident.provincia},${incident.zonaOM},${incident.horaInicio},${incident.duracion},${incident.comentarios},${incident.pedido},${incident.planos},${incident.soporte},${incident.origen}")
                appendLine("${incident.id},${incident.nombreCliente},${incident.telefonos},${incident.domicilio},${incident.poblacion},${incident.provincia},${incident.fecha},${incident.origen}")

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
                Toast.makeText(this@TableManagementActivity, "CSV generado en Documentos", Toast.LENGTH_LONG).show()
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
            Toast.makeText(this@TableManagementActivity, "CSV generado en los archivos de la aplicación", Toast.LENGTH_LONG).show()
        }
    }
}