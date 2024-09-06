package com.gestordatos.pantallas

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
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
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.util.Date
import kotlin.coroutines.resume

class TableManagementActivity : AppCompatActivity() {
    private lateinit var incidentDao: IncidentDao
    private val PERMISSION_REQUEST_CODE = 123
    private var countMain: Int = 0

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                launchFilePicker()
            } else {
                showPermissionDeniedDialog()
            }
        }

    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { showConfirmDialog(it) }
    }

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

        findViewById<Button>(R.id.btnImportarExcel).setOnClickListener{
            checkPermissionAndPickFile()
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
                    nombreCliente = it.nombreCliente,
                    telefonos = it.telefonos,
                    domicilio = it.domicilio,
                    poblacion = it.poblacion,
                    provincia = it.provincia,
                    fecha = it.fecha,
                    origen = it.origen,
                    fechaBackup = System.currentTimeMillis()
                )
            }
            incidentDao.insertBackup(backups)
            incidentDao.deleteAllIncidentsAndResetId()
            updateInfo()
        }
    }

    private fun resetWithoutBackup() {
        lifecycleScope.launch {
            incidentDao.deleteAllIncidentsAndResetId()
            updateInfo()
        }
    }

    private fun performBackup() {
        lifecycleScope.launch {
            val incidents = incidentDao.getAllIncidents()
            val backups = incidents.map {
                IncidentBackupEntity(
                    nombreCliente = it.nombreCliente,
                    telefonos = it.telefonos,
                    domicilio = it.domicilio,
                    poblacion = it.poblacion,
                    provincia = it.provincia,
                    fecha = it.fecha,
                    origen = it.origen,
                    fechaBackup = System.currentTimeMillis()
                )
            }
            incidentDao.insertBackup(backups)
            updateInfo()
            Toast.makeText(this@TableManagementActivity, "Backup realizado con éxito", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkPermissionAndPickFile() {
        launchFilePicker()
        /*
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED -> {

            }
            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) -> {
                showPermissionRationaleDialog()
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }

         */
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
            appendLine("ID,NOMBRE_CLIENTE,TELEFONOS,DOMICILIO,POBLACION,PROVINCIA,FECHA,ORIGEN")
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


    //Funciones para Importar el excel
    private fun launchFilePicker() {
        getContent.launch("*/*")
    }

    private fun showPermissionRationaleDialog() {
        AlertDialog.Builder(this)
            .setTitle("Permiso Necesario")
            .setMessage("Se necesita permiso para acceder a los archivos para importar el CSV.")
            .setPositiveButton("Conceder") { _, _ ->
                requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showPermissionDeniedDialog() {
        AlertDialog.Builder(this)
            .setTitle("Permiso Denegado")
            .setMessage("Se necesita permiso para acceder a los archivos. Por favor, concede el permiso en la configuración de la aplicación.")
            .setPositiveButton("Ir a Configuración") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showConfirmDialog(uri: Uri) {
        AlertDialog.Builder(this)
            .setTitle("Importar CSV")
            .setMessage("¿Deseas limpiar la base de datos antes de importar?")
            .setPositiveButton("Sí") { _, _ -> importCsv(uri, true) }
            .setNegativeButton("No") { _, _ -> importCsv(uri, false) }
            .setNeutralButton("Cancelar", null)
            .show()
    }

    private fun importCsv(uri: Uri, clearDatabase: Boolean) {
        lifecycleScope.launch {
            try {
                val inputStream = contentResolver.openInputStream(uri)
                val reader = BufferedReader(InputStreamReader(inputStream))

                // Leer la primera línea y verificar las columnas
                val headers = reader.readLine()?.split(",")
                if (!verifyHeaders(headers)) {
                    withContext(Dispatchers.Main) {
                        showErrorDialog("El formato del CSV no es correcto")
                    }
                    return@launch
                }

                if (clearDatabase) {
                    incidentDao.deleteAllIncidentsAndResetId()
                }

                reader.forEachLine { line ->
                    val values = line.split(",")
                    if (values.size == 8) {  // Asegurarse de que la línea tiene el número correcto de campos
                        val inci = IncidentEntity(
                            nombreCliente = values[1],
                            telefonos = values[2],
                            domicilio = values[3],
                            poblacion = values[4],
                            provincia = values[5],
                            fecha = values[6],
                            origen = values[7],
                            fechaCreacion = System.currentTimeMillis()
                        )
                        lifecycleScope.launch(Dispatchers.IO){
                            incidentDao.insert(inci)
                        }

                        //incidentDao.insert(inci)
                    }
                }

                withContext(Dispatchers.Main) {
                    showSuccessDialog("Importación completada con éxito")
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showErrorDialog("Error al importar: ${e.message}")
                }
            }
        }
    }

    private fun verifyHeaders(headers: List<String>?): Boolean {
        val expectedHeaders = listOf("ID", "NOMBRE_CLIENTE", "TELEFONOS", "DOMICILIO", "POBLACION", "PROVINCIA", "FECHA", "ORIGEN")

        return if (headers != null) {
            headers.size == expectedHeaders.size
        } else false
    }

    private fun showErrorDialog(message: String) {
        AlertDialog.Builder(this)
            .setTitle("Error")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showSuccessDialog(message: String) {
        AlertDialog.Builder(this)
            .setTitle("Éxito")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }

    private suspend fun insertIncidents(incidents: IncidentEntity) {
           incidentDao.insert(incidents)
    }
}