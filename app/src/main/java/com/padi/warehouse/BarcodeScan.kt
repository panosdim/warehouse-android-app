package com.padi.warehouse

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.google.firebase.ml.common.FirebaseMLException
import com.padi.warehouse.barcodescanning.BarcodeScanningProcessor
import com.padi.warehouse.barcodescanning.BarcodeSearch
import com.padi.warehouse.common.CameraSource
import com.padi.warehouse.textrecognition.TextRecognitionProcessor
import kotlinx.android.synthetic.main.activity_barcode_scan.*
import java.io.IOException
import java.util.*

class BarcodeScan : AppCompatActivity(), ActivityCompat.OnRequestPermissionsResultCallback {
    private var cameraSource: CameraSource? = null
    private var selectedModel = BARCODE_DETECTION

    private val requiredPermissions: Array<String?>
        get() {
            return try {
                val info = this.packageManager
                        .getPackageInfo(this.packageName, PackageManager.GET_PERMISSIONS)
                val ps = info.requestedPermissions
                if (ps != null && ps.isNotEmpty()) {
                    ps
                } else {
                    arrayOfNulls(0)
                }
            } catch (e: Exception) {
                arrayOfNulls(0)
            }

        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate")

        setContentView(R.layout.activity_barcode_scan)

        if (firePreview == null) {
            Log.d(TAG, "Preview is null")
        }
        if (fireFaceOverlay == null) {
            Log.d(TAG, "graphicOverlay is null")
        }

        if (allPermissionsGranted()) {
            createCameraSource(selectedModel)
        } else {
            getRuntimePermissions()
        }
    }

    private fun createCameraSource(model: String) {
        // If there's no existing cameraSource, create one.
        if (cameraSource == null) {
            cameraSource = CameraSource(this, fireFaceOverlay)
        }

        try {
            cameraSource?.let {
                when (model) {
                    TEXT_DETECTION -> {
                        Log.i(TAG, "Using Text Detector Processor")
                        it.setMachineLearningFrameProcessor(TextRecognitionProcessor())
                    }
                    BARCODE_DETECTION -> {
                        Log.i(TAG, "Using Barcode Detector Processor")
                        val search = BarcodeSearch { product ->
                            Log.d(TAG, "Product: $product")
                            val returnIntent = Intent()
                            if (product.getBoolean("found")) {
                                returnIntent.putExtra("result", product.getString("description"))
                            }
                            setResult(Activity.RESULT_OK, returnIntent)
                            finish()
                        }
                        it.setMachineLearningFrameProcessor(BarcodeScanningProcessor { result ->
                            if (search.status == AsyncTask.Status.PENDING) {
                                Log.d(TAG, "Executing Search")
                                search.execute(result)
                            }
                        })
                    }
                    else -> Log.e(TAG, "Unknown model: $model")
                }
            }
        } catch (e: FirebaseMLException) {
            Log.e(TAG, "can not create camera source: $model")
        }

    }

    /**
     * Starts or restarts the camera source, if it exists. If the camera source doesn't exist yet
     * (e.g., because onResume was called before the camera source was created), this will be called
     * again when the camera source is created.
     */
    private fun startCameraSource() {
        cameraSource.let {
            try {
                if (firePreview == null) {
                    Log.d(TAG, "resume: Preview is null")
                }
                if (fireFaceOverlay == null) {
                    Log.d(TAG, "resume: graphOverlay is null")
                }
                firePreview.start(it, fireFaceOverlay)
            } catch (e: IOException) {
                Log.e(TAG, "Unable to start camera source.", e)
                it?.release()
                cameraSource = null
            }

        }
    }

    public override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume")
        startCameraSource()
    }

    /** Stops the camera.  */
    override fun onPause() {
        super.onPause()
        firePreview.stop()
    }

    public override fun onDestroy() {
        super.onDestroy()
        cameraSource?.release()
    }

    private fun allPermissionsGranted(): Boolean {
        for (permission in requiredPermissions) {
            permission?.let {
                if (!isPermissionGranted(this, it)) {
                    return false
                }
            }
        }
        return true
    }

    private fun getRuntimePermissions() {
        val allNeededPermissions = ArrayList<String>()
        for (permission in requiredPermissions) {
            permission?.let {
                if (!isPermissionGranted(this, it)) {
                    allNeededPermissions.add(it)
                }
            }
        }

        if (!allNeededPermissions.isEmpty()) {
            ActivityCompat.requestPermissions(
                    this, allNeededPermissions.toTypedArray(), PERMISSION_REQUESTS)
        }
    }

    override fun onRequestPermissionsResult(
            requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        Log.i(TAG, "Permission granted!")
        if (allPermissionsGranted()) {
            createCameraSource(selectedModel)
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    companion object {
        private const val TEXT_DETECTION = "Text Detection"
        private const val BARCODE_DETECTION = "Barcode Detection"
        private const val TAG = "BarcodeScan"
        private const val PERMISSION_REQUESTS = 1

        private fun isPermissionGranted(context: Context, permission: String): Boolean {
            if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "Permission granted: $permission")
                return true
            }
            Log.i(TAG, "Permission NOT granted: $permission")
            return false
        }
    }
}
