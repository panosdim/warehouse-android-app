package com.padi.warehouse.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import com.padi.warehouse.R
import com.padi.warehouse.utils.FieldState
import com.padi.warehouse.utils.dateFormatter
import com.padi.warehouse.utils.fromEpochMilli
import com.padi.warehouse.utils.toEpochMilli
import com.padi.warehouse.utils.toLocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OutlinedDatePicker(
    state: FieldState<String>,
    label: String,
    modifier: Modifier,
) {
    val openDatePickerDialog = remember { mutableStateOf(false) }
    val datePickerState =
        rememberDatePickerState(
            initialSelectedDateMillis = state.value.toLocalDate()?.toEpochMilli()
        )
    val confirmEnabled = remember {
        derivedStateOf { datePickerState.selectedDateMillis != null }
    }

    if (openDatePickerDialog.value) {
        DatePickerDialog(
            onDismissRequest = {
                openDatePickerDialog.value = false
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        state.value =
                            datePickerState.selectedDateMillis?.fromEpochMilli()
                                ?.format(dateFormatter)
                                .toString()
                        openDatePickerDialog.value = false
                    },
                    enabled = confirmEnabled.value
                ) {
                    Text(stringResource(R.string.ok))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        openDatePickerDialog.value = false
                    }
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState, showModeToggle = false)
        }
    }

    OutlinedTextField(
        modifier = modifier,
        label = { Text(text = label) },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Done
        ),
        singleLine = true,
        value = state.value,
        trailingIcon = {
            Icon(
                imageVector = Icons.Default.CalendarToday,
                contentDescription = null,
                modifier = Modifier.clickable {
                    openDatePickerDialog.value = true
                }
            )
        },
        placeholder = {
            Text(
                text = stringResource(id = R.string.date_format),
            )
        },
        isError = state.hasError,
        supportingText = {
            if (state.hasError) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = state.errorMessage,
                    textAlign = TextAlign.End,
                )
            }
        },
        onValueChange = {
            state.value = it
        },
    )
}