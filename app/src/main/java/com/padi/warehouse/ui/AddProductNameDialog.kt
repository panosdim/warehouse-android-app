package com.padi.warehouse.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.padi.warehouse.R
import com.padi.warehouse.paddingLarge
import com.padi.warehouse.utils.FieldState
import com.padi.warehouse.viewmodels.MainViewModel

@Composable
fun AddProductNameDialog(
    barcode: String,
    open: Boolean,
    onClose: (productName: String) -> Unit
) {
    if (open) {
        val context = LocalContext.current
        val viewModel: MainViewModel = viewModel()

        val productName = remember {
            FieldState("") {
                if (it.isEmpty()) {
                    return@FieldState Pair(
                        true,
                        context.getString(R.string.product_name_error_empty)
                    )
                }
                return@FieldState Pair(false, "")
            }
        }

        Dialog(
            onDismissRequest = {
                onClose("")
            }
        ) {
            Surface(
                modifier = Modifier
                    .wrapContentWidth()
                    .wrapContentHeight(),
                shape = MaterialTheme.shapes.large,
                tonalElevation = AlertDialogDefaults.TonalElevation
            ) {
                // Form
                Column(
                    modifier = Modifier
                        .padding(paddingLarge),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        stringResource(
                            id = R.string.add_product_name
                        ),
                        style = MaterialTheme.typography.headlineMedium
                    )

                    Text(
                        barcode,
                        style = MaterialTheme.typography.headlineMedium
                    )

                    OutlinedTextField(
                        value = productName.value,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            capitalization = KeyboardCapitalization.Characters,
                            imeAction = ImeAction.Next
                        ),
                        isError = productName.hasError,
                        supportingText = {
                            if (productName.hasError) {
                                Text(
                                    modifier = Modifier.fillMaxWidth(),
                                    text = productName.errorMessage,
                                    textAlign = TextAlign.End,
                                )
                            }
                        },
                        singleLine = true,
                        onValueChange = { productName.value = it },
                        label = { Text(stringResource(id = R.string.product_name)) },
                        modifier = Modifier
                            .fillMaxWidth()
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Button(
                            enabled = !productName.hasError,
                            onClick = {
                                viewModel.addProductName(barcode, productName.value)

                                onClose(productName.value)
                            },
                        ) {
                            Icon(
                                Icons.Filled.Add,
                                contentDescription = null,
                                modifier = Modifier.size(ButtonDefaults.IconSize)
                            )
                            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                            Text(stringResource(id = R.string.create))
                        }
                    }
                }
            }
        }
    }
}