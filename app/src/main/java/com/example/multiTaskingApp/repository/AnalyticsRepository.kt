package com.example.multiTaskingApp.repository

import com.example.multiTaskingApp.data.dao.AnalyticsDao
import com.example.multiTaskingApp.data.entities.Analytics
import com.example.multiTaskingApp.data.entities.TaskAttempt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.*

/**
 * Abstracted Repository as promoted by the Architecture Guide.
 * https://developer.android.com/topic/libraries/architecture/guide.html
 */
class AnalyticsRepository(private val analyticsDao: AnalyticsDao) {
    // ### Get Data ---------------------------

    // Room executes all queries on a separate thread.
    // Observed Flow will notify the observer when the data has changed.

    /**
     * Returns the entire list of analysis from analytics_table
     * Used for exporting the analytics data
     */
    val allAnalytics: Flow<List<Analytics>> = analyticsDao.getAnalytics()

    /**
     * Returns the entire list of analysis from analytics_table
     * Used for exporting the analytics data
     */
    val allTaskAttempts: Flow<List<TaskAttempt>> = analyticsDao.getTaskAttempts()

    // ### Insert Data ---------------------------

    /**
     * Insert new analytics
     * Used for inserting a new analysis at the beginning of each task
     *
     * @param analytics Analytics (entire row)
     */
    suspend fun insertAnalytics(analytics: Analytics) = withContext(Dispatchers.IO) {
        analyticsDao.insertAnalytics(analytics)
    }

    /**
     * Insert new task attempt
     * Used for inserting a new task attempt whenever a new attempt is started for a given task
     *
     * @param taskAttempt Task Attempt (entire row)
     */
    suspend fun insertTaskAttempt(taskAttempt: TaskAttempt) = withContext(Dispatchers.IO) {
        analyticsDao.insertTaskAttempt(taskAttempt)
    }

    // ### Update Data ---------------------------

    /**
     * Update number of attempts with given taskId in analytics_table
     *
     * @param taskId Task ID
     * @param attempts Number of attempts
     */
    suspend fun updateAttempts(taskId: String, attempts: Int) =
        withContext(Dispatchers.IO) {
            analyticsDao.updateAttempts(taskId, attempts)
        }

    /**
     * Update end date time with given taskId in analytics_table
     *
     * @param taskId Task ID
     * @param taskEndDateTime The End Date time of the task
     */
    suspend fun updateTaskEndDateTime(taskId: String, taskEndDateTime: Date) =
        withContext(Dispatchers.IO) {
            analyticsDao.updateTaskEndDateTime(taskId, taskEndDateTime)
        }

    /**
     * Update delta date time with given taskId in analytics_table
     *
     * @param taskId Task ID
     * @param taskDeltaDateTime The Delta Date time of the task
     */
    suspend fun updateTaskDeltaDateTime(taskId: String, taskDeltaDateTime: Float) =
        withContext(Dispatchers.IO) {
            analyticsDao.updateTaskDeltaDateTime(taskId, taskDeltaDateTime)
        }

    /**
     * Update detected currency with given taskId in analytics_table
     * @param taskId Task ID
     * @param detectedCurrency Current Detected Currency Value
     */
    suspend fun updateDetectedCurrency(taskId: String, detectedCurrency: String) =
        withContext(Dispatchers.IO) {
            analyticsDao.updateDetectedCurrency(taskId, detectedCurrency)
        }

    /**
     * Update end date time with given taskId and attemptNo in task_attempt_table
     *
     * @param taskId Task ID
     * @param attemptNo Task ID
     * @param attemptEndDateTime The End Date time of the attempt for the given task
     */
    suspend fun updateAttemptEndDateTime(
        taskId: String,
        attemptNo: Int,
        attemptEndDateTime: Date
    ) =
        withContext(Dispatchers.IO) {
            analyticsDao.updateAttemptEndDateTime(taskId, attemptNo, attemptEndDateTime)
        }

    /**
     * Update delta date time with given taskId and attemptNo in task_attempt_table
     *
     * @param taskId Task ID
     * @param attemptNo Task ID
     * @param attemptDeltaDateTime The Delta Date time of the attempt for the given task
     */
    suspend fun updateAttemptDeltaDateTime(
        taskId: String,
        attemptNo: Int,
        attemptDeltaDateTime: Float
    ) =
        withContext(Dispatchers.IO) {
            analyticsDao.updateAttemptDeltaDateTime(taskId, attemptNo, attemptDeltaDateTime)
        }

    /**
     * Update detected currency with given taskId in analytics_table
     * @param taskId Task ID
     * @param attemptNo Attempt Number
     * @param detectedCurrency Current Detected Currency Value
     */
    suspend fun updateUserResponse(taskId: String, attemptNo: Int, detectedCurrency: String) =
        withContext(Dispatchers.IO) {
            analyticsDao.updateUserResponse(taskId, attemptNo, detectedCurrency)
        }

    // ### Delete Data ---------------------------
}
