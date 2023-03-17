package com.example.multiTaskingApp.Multi_Tasking_InterFace.util

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.speech.tts.TextToSpeech
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.multiTaskingApp.R
import java.util.*


class AlarmReceiver : BroadcastReceiver() {

    private lateinit var tts: TextToSpeech

    override fun onReceive(context: Context, intent: Intent) {

        val intent = Intent(context, DestinationActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)

        val builder = NotificationCompat.Builder(context, "Speech_Recognition")
            .setSmallIcon(R.drawable.ic_plus)
            .setContentTitle("Speech Recognition Alarm Maneger")
            .setContentText("Now it's time to wake up")
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)

        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(123,builder.build())

        val notification: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        val mediaPlayer = MediaPlayer.create(context, notification)
        mediaPlayer.start()
    }

}
