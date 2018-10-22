package com.padi.warehouse.barcodescanning

import android.os.AsyncTask
import android.util.Log
import com.google.gson.Gson
import de.timroes.axmlrpc.XMLRPCClient
import org.json.JSONObject
import java.io.BufferedReader
import java.io.DataOutputStream
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.HttpsURLConnection


class BarcodeSearch(private val mCallback: (result: JSONObject) -> Unit) : AsyncTask<String, Void, JSONObject>() {

    override fun doInBackground(vararg params: String): JSONObject {
        var result = findProduct(params[0])
        Log.d(TAG, "i520 Result: $result")
        if (result.isNotEmpty()) {
            val res = JSONObject(result)
            if (res.getBoolean("found")) {
                return res
            }
        }
        result = searchUPCDatabase(params[0])
        Log.d(TAG, "UPC Database Result: $result")
        val res = convertStringToHashMap(result)
        return JSONObject(Gson().toJson(res))
    }

    override fun onPostExecute(result: JSONObject) {
        mCallback(result)
    }

    private fun convertStringToHashMap(text: String): HashMap<String, String> {
        var value = text
        value = value.substring(1, value.length - 1)    //remove curly brackets
        val keyValuePairs = value.split(",") //split the string to create key-value pairs
        val map = HashMap<String, String>()

        for (pair in keyValuePairs)
        {
            val entry = pair.split("=")
            map[entry[0].trim()] = entry[1].trim()
        }
        return map
    }

    private fun findProduct(barcode: String): String {
        val url: URL
        var response = ""
        try {
            val jsonParam = JSONObject()
            jsonParam.put("barcode", barcode)

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

    private fun searchUPCDatabase(barcode: String): String {
        var response = ""
        try {
            val client = XMLRPCClient(URL("https://www.upcdatabase.com/xmlrpc"))
            val params = HashMap<String, String>()
            params["rpc_key"] = "23e95561fc6d2942d45f5ce9ad3bd1bd4b338789"
            params["ean"] = barcode
            response = client.call("lookup", params).toString()
        } catch (ex: Exception) {
            // Any other exception
            Log.d(TAG, ex.toString())
        } finally {
            return response
        }
    }

    companion object {
        private const val TAG = "BarcodeSearch"
    }
}