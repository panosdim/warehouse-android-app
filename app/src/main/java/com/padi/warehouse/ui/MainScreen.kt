package com.padi.warehouse.ui

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.padi.warehouse.R
import com.padi.warehouse.models.Item
import com.padi.warehouse.models.Response
import com.padi.warehouse.paddingLarge
import com.padi.warehouse.utils.SortDirection
import com.padi.warehouse.utils.SortField
import com.padi.warehouse.utils.sort
import com.padi.warehouse.utils.unaccent
import com.padi.warehouse.viewmodels.MainViewModel
import com.padi.warehouse.viewmodels.SortViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val viewModel: MainViewModel = viewModel()
    val sortViewModel: SortViewModel = viewModel()
    val listState = rememberLazyListState()
    val expandedFab by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex == 0
        }
    }
    val skipPartiallyExpanded by remember { mutableStateOf(true) }
    val sortSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = skipPartiallyExpanded
    )

    var openItemDialog by remember { mutableStateOf(false) }

    val sortField =
        sortViewModel.sortField.collectAsStateWithLifecycle(initialValue = SortField.DATE)

    val sortDirection =
        sortViewModel.sortDirection.collectAsStateWithLifecycle(initialValue = SortDirection.ASC)

    var isLoading by remember {
        mutableStateOf(false)
    }

    var searchText by rememberSaveable { mutableStateOf("") }

    var items by remember { mutableStateOf(emptyList<Item>()) }

    var data by remember { mutableStateOf(emptyMap<String?, List<Item>>()) }

    val itemsResponse =
        viewModel.items.collectAsStateWithLifecycle(initialValue = Response.Loading)

    var item: Item? by remember { mutableStateOf(null) }

    when (itemsResponse.value) {
        is Response.Success -> {
            isLoading = false

            data = emptyMap()
            items =
                (itemsResponse.value as Response.Success<List<Item>>).data
        }

        is Response.Error -> {
            Toast.makeText(
                context,
                (itemsResponse.value as Response.Error).errorMessage,
                Toast.LENGTH_SHORT
            )
                .show()

            isLoading = false
        }

        is Response.Loading -> {
            isLoading = true
        }
    }

    if (isLoading) {
        ProgressBar()
    } else {
        Scaffold(
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = {
                        item = null
                        openItemDialog = true
                    },
                    expanded = expandedFab,
                    icon = {
                        Icon(
                            Icons.Filled.Add,
                            stringResource(id = R.string.add_item)
                        )
                    },
                    text = { Text(text = stringResource(id = R.string.add_item)) },
                )
            }
        ) { contentPadding ->
            Column {
                HeaderBar(
                    onSort = { scope.launch { sortSheetState.show() } },
                    listToSearch = items,
                    searchQuery = searchText,
                    onSearchChanged = { searchText = it },
                )

                // Show items
                LazyColumn(
                    Modifier
                        .fillMaxWidth()
                        .padding(paddingLarge),
                    contentPadding = contentPadding,
                    state = listState
                ) {
                    // Sort
                    var itemsList = sort(
                        items,
                        sortField.value,
                        sortDirection.value
                    )

                    // Search
                    if (searchText.isNotBlank()) {
                        itemsList = itemsList.filter {
                            it.name.unaccent().contains(
                                searchText.unaccent().trim(),
                                ignoreCase = true
                            )
                        }.toMutableList()
                    }

                    // Aggregate
                    data = emptyMap()
                    data = itemsList.groupBy {
                        when (sortField.value) {
                            SortField.DATE -> it.expirationDate
                            SortField.BOX -> it.box
                            SortField.NAME -> it.name
                        }
                    }

                    if (data.isNotEmpty()) {
                        data.iterator().forEachRemaining {
                            item {
                                ItemCard(it.key, it.value) {
                                    item = it
                                    openItemDialog = true
                                }
                            }
                        }
                    } else {
                        item {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = stringResource(id = R.string.no_items),
                                    modifier = Modifier

                                )
                                Text(
                                    text = stringResource(id = R.string.no_items)
                                )
                            }
                        }
                    }
                }
            }
        }

        SortSheet(bottomSheetState = sortSheetState)

        ItemDialog(
            item,
            openItemDialog,
        ) { openItemDialog = false }
    }
}