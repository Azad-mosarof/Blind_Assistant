package com.example.multiTaskingApp

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.media.AudioAttributes
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import android.util.Size
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.multiTaskingApp.app.BlindAssistantApplication
import com.example.multiTaskingApp.databinding.ActivityMainBinding
import com.example.multiTaskingApp.utils.YuvToRgbConverter
import com.example.multiTaskingApp.viewmodels.AnalyticsViewModel
import com.example.multiTaskingApp.viewmodels.AnalyticsViewModelFactory
import java.util.*
import java.util.concurrent.Executor

class MainActivity : AppCompatActivity(), ImageAnalysis.Analyzer {

    private lateinit var viewBinding: ActivityMainBinding

    private lateinit var cameraExecutor: Executor
    private lateinit var cam: Camera

    private var textViewList = listOf<TextView>()
    private var progressBarList = listOf<ProgressBar>()
    private lateinit var currVibrator: Vibrator
    private lateinit var detectedCurrencyValue: String
    private var showDetailedLayout: Boolean = false
    var changeLangMode: Boolean = false

    /**
     * Constants used for converting Image Proxy to Bitmap
     */
    private lateinit var yuvToRgbConverter: YuvToRgbConverter
    private lateinit var bitmapBuffer: Bitmap
    private lateinit var rotationMatrix: Matrix

    // For Text to Speech
    private var tts: TextToSpeech? = null

    private val analyticsViewModel: AnalyticsViewModel by viewModels {
        AnalyticsViewModelFactory((application as BlindAssistantApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        initUI()
        subscribeUI()
    }

    /**
     * Initialize the UI variables
     */
    private fun initUI() {
        showDetailedLayout = false

        detectedCurrencyValue = ""

        textViewList = listOf(
            viewBinding.val10, viewBinding.val20, viewBinding.val50, viewBinding.val100,
            viewBinding.val200, viewBinding.val500, viewBinding.val2000, viewBinding.valBack
        )

        progressBarList = listOf(
            viewBinding.val10ProgressBar,
            viewBinding.val20ProgressBar,
            viewBinding.val50ProgressBar,
            viewBinding.val100ProgressBar,
            viewBinding.val200ProgressBar,
            viewBinding.val500ProgressBar,
            viewBinding.val2000ProgressBar,
            viewBinding.valBackProgressBar
        )

        // Hiding the detailed layout
        if (showDetailedLayout) {
            viewBinding.detailedLayout.visibility = View.VISIBLE
            viewBinding.linearLayout3.visibility = View.VISIBLE
        } else {
            viewBinding.detailedLayout.visibility = View.GONE
            viewBinding.linearLayout3.visibility = View.GONE
        }

        // For converting image proxy to bitmap
        yuvToRgbConverter = YuvToRgbConverter(applicationContext)

        // For cameraX API
        cameraExecutor = ContextCompat.getMainExecutor(this)

        // Get instance of Vibrator from current Context
        currVibrator = this.getSystemService(VIBRATOR_SERVICE) as Vibrator

        // Creating instance of TextToSpeech
        tts = TextToSpeech(this@MainActivity) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = tts!!.setLanguage(if(changeLangMode) Locale.forLanguageTag("bn-IN") else Locale.US)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e(TAG, "The Language not supported!")
                } else {
                    tts!!.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                        override fun onStart(utteranceId: String) {
                            handleOnStartUtterance(utteranceId)
                        }

                        override fun onDone(utteranceId: String) {
                            handleOnDoneUtterance(utteranceId)
                        }

                        @Deprecated("Deprecated in Java")
                        override fun onError(utteranceId: String) {
                            handleOnErrorUtterance(utteranceId)
                        }
                    })

                    // Adding a flow for the current session
                    analyticsViewModel.addFlow()

                    // Request camera permissions
                    if (allPermissionsGranted()) {
                        // Prepare and start the camera
                        startCamera()

                        startTask1()
                    } else {
                        ActivityCompat.requestPermissions(
                            this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
                        )
                    }
                }
            }
        }
    }

    /**
     * Subscribe to any data changes
     */
    private fun subscribeUI() {
        subscribeToAnalyticsData()
        subscribeToTaskAttemptData()
        subscribeToAnalyzeFrames()
    }

    /**
     * Subscribes to any change to analytics data
     */
    private fun subscribeToAnalyticsData() {
        analyticsViewModel.allAnalytics.observe(this@MainActivity) {
        }
    }

    /**
     * Subscribes to any change to task attempt data
     */
    private fun subscribeToTaskAttemptData() {
        analyticsViewModel.allTaskAttempts.observe(this@MainActivity) { taskAttempt ->
        }
    }

    /**
     * Subscribes to any change to analyze frame
     */
    private fun subscribeToAnalyzeFrames() {
        analyticsViewModel.analyzeFrames.observe(this@MainActivity) { analyzeFrames ->
            if (analyzeFrames == false) {
                // Completed an analysis
                val currencyConfidences = analyticsViewModel.currencyConfidences.value
                if (currencyConfidences != null) {
                    // find the index of the class with the biggest confidence.
                    var maxPos = 0
                    var maxConfidence = 0f
                    for (i in currencyConfidences.indices) {
                        // Setting the text Views for all currency types
                        val confPrintable: Double =
                            String.format(Locale.ENGLISH, "%.3f", currencyConfidences[i] * 100)
                                .toDouble()
                        val currConfidence = classes[i] + " (" + confPrintable + "%)"
                        textViewList[i].text = currConfidence
                        progressBarList[i].progress = confPrintable.toInt()

                        if (currencyConfidences[i] > maxConfidence) {
                            maxConfidence = currencyConfidences[i]
                            maxPos = i
                        }
                    }

                    detectedCurrencyValue = if(changeLangMode) _classes[maxPos] else classes[maxPos]

                    // Setting the text value
                    viewBinding.currencyValueTV.text = detectedCurrencyValue

                    var instructionText = if(changeLangMode) "এটি একটি $detectedCurrencyValue টাকার নোট।" else "It is a $detectedCurrencyValue note."
                    if (detectedCurrencyValue == CURRENCY_VAL_BACKGROUND) {
                        instructionText = if(changeLangMode) "কোন মুদ্রা সনাক্ত করা হয়নি" else "No currency detected!"
                    }

                    analyticsViewModel.saveDetectedCurrency(detectedCurrencyValue)

                    playVibration()

                    playInstruction7(instructionText)
                }
                else{
                    // TODO: Handle exception (may be retry)
                }
            }
        }
    }

    /**
     * Play vibration according to detected value
     */
    private fun playVibration() {
        // Play vibration according to the detected Value
        var vibrationPattern: LongArray? = null

        when (detectedCurrencyValue) {
            CURRENCY_VAL_10 -> {
                vibrationPattern = VIBRATE_PATTER_VAL_10
            }
            CURRENCY_VAL_20 -> {
                vibrationPattern = VIBRATE_PATTER_VAL_20
            }
            CURRENCY_VAL_50 -> {
                vibrationPattern = VIBRATE_PATTER_VAL_50
            }
            CURRENCY_VAL_100 -> {
                vibrationPattern = VIBRATE_PATTER_VAL_100
            }
            CURRENCY_VAL_200 -> {
                vibrationPattern = VIBRATE_PATTER_VAL_200
            }
            CURRENCY_VAL_500 -> {
                vibrationPattern = VIBRATE_PATTER_VAL_500
            }
            CURRENCY_VAL_2000 -> {
                vibrationPattern = VIBRATE_PATTER_VAL_2000
            }
        }

        // Play the vibration
        if (vibrationPattern != null) {

            // create VibrationEffect instance and createWaveform of vibrationWaveFormDurationPattern
            // -1 here is the parameter which indicates that the vibration shouldn't be repeated.
            // The '-1' here means to vibrate once, as '-1' is out of bounds in the pattern array
            val vibrationEffect =
                VibrationEffect.createWaveform(vibrationPattern, -1)

            // it is safe to cancel all the vibration taking place currently
            currVibrator.cancel()

            // now initiate the vibration of the device
            currVibrator.vibrate(
                vibrationEffect,
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_ASSISTANT)
                    .build()
            )
        }
    }

    /**
     * Turn On/Off flash light as per current illumination
     *
     * @param image Input Image Proxy
     */
    private fun controlFlashLight(image: ImageProxy) {
        val buffer = image.planes[0].buffer
        val byteArray = ByteArray(buffer.capacity())
        buffer.get(byteArray)
        val pixels = byteArray.map { it.toInt() and 0xFF }
        val illumination = pixels.average()

//        val luminosityText = "$illumination"
//        viewBinding.appName!!.text = luminosityText

        // Once turned on, it will stay on
        if (cam.cameraInfo.hasFlashUnit()) {
            if (illumination < 50) {
                // Turn on the flash
                cam.cameraControl.enableTorch(true) // or false
            }
//            else {
//                // Turn off the flash
//                cam.cameraControl.enableTorch(false) // or false
//            }
        }
    }

    private fun startAnalyzingFrames() {
        analyticsViewModel.startAnalyzingFrames()
    }

    private fun stopAnalyzingFrames() {
        analyticsViewModel.stopAnalyzingFrames()
    }


    override fun analyze(image: ImageProxy) {
        // TODO: Move it off the main thread via Coroutine

        // Turn On/Off flash light as per current illumination
        controlFlashLight(image)

        val analyzeFrames = analyticsViewModel.analyzeFrames.value
        if (analyzeFrames != null && analyzeFrames == true) {
            // Rescale the bitmap and classify image
            val bitmapImage = toBitmap(image)

            if (bitmapImage != null) {
                analyticsViewModel.analyzeCurrentCameraFrame(
                    bitmapImage,
                    this@MainActivity
                )
            }
        }

        // Close the image,this tells CameraX to feed the next image to the analyzer
        image.close()
    }

    @SuppressLint("UnsafeExperimentalUsageError", "UnsafeOptInUsageError")
    private fun toBitmap(imageProxy: ImageProxy): Bitmap? {

        val image = imageProxy.image ?: return null

        // Initialise Buffer
        if (!::bitmapBuffer.isInitialized) {
            // The image rotation and RGB image buffer are initialized only once
            Log.d(TAG, "Initialize toBitmap()")
            rotationMatrix = Matrix()
            rotationMatrix.postRotate(imageProxy.imageInfo.rotationDegrees.toFloat())
            bitmapBuffer = Bitmap.createBitmap(
                imageProxy.width, imageProxy.height, Bitmap.Config.ARGB_8888
            )
        }

        // Pass image to an image analyser
        yuvToRgbConverter.yuvToRgb(image, bitmapBuffer)

        // Create the Bitmap in the correct orientation
        return Bitmap.createBitmap(
            bitmapBuffer,
            0,
            0,
            bitmapBuffer.width,
            bitmapBuffer.height,
            rotationMatrix,
            false
        )
    }

    /**
     * Prepare and start the Camera
     */
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewBinding.previewLayout.surfaceProvider)
                }

            val imageAnalyzer = ImageAnalysis.Builder()
                .setTargetResolution(Size(imageSize, imageSize))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            // Setting the analyzer
            imageAnalyzer.setAnalyzer(cameraExecutor, this)


            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cam = cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageAnalyzer
                )
            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun playInstruction(instructionText: String, instructionId: String) {
        tts!!.speak(instructionText, TextToSpeech.QUEUE_FLUSH, null, instructionId)
    }

    private fun playInstruction1() {
        playInstruction(if(changeLangMode) _INSTRUCTION_1 else INSTRUCTION_1, INSTRUCTION_1_ID)
    }

    private fun playInstruction2() {
        playInstruction(if(changeLangMode) _INSTRUCTION_2 else INSTRUCTION_2, INSTRUCTION_2_ID)
    }

    private fun playInstruction3() {
        playInstruction(if(changeLangMode) _INSTRUCTION_3 else INSTRUCTION_3, INSTRUCTION_3_ID)
    }

    private fun playInstruction4() {
        playInstruction(if(changeLangMode) _INSTRUCTION_4 else INSTRUCTION_4, INSTRUCTION_4_ID)
    }

    private fun playInstruction5() {
        playInstruction(if(changeLangMode) _INSTRUCTION_5 else INSTRUCTION_5, INSTRUCTION_5_ID)
    }

    private fun playInstruction6() {
        playInstruction(if(changeLangMode) _INSTRUCTION_6 else INSTRUCTION_6, INSTRUCTION_6_ID)
    }

    private fun playInstruction7(instructionText: String) {
        playInstruction(instructionText, INSTRUCTION_7_ID)
    }

    private fun playInstruction8() {
        playInstruction(if(changeLangMode) _INSTRUCTION_8 else INSTRUCTION_8, INSTRUCTION_8_ID)
    }

    private fun playInstruction9() {
        playInstruction(if(changeLangMode) _INSTRUCTION_9 else INSTRUCTION_9, INSTRUCTION_9_ID)
    }

    private fun playInstruction10() {
        playInstruction(if(changeLangMode) _INSTRUCTION_10 else INSTRUCTION_10, INSTRUCTION_10_ID)
    }

    private fun playInstruction11() {
        playInstruction(if(changeLangMode) _INSTRUCTION_11 else INSTRUCTION_11, INSTRUCTION_11_ID)
    }

    private fun playInstruction12() {
        playInstruction(if(changeLangMode) _INSTRUCTION_12 else INSTRUCTION_12, INSTRUCTION_12_ID)
    }

    private fun playInstruction13() {
        playInstruction(if(changeLangMode) _INSTRUCTION_13 else INSTRUCTION_13, INSTRUCTION_13_ID)
    }

    private fun playInstruction14() {
        playInstruction(if(changeLangMode) _INSTRUCTION_14 else INSTRUCTION_14, INSTRUCTION_14_ID)
    }

    private fun playInstruction15() {
        playInstruction(if(changeLangMode) _INSTRUCTION_15 else INSTRUCTION_15, INSTRUCTION_15_ID)
    }

    private fun playInstruction16() {
        playInstruction(if(changeLangMode) _INSTRUCTION_16 else INSTRUCTION_16, INSTRUCTION_16_ID)
    }

    private fun launchRecognizerIntent(requestCode: Int) {
        // TODO: Increase the Google TTS listening time.
        // TODO: Launch Google TTS dialog as non-closable or if closed should re-open.

        // Create and start Speech to Text Recognizer
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE,
            Locale.getDefault()
        )
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, packageName)

        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 2000.toLong())
        intent.putExtra(
            RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS,
            2000.toLong()
        )
        intent.putExtra(
            RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS,
            2000.toLong()
        )

        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, SPEAK_TO_TEXT)
        try {
            startActivityForResult(intent, requestCode)
        } catch (e: Exception) {
            Log.e(TAG, "Something went wrong!" + e.message)
        }
    }

    /**
     * Start task 1
     */
    private fun startTask1() {
        addTask1()

        playInstruction1()
    }

    /**
     * Start task 2
     */
    private fun startTask2() {
        analyticsViewModel.completeTask()

        addTask2()

        playInstruction4()
    }

    /**
     * Start task 3
     */
    private fun startTask3() {
        addTask3()

        playInstruction8()
    }

    /**
     * Start task 4
     */
    private fun startTask4() {
        analyticsViewModel.completeTask()

        addTask4()

        playInstruction10()
    }

    /**
     * Start task 5
     */
    private fun startTask5() {
        analyticsViewModel.completeTask()

        addTask5()

        playInstruction12()
    }

    /**
     * Start task 6
     */
    private fun startTask6() {
        analyticsViewModel.completeTask()

        addTask6()
    }

    private fun startTask7(){
        analyticsViewModel.completeTask()
        addTask7()
        playInstruction1()
    }

    /**
     * Add task 1
     */
    private fun addTask1() {
        analyticsViewModel.addTask(TASK_1_NO)
    }

    /**
     * Add task 2
     */
    private fun addTask2() {
        analyticsViewModel.addTask(TASK_2_NO)
    }

    /**
     * Add task 3
     */
    private fun addTask3() {
        analyticsViewModel.addTask(TASK_3_NO)
    }

    /**
     * Add task 4
     */
    private fun addTask4() {
        analyticsViewModel.addTask(TASK_4_NO)
    }

    /**
     * Add task 5
     */
    private fun addTask5() {
        analyticsViewModel.addTask(TASK_5_NO)
    }

    /**
     * Add task 6
     */
    private fun addTask6() {
        analyticsViewModel.addTask(TASK_6_NO)
    }

    private fun addTask7() {
        analyticsViewModel.addTask(TASK_7_NO)
    }

    /**
     * Closes the app with a greeting
     */
    private fun closeApp() {
        playInstruction3()
    }

    private fun finishClosingApp() {
        // Close the app
        finish()
    }

    private fun handleOnStartUtterance(utteranceId: String) {
        Log.d(TAG, "TTS Started $utteranceId")
        when (utteranceId) {
            INSTRUCTION_1_ID -> {

            }
            INSTRUCTION_2_ID -> {

            }
            INSTRUCTION_3_ID -> {

            }
            INSTRUCTION_4_ID -> {

            }
            INSTRUCTION_5_ID -> {

            }
            INSTRUCTION_6_ID -> {
                startAnalyzingFrames()
            }
            INSTRUCTION_7_ID -> {

            }
            INSTRUCTION_8_ID -> {

            }
            INSTRUCTION_9_ID -> {

            }
            INSTRUCTION_10_ID -> {

            }
            INSTRUCTION_11_ID -> {

            }
            INSTRUCTION_12_ID -> {

            }
            INSTRUCTION_13_ID -> {

            }
            INSTRUCTION_14_ID -> {

            }
            INSTRUCTION_15_ID -> {

            }
            else -> {
                // Do nothing if Instruction ID don't match
            }
        }
    }

    private fun handleOnDoneUtterance(utteranceId: String) {
        Log.d(TAG, "TTS Done $utteranceId")
        when (utteranceId) {
            INSTRUCTION_1_ID -> {
                launchRecognizerIntent(REQUEST_CODE_WAIT_ID_1)
            }
            INSTRUCTION_2_ID -> {
                playInstruction1()
            }
            INSTRUCTION_3_ID -> {
                finishClosingApp()
            }
            INSTRUCTION_4_ID -> {
                launchRecognizerIntent(REQUEST_CODE_WAIT_ID_2)
            }
            INSTRUCTION_5_ID -> {
                playInstruction4()
            }
            INSTRUCTION_6_ID -> {
                stopAnalyzingFrames()
            }
            INSTRUCTION_7_ID -> {
                startTask3()
            }
            INSTRUCTION_8_ID -> {
                launchRecognizerIntent(REQUEST_CODE_WAIT_ID_3)
            }
            INSTRUCTION_9_ID -> {
                playInstruction8()
            }
            INSTRUCTION_10_ID -> {
                launchRecognizerIntent(REQUEST_CODE_WAIT_ID_4)
            }
            INSTRUCTION_11_ID -> {
                playInstruction10()
            }
            INSTRUCTION_12_ID -> {
                launchRecognizerIntent(REQUEST_CODE_WAIT_ID_5)
            }
            INSTRUCTION_13_ID -> {
                playInstruction12()
            }
            INSTRUCTION_14_ID, INSTRUCTION_15_ID -> {
                launchRecognizerIntent(REQUEST_CODE_WAIT_ID_6)
            }
            INSTRUCTION_16_ID -> {
                exportCollectedData()
            }
            else -> {
                // Do nothing if Instruction ID don't match
            }
        }
    }

    private fun handleOnErrorUtterance(utteranceId: String) {
        Log.d(TAG, "TTS Error $utteranceId")
        when (utteranceId) {
            INSTRUCTION_1_ID -> {

            }
            INSTRUCTION_2_ID -> {

            }
            INSTRUCTION_3_ID -> {

            }
            INSTRUCTION_4_ID -> {

            }
            INSTRUCTION_5_ID -> {

            }
            INSTRUCTION_6_ID -> {

            }
            INSTRUCTION_7_ID -> {

            }
            INSTRUCTION_8_ID -> {

            }
            INSTRUCTION_9_ID -> {

            }
            INSTRUCTION_10_ID -> {

            }
            INSTRUCTION_11_ID -> {

            }
            INSTRUCTION_12_ID -> {

            }
            INSTRUCTION_13_ID -> {

            }
            INSTRUCTION_14_ID -> {

            }
            INSTRUCTION_15_ID -> {

            }
            else -> {
                // Do nothing if Instruction ID don't match
            }
        }
    }

    private fun handleWait1UserResponse(userResponse: String) {
        analyticsViewModel.saveUserResponse(userResponse)
        analyticsViewModel.completeAttempt()
        if (checkPresence(userResponse, AFFIRMATIVE_TEXT)) {
            startTask2()
        } else if (userResponse.contains(EXPORT_TEXT, true)) {
            playInstruction16()
        }else if(userResponse.contains(if(changeLangMode) _LANG_MODE else LANG_MODE, true)
            || userResponse.contains(if(changeLangMode) _LANG_MODE else "Bangla", true)){
            changeLangMode = !changeLangMode
            startTask7()
        } else {
            val currTaskAttempts = analyticsViewModel.taskAttempts.value
            if (currTaskAttempts != null && currTaskAttempts < MAX_REATTEMPT_REACHED) {
                analyticsViewModel.increaseAttempt()

                playInstruction2()
            } else {
                analyticsViewModel.completeTask()

                closeApp()
            }
        }
    }

    private fun handleWait2UserResponse(userResponse: String) {
        analyticsViewModel.saveUserResponse(userResponse)
        analyticsViewModel.completeAttempt()

        if (checkPresence(userResponse, AFFIRMATIVE_TEXT)) {
            analyticsViewModel.completeTask()

            playInstruction6()
        } else {
            val currTaskAttempts = analyticsViewModel.taskAttempts.value
            if (currTaskAttempts != null && currTaskAttempts < MAX_REATTEMPT_REACHED) {
                analyticsViewModel.increaseAttempt()

                playInstruction5()
            } else {
                analyticsViewModel.completeTask()

                closeApp()
            }
        }
    }

    private fun handleWait3UserResponse(userResponse: String) {
        analyticsViewModel.saveUserResponse(userResponse)
        analyticsViewModel.completeAttempt()

        if (checkPresence(userResponse, AFFIRMATIVE_TEXT)) {
            startTask2()
        } else if (checkPresence(userResponse, NEGATIVE_TEXT)) {
            startTask4()
        } else {
            val currTaskAttempts = analyticsViewModel.taskAttempts.value
            if (currTaskAttempts != null && currTaskAttempts < MAX_REATTEMPT_REACHED) {
                analyticsViewModel.increaseAttempt()

                playInstruction9()
            } else {
                startTask4()
            }
        }
    }

    private fun checkPresence(userResponse: String,text: List<String>): Boolean{
        for (x in text){
            if (userResponse.contains(x, true)){
                return true
            }
        }
        return  false
    }

//    private fun handleWait7UserResponse(userResponse: String){
//        val language: String = userResponse
//    }

    private fun handleWait4UserResponse(userResponse: String) {
        // Refactored result text
        var refactoredResult = userResponse
        var saveFeedback = false

        // Rating constants
        val possibleRatings = listOf("one", "two", "three", "four", "five")
        val possibleRatingsRegex = Regex("[1-5]")

        if (refactoredResult.matches(possibleRatingsRegex)) {
            // save refactoredResult
            saveFeedback = true
        } else {
            var matchIndex = -1
            for (i in possibleRatings.indices) {
                if (userResponse.contains(possibleRatings[i])) {
                    matchIndex = i
                    break
                }
            }
            if (matchIndex != -1) {
                refactoredResult = (matchIndex + 1).toString()

                // save refactoredResult
                saveFeedback = true
            }
        }

        if (saveFeedback) {
            analyticsViewModel.saveUserResponse(refactoredResult)
            analyticsViewModel.completeAttempt()

            startTask5()
        } else {
            analyticsViewModel.saveUserResponse(userResponse)
            analyticsViewModel.completeAttempt()

            val currTaskAttempts = analyticsViewModel.taskAttempts.value
            if (currTaskAttempts != null && currTaskAttempts < MAX_REATTEMPT_REACHED) {
                analyticsViewModel.increaseAttempt()

                playInstruction11()
            } else {
                startTask5()
            }
        }
    }

    private fun handleWait5UserResponse(userResponse: String) {
        analyticsViewModel.saveUserResponse(userResponse)
        analyticsViewModel.completeAttempt()

        if (checkPresence(userResponse, AFFIRMATIVE_TEXT)) {
            startTask6()

            playInstruction14()
        } else if (checkPresence(userResponse, NEGATIVE_TEXT)) {
            startTask6()

            playInstruction15()
        } else {
            val currTaskAttempts = analyticsViewModel.taskAttempts.value
            if (currTaskAttempts != null && currTaskAttempts < MAX_REATTEMPT_REACHED) {
                analyticsViewModel.increaseAttempt()

                playInstruction13()
            } else {
                startTask6()

                playInstruction15()
            }
        }
    }

    private fun handleWait6UserResponse(userResponse: String) {
        analyticsViewModel.saveUserResponse(userResponse)
        analyticsViewModel.completeAttempt()
        analyticsViewModel.completeTask()

        closeApp()
    }

    private fun exportCollectedData() {
        analyticsViewModel.exportCollectedData(this@MainActivity)
    }

    override fun onActivityResult(
        requestCode: Int, resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_WAIT_ID_1) {
            if (resultCode == RESULT_OK && data != null) {
                val result = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS
                )

                val resultTextValue: String = Objects.requireNonNull(result)
                    ?.get(0) ?: "Null"
                handleWait1UserResponse(resultTextValue)
            } else {
                // When manually closed or no data found -> relaunch it with the last requestCode
                launchRecognizerIntent(requestCode)
            }
        } else if (requestCode == REQUEST_CODE_WAIT_ID_2) {
            if (resultCode == RESULT_OK && data != null) {
                val result = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS
                )

                val resultTextValue: String = Objects.requireNonNull(result)?.get(0) ?: "Null"
                handleWait2UserResponse(resultTextValue)
            } else {
                // When manually closed or no data found -> relaunch it with the last requestCode
                launchRecognizerIntent(requestCode)
            }
        } else if (requestCode == REQUEST_CODE_WAIT_ID_3) {
            if (resultCode == RESULT_OK && data != null) {
                val result = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS
                )

                val resultTextValue: String = Objects.requireNonNull(result)?.get(0) ?: "Null"
                handleWait3UserResponse(resultTextValue)
            } else {
                // When manually closed or no data found -> relaunch it with the last requestCode
                launchRecognizerIntent(requestCode)
            }
        } else if (requestCode == REQUEST_CODE_WAIT_ID_4) {
            if (resultCode == RESULT_OK && data != null) {
                val result = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS
                )
                val resultTextValue: String = Objects.requireNonNull(result)?.get(0) ?: "Null"
                handleWait4UserResponse(resultTextValue)
            } else {
                // When manually closed or no data found -> relaunch it with the last requestCode
                launchRecognizerIntent(requestCode)
            }
        } else if (requestCode == REQUEST_CODE_WAIT_ID_5) {
            if (resultCode == RESULT_OK && data != null) {
                val result = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS
                )

                val resultTextValue: String = Objects.requireNonNull(result)?.get(0) ?: "Null"
                handleWait5UserResponse(resultTextValue)
            } else {
                // When manually closed or no data found -> relaunch it with the last requestCode
                launchRecognizerIntent(requestCode)
            }
        } else if (requestCode == REQUEST_CODE_WAIT_ID_6) {
            if (resultCode == RESULT_OK && data != null) {
                val result = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS
                )

                val resultTextValue: String = Objects.requireNonNull(result)?.get(0) ?: "Null"
                handleWait6UserResponse(resultTextValue)
            }
            else {
                // When manually closed or no data found -> relaunch it with the last requestCode
                launchRecognizerIntent(requestCode)
            }
        }
    }

    /**
     * Checks whether all permissions are granted or not
     */
    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                // Prepare and start the camera
                startCamera()

                startTask1()
            } else {
                Toast.makeText(
                    this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT
                ).show()
                closeApp()
            }
        }
    }

    override fun onDestroy() {
        // Turn the flash off
        if (cam.cameraInfo.hasFlashUnit()) {
            // Turn off the flash
            cam.cameraControl.enableTorch(false) // or false
        }

        // Shutdown TTS when activity is destroyed
        if (tts != null) {
            tts!!.stop()
            tts!!.shutdown()
        }

        super.onDestroy()
    }

    companion object {
        private const val TAG = "BlindAssistant"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS =
            mutableListOf(
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
            ).toTypedArray()
    }
}