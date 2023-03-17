package com.example.multiTaskingApp.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import java.util.*

/**
 * A basic class representing an entity that is a row in a one-column database table.
 *
 * @ Entity - You must annotate the class as an entity and supply a table name if not class name.
 *
 * @ PrimaryKey - You must identify the primary key.
 *
 * @ ColumnInfo - You must supply the column name if it is different from the variable name.
 *
 *
 * See the documentation for the full rich set of annotations.
 * https://developer.android.com/topic/libraries/architecture/room.html
 */


/**
 * Used for storing the analytics data
 *
 * @param flowId Flow ID
 * @param taskId Task ID
 * @param taskNo Task Number
 * @param attempts Number of attempts
 * @param taskStartDateTime Task Start Date Time
 * @param taskEndDateTime Task End Date Time
 * @param taskDeltaDateTime Total time required to complete the task (End - Start)
 * @param detectedCurrency Detected currency value
 */
@Entity(tableName = "analytics_table", primaryKeys = ["task_id"])
data class Analytics(
    @ColumnInfo(name = "flow_id") val flowId: String,
    @ColumnInfo(name = "task_id") val taskId: String,
    @ColumnInfo(name = "task_no") val taskNo: Int,
    @ColumnInfo(name = "attempts") val attempts: Int?,
    @ColumnInfo(name = "task_start_datetime") val taskStartDateTime: Date?,
    @ColumnInfo(name = "task_end_datetime") val taskEndDateTime: Date?,
    @ColumnInfo(name = "task_delta_datetime") val taskDeltaDateTime: Float?,
    @ColumnInfo(name = "detected_currency") val detectedCurrency: String?
) {
    override fun hashCode(): Int {
        return Objects.hash(
            flowId,
            taskId,
            taskNo,
            attempts,
            taskStartDateTime,
            taskEndDateTime,
            taskDeltaDateTime,
            detectedCurrency
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false

        val analytics: Analytics = other as Analytics

        return flowId == analytics.flowId && taskId == analytics.taskId && taskNo == analytics.taskNo && attempts == analytics.attempts
                && taskStartDateTime == analytics.taskStartDateTime && taskEndDateTime == analytics.taskEndDateTime
                && taskDeltaDateTime == analytics.taskDeltaDateTime && detectedCurrency == analytics.detectedCurrency
    }
}

/**
 * Used for storing the attempts data
 *
 * @param taskId Task ID
 * @param attemptNo Attempt Number of given taskID
 * @param attemptStartDateTime Attempt Start Date Time
 * @param attemptEndDateTime Attempt End Date Time
 * @param attemptDeltaDateTime Total time required to complete the attempt for the given taskID (End - Start)
 * @param userResponse User response (feedback)
 */
@Entity(tableName = "task_attempt_table", primaryKeys = ["task_id", "attempt_no"])
data class TaskAttempt(
    @ColumnInfo(name = "task_id") val taskId: String,
    @ColumnInfo(name = "attempt_no") val attemptNo: Int,
    @ColumnInfo(name = "attempt_start_datetime") val attemptStartDateTime: Date?,
    @ColumnInfo(name = "attempt_end_datetime") val attemptEndDateTime: Date?,
    @ColumnInfo(name = "attempt_delta_datetime") val attemptDeltaDateTime: Float?,
    @ColumnInfo(name = "user_response") val userResponse: String?
) {
    override fun hashCode(): Int {
        return Objects.hash(
            taskId,
            attemptNo,
            attemptStartDateTime,
            attemptEndDateTime,
            attemptDeltaDateTime,
            userResponse
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false

        val taskAttempt: TaskAttempt = other as TaskAttempt

        return taskId == taskAttempt.taskId && attemptNo == taskAttempt.attemptNo && attemptStartDateTime == taskAttempt.attemptStartDateTime
                && attemptEndDateTime == taskAttempt.attemptEndDateTime
                && attemptDeltaDateTime == taskAttempt.attemptDeltaDateTime
                && userResponse == taskAttempt.userResponse
    }
}