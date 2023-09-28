package com.padi.warehouse.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.padi.warehouse.paddingLarge
import com.padi.warehouse.utils.SortDirection
import com.padi.warehouse.utils.SortField
import com.padi.warehouse.viewmodels.SortViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SortSheet(
    bottomSheetState: SheetState
) {
    val edgeToEdgeEnabled by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val sortViewModel: SortViewModel = viewModel()

    val optionsField = enumValues<SortField>().map { it.title }
    val optionsDirection = enumValues<SortDirection>().map { it.title }

    var expandedField by remember { mutableStateOf(false) }
    var expandedDirection by remember { mutableStateOf(false) }

    val selectedFieldText =
        sortViewModel.sortField.collectAsStateWithLifecycle(initialValue = SortField.DATE)
    val selectedDirectionText =
        sortViewModel.sortDirection.collectAsStateWithLifecycle(initialValue = SortDirection.DESC)

    // Sheet content
    if (bottomSheetState.isVisible) {
        val windowInsets = if (edgeToEdgeEnabled)
            WindowInsets(0) else BottomSheetDefaults.windowInsets

        ModalBottomSheet(
            onDismissRequest = { scope.launch { bottomSheetState.hide() } },
            sheetState = bottomSheetState,
            windowInsets = windowInsets
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = paddingLarge, end = paddingLarge),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ExposedDropdownMenuBox(
                    expanded = expandedField,
                    onExpandedChange = { expandedField = !expandedField },
                ) {
                    ElevatedFilterChip(
                        modifier = Modifier
                            .menuAnchor(),
                        selected = false,
                        onClick = { },
                        leadingIcon = { Icon(Icons.Default.Sort, "Sort Field") },
                        label = { Text(selectedFieldText.value.title) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedField) },
                    )
                    ExposedDropdownMenu(
                        expanded = expandedField,
                        onDismissRequest = { expandedField = false },
                    ) {
                        optionsField.forEach { selectionOption ->
                            DropdownMenuItem(
                                text = { Text(selectionOption) },
                                onClick = {
                                    sortViewModel.setSortField(selectionOption)
                                    expandedField = false
                                },
                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                            )
                        }
                    }
                }
                ExposedDropdownMenuBox(
                    expanded = expandedDirection,
                    onExpandedChange = { expandedDirection = !expandedDirection },
                ) {
                    ElevatedFilterChip(
                        modifier = Modifier
                            .menuAnchor(),
                        selected = false,
                        onClick = { },
                        label = { Text(selectedDirectionText.value.title) },
                        leadingIcon = { Icon(Icons.Default.SwapVert, "Sort Direction") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDirection) },
                    )
                    ExposedDropdownMenu(
                        expanded = expandedDirection,
                        onDismissRequest = { expandedDirection = false },
                    ) {
                        optionsDirection.forEach { selectionOption ->
                            DropdownMenuItem(
                                text = { Text(selectionOption) },
                                onClick = {
                                    sortViewModel.setSortDirection(selectionOption)
                                    expandedDirection = false
                                },
                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                            )
                        }
                    }
                }
            }
        }
    }
}