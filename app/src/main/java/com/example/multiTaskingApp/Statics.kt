package com.example.multiTaskingApp

// Image size
const val imageSize = 224

// Currency values
const val _CURRENCY_VAL_10 = " ১০ "
const val _CURRENCY_VAL_20 = " ২০"
const val _CURRENCY_VAL_50 = " ৫০"
const val _CURRENCY_VAL_100 = "১০০"
const val _CURRENCY_VAL_200 = " ২০০"
const val _CURRENCY_VAL_500 = " ৫০০"
const val _CURRENCY_VAL_2000 = " ২০০০"
const val _CURRENCY_VAL_BACKGROUND = "Background"

const val CURRENCY_VAL_10 = "₹ 10"
const val CURRENCY_VAL_20 = "₹ 20"
const val CURRENCY_VAL_50 = "₹ 50"
const val CURRENCY_VAL_100 = "₹ 100"
const val CURRENCY_VAL_200 = "₹ 200"
const val CURRENCY_VAL_500 = "₹ 500"
const val CURRENCY_VAL_2000 = "₹ 2000"
const val CURRENCY_VAL_BACKGROUND = "Background"

val classes =
    arrayOf(
        CURRENCY_VAL_10,
        CURRENCY_VAL_20,
        CURRENCY_VAL_50,
        CURRENCY_VAL_100,
        CURRENCY_VAL_200,
        CURRENCY_VAL_500,
        CURRENCY_VAL_2000,
        CURRENCY_VAL_BACKGROUND
    )

val _classes =
    arrayOf(
        _CURRENCY_VAL_10,
        _CURRENCY_VAL_20,
        _CURRENCY_VAL_50,
        _CURRENCY_VAL_100,
        _CURRENCY_VAL_200,
        _CURRENCY_VAL_500,
        _CURRENCY_VAL_2000,
        _CURRENCY_VAL_BACKGROUND
    )

// Start without a delay
const val VIBRATION_START_DELAY: Long = 0
const val VIBRATION_DELAY: Long = 350
const val SINGLE_VIBRATION_DURATION: Long = 100

// Each element then alternates between vibrate, sleep, vibrate, sleep...
val VIBRATE_PATTER_VAL_10 =
    longArrayOf(
        VIBRATION_START_DELAY,
        SINGLE_VIBRATION_DURATION,
        VIBRATION_DELAY
    )

val VIBRATE_PATTER_VAL_20 =
    longArrayOf(
        VIBRATION_START_DELAY,
        SINGLE_VIBRATION_DURATION,
        VIBRATION_DELAY,
        SINGLE_VIBRATION_DURATION,
        VIBRATION_DELAY
    )

val VIBRATE_PATTER_VAL_50 =
    longArrayOf(
        VIBRATION_START_DELAY,
        SINGLE_VIBRATION_DURATION,
        VIBRATION_DELAY,
        SINGLE_VIBRATION_DURATION,
        VIBRATION_DELAY,
        SINGLE_VIBRATION_DURATION,
        VIBRATION_DELAY
    )

val VIBRATE_PATTER_VAL_100 =
    longArrayOf(
        VIBRATION_START_DELAY,
        SINGLE_VIBRATION_DURATION,
        VIBRATION_DELAY,
        SINGLE_VIBRATION_DURATION,
        VIBRATION_DELAY,
        SINGLE_VIBRATION_DURATION,
        VIBRATION_DELAY,
        SINGLE_VIBRATION_DURATION,
        VIBRATION_DELAY
    )

val VIBRATE_PATTER_VAL_200 =
    longArrayOf(
        VIBRATION_START_DELAY,
        SINGLE_VIBRATION_DURATION,
        VIBRATION_DELAY,
        SINGLE_VIBRATION_DURATION,
        VIBRATION_DELAY,
        SINGLE_VIBRATION_DURATION,
        VIBRATION_DELAY,
        SINGLE_VIBRATION_DURATION,
        VIBRATION_DELAY,
        SINGLE_VIBRATION_DURATION,
        VIBRATION_DELAY
    )

val VIBRATE_PATTER_VAL_500 =
    longArrayOf(
        VIBRATION_START_DELAY,
        SINGLE_VIBRATION_DURATION,
        VIBRATION_DELAY,
        SINGLE_VIBRATION_DURATION,
        VIBRATION_DELAY,
        SINGLE_VIBRATION_DURATION,
        VIBRATION_DELAY,
        SINGLE_VIBRATION_DURATION,
        VIBRATION_DELAY,
        SINGLE_VIBRATION_DURATION,
        VIBRATION_DELAY,
        SINGLE_VIBRATION_DURATION,
        VIBRATION_DELAY
    )

val VIBRATE_PATTER_VAL_2000 =
    longArrayOf(
        VIBRATION_START_DELAY,
        SINGLE_VIBRATION_DURATION,
        VIBRATION_DELAY,
        SINGLE_VIBRATION_DURATION,
        VIBRATION_DELAY,
        SINGLE_VIBRATION_DURATION,
        VIBRATION_DELAY,
        SINGLE_VIBRATION_DURATION,
        VIBRATION_DELAY,
        SINGLE_VIBRATION_DURATION,
        VIBRATION_DELAY,
        SINGLE_VIBRATION_DURATION,
        VIBRATION_DELAY,
        SINGLE_VIBRATION_DURATION,
        VIBRATION_DELAY
    )

/**
 * Date time format in which all data time object will be represented
 */
const val DATE_TIME_FORMAT = "dd-MM-yyyy hh:mm:ss"

/**
 * Current App Provider Authority
 */
const val APP_PROVIDER_AUTHORITY = "com.akash2099.blindassistant.export_file_share"

/**
 * Request code for ask permission of Write External Storage
 */
const val REQ_CODE_ASK_PERMISSION_WRITE_EXTERNAL_STORAGE = 195

// Maximum attempts
const val MAX_REATTEMPT_REACHED = 3

// Instruction IDs
const val INSTRUCTION_1_ID = "INSTRUCTION_1_ID"
const val INSTRUCTION_2_ID = "INSTRUCTION_2_ID"
const val INSTRUCTION_3_ID = "INSTRUCTION_3_ID"
const val INSTRUCTION_4_ID = "INSTRUCTION_4_ID"
const val INSTRUCTION_5_ID = "INSTRUCTION_5_ID"
const val INSTRUCTION_6_ID = "INSTRUCTION_6_ID"
const val INSTRUCTION_7_ID = "INSTRUCTION_7_ID"
const val INSTRUCTION_8_ID = "INSTRUCTION_8_ID"
const val INSTRUCTION_9_ID = "INSTRUCTION_9_ID"
const val INSTRUCTION_10_ID = "INSTRUCTION_10_ID"
const val INSTRUCTION_11_ID = "INSTRUCTION_11_ID"
const val INSTRUCTION_12_ID = "INSTRUCTION_12_ID"
const val INSTRUCTION_13_ID = "INSTRUCTION_13_ID"
const val INSTRUCTION_14_ID = "INSTRUCTION_14_ID"
const val INSTRUCTION_15_ID = "INSTRUCTION_15_ID"
const val INSTRUCTION_16_ID = "INSTRUCTION_16_ID"
const val INSTRUCTION_17_ID = "INSTRUCTION_17_ID"

// Instruction Texts
const val INSTRUCTION_1 = "Opening camera, are you ready? say either yes or no"
const val INSTRUCTION_2 = "Please provide correct input"
const val INSTRUCTION_3 = "Closing the app. Thank you for using Blind Assistant"
const val INSTRUCTION_4 = "Please hold the currency in front of your camera, and then say yes"
const val INSTRUCTION_5 = "Please provide correct input"
const val INSTRUCTION_6 = "Processing"
const val INSTRUCTION_7 = "It is a X rupee note"
const val INSTRUCTION_8 = "Do you want to detect more currencies? say either yes or no"
const val INSTRUCTION_9 = "Please provide correct input"
const val INSTRUCTION_10 = "For future improvements, please rate me by giving score between 1 to 5. Say 5 for best experience completely satisfied, say 4 for a good experience quite satisfied, say 3 for moderate experience moderate satisfied, say 2 for bad experience less satisfied, say 1 for very bad experience unsatisfied."
const val INSTRUCTION_11 = "Please provide correct input"
const val INSTRUCTION_12 = "Have you faced any difficulty during navigation? say either yes or no"
const val INSTRUCTION_13 = "Please provide correct input"
const val INSTRUCTION_14 = "Please say your difficulty?"
const val INSTRUCTION_15 = "Any suggestion about how we can improve the app?"
const val INSTRUCTION_16 = "Exporting collected data"
const val INSTRUCTION_17 = "Now the app is able to take instructions in English"


const val _INSTRUCTION_1 = "আমি ক্যামেরা খুলছি, আপনি কী প্রস্তুত? yes বা no বলুন"
const val _INSTRUCTION_2 = "সঠিক ইনপুট প্রদান করুন"
const val _INSTRUCTION_3 =
"অ্যাপ বন্ধ করা হচ্ছে। ব্লাইন্ড অ্যাসিস্ট্যান্ট ব্যবহার করার জন্য আপনাকে ধন্যবাদ"
const val _INSTRUCTION_4 = "আপনার ক্যামেরার সামনে মুদ্রা ধরুন, এবং তারপর yes বলুন"
const val _INSTRUCTION_5 = "সঠিক ইনপুট প্রদান করুন"
const val _INSTRUCTION_6 = "সনাক্তকরণ চলছে"
const val _INSTRUCTION_7 = "এটি একটি X টাকার নোট"
const val _INSTRUCTION_8 = "আপনি কী আরো মুদ্রা সনাক্ত করতে চান? yes বা No বলুন"
const val _INSTRUCTION_9 = "সঠিক ইনপুট প্রদান করুন"
const val _INSTRUCTION_10 =
"ভবিষ্যতের উন্নতির জন্য, দয়া করে আমাকে 1 থেকে 5 এর মধ্যে রেটিং দিন" +
"সম্পূর্ণরূপে সন্তুষ্ট এবং সেরা অভিজ্ঞতার জন্য Five বলুন, " +
"ভাল অভিজ্ঞতা এবং বেশ সন্তুষ্টর জন্য Four বলুন , " +
"মাঝারি অভিজ্ঞতা এবং মাঝারি সন্তুষ্টর জন্য Three বলুন, " +
"খারাপ অভিজ্ঞতা এবং কম সন্তুষ্ট জন্য Two বলুন, " +
"খুব খারাপ অভিজ্ঞতা এবং অসন্তুষ্ট জন্য One বলুন."
const val _INSTRUCTION_11 = "সঠিক ইনপুট প্রদান করুন"
const val _INSTRUCTION_12 = "নেভিগেশনের সময় আপনি কি কোন অসুবিধার সম্মুখীন হয়েছেন? yes বা No বলুন"
const val _INSTRUCTION_13 = "সঠিক ইনপুট প্রদান করুন"
const val _INSTRUCTION_14 = "আপনি যদি কোন অসুবিধার সম্মুখীন হয়ে থাকেন তবে দয়া করে আমাকে বলুন"
const val _INSTRUCTION_15 = "আপনি কি আমাকে কোন পরামর্শ দিতে পারেন কিভাবে আমরা আমাদের অ্যাপটিকে আরও উন্নত করতে পারি?"
const val _INSTRUCTION_16 = "সংগৃহীত ডেটা রপ্তানি করা হচ্ছে"
const val _INSTRUCTION_17 = "এখন অ্যাপটি বাংলায় নির্দেশনা নিতে সক্ষম"

const val _EXPORT_TEXT = "Export"
const val _LANG_MODE = "English"
const val _SPEAK_TO_TEXT = "Listening ..."


// Task Numbers
const val TASK_1_NO = 1
const val TASK_2_NO = 2
const val TASK_3_NO = 3
const val TASK_4_NO = 4
const val TASK_5_NO = 5
const val TASK_6_NO = 6
const val TASK_7_NO = 7

// Waiting for user request codes
const val REQUEST_CODE_WAIT_ID_1 = 140
const val REQUEST_CODE_WAIT_ID_2 = 141
const val REQUEST_CODE_WAIT_ID_3 = 142
const val REQUEST_CODE_WAIT_ID_4 = 143
const val REQUEST_CODE_WAIT_ID_5 = 144
const val REQUEST_CODE_WAIT_ID_6 = 145
const val REQUEST_CODE_WAIT_ID_7 = 146


// Others
var AFFIRMATIVE_TEXT = arrayListOf("Yes", "Huh", "haa", "hai", "ha", "ok")
var NEGATIVE_TEXT = arrayListOf("No", "na", "nah", "naa")
var EXPORT_TEXT = "Export"
var LANG_MODE = "bengali"
var SPEAK_TO_TEXT = "Listening ..."

// Storage
const val ANALYTICS_FOLDER_NAME = "Analytics"
const val ANALYTICS_FILE_NAME = "Analytics_Data"

// TODO: Later move the constant texts from here to strings.xml