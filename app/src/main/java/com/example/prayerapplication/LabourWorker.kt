package com.example.prayerapplication

import android.content.Context
import android.media.AudioManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.batoulapps.adhan.CalculationMethod
import com.batoulapps.adhan.Coordinates
import com.batoulapps.adhan.Madhab
import com.batoulapps.adhan.PrayerTimes
import com.batoulapps.adhan.data.DateComponents
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*

class LabourWorker(private val context: Context, private val workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    var secondTime = "02:39 PM"
    var firstTime = "02:37 PM"

    @RequiresApi(Build.VERSION_CODES.O)
    override fun doWork(): Result {
        /*Handler(Looper.getMainLooper()).postDelayed({
            Toast.makeText(context, "lolololo", Toast.LENGTH_LONG).show()
            Log.d("WorkManager", "dddddddddddddddddddddd")
            *//*var startTime = System.currentTimeMillis().toString()
            Toast.makeText(context, "lolololo",Toast.LENGTH_SHORT).show()
            val formatter = SimpleDateFormat("hh:mm a", Locale.US)
            val localCurrentDateTime = formatter.format(Calendar.getInstance().time)

            if (localCurrentDateTime == tempTime2) {
                Toast.makeText(context, "lolololo", Toast.LENGTH_SHORT).show()
                val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                audioManager.ringerMode = AudioManager.RINGER_MODE_SILENT
                Log.d("WorkManager", "YESSSSS")
            }*//*
        }, 60000)*/
        workManagerOperation(context)
        backgroundOperation(context)
        return Result.success()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun backgroundOperation(context: Context) {
        val coordinates = Coordinates(23.8103, 90.4125)
        val dateComponents = DateComponents.from(Date())
        val parameters = CalculationMethod.KARACHI.parameters
        parameters.madhab = Madhab.HANAFI

        val formatter = SimpleDateFormat("hh:mm a", Locale.US)
        //val localCurrentDateTime = formatter.format(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)).toLong()
        val localCurrentDateTimeInSec =
            formatter.format(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC) / 1000).toLong()

        /*val zoneDateTime: ZonedDateTime = ZonedDateTime.of(LocalDateTime.now(), ZoneId.systemDefault())
        val localCurrentTimeMill = zoneDateTime.toInstant().toEpochMilli()*/

        //val integerLocalDateTime = localCurrentDateTime.filter(Char::isDigit).toInt()

        val prayerTimes = PrayerTimes(coordinates, dateComponents, parameters)

        val fajr = formatter.format(prayerTimes.fajr)
        val intFajr = fajr.filter(Char::isDigit).toInt()

        val dhuhr = formatter.format(prayerTimes.dhuhr)
        val intDhuhr = dhuhr.filter(Char::isDigit).toInt()

        val asr = formatter.format(prayerTimes.asr)
        val intAsr = asr.filter(Char::isDigit).toInt()

        val maghrib = formatter.format(prayerTimes.maghrib)
        val intMaghrib = maghrib.filter(Char::isDigit).toInt()

        val isha = formatter.format(prayerTimes.isha)
        val intIsha = isha.filter(Char::isDigit).toInt()

        val fajrActualTime = (localCurrentDateTimeInSec - intFajr) * 60 // to Seconds
        val dhuhrActualTime = (localCurrentDateTimeInSec - intDhuhr) * 60 // to Seconds
        val asrActualTime = (localCurrentDateTimeInSec - intAsr) * 60 // to Seconds
        val maghribActualTime = (localCurrentDateTimeInSec - intMaghrib) * 60 // to Seconds
        val ishaActualTime = (localCurrentDateTimeInSec - intIsha) * 60 // to Seconds

        if ((fajrActualTime in 0..60) && (fajrActualTime <= 15 * 60)) {
            backgroundTask(context, fajrActualTime.toString())
        } else if ((dhuhrActualTime in 0..60) && (dhuhrActualTime <= 15 * 60)) {
            backgroundTask(context, dhuhrActualTime.toString())
        } else if ((asrActualTime in 0..60) && (asrActualTime <= 15 * 60)) {
            backgroundTask(context, asrActualTime.toString())
        } else if ((maghribActualTime in 0..60) && (maghribActualTime <= 15 * 60)) {
            backgroundTask(context, maghribActualTime.toString())
        } else if ((ishaActualTime in 0..60) && (ishaActualTime <= 15 * 60)) {
            backgroundTask(context, ishaActualTime.toString())
        }

        /*if((localCurrentDateTimeInSec - integerFajr >= 0) && (localCurrentDateTimeInSec - integerFajr <= 15*60*1000)){
            backgroundTask(context, localCurrentDateTimeInSec.toString())
        } else if ((localCurrentDateTimeInSec - dhuhr.toLong() >= 0) && (localCurrentDateTimeInSec - dhuhr.toLong() <= 15*60*1000)) {
            backgroundTask(context, localCurrentDateTimeInSec.toString())
        } else if ((localCurrentDateTimeInSec - asr.toLong() >= 0) && (localCurrentDateTimeInSec - asr.toLong() <= 15*60*1000)){
            backgroundTask(context, localCurrentDateTimeInSec.toString())
        } else if ((localCurrentDateTimeInSec - maghrib.toLong() >= 0) && (localCurrentDateTimeInSec - maghrib.toLong() <= 15*60*1000)){
            backgroundTask(context, localCurrentDateTimeInSec.toString())
        } else if ((localCurrentDateTimeInSec - isha.toLong() >= 0) && (localCurrentDateTimeInSec - isha.toLong() <= 15*60*1000)){
            backgroundTask(context, localCurrentDateTimeInSec.toString())
        }*/

        Log.d("WorkManager", "handlerOperation")
    }

    private fun backgroundTask(context: Context, localCurrentDateTime: String?) {
        Handler(Looper.getMainLooper()).postDelayed({
            Toast.makeText(context, "Background Task call", Toast.LENGTH_SHORT).show()
            //Log.d("WorkManager", "Handler YESSSSS")

            doSilent(context, localCurrentDateTime)
        }, 60000)
    }

    private fun workManagerOperation(context: Context) {
        val coordinates = Coordinates(23.8103, 90.4125)
        val dateComponents = DateComponents.from(Date())
        val parameters = CalculationMethod.KARACHI.parameters
        parameters.madhab = Madhab.HANAFI

        val formatter = SimpleDateFormat("hh:mm a", Locale.US)

        val localCurrentDateTime = formatter.format(Calendar.getInstance().time)
        val prayerTimes = PrayerTimes(coordinates, dateComponents, parameters)
        val fajr = formatter.format(prayerTimes.fajr)
        val dhuhr = formatter.format(prayerTimes.dhuhr)
        val asr = formatter.format(prayerTimes.asr)
        val maghrib = formatter.format(prayerTimes.maghrib)
        val isha = formatter.format(prayerTimes.isha)

        if (fajr == localCurrentDateTime) {
            doSilent(context, localCurrentDateTime)
        } else if (dhuhr == localCurrentDateTime) {
            doSilent(context, localCurrentDateTime)
        } else if (asr == localCurrentDateTime) {
            doSilent(context, localCurrentDateTime)
        } else if (maghrib == localCurrentDateTime) {
            doSilent(context, localCurrentDateTime)
        } else if (isha == localCurrentDateTime) {
            doSilent(context, localCurrentDateTime)
        }

        Log.d("WorkManager", "workManagerOperation")
    }

    private fun doSilent(context: Context, localCurrentDateTime: String?) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.ringerMode = AudioManager.RINGER_MODE_SILENT
        Log.d("WorkManager", "Main YES $localCurrentDateTime")
    }

    private fun internalTimeCheck() {
        Handler(Looper.getMainLooper()).postDelayed({
            //Log.d("WorkManager", "YESSSSS")
//            var startTime = System.currentTimeMillis().toString()
            Toast.makeText(context, "demo Toast", Toast.LENGTH_SHORT).show()
            val formatter = SimpleDateFormat("hh:mm a", Locale.US)
            val localCurrentDateTime = formatter.format(Calendar.getInstance().time)

            if (localCurrentDateTime == secondTime) {
                Toast.makeText(context, "original Toast", Toast.LENGTH_LONG).show()
                val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                audioManager.ringerMode = AudioManager.RINGER_MODE_SILENT
                Log.d("WorkManager", "YESSSSS")
            }
        }, 60000)
    }

    private fun extendTime() {
        var currentTime = Calendar.getInstance()
        currentTime.add(Calendar.MINUTE, 18)
        currentTime.time

        Log.d("WorkManager", (currentTime.time).toString())
    }

}