package com.gestordatos.pantallas

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gestordatos.BBDD.AppDatabase
import com.gestordatos.BBDD.IncidentRepository
import com.gestordatos.R
import com.gestordatos.varios.IncidentsByPoblationAdapter
import com.gestordatos.varios.IncidentsByPoblationViewModel


class IncidentsByPoblationActivity : AppCompatActivity() {
    private lateinit var viewModel: IncidentsByPoblationViewModel
    private lateinit var adapter: IncidentsByPoblationAdapter
    private var provincia: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_incidents_by_poblation)

        provincia = intent.getStringExtra("PROVINCIA") ?: return

        val dao = AppDatabase.getDatabase(application).incidentDao()
        val repository = IncidentRepository(dao)
        viewModel = ViewModelProvider(this, IncidentsByPoblationViewModelFactory(repository))
            .get(IncidentsByPoblationViewModel::class.java)

        setupRecyclerView()
        observeViewModel(provincia)
    }

    private fun setupRecyclerView() {
        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        adapter = IncidentsByPoblationAdapter { poblacion ->
            // Navegar a la pantalla de detalles
            val intent = Intent(this, IncidentDetailActivity::class.java)

            intent.putExtra("POBLACION", poblacion)
            intent.putExtra("PROVINCIA",provincia)
            startActivity(intent)
        }
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun observeViewModel(provincia: String) {
        viewModel.getIncidentsByPoblation(provincia).observe(this) { incidents ->
            adapter.submitList(incidents)
        }
    }
}

class IncidentsByPoblationViewModelFactory(private val repository: IncidentRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(IncidentsByPoblationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return IncidentsByPoblationViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}