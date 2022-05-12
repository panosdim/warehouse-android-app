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
import com.padi.warehouse.model.FileMetadata
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.UnsupportedEncodingException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.text.Normalizer
import java.time.format.DateTimeFormatter
import javax.net.ssl.HttpsURLConnection


val REGEX_UNACCENT = "\\p{InCombiningDiacriticalMarks}+".toRegex()
var refId: Long = -1
val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

fun CharSequence.unaccent(): String {
    val temp = Normalizer.normalize(this, Normalizer.Form.NFD)
    return REGEX_UNACCENT.replace(temp, "")
}

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
        val data: HashMap<String, String> = HashMap()

        data["VAL"] = barcode
        data["TYPE"] = "1"
        data["lang"] = "el"
        val urlParameters = getDataString(data)
        val postData: ByteArray = urlParameters.toByteArray(StandardCharsets.UTF_8)
        val postDataLength = postData.size
        val request = "https://www.i520.gr/index.php"
        url = URL(request)
        val conn = url.openConnection() as HttpURLConnection
        conn.doOutput = true
        conn.instanceFollowRedirects = false
        conn.requestMethod = "POST"
        conn.readTimeout = 15000
        conn.connectTimeout = 15000
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
        conn.setRequestProperty("charset", "utf-8")
        conn.setRequestProperty("Content-Length", postDataLength.toString())
        conn.useCaches = false
        DataOutputStream(conn.outputStream).use { wr -> wr.write(postData) }

        val responseCode = conn.responseCode

        if (responseCode == HttpsURLConnection.HTTP_OK) {
            val htmlCode = conn.inputStream.bufferedReader().use(BufferedReader::readText)
            val doc: Document = Jsoup.parse(htmlCode)
            val cssSelectorResult =
                doc.select("#gepirresults > table > tbody > tr.altrow > td:nth-child(2) > em")
            for (description in cssSelectorResult) {
                response = description.text()
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }

    return response
}

@Throws(UnsupportedEncodingException::class)
fun getDataString(params: HashMap<String, String>): String {
    val result = StringBuilder()
    var first = true
    for ((key, value) in params.entries) {
        if (first) first = false else result.append("&")
        result.append(URLEncoder.encode(key, "UTF-8"))
        result.append("=")
        result.append(URLEncoder.encode(value, "UTF-8"))
    }
    return result.toString()
}