package com.example.prayerapplication.worker

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.azan.Azan
import com.azan.Method
import com.azan.astrologicalCalc.Location
import com.azan.astrologicalCalc.SimpleDate
import com.example.prayerapplication.model.PrayersTime
import com.example.prayerapplication.receiver.PrayersAlertReceiver
import kotlinx.coroutines.runBlocking
import java.io.IOException
import java.util.Calendar
import java.util.GregorianCalendar

class PrayersWorker(private val context: Context, private val params: WorkerParameters) : Worker(context, params) {

    private lateinit var alarmManager: AlarmManager

    override fun doWork(): Result {
        return try {
            alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            for (i in getPrayersTime()) {
                setRepeatingAlarmExactTime(i.id, i.name, i.hours, i.minutes)
            }

            Result.success()
        }catch (e: IOException) {
            Result.retry()
        }
    }

    private fun setRepeatingAlarmExactTime(id: Int, name: String, hours: Int, minutes: Int) {
        val intent = Intent(context, PrayersAlertReceiver::class.java)
        intent.putExtra("NAME", name)
        intent.putExtra("ID", id)
        intent.putExtra("DELAY_TIME",(1000 * 15))

        val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getBroadcast(
                applicationContext,
                id,
                intent,
                PendingIntent.FLAG_MUTABLE
            )
        } else {
            PendingIntent.getBroadcast(
                applicationContext,
                id,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        }

        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, hours)
            set(Calendar.MINUTE, minutes)
            set(Calendar.SECOND, 0)
        }

        val prayerTimeMillis = calendar.timeInMillis

        alarmManager.cancel(pendingIntent) // first cancel alarm then set the new alarm
        alarmManager.setExact(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            prayerTimeMillis,
            pendingIntent
        )

    }

    private fun getPrayersTime() = runBlocking {
        val today = SimpleDate(GregorianCalendar())
        val location = Location(23.8103, 90.4125, +6.0, 0)
        val azan = Azan(location, Method.KARACHI_HANAF)
        val prayerTimes = azan.getPrayerTimes(today)
        val imsaak = azan.getImsaak(today)

        val prayerTimeList = arrayListOf(
            PrayersTime(
                1,
                "Fajr Time",
                prayerTimes.fajr().hour,
                prayerTimes.fajr().minute
            ),
            PrayersTime(
                2,
                "Dhuhr Time",
                prayerTimes.thuhr().hour,
                prayerTimes.thuhr().minute
            ),
            PrayersTime(
                3,
                "Asr Time",
                prayerTimes.assr().hour,
                prayerTimes.assr().minute
            ),
            PrayersTime(
                4,
                "Maghrib Time",
                prayerTimes.maghrib().hour,
                prayerTimes.maghrib().minute
            ),
            PrayersTime(
                5,
                "Isha Time",
                prayerTimes.ishaa().hour,
                prayerTimes.ishaa().minute
            )
        )
        return@runBlocking prayerTimeList
    }

}