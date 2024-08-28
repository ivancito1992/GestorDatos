package com.gestordatos.BBDD

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

//Aqui establecemos los metodos que nos van a permitir obtener la informacion de base de datos
//es decir los insert delete select y updates
@Dao
interface IncidentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(incident: IncidentEntity)

    @Update
    suspend fun update(incident: IncidentEntity)

    @Delete
    suspend fun delete(incident: IncidentEntity)

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

    @Query("DELETE FROM sqlite_sequence WHERE name='T_INCIDENCIAS'")
    suspend fun resetAutoIncrement()

    @Transaction
    suspend fun deleteAllIncidentsAndResetId() {
        deleteAllIncidents()
        resetAutoIncrement()
    }

    @Query("SELECT * FROM T_INCIDENCIAS")
    suspend fun getAllIncidents(): List<IncidentEntity>

    @Query("SELECT MAX(FECHA_BACKUP) FROM T_INCIDENCIAS_BCK")
    suspend fun getLastBackupDate(): Long?

    @Query("DELETE FROM T_INCIDENCIAS_BCK")
    suspend fun deleteAllBackups()

    @Query("DELETE FROM sqlite_sequence WHERE name='T_INCIDENCIAS_BCK'")
    suspend fun resetAutoIncrementBCK()

    @Transaction
    suspend fun deleteAllIncidentsAndResetIdBCK() {
        deleteAllBackups()
        resetAutoIncrementBCK()
    }

    //funciones para el tema de provincias
    @Query("SELECT PROVINCIA, COUNT(*) as count FROM T_INCIDENCIAS GROUP BY PROVINCIA ORDER BY UPPER(PROVINCIA) ASC")
    fun getIncidentsByProvince(): Flow<List<ProvinceIncidentCount>>

    @Query("SELECT * FROM T_INCIDENCIAS WHERE PROVINCIA = :provincia AND POBLACION = :poblacion ORDER BY UPPER(POBLACION), FECHA_CREACION, NOMBRE_CLIENTE DESC")
    fun getIncidentDetailsByPoblation(poblacion: String, provincia: String): Flow<List<IncidentEntity>>

    @Query("SELECT POBLACION, COUNT(*) as count FROM T_INCIDENCIAS WHERE PROVINCIA = :provincia GROUP BY POBLACION ORDER BY UPPER(POBLACION) ASC")
    fun getIncidentsByPoblation(provincia: String): Flow<List<PoblationIncidentCount>>
}
data class ProvinceIncidentCount(
    @ColumnInfo(name = "PROVINCIA") val provincia: String,
    @ColumnInfo(name = "count") val count: Int
)

data class PoblationIncidentCount(
    @ColumnInfo(name = "POBLACION") val poblacion: String,
    @ColumnInfo(name = "count") val count: Int
)