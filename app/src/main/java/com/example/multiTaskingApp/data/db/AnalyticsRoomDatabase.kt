package com.example.multiTaskingApp.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.multiTaskingApp.data.dao.AnalyticsDao
import com.example.multiTaskingApp.data.entities.Analytics
import com.example.multiTaskingApp.data.entities.TaskAttempt
import com.example.multiTaskingApp.utils.DateTimeConverter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * This is the backend. The database. This used to be done by the OpenHelper.
 * The fact that this has very few comments emphasizes its coolness.
 */
@Database(entities = [Analytics::class, TaskAttempt::class], version = 1)
@TypeConverters(DateTimeConverter::class)

abstract class AnalyticsRoomDatabase : RoomDatabase() {

    abstract fun analyticsDao(): AnalyticsDao

    companion object {
        @Volatile
        private var INSTANCE: AnalyticsRoomDatabase? = null

        private const val DATABASE_NAME = "analytics_database"

        fun getDatabase(
            context: Context,
            scope: CoroutineScope
        ): AnalyticsRoomDatabase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AnalyticsRoomDatabase::class.java,
                    DATABASE_NAME
                )
                    // Wipes and rebuilds instead of migrating if no Migration object.
                    // Migration is not part of this codelab.
                    .fallbackToDestructiveMigration()
                    .addCallback(AnalyticsDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                // return instance
                instance
            }
        }

        private class AnalyticsDatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            /**
             * Override the onCreate method to populate the database.
             */
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // If you want to keep the data through app restarts,
                // comment out the following line.
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateDatabase(database.analyticsDao())
                    }
                }
            }
        }

        /**
         * Populate the database in a new coroutine.
         * If you want to start with more words, just add them.
         */
        suspend fun populateDatabase(analyticsDao: AnalyticsDao) {
            // Start the app with a clean database every time.
            // Not needed if you only populate on creation.
            analyticsDao.deleteAllAnalytics()
            analyticsDao.deleteAllTaskAttempts()

            // Here, default data can be inserted
        }
    }
}
