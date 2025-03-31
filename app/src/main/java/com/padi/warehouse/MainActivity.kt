package com.padi.warehouse

import android.Manifest
import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.app.NotificationManagerCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.firebase.FirebaseApp
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.padi.warehouse.ui.MainScreen
import com.padi.warehouse.ui.theme.WarehouseTheme
import com.padi.warehouse.utils.checkForNewVersion
import com.padi.warehouse.utils.createNotificationChannel
import com.padi.warehouse.utils.refId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    private lateinit var manager: DownloadManager
    private lateinit var remoteConfig: FirebaseRemoteConfig
    private val scope = CoroutineScope(Dispatchers.IO)

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val requestPermissionLauncher =
            registerForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()
            ) { result ->
                result.forEach {
                    if (!it.value) {
                        Toast.makeText(
                            this,
                            "Permission ${it.key} not granted.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

        // Handle new version installation after the download of APK file.
        manager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        onComplete = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val referenceId = intent!!.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                if (referenceId != -1L && referenceId == refId) {
                    val apkUri = manager.getUriForDownloadedFile(refId)
                    val installIntent = Intent(Intent.ACTION_VIEW)
                    installIntent.setDataAndType(apkUri, "application/vnd.android.package-archive")
                    installIntent.flags =
                        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
                    startActivity(installIntent)
                }

            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(
                onComplete,
                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
                RECEIVER_EXPORTED
            )
        } else {
            registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
        }

        // Initialize Firebase
        createNotificationChannel(this)
        FirebaseApp.initializeApp(this)

        remoteConfig = FirebaseRemoteConfig.getInstance()
        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(2592000) // Fetch at least every 30 days
            .build()
        remoteConfig.setConfigSettingsAsync(configSettings)

        remoteConfig.fetchAndActivate()
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val updateUrl = remoteConfig.getString("UPDATE_URL")
                    // Check for new version
                    scope.launch {
                        checkForNewVersion(this@MainActivity, updateUrl)
                    }
                } else {
                    // Handle fetch failure (e.g., log the error)
                    Log.e(TAG, "Error fetching remote config", task.exception)
                }
            }

        // Check for Notifications Permissions
        if (!NotificationManagerCompat.from(this).areNotificationsEnabled()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requestPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.POST_NOTIFICATIONS,
                        Manifest.permission.CAMERA
                    )
                )
            }
        }


        // Check for expired items
        val itemExpiredBuilder =
            PeriodicWorkRequestBuilder<ExpiredItemsWorker>(7, TimeUnit.DAYS)

        val itemExpiredWork = itemExpiredBuilder.build()
        // Then enqueue the recurring task:
        WorkManager.getInstance(this@MainActivity).enqueueUniquePeriodicWork(
            "warehouse-item-expired",
            ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
            itemExpiredWork
        )

        setContent {
            WarehouseTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
        }
    }
}