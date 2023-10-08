package com.example.prayerapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.batoulapps.adhan.CalculationMethod
import com.batoulapps.adhan.Coordinates
import com.batoulapps.adhan.Madhab
import com.batoulapps.adhan.PrayerTimes
import com.batoulapps.adhan.data.DateComponents
import com.example.prayerapplication.databinding.ActivityMainBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        workingMachine()
        showTime()
    }

    private fun workingMachine() {
        val request = PeriodicWorkRequestBuilder<LabourWorker>(15, TimeUnit.MINUTES).build()
        //val request = OneTimeWorkRequestBuilder<LabourWorker>().build()

        val requestUUID = request.id
        val workManager = WorkManager.getInstance(this)

        // In this case we use enqueueUniquePeriodicWork for destroy previous background task
        workManager.enqueueUniquePeriodicWork("Getdata", ExistingPeriodicWorkPolicy.UPDATE, request)
        //workManager.enqueue(request)

        workManager.getWorkInfoByIdLiveData(requestUUID).observe(this, Observer { workInfo ->
            if (workInfo != null) {
                val result = workInfo.outputData.getString("work_result")
                if (workInfo.state == WorkInfo.State.SUCCEEDED) {
                    Log.d("WorkManager onSuccess", "$result")
                } else if (workInfo.state == WorkInfo.State.FAILED) {
                    Log.d("WorkManager onFailed", "$result")
                }
            }
        })
    }

    private fun showTime() {
        val coordinates = Coordinates(23.8103, 90.4125)
        val dateComponents = DateComponents.from(Date())
        val parameters = CalculationMethod.KARACHI.parameters
        parameters.madhab = Madhab.HANAFI

        val formatter = SimpleDateFormat("hh:mm a", Locale.US)
        val localCurrentDateTime = formatter.format(Calendar.getInstance().time)
        val prayerTimes = PrayerTimes(coordinates, dateComponents, parameters)

        val fajr = formatter.format(prayerTimes.fajr)
        val juhor = formatter.format(prayerTimes.dhuhr)
        val asr = formatter.format(prayerTimes.asr)
        val maghrib = formatter.format(prayerTimes.maghrib)
        val esha = formatter.format(prayerTimes.isha)
        val sunRise = formatter.format(prayerTimes.sunrise)

        binding.fazorTimeTV.text = fajr
        binding.juhorTimeTV.text = juhor
        binding.asorTimeTV.text = asr
        binding.magribTimeTV.text = maghrib
        binding.eshaTimeTV.text = esha
        binding.sunriseTimeTV.text = sunRise
    }
}