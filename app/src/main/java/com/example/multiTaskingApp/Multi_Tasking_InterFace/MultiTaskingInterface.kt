package com.example.multiTaskingApp.Multi_Tasking_InterFace

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.*
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.location.Geocoder
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.MediaStore
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.Preview
import androidx.core.app.ActivityCompat
import com.example.multiTaskingApp.MainActivity
import com.example.multiTaskingApp.Multi_Tasking_InterFace.util.*
import com.example.multiTaskingApp.databinding.ActivityMultiTaskingInterfaceBinding
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import com.mvp.handyopinion.URIPathHelper
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.schedule

class MultiTaskingInterface : AppCompatActivity() {

    private lateinit var binding: ActivityMultiTaskingInterfaceBinding
    private lateinit var tts: TextToSpeech
    private var RQ_CODE = 102

    private val camMan by lazy { getSystemService(Context.CAMERA_SERVICE) as CameraManager}
    private lateinit var cameraId: String
    private var torchMode: Boolean = false

    private var dir: String? = null
    private var file: String? = null
    private var mediaRecorder: MediaRecorder? = null
    private var state: Boolean = false
    private var recordingStopped: Boolean = false

    private lateinit var player: MediaPlayer
    private var makeCall:Boolean = false
    private var getContact: Boolean = false
    private var getName: Boolean = false
    private var numbervalidity:Boolean = false
    private var phoneNumber: String? = null

    private lateinit var alarmManager: AlarmManager
    private lateinit var pendingIntent: PendingIntent
    private lateinit var calender: Calendar

    private lateinit var locationRequest: LocationRequest
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var dstLangEnglishTranslator: com.google.mlkit.nl.translate.Translator
    private lateinit var englishDstLangTranslator: com.google.mlkit.nl.translate.Translator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMultiTaskingInterfaceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        if(ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO),102)
            SPEAK("Please give the Microphone Access")
        }
        else{
            SPEAK("Please tap on the microphone to say something")
        }

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        dir = externalCacheDir?.absolutePath
        mediaRecorder = MediaRecorder()

        mediaRecorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
        mediaRecorder?.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
        mediaRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)


        binding.imageView3.setOnClickListener{
            askSpeechInput()
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    @SuppressLint("QueryPermissionsNeeded")
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == RQ_ALARM && resultCode == RESULT_OK) {
            val result: ArrayList<String>? =
                data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            binding.textView.text = result?.get(0).toString()

            if (result?.get(0).toString().containsAllOfIgnoreCase(arrayListOf("set","alarm"))) {

            }
        }


        if(requestCode == RQ_CODE && resultCode == RESULT_OK) {
            val result: ArrayList<String>? =
                data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            binding.textView.text = result?.get(0).toString()


            if(langChanged){
                dstEngTranslate(result?.get(0).toString())
            }else{
                prompt = result?.get(0).toString()
            }

            //Code for Question answer model
            if(langMode){
                val x: String = prompt
                val desLang: String? = checkFieldInDictionary2(x, languageDict)
                if( desLang != null){
                    SPEAK("সবকিছু ঠিক আছে কি না তা পরীক্ষা করা হচ্ছে।")
                    setUpTranslator(desLang)
                    langMode = false
                    langChanged = true
                    destLanguage = Locale.forLanguageTag(desLang)
                }
                else{
                    SPEAK("Please provide the correct language")
                }
            }
            else if(qnaMode){
                if (prompt.containsAllOfIgnoreCase(arrayListOf("sleep")) || prompt.containsAllOfIgnoreCase(arrayListOf("asleep"))){
                    SPEAK("Question answer mode is off")
                    qnaMode = false
                    destLanguage = Locale.getDefault()
                }else if(prompt.containsAllOfIgnoreCase(arrayListOf("change", "language"))){
                    SPEAK("PLease tell me the language")
                    Timer().schedule(3000){
                        askSpeechInput()
                        langMode = true
                    }
                }
                else{
                    Timer().schedule(1500){
                        GlobalScope.launch {
                            try{
                                if(langChanged) {
                                    engDstTranslate(openAiResponse().getResponse(prompt))
                                    Log.i("knoww", prompt)
                                }
                                else
                                    SPEAK(openAiResponse().getResponse(prompt))
                            }
                            catch (e: Exception){
                                SPEAK("Sorry I have not understand your question please tell me again")
                            }
                        }
                    }
                }
            }else {

                if (result?.get(0).toString()
                        .containsAllOfIgnoreCase(arrayListOf("question", "answer", "activate"))
                ) {
                    SPEAK("Question answer mode is activated")
                    qnaMode = true
                }


                //Code for setting the alarm
                if (_minute) {
                    try {
                        minute = result?.get(0)!!.toInt()
                        setAlarm()
                    } catch (e: Exception) {
                        val x = checkFieldInDictionary(result?.get(0)!!, alarm_timer)
                        if (x != -1) {
                            minute = x
                            setAlarm()
                        } else {
                            SPEAK("Please give me me the correct minute")
                        }
                    }
                }

                if (_hour) {
                    try {
                        hour = result?.get(0)!!.toInt()
                        SPEAK("Please tell me the minute")
                        Timer().schedule(3000) {
                            _minute = true
                            _hour = false
                            askSpeechInput()
                        }
                    } catch (e: Exception) {
                        val x = checkFieldInDictionary(result?.get(0)!!, alarm_timer)
                        if (x != -1) {
                            hour = x
                            SPEAK("Please tell me the minute")
                            Timer().schedule(3000) {
                                _minute = true
                                _hour = false
                                askSpeechInput()
                            }
                        } else {
                            SPEAK("PLease Provide the Correct hour")
                            Timer().schedule(3000) {
                                askSpeechInput()
                            }
                        }
                    }
                }

                if (result?.get(0).toString()
                        .containsAllOfIgnoreCase(arrayListOf("set", "alarm"))
                ) {
                    SPEAK("Please tell me the hour")
                    Timer().schedule(3000) {
                        _hour = true
                        askSpeechInput()
                    }
                }

                //Code for opening Camera
                if (result?.get(0).toString()
                        .containsAllOfIgnoreCase(arrayListOf("open", "camera"))
                ) {
                    if (ActivityCompat.checkSelfPermission(
                            this,
                            Manifest.permission.CAMERA
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        ActivityCompat.requestPermissions(
                            this,
                            arrayOf(Manifest.permission.CAMERA),
                            111
                        )
                        SPEAK("Please give the Camera Access")
                    } else {
                        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        startActivityForResult(intent, 101)
                        SPEAK("Camera is successfully started")
                    }
                }

                //Code to start the video recording
                if (result?.get(0).toString()
                        .containsAllOfIgnoreCase(arrayListOf("record", "video"))
                ) {
                    if (ActivityCompat.checkSelfPermission(
                            this,
                            Manifest.permission.CAMERA
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        ActivityCompat.requestPermissions(
                            this,
                            arrayOf(
                                Manifest.permission.CAMERA,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                            ),
                            11
                        )
                        SPEAK("Please give the Camera Access")
                    } else {
                        val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
                        if (intent.resolveActivity(packageManager) != null) {
                            startActivityForResult(intent, 121)
                        }
                        SPEAK("Video recording is successfully started")
                    }
                }

                //Code to make torch on
                if (result?.get(0).toString().containsAllOfIgnoreCase(arrayListOf("torch", "on"))) {
                    val isFlashAvailable = applicationContext.packageManager
                        .hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)
                    if (!isFlashAvailable) {
                        SPEAK("Sorry your mobile don't have flash light")
                    } else {
                        try {
                            cameraId = camMan.cameraIdList[0]
                        } catch (e: CameraAccessException) {
                            e.printStackTrace()
                        }
                        torchMode = true
                        switchFlashLight(torchMode)
                    }
                }

                //Code to make torch off
                if (result?.get(0).toString()
                        .containsAllOfIgnoreCase(arrayListOf("torch", "off"))
                ) {
                    if (torchMode) {
                        torchMode = false
                        switchFlashLight(torchMode)
                    }
                }

                //Code fo getting user's current location
                if (result?.get(0).toString()
                        .containsAllOfIgnoreCase(arrayListOf("My", "Location"))
                ) {
                    checkLocationPermission()
                }

                //Code for recording audio
                if (result?.get(0).toString()
                        .containsAllOfIgnoreCase(arrayListOf("audio", "recording", "start"))
                ) {
                    startAudioRecording()
                }
                if (result?.get(0).toString()
                        .containsAllOfIgnoreCase(arrayListOf("audio", "recording", "stop"))
                ) {
                    stopAudioRecording()
                }
                if (result?.get(0).toString()
                        .containsAllOfIgnoreCase(arrayListOf("pause", "recording", "audio"))
                ) {
                    pauseAudioRecording()
                }
                if (result?.get(0).toString()
                        .containsAllOfIgnoreCase(arrayListOf("resume", "recording", "audio"))
                ) {
                    resumeRocording()
                }
                if (result?.get(0).toString()
                        .containsAllOfIgnoreCase(arrayListOf("play", "recorded", "audio"))
                ) {
                    startPlaying()
                }

                //Code detecting object using object detection model
                if (result?.get(0).toString()
                        .containsAllOfIgnoreCase(arrayListOf("detect", "objects"))
                ) {
                    val intent = Intent(this, Preview::class.java)
                    startActivity(intent)
                }

                //Code making phone calls
                if (makeCall) {
                    phoneNumber = result?.get(0).toString()
                    var contact: ContactModel? = null
                    try {
                        contact = checkName(phoneNumber!!)!!
                    } catch (e: Exception) {
                        Log.i("error_msg", e.message.toString())
                    } finally {
                        if (contact != null) {
                            Timer().schedule(1000) {
                                phoneNumber = contact.mobileNumber
                                SPEAK("your given contact name is ${contact.name} and number is $phoneNumber")
                            }
                        } else {
                            Timer().schedule(3500) {
                                SPEAK("your given phone number is $phoneNumber")
                            }
                        }
                        Timer().schedule(6000) {
                            SPEAK("Is this number correct")
                        }
                        Timer().schedule(13000) {
                            askSpeechInput()
                            numbervalidity = true
                        }
                        makeCall = false
                    }
                }
                if (numbervalidity) {
                    if (result?.get(0).toString().containsAllOfIgnoreCase(arrayListOf("yes"))) {
                        val callIntent = Intent(Intent.ACTION_CALL)
                        callIntent.data = Uri.parse("tel:$phoneNumber")
                        startActivity(callIntent)
                    }
                    numbervalidity = false
                }
                if (result?.get(0).toString()
                        .containsAllOfIgnoreCase(arrayListOf("make a", "call"))
                ) {
                    if (ActivityCompat.checkSelfPermission(
                            this,
                            Manifest.permission.CALL_PHONE
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        ActivityCompat.requestPermissions(
                            this,
                            arrayOf(Manifest.permission.CALL_PHONE),
                            121
                        )
                    } else if (ActivityCompat.checkSelfPermission(
                            this,
                            Manifest.permission.READ_CONTACTS
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        ActivityCompat.requestPermissions(
                            this,
                            arrayOf(Manifest.permission.READ_CONTACTS),
                            27
                        )
                    } else {
                        SPEAK("Please provide the contact number or tell me the contact name")
                        Timer().schedule(4000) {
                            askSpeechInput()
                            makeCall = true
                        }
                    }
                }

                //Code for saving a contact in the contact list
                if (getName) {
                    val contactName = result?.get(0).toString()
                    saveContact(contactName, phoneNumber)
                    SPEAK("Your contact successfully added")
                    getName = false
                }

                if (getContact) {
                    phoneNumber = result?.get(0).toString()
                    SPEAK("Please tell me the contact's name")
                    Timer().schedule(3000) {
                        askSpeechInput()
                        getContact = false
                        getName = true
                    }
                }

                if (result?.get(0).toString()
                        .containsAllOfIgnoreCase(arrayListOf("save", "contact"))
                ) {
                    if (ActivityCompat.checkSelfPermission(
                            this,
                            Manifest.permission.WRITE_CONTACTS
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        ActivityCompat.requestPermissions(
                            this,
                            arrayOf(Manifest.permission.WRITE_CONTACTS),
                            131
                        )
                    } else {
                        SPEAK("Please tell me the contact number")
                        Timer().schedule(3000) {
                            askSpeechInput()
                            getContact = true
                        }
                    }
                }

                //Code for searching youtube video
                if (result?.get(0).toString().containsAllOfIgnoreCase(arrayListOf("search"))) {
                    val text = result?.get(0).toString()
                    val intent = Intent(this, web_activity::class.java)
                    intent.putExtra("quarry", text)
                    startActivity(intent)
                }

                //Code fo detecting currency
                if (result?.get(0).toString()
                        .containsAllOfIgnoreCase(arrayListOf("detect", "currency"))
                ) {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                }
            }
        }

        if(requestCode == 121 && resultCode == RESULT_OK) {
            //get data from uri
            val videoUri = data?.data
            saveVideo(videoUri!!)
            val scale = resources.displayMetrics.density
            val dpWidthInPx = (350 * scale).toInt()
            val dpHeightInPx = (350 * scale).toInt()
            binding.imageView2.layoutParams.width = 0
            binding.imageView2.layoutParams.height = 0
            binding.videoView.layoutParams.height = dpHeightInPx
            binding.videoView.layoutParams.width = dpWidthInPx
            binding.videoView.setVideoURI(videoUri)
            binding.videoView.start()
        }

        if (requestCode == 101) {
            val scale = resources.displayMetrics.density
            val dpWidthInPx = (350 * scale).toInt()
            val dpHeightInPx = (350 * scale).toInt()
            binding.videoView.layoutParams.width = 0
            binding.videoView.layoutParams.height = 0
            binding.imageView2.layoutParams.height = dpHeightInPx
            binding.imageView2.layoutParams.width = dpWidthInPx
            binding.videoView.pause()
            val img = data?.getParcelableExtra<Bitmap>("data")
            binding.imageView2.setImageBitmap(img)
        }
    }


    private fun String.containsAllOfIgnoreCase(keywords:List<String>): Boolean{
        for(keyword in keywords){
            if (!this.contains(keyword,true)) {
                return false
            }
        }
        return  true
    }

    private fun askSpeechInput(){
        val i = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, destLanguage.toString())
        i.putExtra(RecognizerIntent.EXTRA_PROMPT,"Say Something please!")
        try {
            this.startActivityForResult(i,RQ_CODE)
        } catch (e: Exception) {
            // on below line we are displaying error message in toast
            Toast
                .makeText(
                    this, " " + e.message,
                    Toast.LENGTH_SHORT
                )
                .show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == 111 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            SPEAK("Permission Granted")
        }

        if(requestCode == 11 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            SPEAK("Permission Granted")
        }
        if(requestCode == 102 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            SPEAK("Permission Granted")
        }
    }

    private fun saveVideo(videoUri: Uri){
        val uriHelper = URIPathHelper()
        val filepath = uriHelper.getPath(this,videoUri)
        Log.i("file_name" , filepath!!)
    }

    private fun SPEAK(text: String){
        tts = TextToSpeech(applicationContext) {
            if (it == TextToSpeech.SUCCESS) {
                tts.language = destLanguage
                tts.setSpeechRate(1.0f)
                tts.speak(text, TextToSpeech.QUEUE_ADD, null)
            }
        }
    }

    private fun switchFlashLight(status: Boolean) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                camMan.setTorchMode(cameraId, status)
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun startAudioRecording(){
        mediaRecorder = MediaRecorder().apply {
            val simpleDateFormat = SimpleDateFormat("yyyy.MM.MM.DD_hh.mm.ss")
            val date: String = simpleDateFormat.format(Date())
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            file = "$dir/audio_record_$date.mp3"
            setOutputFile(file)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            try {
                prepare()
                state = true
                SPEAK("Audio recording is successfully started")
            } catch (e: IOException) {
                SPEAK("Failed to start audio recording")
            }
            start()
        }
    }

    private fun stopAudioRecording(){
        if(state){
            mediaRecorder?.stop()
            mediaRecorder?.release()
            state = false
        }
        else{
            SPEAK("You are not started audio recording yet")
        }
    }

    @SuppressLint("RestrictedApi", "SetTextI18n")
    private fun pauseAudioRecording(){
        if(state){
            if(!recordingStopped){
                mediaRecorder?.pause()
                recordingStopped = true
                SPEAK("Audio recording paused")
            }
            else{
                resumeRocording()
            }
        }
    }

    @SuppressLint("RestrictedApi", "SetTextI18n")
    @TargetApi(Build.VERSION_CODES.N)
    private fun resumeRocording(){
        mediaRecorder?.resume()
        recordingStopped = false
        SPEAK("Audio recording started again")
    }

    private fun startPlaying() {
        player = MediaPlayer().apply {
            try {
                setDataSource(file)
                prepare()
                start()
            } catch (e: IOException) {
                SPEAK("Unable to play the recording audio")
            }
        }
    }


    //Location finder
    private fun checkLocationPermission(){
        if(ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),101)
        }
        else{
            checkGps()
        }
    }

    private  fun checkGps(){
        locationRequest = LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 5000
        locationRequest.fastestInterval = 2000

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)

        builder.setAlwaysShow(true)

        val result = LocationServices.getSettingsClient(
            this.applicationContext
        ).checkLocationSettings(builder.build())

        result.addOnCompleteListener{task ->
            try{
                task.getResult(
                    ApiException::class.java
                )
                getUserLocation()
            }catch (e : ApiException){
                //when GPS is off
                e.printStackTrace()

                when(e.statusCode){
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> try{
                        val resolveApiException = e as ResolvableApiException
                        resolveApiException.startResolutionForResult(this,200)
                    }catch (sendIntentException : IntentSender.SendIntentException){
                    }
                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                        //when setting is unavailable
                    }
                }
            }
        }
    }

    private fun getUserLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        fusedLocationProviderClient.lastLocation.addOnCompleteListener{ task ->
            val location = task.result
            if(location != null){
                try {
                    val geoCoder = Geocoder(this, Locale.getDefault())
                    val address = geoCoder.getFromLocation(location.latitude,location.longitude,1)

                    val addressLine = address?.get(0)?.getAddressLine(0)
                    Timer().schedule(2000){
                        SPEAK("Your current Location is $addressLine")
                    }
                }catch (e: IOException){
                }
            }
        }
    }

    @SuppressLint("Range")
    private fun getContactList(): ArrayList<ContactModel> {
        val contactsList = ArrayList<ContactModel>()
        val cr: ContentResolver = contentResolver
        val cur: Cursor? = cr.query(
            ContactsContract.Contacts.CONTENT_URI,
            null, null, null, null
        )
        if ((cur?.count ?: 0) > 0) {
            while (cur != null && cur.moveToNext()) {
                val id: String = cur.getString(
                    cur.getColumnIndex(ContactsContract.Contacts._ID)
                )
                val name: String? = cur.getString(
                    cur.getColumnIndex(
                        ContactsContract.Contacts.DISPLAY_NAME
                    )
                )
                if (cur.getInt(
                        cur.getColumnIndex(
                            ContactsContract.Contacts.HAS_PHONE_NUMBER
                        )
                    ) > 0
                ) {
                    val pCur: Cursor? = cr.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", arrayOf(id), null
                    )
                    while (pCur!!.moveToNext()) {
                        val phoneNo: String = pCur.getString(
                            pCur.getColumnIndex(
                                ContactsContract.CommonDataKinds.Phone.NUMBER
                            )
                        )
                        Log.i("name", "Name: $name")
                        Log.i("Ph No", "Phone Number: $phoneNo")
                        contactsList.add(ContactModel(name,phoneNo))
                    }
                    pCur.close()
                }
            }
        }
        cur?.close()
        return contactsList
    }

    private fun checkName(name: String): ContactModel? {
        val contactsList: ArrayList<ContactModel> = getContactList()
        for (contact in contactsList){
            if(contact.name?.containsAllOfIgnoreCase(listOf(name)) == true){
                return contact
            }
        }
        return null
    }

    private fun saveContact(displayName: String?, number: String?){
        val ops = ArrayList<ContentProviderOperation>()

        ops.add(
            ContentProviderOperation.newInsert(
                ContactsContract.RawContacts.CONTENT_URI
            )
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                .build()
        )

        if (displayName != null) {
            ops.add(ContentProviderOperation.newInsert(
                ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE,
                    ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                .withValue(
                    ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
                    displayName).build())
        }

        if (number != null) {
            ops.add(ContentProviderOperation.
            newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE,
                    ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, number)
                .withValue(ContactsContract.CommonDataKinds.Phone.TYPE,
                    ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                .build())
        }
        try {
            contentResolver.applyBatch(ContactsContract.AUTHORITY, ops)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Exception: " + e.message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun setAlarm(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name : CharSequence = "SpeechRecognitionAlarmChannel"
            val description = "Channel for Speech Recognition alarm"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("Speech_Recognition", name, importance)
            channel.description = description
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
        calender = Calendar.getInstance()
        calender[Calendar.HOUR_OF_DAY] = hour
        calender[Calendar.MINUTE] = minute
        calender[Calendar.SECOND] = 0
        calender[Calendar.MILLISECOND] = 0

        alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlarmReceiver::class.java)
        pendingIntent = PendingIntent.getBroadcast(this,0,intent,0)
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP, calender.timeInMillis,
            AlarmManager.INTERVAL_DAY, pendingIntent
        )
        _minute = false
        SPEAK("Alarm successfully set at $hour am $minute minute")
    }

    private fun checkFieldInDictionary(field: String, dictionary:Map<String, Int>): Int {
        for( x in dictionary.keys){
            if(x.equals(field,true)) {
                return dictionary[x]!!
            }
        }
        return -1
    }

    private fun checkFieldInDictionary2(field: String, dictionary:Map<String, String>): String? {
        for( x in dictionary.keys){
            if(x.equals(field,true)) {
                return dictionary[x]!!
            }
        }
        return null
    }


    fun setUpTranslator(dstLanguage: String) {
        val dstEngOptions = TranslatorOptions.Builder()
            .setSourceLanguage(dstLanguage)
            .setTargetLanguage(TranslateLanguage.ENGLISH)
            .build()
        dstLangEnglishTranslator = Translation.getClient(dstEngOptions)

        val dstEngConditions = DownloadConditions.Builder()
            .requireWifi()
            .build()
        dstLangEnglishTranslator.downloadModelIfNeeded(dstEngConditions)
            .addOnSuccessListener {
                // Model downloaded successfully. Okay to start translating.
                speakGlobal(this, "সবকিছু ঠিক আছে. আপনি প্রশ্ন করা শুরু করতে পারেন")
            }
            .addOnFailureListener { exception ->
                speakGlobal(this, exception.message.toString())
                Toast.makeText(
                    this,
                    "Model could not be downloaded or other internal error",
                    Toast.LENGTH_SHORT
                ).show()
            }


        val engDstOptions = TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.ENGLISH)
            .setTargetLanguage(dstLanguage)
            .build()
        englishDstLangTranslator = Translation.getClient(engDstOptions)

        val engDstConditions = DownloadConditions.Builder()
            .requireWifi()
            .build()
        englishDstLangTranslator.downloadModelIfNeeded(engDstConditions)
            .addOnSuccessListener {
                speakGlobal(this, "সবকিছু ঠিক আছে. আপনি প্রশ্ন করা শুরু করতে পারেন")
            }
            .addOnFailureListener { exception ->
                speakGlobal(this, exception.message.toString())
                Toast.makeText(
                    this,
                    "Model could not be downloaded or other internal error",
                    Toast.LENGTH_SHORT
                ).show()
            }

    }

    fun dstEngTranslate(text: String){
        dstLangEnglishTranslator.translate(text)
            .addOnSuccessListener {translatedText ->
                binding.textView.text = translatedText
                prompt = translatedText
                Log.i("xxxxx", translatedText)
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Sorry some error occurred", Toast.LENGTH_SHORT).show()
                speakGlobal(this, exception.message.toString())
            }
    }

    fun engDstTranslate(text: String) {

        englishDstLangTranslator.translate(text)
            .addOnSuccessListener { translatedText ->
                binding.textView.text = translatedText
                SPEAK(translatedText.replace("?", ""))
            }
            .addOnFailureListener { exception ->
                speakGlobal(this, exception.message.toString())
                Toast.makeText(this, "Sorry some error occurred", Toast.LENGTH_SHORT).show()
            }
    }

}












