package com.gestordatos.pantallas

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gestordatos.BBDD.AppDatabase
import com.gestordatos.BBDD.IncidentRepository
import com.gestordatos.R
import com.gestordatos.varios.IncidentDetailAdapter
import com.gestordatos.varios.IncidentDetailViewModel

class IncidentDetailActivity : AppCompatActivity() {
    private lateinit var viewModel: IncidentDetailViewModel
    private lateinit var adapter: IncidentDetailAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_incident_detail)

        val poblacion = intent.getStringExtra("POBLACION") ?: return
        val provincia = intent.getStringExtra("PROVINCIA") ?: return

        val dao = AppDatabase.getDatabase(application).incidentDao()
        val repository = IncidentRepository(dao)
        viewModel = ViewModelProvider(this, IncidentDetailViewModelFactory(repository))[IncidentDetailViewModel::class.java]

        setupRecyclerView()
        observeViewModel(poblacion, provincia)
    }

    private fun setupRecyclerView() {
        val recyclerView: RecyclerView = findViewById(R.id.recyclerViewDetail)
        adapter = IncidentDetailAdapter()
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun observeViewModel(poblacion: String, provincia: String) {
        viewModel.getIncidentDetailsByProvince(poblacion, provincia).observe(this) { incidents ->
            adapter.submitList(incidents)
        }
    }
}

class IncidentDetailViewModelFactory(private val repository: IncidentRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(IncidentDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return IncidentDetailViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}