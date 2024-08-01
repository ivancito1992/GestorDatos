package com.gestordatos.BBDD

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "T_INCIDENCIAS")
data class IncidentEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "ID") val id: Int = 0,
    @ColumnInfo(name = "NUM_INCIDENCIA") val numeroIncidencia: String,
    @ColumnInfo(name = "PROYECTO") val proyecto: String,
    @ColumnInfo(name = "NOMBRE_CLIENTE") val nombreCliente: String,
    @ColumnInfo(name = "TELEFONO_1") val telefono1: String,
    @ColumnInfo(name = "TELEFONO_2") val telefono2: String,
    @ColumnInfo(name = "DOMICILIO") val domicilio: String,
    @ColumnInfo(name = "POBLACION") val poblacion: String,
    @ColumnInfo(name = "PROVINCIA") val provincia: String,
    @ColumnInfo(name = "ZONA_OM") val zonaOM: String,
    @ColumnInfo(name = "HORA_INICIO") val horaInicio: String,
    @ColumnInfo(name = "DURACION") val duracion: String,
    @ColumnInfo(name = "COMENTARIOS") val comentarios: String,
    @ColumnInfo(name = "PEDIDO") val pedido: String,
    @ColumnInfo(name = "PLANOS") val planos: String,
    @ColumnInfo(name = "SOPORTE") val soporte: String,
    @ColumnInfo(name = "ORIGEN") val origen: String,
    @ColumnInfo(name = "FECHA_CREACION") val fechaCreacion: Long
)

@Entity(tableName = "T_INCIDENCIAS_BCK")
data class IncidentBackupEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "ID") val id: Int = 0,
    @ColumnInfo(name = "NUM_INCIDENCIA") val numeroIncidencia: String,
    @ColumnInfo(name = "PROYECTO") val proyecto: String,
    @ColumnInfo(name = "NOMBRE_CLIENTE") val nombreCliente: String,
    @ColumnInfo(name = "TELEFONO_1") val telefono1: String,
    @ColumnInfo(name = "TELEFONO_2") val telefono2: String,
    @ColumnInfo(name = "DOMICILIO") val domicilio: String,
    @ColumnInfo(name = "POBLACION") val poblacion: String,
    @ColumnInfo(name = "PROVINCIA") val provincia: String,
    @ColumnInfo(name = "ZONA_OM") val zonaOM: String,
    @ColumnInfo(name = "HORA_INICIO") val horaInicio: String,
    @ColumnInfo(name = "DURACION") val duracion: String,
    @ColumnInfo(name = "COMENTARIOS") val comentarios: String,
    @ColumnInfo(name = "PEDIDO") val pedido: String,
    @ColumnInfo(name = "PLANOS") val planos: String,
    @ColumnInfo(name = "SOPORTE") val soporte: String,
    @ColumnInfo(name = "ORIGEN") val origen: String,
    @ColumnInfo(name = "FECHA_BACKUP") val fechaBackup: Long
)

