package com.example.multiTaskingApp.viewmodels

import android.content.Context
import android.graphics.Bitmap
import android.media.ThumbnailUtils
import android.util.Log
import androidx.lifecycle.*
import com.example.multiTaskingApp.*
import com.example.multiTaskingApp.data.entities.Analytics
import com.example.multiTaskingApp.data.entities.TaskAttempt
import com.example.multiTaskingApp.ml.Model
import com.example.multiTaskingApp.repository.AnalyticsRepository
import com.example.multiTaskingApp.ANALYTICS_FILE_NAME
import com.example.multiTaskingApp.ANALYTICS_FOLDER_NAME
import com.example.multiTaskingApp.imageSize
import com.example.multiTaskingApp.utils.ReadWriteExternalStorage
import com.example.multiTaskingApp.utils.ShareDataToOthers
import com.example.multiTaskingApp.utils.getCurrentDateTime
import com.example.multiTaskingApp.utils.getRandomUniqueString
import kotlinx.coroutines.launch
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*
import kotlin.math.min

/**
 * View Model to keep a reference to the frame repository and
 * an up-to-date list of all frames.
 */

/**
 * Frame Activity View Model
 */
class AnalyticsViewModel(private val repository: AnalyticsRepository) : ViewModel() {
    // Using LiveData and caching what allFrames returns has several benefits:
    // - We can put an observer on the data (instead of polling for changes) and only update the
    //   the UI when the data actually changes.
    // - Repository is completely separated from the UI through the ViewModel.

    // Flow ID
    var currFlowId = MutableLiveData<String>()

    // Task ID
    var currTaskId = MutableLiveData<String>()

    // Task No
    var currTaskNo = MutableLiveData<Int>()

    // Number of attempts
    var taskAttempts = MutableLiveData<Int>()

    // Task Start Date Time (Date)
    var taskStartDateTime = MutableLiveData<Date>()

    // Task End Date Time (Date)
    var taskEndDateTime = MutableLiveData<Date>()

    // Attempt Start Date Time (Date)
    var attemptStartDateTime = MutableLiveData<Date>()

    // Attempt End Date Time (Date)
    var attemptEndDateTime = MutableLiveData<Date>()

    // Detected currency
    var currencyConfidences = MutableLiveData<FloatArray>()

    // For checking whether we need to analyze
    var analyzeFrames = MutableLiveData<Boolean>()

    /**
     * For list of analytics
     */
    val allAnalytics: LiveData<List<Analytics>> =
        repository.allAnalytics.asLiveData()

    /**
     * For list of task attempts
     */
    val allTaskAttempts: LiveData<List<TaskAttempt>> =
        repository.allTaskAttempts.asLiveData()

    /**
     * Launching a new coroutine to insert the data in a non-blocking way
     *
     * @param analytics Analytics (entire row)
     */
    private fun insertAnalytics(analytics: Analytics) = viewModelScope.launch {
        repository.insertAnalytics(analytics)
    }

    /**
     * Launching a new coroutine to insert the data in a non-blocking way
     *
     * @param taskAttempt Analytics (entire row)
     */
    private fun insertTaskAttempt(taskAttempt: TaskAttempt) = viewModelScope.launch {
        repository.insertTaskAttempt(taskAttempt)
    }

    /**
     * Update Number of Attempts Coroutine
     *
     * @param taskId Task ID
     * @param attempts Number of attempts
     */
    private fun updateAttempts(taskId: String, attempts: Int) = viewModelScope.launch {
        repository.updateAttempts(taskId, attempts)
    }

    /**
     * Update Task End Date Time Coroutine
     *
     * @param taskId Task ID
     * @param taskEndDateTime The End Date time of the task
     */
    private fun updateTaskEndDateTime(taskId: String, taskEndDateTime: Date) =
        viewModelScope.launch {
            repository.updateTaskEndDateTime(taskId, taskEndDateTime)
        }

    /**
     * Update Task Delta Date Time Coroutine
     *
     * @param taskId Task ID
     * @param taskDeltaDateTime The Delta Date time of the task
     */
    private fun updateTaskDeltaDateTime(taskId: String, taskDeltaDateTime: Float) =
        viewModelScope.launch {
            repository.updateTaskDeltaDateTime(taskId, taskDeltaDateTime)
        }

    /**
     * Update Detected Currency Coroutine
     *
     * @param taskId Task ID
     * @param detectedCurrency Current Detected Currency Value
     */
    private fun updateDetectedCurrency(taskId: String, detectedCurrency: String) =
        viewModelScope.launch {
            repository.updateDetectedCurrency(taskId, detectedCurrency)
        }

    /**
     * Update Attempt End Date Time Coroutine
     *
     * @param taskId Task ID
     * @param attemptNo Attempt Number
     * @param attemptEndDateTime The End Date time of the task
     */
    private fun updateAttemptEndDateTime(
        taskId: String,
        attemptNo: Int,
        attemptEndDateTime: Date
    ) =
        viewModelScope.launch {
            repository.updateAttemptEndDateTime(taskId, attemptNo, attemptEndDateTime)
        }

    /**
     * Update Attempt Delta Date Time Coroutine
     *
     * @param taskId Task ID
     * @param attemptNo Attempt Number
     * @param attemptDeltaDateTime The Delta Date time of the task
     */
    private fun updateAttemptDeltaDateTime(
        taskId: String,
        attemptNo: Int,
        attemptDeltaDateTime: Float
    ) =
        viewModelScope.launch {
            repository.updateAttemptDeltaDateTime(taskId, attemptNo, attemptDeltaDateTime)
        }

    /**
     * Update User Response Coroutine
     *
     * @param taskId Task ID
     * @param attemptNo Attempt Number
     * @param userResponse User Response
     */
    private fun updateUserResponse(taskId: String, attemptNo: Int, userResponse: String) =
        viewModelScope.launch {
            repository.updateUserResponse(taskId, attemptNo, userResponse)
        }

    /**
     * Finds the time taken to complete the given task
     *
     * @return Difference between the task start and end date time
     */
    private fun getTaskDeltaTime(): Float {
        val startDateTime = taskStartDateTime.value
        val endDateTime = taskEndDateTime.value
        var diff = 0F

        if (startDateTime != null && endDateTime != null) {
            diff = (endDateTime.time - startDateTime.time).toFloat() / 1000
        }

        return diff
    }

    /**
     * Finds the time taken to complete the current attempt of the given task
     *
     * @return Difference between the attempt start and end date time
     */
    private fun getAttemptDeltaTime(): Float {
        val startDateTime = attemptStartDateTime.value
        val endDateTime = attemptEndDateTime.value
        var diff = 0F

        if (startDateTime != null && endDateTime != null) {
            diff = (endDateTime.time - startDateTime.time).toFloat() / 1000
        }

        return diff
    }

    /*
    /**
     * Reset all values
     */
    fun resetValues() {
        currTaskNo.value = 0
        taskAttempts.value = 0
        taskStartDateTime.value = 0
        taskEndDateTime.value = 0
        attemptStartDateTime.value = 0
        attemptEndDateTime.value = 0
    }
    */

    /**
     * Adds a new flow/session
     */
    fun addFlow() {
        currFlowId.value = getRandomUniqueString()
    }

    private fun addTaskAttempt() {
        val taskAttempt = TaskAttempt(
            taskId = currTaskId.value!!,
            attemptNo = taskAttempts.value!!,
            attemptStartDateTime = attemptStartDateTime.value!!,
            attemptEndDateTime = attemptEndDateTime.value!!,
            attemptDeltaDateTime = getAttemptDeltaTime(),
            userResponse = ""
        )

        // Inserting the new attempt for the current task to DB
        insertTaskAttempt(taskAttempt)
    }

    /**
     * Resets the task variable(s) and add the new task and first attempt to the analytics_table and task_attempt_table respectively
     * Usually called at the beginning of a new task
     */
    fun addTask(taskNo: Int) =
        viewModelScope.launch {
            // Resetting the task variables
            currTaskId.value = getRandomUniqueString()
            currTaskNo.value = taskNo
            taskAttempts.value = 1

            val currentDateTime = getCurrentDateTime()
            taskStartDateTime.value = currentDateTime
            taskEndDateTime.value = currentDateTime
            attemptStartDateTime.value = currentDateTime
            attemptEndDateTime.value = currentDateTime

            val analytics = Analytics(
                flowId = currFlowId.value!!,
                taskId = currTaskId.value!!,
                taskNo = currTaskNo.value!!,
                attempts = taskAttempts.value!!,
                taskStartDateTime = taskStartDateTime.value!!,
                taskEndDateTime = taskEndDateTime.value!!,
                taskDeltaDateTime = getTaskDeltaTime(),
                detectedCurrency = ""
            )

            // Inserting the new analytics to DB
            insertAnalytics(analytics)

            // Inserting the new attempt for the current task to DB
            addTaskAttempt()
        }

    /**
     * Increase the number of attempts
     * It is called after completeAttempt
     */
    fun increaseAttempt() {
        val taskId = currTaskId.value
        if (taskId != null) {
            var currAttempts = taskAttempts.value
            if (currAttempts != null) {
                // Increasing the number of attempts
                ++currAttempts
                taskAttempts.value = currAttempts!!

                // Update number of attempts in analytics table
                updateAttempts(taskId, currAttempts)

                val currentDateTime = getCurrentDateTime()
                attemptStartDateTime.value = currentDateTime
                attemptEndDateTime.value = currentDateTime

                // Inserting the new attempt for the current task to DB
                addTaskAttempt()
            }
        }
    }

    /**
     * Complete the given attempt
     */
    fun completeAttempt() {
        val taskId = currTaskId.value
        val attemptNo = taskAttempts.value
        if (taskId != null && attemptNo != null) {
            // Setting the task end date time
            val currentDateTime = getCurrentDateTime()
            attemptEndDateTime.value = currentDateTime

            // Update attempt end datetime in DB
            updateAttemptEndDateTime(taskId, attemptNo, currentDateTime)

            // Update attempt delta datetime in DB
            updateAttemptDeltaDateTime(taskId, attemptNo, getAttemptDeltaTime())
        }
    }

    /**
     * Completes the given task
     */
    fun completeTask() {
        val taskId = currTaskId.value
        if (taskId != null) {
            // Setting the task end date time
            val currentDateTime = getCurrentDateTime()
            taskEndDateTime.value = currentDateTime

            // Update task end datetime in DB
            updateTaskEndDateTime(taskId, currentDateTime)

            // Update task delta datetime in DB
            updateTaskDeltaDateTime(taskId, getTaskDeltaTime())
        }
    }

    /**
     * Saves the given detected currency (if any)
     */
    fun saveDetectedCurrency(detectedCurrency: String) {
        val taskId = currTaskId.value
        if (taskId != null) {
            // Save detected currency in DB
            updateDetectedCurrency(taskId, detectedCurrency)
        }
    }

    /**
     * Saves the given detected currency (if any)
     */
    fun saveUserResponse(userResponse: String) {
        val taskId = currTaskId.value
        val attemptNo = taskAttempts.value
        if (taskId != null && attemptNo != null) {
            // Save user response in DB
            updateUserResponse(taskId, attemptNo, userResponse)
        }
    }

    /**
     * Starts analyzing the frame
     */
    fun startAnalyzingFrames() = viewModelScope.launch {
        // Clearing any old frame data
        // Update the currencyConfidences (Reset to Of)
        currencyConfidences.value = floatArrayOf(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f)

        // Start analysing frames
        analyzeFrames.value = true
    }

    /**
     * Stop analyzing the frame
     */
    fun stopAnalyzingFrames() = viewModelScope.launch {
        // Stop analysing frames
        analyzeFrames.value = false
    }

    /**
     * Add the current confidence float array to the currencyConfidences List<FloatArray>
     */
    private fun addConfidence(confidence: FloatArray) {
        val currencyConfidencesValue = currencyConfidences.value
        if (currencyConfidencesValue != null) {
            var maxPos = 0
            var maxConfidence = 0f
            for (i in confidence.indices) {
                if (confidence[i] > maxConfidence) {
                    maxConfidence = confidence[i]
                    maxPos = i
                }
            }

            ++currencyConfidencesValue[maxPos]

            // For logging purposes
            var currencyValues = ""
            for (i in currencyConfidencesValue.indices) {
                val currentConfidence = currencyConfidencesValue[i].toString()
                currencyValues += "$currentConfidence ,"
            }
            Log.d("Confidence Log", currencyValues)
        }

        // Update the currencyConfidences
        currencyConfidences.value = currencyConfidencesValue!!
    }

    /**
     * Classify current bitmap image to most probable currency type
     *
     * @param image Input image
     * @param context Context
     */
    private fun classifyImage(image: Bitmap, context: Context) {
        try {
            val currencyDetectorModel = Model.newInstance(context)

            // Creates inputs for reference.
            val inputFeature0 =
                TensorBuffer.createFixedSize(
                    intArrayOf(1, imageSize, imageSize, 3),
                    DataType.FLOAT32
                )
            val byteBuffer = ByteBuffer.allocateDirect(4 * imageSize * imageSize * 3)
            byteBuffer.order(ByteOrder.nativeOrder())

            // get 1D array of 224 * 224 pixels in image
            val intValues = IntArray(imageSize * imageSize)
            image.getPixels(intValues, 0, image.width, 0, 0, image.width, image.height)

            // iterate over pixels and extract R, G, and B values. Add to bytebuffer.
            var pixel = 0
            for (i in 0 until imageSize) {
                for (j in 0 until imageSize) {
                    val `val` = intValues[pixel++] // RGB

                    // Normalizing image pixels
                    byteBuffer.putFloat((`val` shr 16 and 0xFF) * (1f / 255f))
                    byteBuffer.putFloat((`val` shr 8 and 0xFF) * (1f / 255f))
                    byteBuffer.putFloat((`val` and 0xFF) * (1f / 255f))

//                    byteBuffer.putFloat((`val` shr 16 and 0xFF) * (1f / 1f))
//                    byteBuffer.putFloat((`val` shr 8 and 0xFF) * (1f / 1f))
//                    byteBuffer.putFloat((`val` and 0xFF) * (1f / 1f))
                }
            }
            inputFeature0.loadBuffer(byteBuffer)

            // Runs model inference and gets result.
            val outputs = currencyDetectorModel.process(inputFeature0)
            val outputFeature0 = outputs.outputFeature0AsTensorBuffer
            val confidences = outputFeature0.floatArray

            // Adding confidence function for
            addConfidence(confidences)

            // Releases model resources if no longer used.
            currencyDetectorModel.close()
        } catch (e: IOException) {
            // TODO Handle the exception
        }
    }

    fun analyzeCurrentCameraFrame(bitmap: Bitmap?, context: Context) =
        viewModelScope.launch {
            if (bitmap != null) {
                val dimension = min(bitmap.width, bitmap.height)

                // Get the thumbnail and scale it
                var bitmapImageFinal =
                    ThumbnailUtils.extractThumbnail(bitmap, dimension, dimension)

                // Scaling image bitmap to image size
                bitmapImageFinal =
                    Bitmap.createScaledBitmap(bitmapImageFinal, imageSize, imageSize, false)

                // Classify the image
                classifyImage(bitmapImageFinal, context)
            }
        }

    fun exportCollectedData(context: Context) = viewModelScope.launch {
        val analyticsList = allAnalytics.value
        val taskAttemptList = allTaskAttempts.value

        // Create the excel file
        val readWriteExternalStorage = ReadWriteExternalStorage(
            ANALYTICS_FOLDER_NAME,
            ANALYTICS_FILE_NAME,
            context
        )

        val resultFile =
            readWriteExternalStorage.analyticsExportToExcel(
                analyticsList as ArrayList<Analytics>?,
                taskAttemptList as ArrayList<TaskAttempt>?
            )

        // Sharing the file
        // TODO: Later set the proper pre-text
        val shareDataToOthers =
            ShareDataToOthers(
                resultFile,
                context,
                "Something"
            )
        shareDataToOthers.exportIntent()
    }
}

class AnalyticsViewModelFactory(private val repository: AnalyticsRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AnalyticsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AnalyticsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
