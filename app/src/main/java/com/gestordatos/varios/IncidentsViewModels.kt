package com.gestordatos.varios

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.gestordatos.BBDD.IncidentRepository

class IncidentsByProvinceViewModel(private val repository: IncidentRepository) : ViewModel() {
    val incidentsByProvince = repository.getIncidentsByProvince().asLiveData()
}

class IncidentsByPoblationViewModel(private val repository: IncidentRepository) : ViewModel() {
    //val incidentsByPoblation = repository.getIncidentsByPoblation().asLiveData()
    fun getIncidentsByPoblation(provincia: String) = repository.getIncidentsByPoblation(provincia).asLiveData()
}

class IncidentDetailViewModel(private val repository: IncidentRepository) : ViewModel() {
    fun getIncidentDetailsByProvince(poblacion: String, provincia: String) = repository.getIncidentDetailsByProvince(poblacion, provincia).asLiveData()
}