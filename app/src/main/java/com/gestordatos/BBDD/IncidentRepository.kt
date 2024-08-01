package com.gestordatos.BBDD

class IncidentRepository(private val incidentDao: IncidentDao) {
    fun getIncidentsByProvince() = incidentDao.getIncidentsByProvince()
    fun getIncidentDetailsByProvince(provincia: String) = incidentDao.getIncidentDetailsByProvince(provincia)
}