package com.example.data

import kotlinx.coroutines.flow.Flow

class ModuleRepository(private val dao: ModuleDao) {
    val allModules: Flow<List<ModuleEntity>> = dao.getAllModules()
    val completedCount: Flow<Int> = dao.getCompletedCount()

    fun getModule(id: String): Flow<ModuleEntity?> = dao.getModuleById(id)

    suspend fun initializeDefaultsIfNeeded() {
        if (dao.getModuleCount() == 0) {
            val defaults = listOf(
                ModuleEntity(
                    id = "1",
                    title = "Underground Gas & Ventilation",
                    category = "GAS_VENTILATION",
                    description = "Detect dangerous methane build-up, identify swirling gas leaks, and activate ventilation stopping doors.",
                    riskLevel = "HIGH",
                    duration = "42 min",
                    progress = 0.35f,
                    status = "IN_PROGRESS",
                    score = 0,
                    hazardFound = false,
                    checklistCount = 1,
                    totalChecklist = 3
                ),
                ModuleEntity(
                    id = "2",
                    title = "Fire & Emergency Evacuation",
                    category = "FIRE_EVACUATION",
                    description = "Follow dynamic 3D green evacuation arrows along tunnel egress route away from active fire hazards to safety.",
                    riskLevel = "HIGH",
                    duration = "28 min",
                    progress = 0.0f,
                    status = "NOT_STARTED",
                    score = 0,
                    hazardFound = false,
                    checklistCount = 0,
                    totalChecklist = 3
                ),
                ModuleEntity(
                    id = "3",
                    title = "Conveyor & Machine Guarding",
                    category = "MACHINERY",
                    description = "Lock-out/tag-out drills on coal bulk conveyors and pinch-point machine guards.",
                    riskLevel = "HIGH",
                    duration = "35 min",
                    progress = 1.0f,
                    status = "COMPLETED",
                    score = 92,
                    hazardFound = true,
                    checklistCount = 3,
                    totalChecklist = 3,
                    completedAt = System.currentTimeMillis() - 86400000L * 2
                ),
                ModuleEntity(
                    id = "4",
                    title = "Electrical Isolation & Arc Flash",
                    category = "ELECTRICAL",
                    description = "Safe high-voltage electrical substation isolation and breaker interlock verification.",
                    riskLevel = "MEDIUM",
                    duration = "30 min",
                    progress = 0.0f,
                    status = "NOT_STARTED",
                    score = 0,
                    hazardFound = false,
                    checklistCount = 0,
                    totalChecklist = 3
                ),
                ModuleEntity(
                    id = "5",
                    title = "PPE & Mine Site Access",
                    category = "PPE",
                    description = "Mandatory DGMS compliant self-rescuer respirator, cap lamp, and anti-static boots check.",
                    riskLevel = "LOW",
                    duration = "18 min",
                    progress = 1.0f,
                    status = "COMPLETED",
                    score = 96,
                    hazardFound = true,
                    checklistCount = 3,
                    totalChecklist = 3,
                    completedAt = System.currentTimeMillis() - 86400000L * 5
                )
            )
            dao.insertModules(defaults)
        }
    }

    suspend fun updateHazardDetected(id: String) {
        val module = when (id) {
            "1" -> {
                dao.updateModuleProgress(
                    id = id,
                    progress = 0.75f,
                    status = "IN_PROGRESS",
                    score = 85,
                    hazardFound = true,
                    checklistCount = 2,
                    completedAt = null
                )
            }
            "2" -> {
                dao.updateModuleProgress(
                    id = id,
                    progress = 0.80f,
                    status = "IN_PROGRESS",
                    score = 90,
                    hazardFound = true,
                    checklistCount = 2,
                    completedAt = null
                )
            }
            else -> {
                dao.updateModuleProgress(
                    id = id,
                    progress = 0.70f,
                    status = "IN_PROGRESS",
                    score = 80,
                    hazardFound = true,
                    checklistCount = 2,
                    completedAt = null
                )
            }
        }
    }

    suspend fun completeModule(id: String, score: Int) {
        dao.updateModuleProgress(
            id = id,
            progress = 1.0f,
            status = "COMPLETED",
            score = score,
            hazardFound = true,
            checklistCount = 3,
            completedAt = System.currentTimeMillis()
        )
    }

    suspend fun resetModule(id: String) {
        dao.resetModule(id)
    }
}
