package com.example.multiTaskingApp.utils

import android.content.Context
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.room.TypeConverter
import com.example.multiTaskingApp.DATE_TIME_FORMAT
import java.text.SimpleDateFormat
import java.util.*

/**
 * Returns the given context theme color
 *
 * @param context Context
 * @param attrRes Resource id (R.id.something)
 * @return int color (R.color.something)
 */
fun getContextThemeColor(context: Context, @AttrRes attrRes: Int): Int {
    val typedValue = TypedValue()
    context.theme.resolveAttribute(attrRes, typedValue, true)
    return typedValue.data
}

/**
 * Returns a random string, ideal for using it as an ID
 *
 * @return Random String
 */
fun getRandomUniqueString(): String {
    val uuid: UUID = UUID.randomUUID()
    return uuid.toString()
}

/**
 * Returns the current dateTime in the for of a string
 *
 * @return CurrDateTime String
 */
fun getCurrentDateTime(): Date {
    val formatter = SimpleDateFormat(DATE_TIME_FORMAT, Locale.US)
    val date = Calendar.getInstance().time
//    val formatter = SimpleDateFormat.getDateTimeInstance()
//    return formatter.format(date)
    return date
}


/**
 * Returns the current dateTime in milliseconds
 *
 * @return CurrDateTime Int
 */
fun getCurrentDateTimeMillis(): Long {
    return Calendar.getInstance().timeInMillis
}

/**
 * Date Time Converter for converting date time to long in entity
 */
class DateTimeConverter {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}