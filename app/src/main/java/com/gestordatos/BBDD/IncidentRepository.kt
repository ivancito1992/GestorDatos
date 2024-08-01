package com.gestordatos.BBDD

class IncidentRepository(private val incidentDao: IncidentDao) {
    fun getIncidentsByProvince() = incidentDao.getIncidentsByProvince()
    fun getIncidentsByPoblation(provincia: String) = incidentDao.getIncidentsByPoblation(provincia)
    fun getIncidentDetailsByProvince(provincia: String, poblacion: String) = incidentDao.getIncidentDetailsByPoblation(provincia, poblacion)
}