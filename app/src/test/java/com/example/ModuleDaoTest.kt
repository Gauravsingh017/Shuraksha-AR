package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppDatabase
import com.example.data.ModuleDao
import com.example.data.ModuleEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ModuleDaoTest {
    private lateinit var db: AppDatabase
    private lateinit var dao: ModuleDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.moduleDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertAndRetrieveModules() = runBlocking {
        val testModule = ModuleEntity(
            id = "1",
            title = "Underground Gas & Ventilation",
            category = "GAS_VENTILATION",
            description = "Test description",
            riskLevel = "HIGH",
            duration = "42 min",
            progress = 0.5f,
            status = "IN_PROGRESS"
        )
        dao.insertModules(listOf(testModule))

        val retrieved = dao.getAllModules().first()
        assertEquals(1, retrieved.size)
        assertEquals("Underground Gas & Ventilation", retrieved[0].title)
    }

    @Test
    fun updateModuleProgressAndCompletedCount() = runBlocking {
        val testModule = ModuleEntity(
            id = "2",
            title = "Fire & Emergency Evacuation",
            category = "FIRE_EVACUATION",
            description = "Follow floor arrows",
            riskLevel = "HIGH",
            duration = "28 min",
            progress = 0.0f,
            status = "NOT_STARTED"
        )
        dao.insertModules(listOf(testModule))

        dao.updateModuleProgress(
            id = "2",
            progress = 1.0f,
            status = "COMPLETED",
            score = 96,
            hazardFound = true,
            checklistCount = 3,
            completedAt = System.currentTimeMillis()
        )

        val completedCount = dao.getCompletedCount().first()
        assertEquals(1, completedCount)

        val updated = dao.getModuleById("2").first()
        assertEquals("COMPLETED", updated?.status)
        assertEquals(96, updated?.score)
        assertTrue(updated?.hazardFound == true)
    }
}
