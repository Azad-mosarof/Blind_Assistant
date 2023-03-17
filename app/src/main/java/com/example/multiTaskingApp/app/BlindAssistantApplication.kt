package com.example.multiTaskingApp.app

import android.app.Application
import com.example.multiTaskingApp.data.db.AnalyticsRoomDatabase
import com.example.multiTaskingApp.repository.AnalyticsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class BlindAssistantApplication : Application() {
    // No need to cancel this scope as it'll be torn down with the process
    val applicationScope = CoroutineScope(SupervisorJob())

    // Using by lazy so the database and the repository are only created when they're needed
    // rather than when the application starts
    val database by lazy { AnalyticsRoomDatabase.getDatabase(this, applicationScope) }
    val repository by lazy { AnalyticsRepository(database.analyticsDao()) }
}
