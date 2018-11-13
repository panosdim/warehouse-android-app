package com.padi.warehouse

import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.KeyEvent
import android.view.View
import com.journeyapps.barcodescanner.CaptureManager
import kotlinx.android.synthetic.main.activity_barcode_scan.*

class BarcodeScan : AppCompatActivity() {

    private var capture: CaptureManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_barcode_scan)

        // if the device does not have flashlight in its camera,
        // then remove the switch flashlight button...
        if (!hasFlash()) {
            switch_flashlight.visibility = View.GONE
        } else {
            switch_flashlight.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    zxing_barcode_scanner.setTorchOn()
                } else {
                    zxing_barcode_scanner.setTorchOff()
                }
            }
        }

        //start capture
        capture = CaptureManager(this, zxing_barcode_scanner)
        capture!!.initializeFromIntent(intent, savedInstanceState)
        capture!!.decode()
    }


    /**
     * Check if the device's camera has a Flashlight.
     *
     * @return true if there is Flashlight, otherwise false.
     */
    private fun hasFlash(): Boolean {
        return applicationContext.packageManager
                .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)
    }

    override fun onResume() {
        super.onResume()
        capture!!.onResume()
    }

    override fun onPause() {
        super.onPause()
        capture!!.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        capture!!.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        capture!!.onSaveInstanceState(outState)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        return zxing_barcode_scanner.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event)
    }

}