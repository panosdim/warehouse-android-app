package com.padi.warehouse

import android.os.AsyncTask
import android.util.Log
import de.timroes.axmlrpc.XMLRPCClient
import org.json.JSONObject
import java.io.BufferedReader
import java.io.DataOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import javax.net.ssl.HttpsURLConnection
import de.timroes.axmlrpc.XMLRPCServerException
import de.timroes.axmlrpc.XMLRPCException
import de.timroes.axmlrpc.XMLRPCCallback



class BarcodeSearch(private val mCallback: (result: String) -> Unit) : AsyncTask<String, Void, String>() {

    override fun doInBackground(vararg params: String): String {
        var result = findProduct(params[0])
        Log.d(TAG, "i520 Result: $result")
        if (result.isNotEmpty()) {
            val res = JSONObject(result)
            if (res.getBoolean("found")) {
                return result
            }
        }
        result = searchUPCdatabase(params[0])
        Log.d(TAG, "UPC Database Result: $result")
        return result
    }

    override fun onPostExecute(result: String) {
        mCallback(result)
    }

    private fun findProduct(barcode: String): String {
        val url: URL
        var response = ""
        try {
            val jsonParam = JSONObject()
            jsonParam.put("barcode", barcode)
            Log.d(TAG, jsonParam.toString())

            url = URL("http://warehouse.cc.nf/api/v1/barcode.php")

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

    fun searchUPCdatabase(barcode: String): String {
        val listener: XMLRPCCallback = object : XMLRPCCallback {
            override fun onResponse(id: Long, result: Any) {
                // Handling the servers response
                Log.d(TAG, result.toString())
            }

            override fun onError(id: Long, error: XMLRPCException) {
                // Handling any error in the library
            }

            override fun onServerError(id: Long, error: XMLRPCServerException) {
                // Handling an error response from the server
            }
        }
        try {
            Log.d(TAG, barcode)
            val client = XMLRPCClient(URL("https://www.upcdatabase.com/xmlrpc"))
            val params = HashMap<String, String>()
            params["rpc_key"] = "23e95561fc6d2942d45f5ce9ad3bd1bd4b338789"
            params["ean"] = barcode
            val result = client.callAsync(listener, "lookup", params)
            Log.d(TAG, result.toString())
        } catch (ex: Exception) {
            // Any other exception
            Log.d(TAG, ex.toString())
        } finally {
            return ""
        }
    }

    companion object {
        private const val TAG = "BarcodeSearch"
    }
}