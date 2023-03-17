package com.example.multiTaskingApp.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.multiTaskingApp.data.entities.Analytics
import com.example.multiTaskingApp.data.entities.TaskAttempt
import kotlinx.coroutines.flow.Flow
import java.util.*

/**
 * The Room Magic is in this file, where you map a method call to an SQL query.
 *
 * When you are using complex data types, such as Date, you have to also supply type converters.
 * To keep this example basic, no types that require type converters are used.
 * See the documentation at
 * https://developer.android.com/topic/libraries/architecture/room.html#type-converters
 */

@Dao
interface AnalyticsDao {
    // ### Get Queries ---------------------------

    // The flow always holds/caches latest version of data. Notifies its observers when the
    // data has changed.
    /**
     * Returns the entire list of analysis from analytics_table
     */
    @Query("SELECT * FROM analytics_table")
    fun getAnalytics(): Flow<List<Analytics>>

    /**
     * Returns the entire list of task attempts from task_attempt_table
     */
    @Query("SELECT * FROM task_attempt_table")
    fun getTaskAttempts(): Flow<List<TaskAttempt>>

    // ### Insert Queries ---------------------------

    /**
     * Insert the given analytics record to analytics_table
     *
     * @param analytics Analytics (entire row)
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnalytics(analytics: Analytics)


    /**
     * Insert the given task attempt to task_attempt_table
     *
     * @param taskAttempt Task Attempt (entire row)
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTaskAttempt(taskAttempt: TaskAttempt)

    // ### Update Queries ---------------------------'

    /**
     * Update number of attempts with given taskId in analytics_table
     */
    @Query("UPDATE analytics_table SET attempts = :attempts WHERE task_id = :taskId")
    suspend fun updateAttempts(taskId: String, attempts: Int)

    /**
     * Update end date time with given taskId in analytics_table
     */
    @Query("UPDATE analytics_table SET task_end_datetime = :taskEndDateTime WHERE task_id = :taskId")
    suspend fun updateTaskEndDateTime(taskId: String, taskEndDateTime: Date)

    /**
     * Update delta date time with given taskId in analytics_table
     */
    @Query("UPDATE analytics_table SET task_delta_datetime = :taskDeltaDateTime WHERE task_id = :taskId")
    suspend fun updateTaskDeltaDateTime(taskId: String, taskDeltaDateTime: Float)

    /**
     * Update detected currency with given taskId in analytics_table
     */
    @Query("UPDATE analytics_table SET detected_currency = :detectedCurrency WHERE task_id = :taskId")
    suspend fun updateDetectedCurrency(taskId: String, detectedCurrency: String)

    /**
     * Update end date time with given taskId and attemptNo in task_attempt_table
     */
    @Query("UPDATE task_attempt_table SET attempt_end_datetime = :attemptEndDateTime WHERE (task_id = :taskId AND attempt_no = :attemptNo)")
    suspend fun updateAttemptEndDateTime(taskId: String, attemptNo: Int, attemptEndDateTime: Date)

    /**
     * Update delta date time with given taskId and attemptNo in task_attempt_table
     */
    @Query("UPDATE task_attempt_table SET attempt_delta_datetime = :attemptDeltaDateTime WHERE (task_id = :taskId AND attempt_no = :attemptNo)")
    suspend fun updateAttemptDeltaDateTime(
        taskId: String,
        attemptNo: Int,
        attemptDeltaDateTime: Float
    )

    /**
     * Update user response with given taskId and attemptNo in task_attempt_table
     */
    @Query("UPDATE task_attempt_table SET user_response = :userResponse WHERE (task_id = :taskId AND attempt_no = :attemptNo)")
    suspend fun updateUserResponse(taskId: String, attemptNo: Int, userResponse: String)

    // ### Delete Queries ---------------------------

    /**
     * Delete all the analytics from analytics_table
     */
    @Query("DELETE FROM analytics_table")
    suspend fun deleteAllAnalytics()

    /**
     * Delete all the task attempts from task_attempt_table
     */
    @Query("DELETE FROM task_attempt_table")
    suspend fun deleteAllTaskAttempts()
}