package com.gestordatos.varios

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.gestordatos.BBDD.IncidentRepository

class IncidentsByProvinceViewModel(private val repository: IncidentRepository) : ViewModel() {
    val incidentsByProvince = repository.getIncidentsByProvince().asLiveData()
}

class IncidentDetailViewModel(private val repository: IncidentRepository) : ViewModel() {
    fun getIncidentDetailsByProvince(provincia: String) = repository.getIncidentDetailsByProvince(provincia).asLiveData()
}