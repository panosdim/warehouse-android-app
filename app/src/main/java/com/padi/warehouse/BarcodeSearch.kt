package com.padi.warehouse

import android.os.AsyncTask
import android.util.Log
import org.json.JSONObject
import java.io.BufferedReader
import java.io.DataOutputStream
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.HttpsURLConnection
import java.util.HashMap
import de.timroes.axmlrpc.XMLRPCClient








class BarcodeSearch(private val mCallback: (result: String) -> Unit) : AsyncTask<String, Void, String>() {

    override fun doInBackground(vararg params: String): String {
        return searchUPCdatabase(params[0])
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

            url = URL("http://warehouse.cc.nf/php/barcode.php")

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

    private fun searchUPCdatabase(barcode: String): String {
        try {
            Log.d("PANOS", barcode)
            val client = XMLRPCClient(URL("https://www.upcdatabase.com/xmlrpc"))
            val params = HashMap<String, String>()
            params["rpc_key"] = "23e95561fc6d2942d45f5ce9ad3bd1bd4b338789"
            params["ean"] = barcode
            val result = client.call("lookup", params) as HashMap<*,*>
            Log.d("PANOS", result.toString())
        } catch (ex: Exception) {
            // Any other exception
            Log.d("PANOS", ex.toString())
        } finally {
            return ""
        }
    }

    companion object {
        private const val TAG = "BarcodeSearch"
    }
}