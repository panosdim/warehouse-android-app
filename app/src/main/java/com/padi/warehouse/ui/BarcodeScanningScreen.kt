package com.padi.warehouse.ui

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.mlkit.vision.barcode.common.Barcode
import com.padi.warehouse.R
import com.padi.warehouse.barcode.BarcodeScanningAnalyzer
import com.padi.warehouse.barcode.CameraView
import com.padi.warehouse.camera
import com.padi.warehouse.paddingLarge
import com.padi.warehouse.viewmodels.MainViewModel

@Composable
fun BarcodeScanningScreen(onClose: (productName: String) -> Unit) {
    val context = LocalContext.current
    var hasPermission by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            hasPermission = isGranted
            if (!isGranted) {
                Toast.makeText(context, "Camera permission denied.", Toast.LENGTH_LONG).show()
            }
        }
    )

    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    if (hasPermission) {
        ScanSurface(onClose)
    } else {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "Camera permission is required to scan barcodes.")
        }
    }
}

@Composable
fun ScanSurface(onClose: (productName: String) -> Unit) {
    val viewModel: MainViewModel = viewModel()
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val detectedBarcode = remember { mutableStateOf<Barcode?>(null) }

    val torchEnabled = remember { mutableStateOf(false) }
    val hasFlash = context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)

    var openAddProductNameDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        CameraView(
            context = context,
            lifecycleOwner = lifecycleOwner,
            analyzer = BarcodeScanningAnalyzer { barcodes ->
                if (detectedBarcode.value == null) {
                    detectedBarcode.value = barcodes.firstOrNull()
                }
            }
        )
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(paddingLarge)
                .fillMaxHeight()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = paddingLarge),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    IconButton(onClick = { onClose("") }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                        )
                    }
                    Text(
                        text = stringResource(id = R.string.barcode_detection_title),
                        style = MaterialTheme.typography.titleLarge,
                    )
                    Spacer(Modifier.weight(1f))
                    if (hasFlash) {
                        IconButton(
                            onClick = {
                                torchEnabled.value = !torchEnabled.value
                                camera?.cameraControl?.enableTorch(torchEnabled.value)
                            }) {
                            Icon(
                                imageVector = if (torchEnabled.value) Icons.Filled.FlashOff else Icons.Filled.FlashOn,
                                contentDescription = null,
                            )
                        }
                    }
                }
            }

            detectedBarcode.value?.let { barcode ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = paddingLarge),
                ) {
                    Column(
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(paddingLarge)
                    ) {
                        Text(
                            text = "Checking barcode",
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                        Text(
                            text = barcode.displayValue.toString(),
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                }

                val productName = viewModel.findProductName(barcode.displayValue.toString())
                    .collectAsStateWithLifecycle(initialValue = null)

                productName.value?.let {
                    if (it.isNotEmpty()) {
                        onClose(it)
                    } else {
                        openAddProductNameDialog = true
                    }
                }

                AddProductNameDialog(
                    barcode.displayValue.toString(),
                    openAddProductNameDialog,
                ) {
                    openAddProductNameDialog = false
                    onClose(it)
                }
            }
        }
    }
}
