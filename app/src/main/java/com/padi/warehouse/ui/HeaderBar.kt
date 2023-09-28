package com.padi.warehouse.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.padi.warehouse.R
import com.padi.warehouse.models.Item
import com.padi.warehouse.paddingLarge
import com.padi.warehouse.viewmodels.MainViewModel

@Composable
fun HeaderBar(
    onSort: () -> Unit,
    listToSearch: List<Item>,
    searchQuery: String,
    onSearchChanged: (String) -> Unit,
) {
    val viewModel: MainViewModel = viewModel()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = paddingLarge, end = paddingLarge),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        TextButton(onClick = { onSort() }) {
            Icon(
                Icons.Default.Sort,
                contentDescription = null,
                modifier = Modifier.size(ButtonDefaults.IconSize)
            )
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text(
                stringResource(id = R.string.sort)
            )
        }

        SearchDialog(
            listToSearch = listToSearch,
            searchQuery = searchQuery,
            onSearchChanged = onSearchChanged
        )

        TextButton(
            onClick = { viewModel.signOut() },
        ) {
            Icon(
                Icons.Default.Logout,
                contentDescription = null,
                modifier = Modifier.size(ButtonDefaults.IconSize)
            )
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text(
                stringResource(id = R.string.logout)
            )
        }
    }
}