package com.gestordatos.BBDD

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import java.util.Date

//Aqui establecemos los metodos que nos van a permitir obtener la informacion de base de datos
//es decir los insert delete select y updates
@Dao
interface IncidentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(incident: IncidentEntity)

    @Update
    fun update(incident: IncidentEntity)

    @Delete
    fun delete(incident: IncidentEntity)

    @Query("SELECT * FROM T_INCIDENCIAS WHERE ID = :id")
    suspend fun getIncidentById(id: Int): IncidentEntity?


    //Funciones adicionales
    @Query("SELECT COUNT(*) FROM T_INCIDENCIAS WHERE ORIGEN = :origen")
    suspend fun getCountByOrigin(origen: String): Int

    @Query("SELECT COUNT(*) FROM T_INCIDENCIAS")
    suspend fun getTotalIncidentes(): Int

    @Insert
    suspend fun insertBackup(incidents: List<IncidentBackupEntity>)

    @Query("DELETE FROM T_INCIDENCIAS")
    suspend fun deleteAllIncidents()

    @Query("SELECT * FROM T_INCIDENCIAS")
    suspend fun getAllIncidents(): List<IncidentEntity>

    @Query("SELECT MAX(FECHA_BACKUP) FROM T_INCIDENCIAS_BCK")
    suspend fun getLastBackupDate(): Long?

    @Query("DELETE FROM T_INCIDENCIAS_BCK")
    suspend fun deleteAllBackups()

    //funciones para el tema de provincias
    @Query("SELECT PROVINCIA, COUNT(*) as count FROM T_INCIDENCIAS GROUP BY PROVINCIA ORDER BY PROVINCIA ASC")
    fun getIncidentsByProvince(): Flow<List<ProvinceIncidentCount>>

    @Query("SELECT * FROM T_INCIDENCIAS WHERE PROVINCIA = :provincia ORDER BY POBLACION, FECHA_CREACION, NOMBRE_CLIENTE DESC")
    fun getIncidentDetailsByProvince(provincia: String): Flow<List<IncidentEntity>>
}
data class ProvinceIncidentCount(
    @ColumnInfo(name = "PROVINCIA") val provincia: String,
    @ColumnInfo(name = "count") val count: Int
)