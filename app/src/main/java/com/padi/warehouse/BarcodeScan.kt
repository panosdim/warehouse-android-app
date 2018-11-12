package com.padi.warehouse

import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.KeyEvent
import android.view.View
import com.journeyapps.barcodescanner.CaptureManager
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import com.padi.warehouse.R
import kotlinx.android.synthetic.main.activity_barcode_scan.*

class BarcodeScan : AppCompatActivity(), DecoratedBarcodeView.TorchListener {

    private var capture: CaptureManager? = null
    private var isFlashLightOn = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_barcode_scan)

        //set torch listener
        zxing_barcode_scanner.setTorchListener(this)

        // if the device does not have flashlight in its camera,
        // then remove the switch flashlight button...
        if (!hasFlash()) {
            switch_flashlight.visibility = View.GONE
        } else {
            switch_flashlight.setOnClickListener { switchFlashlight() }
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

    private fun switchFlashlight() {
        isFlashLightOn = if (isFlashLightOn) {
            zxing_barcode_scanner.setTorchOff()
            false
        } else {
            zxing_barcode_scanner.setTorchOn()
            true
        }

    }

    override fun onTorchOn() {
        switch_flashlight.setText(R.string.flashlight_on)
    }

    override fun onTorchOff() {
        switch_flashlight.setText(R.string.flashlight_off)
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