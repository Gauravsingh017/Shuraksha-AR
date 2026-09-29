package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "safety_modules")
data class ModuleEntity(
    @PrimaryKey val id: String,
    val title: String,
    val category: String, // "GAS_VENTILATION", "FIRE_EVACUATION", "MACHINERY", "ELECTRICAL", "PPE"
    val description: String,
    val riskLevel: String, // "HIGH", "MEDIUM", "LOW"
    val duration: String,
    val progress: Float = 0f,
    val status: String = "NOT_STARTED", // "NOT_STARTED", "IN_PROGRESS", "COMPLETED"
    val score: Int = 0,
    val hazardFound: Boolean = false,
    val checklistCount: Int = 0,
    val totalChecklist: Int = 3,
    val completedAt: Long? = null
)
