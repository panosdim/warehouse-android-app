package com.padi.warehouse

import com.padi.warehouse.utils.findProductDescription
import com.padi.warehouse.utils.getDataString
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class BarcodeUnitTest {
    @Test
    fun urlEncodedString() {
        val data: HashMap<String, String> = HashMap()

        data["VAL"] = "5201050124042"
        data["TYPE"] = "1"
        data["lang"] = "el"
        val result = getDataString(data)
        assertEquals("VAL=5201050124042&lang=el&TYPE=1", result)
    }

    @Test
    fun barcodeScan() {
        val result = findProductDescription("5201050124042")
        assertEquals("ΚΑΛΑΣ-20% ΑΛΑΤΙ 400ΓΡ.", result)
    }
}
