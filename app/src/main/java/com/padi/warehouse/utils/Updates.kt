package com.padi.warehouse.utils

import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.core.content.pm.PackageInfoCompat
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.padi.warehouse.CHANNEL_ID
import com.padi.warehouse.R
import com.padi.warehouse.TAG
import com.padi.warehouse.models.FileMetadata
import kotlinx.serialization.json.Json

var refId: Long = -1

fun checkForNewVersion(context: Context) {
    val storage = Firebase.storage
    val metadataFileName = "output-metadata.json"
    val apkFileName = "app-release.apk"

    // Create a storage reference from our app
    val storageRef = storage.reference

    // Create a metadata reference
    val metadataRef: StorageReference = storageRef.child(metadataFileName)

    metadataRef.getBytes(Long.MAX_VALUE).addOnSuccessListener {
        // Use the bytes to display the image
        val data = String(it)
        val fileMetadata = Json.decodeFromString<FileMetadata>(data)
        val version = fileMetadata.elements[0].versionCode

        val appVersion = PackageInfoCompat.getLongVersionCode(
            context.packageManager.getPackageInfo(
                context.packageName,
                0
            )
        )

        if (version > appVersion) {
            Toast.makeText(
                context,
                context.getString(R.string.new_version),
                Toast.LENGTH_LONG
            ).show()

            val versionName = fileMetadata.elements[0].versionName

            // Create an apk reference
            val apkRef = storageRef.child(apkFileName)

            apkRef.downloadUrl.addOnSuccessListener { uri ->
                downloadNewVersion(context, uri, versionName)
            }.addOnFailureListener {
                // Handle any errors
                Log.w(TAG, "Fail to download file $apkFileName")
            }
        }
    }.addOnFailureListener {
        // Handle any errors
        Log.w(TAG, "Fail to retrieve $metadataFileName")
    }
}

private fun downloadNewVersion(context: Context, downloadUrl: Uri, version: String) {
    val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    val request =
        DownloadManager.Request(downloadUrl)
    request.setDescription("Downloading new version of Warehouse.")
    request.setTitle("New Warehouse Version: $version")
    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
    request.setDestinationInExternalPublicDir(
        Environment.DIRECTORY_DOWNLOADS,
        "Warehouse-${version}.apk"
    )
    refId = manager.enqueue(request)
}

fun createNotificationChannel(context: Context) {
    // Create the NotificationChannel, but only on API 26+ because
    // the NotificationChannel class is new and not in the support library
    val name = context.getString(R.string.channel_name)
    val importance = NotificationManager.IMPORTANCE_DEFAULT

    val channel = NotificationChannel(CHANNEL_ID, name, importance)
    // Register the channel with the system
    val notificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.createNotificationChannel(channel)
}