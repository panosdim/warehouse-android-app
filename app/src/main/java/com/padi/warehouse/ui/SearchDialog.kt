package com.padi.warehouse.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.padi.warehouse.R
import com.padi.warehouse.models.Item
import com.padi.warehouse.paddingLarge
import com.padi.warehouse.paddingSmall
import com.padi.warehouse.utils.unaccent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchDialog(
    listToSearch: List<Item>,
    searchQuery: String,
    onSearchChanged: (searchText: String) -> Unit
) {
    val openSearchDialog = remember { mutableStateOf(false) }
    val source = remember { MutableInteractionSource() }
    if (source.collectIsPressedAsState().value) {
        openSearchDialog.value = true
    }
    val focusRequester = remember { FocusRequester() }
    var active by rememberSaveable { mutableStateOf(true) }
    var searchText by rememberSaveable { mutableStateOf(searchQuery) }

    LaunchedEffect(openSearchDialog.value) {
        if (openSearchDialog.value) {
            focusRequester.requestFocus()
        }
    }

    if (openSearchDialog.value) {
        Dialog(
            onDismissRequest = {
                openSearchDialog.value = false
            },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Column {
                SearchBar(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(paddingLarge)
                        .focusRequester(focusRequester),
                    query = searchText,
                    onQueryChange = {
                        searchText = it
                        onSearchChanged(it)
                    },
                    onSearch = {
                        active = false
                        openSearchDialog.value = false
                    },
                    active = true,
                    onActiveChange = {
                        if (!it) {
                            openSearchDialog.value = false
                        }
                    },
                    placeholder = { Text(stringResource(id = R.string.search)) },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null
                        )
                    },
                    trailingIcon = {
                        if (searchText.isNotEmpty()) {
                            Icon(
                                Icons.Default.Clear,
                                contentDescription = null,
                                modifier = Modifier.clickable {
                                    onSearchChanged("")
                                    searchText = ""
                                    active = false
                                    openSearchDialog.value = false
                                })
                        }
                    }
                ) {
                    if (searchText.isNotEmpty() && searchText.length > 2) {
                        listToSearch.filter {
                            it.name.unaccent()
                                .contains(searchText.unaccent().trim(), true)
                        }.forEach {
                            ListItem(
                                headlineContent = { Text(it.name) },
                                modifier = Modifier
                                    .clickable {
                                        searchText = it.name
                                        active = false
                                    }
                                    .fillMaxWidth()
                                    .padding(
                                        horizontal = paddingLarge,
                                        vertical = paddingSmall
                                    )
                            )
                        }

                    }
                }
            }
        }
    }

    TextButton(
        onClick = { openSearchDialog.value = true },
        colors = ButtonDefaults.textButtonColors(
            contentColor = if (searchText.isNotEmpty()) {
                Color.Green
            } else {
                MaterialTheme.colorScheme.primary
            }
        )
    ) {
        Icon(
            Icons.Default.Search,
            contentDescription = null,
            modifier = Modifier.size(ButtonDefaults.IconSize)
        )
        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
        Text(
            stringResource(id = R.string.search)
        )
    }
}