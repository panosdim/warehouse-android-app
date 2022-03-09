package com.padi.warehouse.activities

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.journeyapps.barcodescanner.CaptureManager
import com.padi.warehouse.databinding.ActivityBarcodeScanBinding

class BarcodeScanActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBarcodeScanBinding
    private lateinit var capture: CaptureManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBarcodeScanBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        // if the device does not have flashlight in its camera,
        // then remove the switch flashlight button...
        if (!hasFlash()) {
            binding.switchFlashlight.visibility = View.GONE
        } else {
            binding.switchFlashlight.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    binding.zxingBarcodeScanner.setTorchOn()
                } else {
                    binding.zxingBarcodeScanner.setTorchOff()
                }
            }
        }

        //start capture
        capture = CaptureManager(this, binding.zxingBarcodeScanner)
        capture.initializeFromIntent(intent, savedInstanceState)
        capture.decode()
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
        capture.onResume()
    }

    override fun onPause() {
        super.onPause()
        capture.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        capture.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        capture.onSaveInstanceState(outState)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        return binding.zxingBarcodeScanner.onKeyDown(keyCode, event) || super.onKeyDown(
            keyCode,
            event
        )
    }

}