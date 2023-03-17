package com.example.multiTaskingApp.Multi_Tasking_InterFace.util

import android.content.Context
import android.speech.tts.TextToSpeech
import com.google.mlkit.nl.translate.TranslateLanguage
import java.util.*

private lateinit var tts: TextToSpeech
const val RQ_ALARM = 200
var _hour = false
var hour = 0
var minute = 0
var _minute = false

val alarm_timer = mapOf<String, Int>("zero" to 0,"one" to 1, "two" to 2, "three" to 3, "four" to 4, "five" to 5,
    "six" to 6, "seven" to 7, "eight" to 8, "nine" to 9, "ten" to 10, "eleven" to 11, "twelve" to 12)
var qnaMode: Boolean = false
var langMode: Boolean = false
var destLanguage: Locale = Locale.getDefault()
var prompt: String = ""
var langChanged: Boolean = false


fun speakGlobal(context: Context, text: String){
    tts = TextToSpeech(context) {
        if (it == TextToSpeech.SUCCESS) {
            tts.language = destLanguage
            tts.setSpeechRate(1.0f)
            tts.speak(text, TextToSpeech.QUEUE_ADD, null)
        }
    }
}

var languageDict = mapOf<String, String>("bengali" to TranslateLanguage.BENGALI,
                                        "english" to TranslateLanguage.ENGLISH,
                                        "hindi" to  TranslateLanguage.HINDI,
                                        "kannada" to TranslateLanguage.KANNADA,
                                        "tamil" to TranslateLanguage.TAMIL,
                                        "arabic" to TranslateLanguage.ARABIC,
                                        "gujarati" to TranslateLanguage.GUJARATI,
                                        "korean" to TranslateLanguage.KOREAN,
                                        "japanese" to TranslateLanguage.JAPANESE,
                                        "marathi" to TranslateLanguage.MARATHI,
                                        "german" to TranslateLanguage.GERMAN)