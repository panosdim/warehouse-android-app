package com.padi.warehouse.ui

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddBox
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.IndeterminateCheckBox
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewmodel.compose.viewModel
import com.padi.warehouse.R
import com.padi.warehouse.models.Item
import com.padi.warehouse.paddingLarge
import com.padi.warehouse.utils.FieldState
import com.padi.warehouse.utils.dateRegex
import com.padi.warehouse.viewmodels.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemSheet(item: Item?, bottomSheetState: SheetState) {
    if (bottomSheetState.isVisible) {
        val context = LocalContext.current
        val viewModel: MainViewModel = viewModel()
        val openDeleteDialog = remember { mutableStateOf(false) }
        val showBarcodeScanner = remember { mutableStateOf(false) }
        val scope = rememberCoroutineScope()

        val productName = remember {
            FieldState(item?.name ?: "") {
                if (it.isEmpty()) {
                    return@FieldState Pair(
                        true,
                        context.getString(R.string.product_name_error_empty)
                    )
                }
                return@FieldState Pair(false, "")
            }
        }
        val amount = remember {
            FieldState(item?.amount ?: "1") {
                if (it.isEmpty()) {
                    return@FieldState Pair(true, context.getString(R.string.amount_error_empty))
                }
                return@FieldState Pair(false, "")
            }
        }
        val expiration = remember {
            FieldState(item?.expirationDate ?: "") {
                if (it.matches(Regex(dateRegex))) {
                    return@FieldState Pair(false, "")
                } else {
                    if (it.isEmpty()) {
                        return@FieldState Pair(
                            true,
                            context.getString(R.string.expiration_error_empty)
                        )
                    }
                    return@FieldState Pair(true, context.getString(R.string.date_format_not_valid))
                }
            }
        }
        val box = remember {
            FieldState(item?.box ?: "") {
                if (it.isEmpty()) {
                    return@FieldState Pair(true, context.getString(R.string.box_error_empty))
                }
                return@FieldState Pair(false, "")
            }
        }

        if (openDeleteDialog.value) {
            AlertDialog(
                onDismissRequest = {
                    openDeleteDialog.value = false
                },
                title = {
                    Text(text = stringResource(id = R.string.delete_item_dialog_title))
                },
                text = {
                    Text(
                        stringResource(id = R.string.delete_item_dialog_description)
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            openDeleteDialog.value = false

                            if (item != null) {
                                scope.launch {
                                    viewModel.deleteItem(item).collect {
                                        withContext(Dispatchers.Main) {
                                            if (it) {
                                                Toast.makeText(
                                                    context, R.string.delete_item_toast,
                                                    Toast.LENGTH_LONG
                                                ).show()

                                                scope.launch {
                                                    bottomSheetState.hide()
                                                }
                                            } else {
                                                Toast.makeText(
                                                    context,
                                                    R.string.delete_item_error_toast,
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            }
                                        }
                                    }
                                }
                            }

                            scope.launch {
                                bottomSheetState.hide()
                            }
                        }
                    ) {
                        Text(stringResource(id = R.string.confirm))
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            openDeleteDialog.value = false
                        }
                    ) {
                        Text(stringResource(id = R.string.dismiss))
                    }
                }
            )
        }

        fun isFormValid(): Boolean {
            if (amount.hasError || box.hasError || productName.hasError || expiration.hasError) {
                return false
            } else {
                // Check if we change something in the object
                if (item != null &&
                    item.expirationDate == expiration.value &&
                    item.amount == amount.value &&
                    item.name == productName.value &&
                    item.box == box.value
                ) {
                    return false
                }
            }
            return true
        }

        if (showBarcodeScanner.value) {
            BarcodeScanningScreen {
                showBarcodeScanner.value = false
                if (it.isNotEmpty()) {
                    productName.value = it
                }
            }
        } else {
            ModalBottomSheet(
                onDismissRequest = { scope.launch { bottomSheetState.hide() } },
                sheetState = bottomSheetState,
            ) {
                // Item Form
                Column(
                    modifier = Modifier
                        .padding(paddingLarge),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (item != null) stringResource(id = R.string.edit_item) else stringResource(
                            id = R.string.add_item
                        ),
                        style = MaterialTheme.typography.headlineMedium
                    )

                    OutlinedTextField(
                        value = productName.value,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            capitalization = KeyboardCapitalization.Characters,
                            imeAction = ImeAction.Next
                        ),
                        trailingIcon = {
                            Icon(
                                painter = painterResource(id = R.drawable.barcode_scanner),
                                contentDescription = null,
                                modifier = Modifier.clickable {
                                    showBarcodeScanner.value = true
                                }
                            )
                        },
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

                    OutlinedDatePicker(
                        state = expiration,
                        label = stringResource(id = R.string.expiration_date),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = paddingLarge)
                    )

                    OutlinedTextField(
                        value = amount.value,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        isError = amount.hasError,
                        supportingText = {
                            if (amount.hasError) {
                                Text(
                                    modifier = Modifier.fillMaxWidth(),
                                    text = amount.errorMessage,
                                    textAlign = TextAlign.End,
                                )
                            }
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.IndeterminateCheckBox,
                                contentDescription = null,
                                modifier = Modifier.clickable {
                                    if (amount.value.isNotEmpty()) {
                                        var newAmount = amount.value.toInt()
                                        if (newAmount > 1) {
                                            newAmount--
                                        }
                                        amount.value = newAmount.toString()
                                    } else {
                                        amount.value = "1"
                                    }
                                }
                            )
                        },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.AddBox,
                                contentDescription = null,
                                modifier = Modifier.clickable {
                                    if (amount.value.isNotEmpty()) {
                                        var newAmount = amount.value.toInt()
                                        newAmount++
                                        amount.value = newAmount.toString()
                                    } else {
                                        amount.value = "1"
                                    }
                                }
                            )
                        },
                        singleLine = true,
                        onValueChange = { amount.value = it },
                        label = { Text(stringResource(id = R.string.amount)) },
                        modifier = Modifier
                            .padding(bottom = paddingLarge)
                            .fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = box.value,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        placeholder = {
                            Text(
                                text = stringResource(id = R.string.box_number),
                            )
                        },
                        isError = box.hasError,
                        supportingText = {
                            if (box.hasError) {
                                Text(
                                    modifier = Modifier.fillMaxWidth(),
                                    text = box.errorMessage,
                                    textAlign = TextAlign.End,
                                )
                            }
                        },
                        singleLine = true,
                        onValueChange = { box.value = it },
                        label = { Text(stringResource(id = R.string.box)) },
                        modifier = Modifier
                            .fillMaxWidth()
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .padding(bottom = paddingLarge)
                            .fillMaxWidth()
                    ) {
                        SuggestionChip(
                            onClick = { box.value = "1" },
                            label = { Text("Box 1") }
                        )
                        SuggestionChip(
                            onClick = { box.value = "2" },
                            label = { Text("Box 2") }
                        )
                        SuggestionChip(
                            onClick = { box.value = "3" },
                            label = { Text("Box 3") }
                        )
                        SuggestionChip(
                            onClick = { box.value = "4" },
                            label = { Text("Box 4") }
                        )
                    }

                    if (item != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                        ) {
                            OutlinedButton(
                                onClick = { openDeleteDialog.value = true },
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = null,
                                    modifier = Modifier.size(ButtonDefaults.IconSize)
                                )
                                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                                Text(stringResource(id = R.string.delete))
                            }

                            Button(
                                enabled = isFormValid(),
                                onClick = {
                                    // Update item object
                                    item.expirationDate = expiration.value
                                    item.amount = amount.value
                                    item.name = productName.value
                                    item.box = box.value

                                    scope.launch {
                                        viewModel.updateItem(item).collect {
                                            withContext(Dispatchers.Main) {
                                                if (it) {
                                                    Toast.makeText(
                                                        context, R.string.item_updated_toast,
                                                        Toast.LENGTH_LONG
                                                    ).show()

                                                    scope.launch {
                                                        bottomSheetState.hide()
                                                    }
                                                } else {
                                                    Toast.makeText(
                                                        context,
                                                        R.string.item_updated_error_toast,
                                                        Toast.LENGTH_LONG
                                                    ).show()
                                                }
                                            }
                                        }
                                    }
                                },
                            ) {
                                Icon(
                                    Icons.Filled.Save,
                                    contentDescription = null,
                                    modifier = Modifier.size(ButtonDefaults.IconSize)
                                )
                                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                                Text(stringResource(id = R.string.update))
                            }
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier
                                .fillMaxWidth()
                        ) {
                            Button(
                                enabled = isFormValid(),
                                onClick = {
                                    val newItem =
                                        Item(
                                            null,
                                            expirationDate = expiration.value,
                                            amount = amount.value,
                                            name = productName.value,
                                            box = box.value
                                        )

                                    scope.launch {
                                        viewModel.addNewItem(newItem).collect {
                                            withContext(Dispatchers.Main) {
                                                if (it) {
                                                    Toast.makeText(
                                                        context, R.string.item_added_toast,
                                                        Toast.LENGTH_LONG
                                                    ).show()

                                                    scope.launch {
                                                        bottomSheetState.hide()
                                                    }
                                                } else {
                                                    Toast.makeText(
                                                        context,
                                                        R.string.item_added_error_toast,
                                                        Toast.LENGTH_LONG
                                                    ).show()
                                                }
                                            }
                                        }
                                    }
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
}