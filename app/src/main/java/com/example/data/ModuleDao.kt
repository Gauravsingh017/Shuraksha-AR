package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ModuleDao {
    @Query("SELECT * FROM safety_modules ORDER BY id ASC")
    fun getAllModules(): Flow<List<ModuleEntity>>

    @Query("SELECT * FROM safety_modules WHERE id = :id LIMIT 1")
    fun getModuleById(id: String): Flow<ModuleEntity?>

    @Query("SELECT COUNT(*) FROM safety_modules WHERE status = 'COMPLETED'")
    fun getCompletedCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertModules(modules: List<ModuleEntity>)

    @Update
    suspend fun updateModule(module: ModuleEntity)

    @Query("UPDATE safety_modules SET progress = :progress, status = :status, score = :score, hazardFound = :hazardFound, checklistCount = :checklistCount, completedAt = :completedAt WHERE id = :id")
    suspend fun updateModuleProgress(
        id: String,
        progress: Float,
        status: String,
        score: Int,
        hazardFound: Boolean,
        checklistCount: Int,
        completedAt: Long?
    )

    @Query("UPDATE safety_modules SET progress = 0.0, status = 'NOT_STARTED', score = 0, hazardFound = 0, checklistCount = 0, completedAt = NULL WHERE id = :id")
    suspend fun resetModule(id: String)

    @Query("SELECT COUNT(*) FROM safety_modules")
    suspend fun getModuleCount(): Int
}
