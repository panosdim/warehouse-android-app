package com.padi.warehouse.ui

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionRequired
import com.google.accompanist.permissions.rememberPermissionState
import com.google.mlkit.vision.barcode.common.Barcode
import com.padi.warehouse.R
import com.padi.warehouse.barcode.BarcodeScanningAnalyzer
import com.padi.warehouse.barcode.CameraView
import com.padi.warehouse.camera
import com.padi.warehouse.paddingLarge
import com.padi.warehouse.viewmodels.MainViewModel

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun BarcodeScanningScreen(onClose: (productName: String) -> Unit) {
    val context = LocalContext.current
    val cameraPermissionState =
        rememberPermissionState(permission = Manifest.permission.CAMERA)

    PermissionRequired(
        permissionState = cameraPermissionState,
        permissionNotGrantedContent = {
            LaunchedEffect(Unit) {
                cameraPermissionState.launchPermissionRequest()
            }
        },
        permissionNotAvailableContent = {
            Column {
                Toast.makeText(context, "Permission denied.", Toast.LENGTH_LONG).show()
            }
        },
        content = {
            ScanSurface(onClose)
        }
    )
}

@Composable
fun ScanSurface(onClose: (productName: String) -> Unit) {
    val viewModel: MainViewModel = viewModel()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
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
                            imageVector = Icons.Filled.ArrowBack,
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

