package com.padi.warehouse.utils

import android.Manifest
import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import androidx.core.content.ContextCompat
import androidx.core.content.pm.PackageInfoCompat
import com.padi.warehouse.BACKEND_URL
import com.padi.warehouse.CHANNEL_ID
import com.padi.warehouse.R
import org.json.JSONObject
import java.io.BufferedReader
import java.io.DataOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.text.Normalizer
import java.time.format.DateTimeFormatter
import javax.net.ssl.HttpsURLConnection


val REGEX_UNACCENT = "\\p{InCombiningDiacriticalMarks}+".toRegex()
var refId: Long = -1

fun CharSequence.unaccent(): String {
    val temp = Normalizer.normalize(this, Normalizer.Form.NFD)
    return REGEX_UNACCENT.replace(temp, "")
}

val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

fun checkForNewVersion(context: Context) {
    val url: URL
    val response: String
    try {
        url = URL(BACKEND_URL + "output.json")

        val conn = url.openConnection() as HttpURLConnection

        conn.readTimeout = 15000
        conn.connectTimeout = 15000
        conn.requestMethod = "GET"
        conn.doOutput = false

        conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8")

        val responseCode = conn.responseCode

        if (responseCode == HttpsURLConnection.HTTP_OK) {
            response = conn.inputStream.bufferedReader().use(BufferedReader::readText)
            val version = JSONObject(response).getLong("versionCode")
            val appVersion = PackageInfoCompat.getLongVersionCode(
                context.packageManager.getPackageInfo(
                    context.packageName,
                    0
                )
            )
            if (version > appVersion && ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                val versionName = JSONObject(response).getString("versionName")
                downloadNewVersion(context, versionName)
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

private fun downloadNewVersion(context: Context, version: String) {
    val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    val request =
        DownloadManager.Request(Uri.parse(BACKEND_URL + "app-release.apk"))
    request.setDescription("Downloading new version of Warehouse.")
    request.setTitle("New Warehouse Version")
    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
    request.setDestinationInExternalPublicDir(
        Environment.DIRECTORY_DOWNLOADS,
        "Warehouse-${version}.apk"
    )
    refId = manager.enqueue(request)
}

fun createNotificationChannel(context: Context) {
    val name = context.getString(R.string.channel_name)
    val descriptionText = context.getString(R.string.channel_description)
    val importance = NotificationManager.IMPORTANCE_DEFAULT

    val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
        description = descriptionText
    }
    // Register the channel with the system
    val notificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.createNotificationChannel(channel)
}

fun findProductDescription(barcode: String): String {
    val url: URL
    var response = ""
    try {
        val jsonParam = JSONObject()
        jsonParam.put("barcode", barcode)
        url = URL(BACKEND_URL + "barcode.php")

        val conn = url.openConnection() as HttpURLConnection

        conn.readTimeout = 15000
        conn.connectTimeout = 15000
        conn.requestMethod = "POST"
        conn.doOutput = true

        conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8")

        val printout = DataOutputStream(conn.outputStream)
        printout.write(jsonParam.toString().toByteArray(Charsets.UTF_8))
        printout.flush()
        printout.close()

        val responseCode = conn.responseCode

        if (responseCode == HttpsURLConnection.HTTP_OK) {
            response = conn.inputStream.bufferedReader().use(BufferedReader::readText)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }

    return response
}