package com.example.prayerapplication

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.azan.Azan
import com.azan.Method
import com.azan.astrologicalCalc.Location
import com.azan.astrologicalCalc.SimpleDate
import com.example.prayerapplication.databinding.ActivityMainBinding
import com.example.prayerapplication.prefs.Prefs
import com.example.prayerapplication.viewmodel.PrayerViewModel
import com.example.prayerapplication.worker.PrayersWorker
import dagger.hilt.android.AndroidEntryPoint
import java.util.GregorianCalendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val prayersViewModel by viewModels<PrayerViewModel>()
    private lateinit var alarmManager: AlarmManager
    private lateinit var workManager: WorkManager
    private lateinit var periodicWorkRequest: PeriodicWorkRequest
    private val WORKER_TAG = "PRAYERS_WORKER"

    @Inject
    lateinit var prefs: Prefs

    private var isPermission = false
    private var checkNotificationPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        isPermission = isGranted
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initialize()
        dailyOneTimeRunWorkerTrigger()
        clickListeners()
    }

    private fun dailyOneTimeRunWorkerTrigger() {
        workManager = WorkManager.getInstance(applicationContext)

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val data = Data.EMPTY

        periodicWorkRequest = PeriodicWorkRequestBuilder<PrayersWorker>(1, TimeUnit.DAYS) //, 15, TimeUnit.MINUTES
            .setInputData(data)
            .setConstraints(constraints)
            .addTag(WORKER_TAG)
            .setBackoffCriteria(BackoffPolicy.LINEAR, PeriodicWorkRequest.MIN_PERIODIC_INTERVAL_MILLIS, TimeUnit.MILLISECONDS)
            .build()


        workManager.enqueueUniquePeriodicWork("Prayers Worker", ExistingPeriodicWorkPolicy.UPDATE, periodicWorkRequest)

        workManager.getWorkInfoByIdLiveData(periodicWorkRequest.id).observe(this){
            when(it.state){
                WorkInfo.State.ENQUEUED -> {
                    Log.d("PRAYERS_WORKER", "ENQUEUED : ${it.progress}")
                }
                WorkInfo.State.RUNNING -> {
                    Log.d("PRAYERS_WORKER", "RUNNING : ${it.progress}")
                }
                WorkInfo.State.SUCCEEDED -> {
                    Log.d("PRAYERS_WORKER", "SUCCEEDED : ${it.progress}")
                }
                WorkInfo.State.CANCELLED -> {
                    Log.d("PRAYERS_WORKER", "CANCELLED : ${it.progress}")
                }
                WorkInfo.State.BLOCKED -> {
                    Log.d("PRAYERS_WORKER", "BLOCKED : ${it.progress}")
                }
                else ->{
                    Log.d("PRAYERS_WORKER", "else : ${it.progress}")
                }
            }
        }
    }

    private fun initialize() {
        setSilentModePolicy()
        checkPermission()
        showTime()
    }

    private fun showTime() {
        val today = SimpleDate(GregorianCalendar())
        val location = Location(23.8103, 90.4125, +6.0, 0)
        val azan = Azan(location, Method.KARACHI_HANAF)
        val prayerTimes = azan.getPrayerTimes(today)

        binding.fazorTimeTV.text = prayerTimes.fajr().toString()
        binding.juhorTimeTV.text = prayerTimes.thuhr().toString()
        binding.asorTimeTV.text = prayerTimes.assr().toString()
        binding.magribTimeTV.text = prayerTimes.maghrib().toString()
        binding.eshaTimeTV.text = prayerTimes.ishaa().toString()
    }

    private fun clickListeners() {

    }

    private fun setSilentModePolicy() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (!notificationManager.isNotificationPolicyAccessGranted) {
            val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
            startActivity(intent)
        }
    }

    private fun checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                isPermission = true
            } else {
                isPermission = false
                checkNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            isPermission = true
        }
    }
}