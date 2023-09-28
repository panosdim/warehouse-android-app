package com.padi.warehouse.utils

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.UnsupportedEncodingException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import javax.net.ssl.HttpsURLConnection

fun findProductNameInSite(barcode: String): String {
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