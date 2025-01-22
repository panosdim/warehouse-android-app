package com.padi.warehouse.ui

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.padi.warehouse.LoginActivity
import com.padi.warehouse.R
import com.padi.warehouse.models.Item
import com.padi.warehouse.onComplete
import com.padi.warehouse.paddingLarge
import com.padi.warehouse.paddingSmall
import com.padi.warehouse.utils.unaccent
import com.padi.warehouse.viewmodels.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeaderBar(
    onSort: () -> Unit,
    listToSearch: List<Item>,
    searchQuery: String,
    onSearchChanged: (String) -> Unit,
) {
    val viewModel: MainViewModel = viewModel()
    val context = LocalContext.current

    var expanded by rememberSaveable { mutableStateOf(false) }
    var query by remember { mutableStateOf(searchQuery) }

    SearchBar(
        modifier = Modifier
            .fillMaxWidth()
            .padding(paddingLarge),
        inputField = {
            SearchBarDefaults.InputField(
                onSearch = { expanded = false },
                expanded = expanded,
                onExpandedChange = { expanded = it },
                placeholder = { Text(stringResource(R.string.hinted_product_search)) },
                leadingIcon = {
                    if (query.isNotEmpty()) {
                        Icon(
                            Icons.Default.Clear,
                            contentDescription = null,
                            modifier = Modifier.clickable {
                                onSearchChanged("")
                                query = ""
                                expanded = false
                            })
                    } else {
                        Icon(Icons.Default.Search, contentDescription = null)
                    }
                },
                trailingIcon = {
                    Row {
                        IconButton(onClick = { onSort() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Sort,
                                contentDescription = null
                            )
                        }
                        IconButton(onClick = {
                            viewModel.signOut()
                            context.unregisterReceiver(onComplete)
                            Firebase.auth.signOut()

                            val intent = Intent(context, LoginActivity::class.java)
                            intent.flags =
                                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            context.startActivity(intent)
                        }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Logout,
                                contentDescription = null
                            )
                        }
                    }

                },
                query = query,
                onQueryChange = {
                    query = it
                    onSearchChanged(it)
                },
            )
        },
        expanded = expanded,
        onExpandedChange = { expanded = it },
    ) {
        Column(Modifier.verticalScroll(rememberScrollState())) {
            if (query.isNotEmpty() && query.length > 2) {
                listToSearch.filter {
                    it.name.unaccent()
                        .contains(query.unaccent().trim(), true)
                }.forEach {
                    ListItem(
                        headlineContent = { Text(it.name) },
                        modifier = Modifier
                            .clickable {
                                query = it.name
                                onSearchChanged(it.name)
                                expanded = false
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