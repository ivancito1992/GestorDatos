package com.gestordatos.pantallas

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.gestordatos.BBDD.AppDatabase
import com.gestordatos.BBDD.IncidentDao
import com.gestordatos.BBDD.IncidentEntity
import com.gestordatos.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FormActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var incidentDao: IncidentDao
    private var incidentId: Int? = null

    private lateinit var etNombreCliente: EditText
    private lateinit var etTelefonos: EditText
    private lateinit var etDomicilio: EditText
    private lateinit var etPoblacion: EditText
    private lateinit var etProvincia: EditText
    private lateinit var etFecha: EditText
    private lateinit var btnGuardar: Button

    private fun isEntryValid(
        nombreCliente: String, telefonos: String, domicilio: String,
        poblacion: String, provincia: String, fecha: String
    ): Boolean {
        return !(nombreCliente.isBlank() || telefonos.isBlank() || domicilio.isBlank() ||
                poblacion.isBlank() || provincia.isBlank() || fecha.isBlank())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_form)

        db = AppDatabase.getDatabase(applicationContext)
        incidentDao = db.incidentDao()

        etNombreCliente = findViewById(R.id.etNombreCliente)
        etTelefonos = findViewById(R.id.etTelefonos)
        etDomicilio = findViewById(R.id.etDomicilio)
        etPoblacion = findViewById(R.id.etPoblacion)
        etProvincia = findViewById(R.id.etProvincia)
        etFecha = findViewById(R.id.etFecha)

       btnGuardar = findViewById(R.id.btnGuardar)

        incidentId = intent.getIntExtra("INCIDENT_ID", -1)
        if (incidentId != -1) {
            loadIncidentData()
        }


        btnGuardar.setOnClickListener {

            val nombreCliente: String = etNombreCliente.text.toString()
            val telefonos: String = etTelefonos.text.toString()
            val domicilio: String = etDomicilio.text.toString()
            val poblacion: String = etPoblacion.text.toString().uppercase()
            val provincia: String = etProvincia.text.toString().uppercase()
            val fecha: String = etFecha.text.toString()

            if (incidentId == -1) {
                insertIncident(nombreCliente, telefonos, domicilio, poblacion, provincia, fecha)
            }
            else {
                updateIncident(nombreCliente, telefonos, domicilio, poblacion, provincia, fecha)
            }
        }
    }

    //funcion para saber si tengo cargado el ID
    private fun loadIncidentData() {
        lifecycleScope.launch(Dispatchers.IO) {
            val incident = incidentDao.getIncidentById(incidentId!!)
            withContext(Dispatchers.Main) {
                incident?.let { safeIncident ->
                    etNombreCliente.setText(safeIncident.nombreCliente)
                    etTelefonos.setText(safeIncident.telefonos)
                    etDomicilio.setText(safeIncident.domicilio)
                    etPoblacion.setText(safeIncident.poblacion)
                    etProvincia.setText(safeIncident.provincia)
                    etFecha.setText(safeIncident.fecha)
                } ?: run {
                    Toast.makeText(this@FormActivity, "No se pudo cargar el incidente", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }

    //Funciones para el insert
    private fun insertIncident(nombreCliente: String, telefonos: String, domicilio: String, poblacion: String,
                               provincia: String, fecha: String) {
        val incident = IncidentEntity(
            nombreCliente = nombreCliente,
            telefonos = telefonos,
            domicilio = domicilio,
            poblacion = poblacion,
            provincia = provincia,
            fecha = fecha,
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
                finish()
            }
        }
    }

    //Funciones para el update
    private fun updateIncident(nombreCliente: String, telefonos: String, domicilio: String, poblacion: String,
                               provincia: String, fecha: String) {
        val incident = IncidentEntity(
            id = incidentId!!,
            nombreCliente = nombreCliente,
            telefonos = telefonos,
            domicilio = domicilio,
            poblacion = poblacion,
            provincia = provincia,
            fecha = fecha,
            origen = "FORMULARIO",
            fechaCreacion = System.currentTimeMillis()
        )

        lifecycleScope.launch(Dispatchers.IO) {
            incidentDao.update(incident)
            withContext(Dispatchers.Main) {
                Toast.makeText(this@FormActivity, "Incidencia actualizada con éxito", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}