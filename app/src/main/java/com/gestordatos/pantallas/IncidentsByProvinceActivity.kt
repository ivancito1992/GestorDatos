package com.gestordatos.pantallas

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
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
import com.gestordatos.varios.IncidentsByProvinceAdapter
import com.gestordatos.varios.IncidentsByProvinceViewModel

class IncidentsByProvinceActivity : AppCompatActivity() {
    private lateinit var viewModel: IncidentsByProvinceViewModel
    private lateinit var adapter: IncidentsByProvinceAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_incidents_by_province)

        val dao = AppDatabase.getDatabase(application).incidentDao()
        val repository = IncidentRepository(dao)
        viewModel = ViewModelProvider(this, IncidentsByProvinceViewModelFactory(repository))
            .get(IncidentsByProvinceViewModel::class.java)

        setupRecyclerView()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        adapter = IncidentsByProvinceAdapter { provincia ->
            // Navegar a la pantalla de detalles
            val intent = Intent(this, IncidentDetailActivity::class.java)

            intent.putExtra("PROVINCIA", provincia)
            startActivity(intent)
        }
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun observeViewModel() {
        viewModel.incidentsByProvince.observe(this) { incidents ->
            adapter.submitList(incidents)
        }
    }
}

class IncidentsByProvinceViewModelFactory(private val repository: IncidentRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(IncidentsByProvinceViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return IncidentsByProvinceViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}