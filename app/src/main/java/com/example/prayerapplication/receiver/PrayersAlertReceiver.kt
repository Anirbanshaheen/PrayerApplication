package com.example.prayerapplication.receiver

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.RingtoneManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.example.prayerapplication.MainActivity
import com.example.prayerapplication.R

class PrayersAlertReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        sendNotification(context, intent)
        changeRingingMode(context, intent)
    }

    private fun changeRingingMode(context: Context, intent: Intent) {
        val audioManger = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManger.ringerMode = AudioManager.RINGER_MODE_VIBRATE

        Handler(Looper.getMainLooper()).postDelayed({
            audioManger.ringerMode = AudioManager.RINGER_MODE_NORMAL
        }, intent.getLongExtra("DELAY_TIME", (1000 * 15)))
    }

    private fun sendNotification(context: Context, intent: Intent) {
        val id = intent.getIntExtra("ID", 1)
        val intent = Intent(context, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        intent.putExtra(NOTIFICATION_ID, id)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val titleNotification = intent.getStringExtra("NAME")
        val subTitleNotification = "Your Phone Is Going To Vibrate Mode Now."
        val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_MUTABLE)
        } else {
            PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        }

        val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(titleNotification)
            .setContentText(subTitleNotification)
            .setDefaults(Notification.DEFAULT_ALL)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        notification.priority = NotificationCompat.PRIORITY_MAX

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notification.setChannelId(NOTIFICATION_CHANNEL)

            val ringtoneManager = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val channel = NotificationChannel(NOTIFICATION_CHANNEL, NOTIFICATION_NAME, NotificationManager.IMPORTANCE_HIGH)

            channel.enableLights(true)
            channel.lightColor = Color.RED
            channel.enableVibration(true)
            channel.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 600, 700, 800, 900)
            channel.setSound(ringtoneManager, audioAttributes)
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(id, notification.build())
    }

    companion object {
        const val NOTIFICATION_ID = "appName_notification_id"
        const val NOTIFICATION_NAME = "appName"
        const val NOTIFICATION_CHANNEL = "appName_channel_01"
    }
}