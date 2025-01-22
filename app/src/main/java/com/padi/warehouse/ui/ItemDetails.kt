package com.padi.warehouse.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.padi.warehouse.R
import com.padi.warehouse.models.Item
import com.padi.warehouse.ui.theme.expiredDark
import com.padi.warehouse.ui.theme.expiredLight
import com.padi.warehouse.ui.theme.expiresSoonDark
import com.padi.warehouse.ui.theme.expiresSoonLight
import com.padi.warehouse.utils.SortField
import com.padi.warehouse.utils.formatDate
import com.padi.warehouse.utils.toLocalDate
import com.padi.warehouse.viewmodels.SortViewModel
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

@Composable
fun ItemDetails(itemDetails: Item, selectedItem: (item: Item) -> Unit) {
    val sortViewModel: SortViewModel = viewModel()
    val sortField =
        sortViewModel.sortField.collectAsStateWithLifecycle(initialValue = SortField.DATE)

    val today = LocalDate.now()
    val nextMonthLastDay = today.with(TemporalAdjusters.firstDayOfNextMonth())
        .with(TemporalAdjusters.lastDayOfMonth())

    val darkTheme: Boolean = isSystemInDarkTheme()
    var listItemColors = ListItemDefaults.colors()
    if (sortField.value != SortField.DATE) {
        itemDetails.expirationDate.toLocalDate()?.let {
            if (it.isBefore(today)) {
                listItemColors =
                    ListItemDefaults.colors(
                        headlineColor = if (darkTheme) expiredDark else expiredLight,
                        supportingColor = if (darkTheme) expiredDark else expiredLight,
                        trailingIconColor = if (darkTheme) expiredDark else expiredLight,
                    )
            }

            if ((it.isBefore(nextMonthLastDay) || it.isEqual(nextMonthLastDay))
                && it.isAfter(today)
            ) {
                listItemColors =
                    ListItemDefaults.colors(
                        headlineColor = if (darkTheme) expiresSoonDark else expiresSoonLight,
                        supportingColor = if (darkTheme) expiresSoonDark else expiresSoonLight,
                        trailingIconColor = if (darkTheme) expiresSoonDark else expiresSoonLight,
                    )
            }
        }
    }

    when (sortField.value) {
        SortField.DATE -> {
            ListItem(
                colors = listItemColors,
                headlineContent = {
                    Text(
                        text = itemDetails.name,
                    )
                },
                supportingContent = {
                    Text(
                        text = "Amount: ${itemDetails.amount}",
                    )
                },
                trailingContent = {
                    TextWithIcon(
                        text = {
                            Text(
                                text = itemDetails.box,
                                style = MaterialTheme.typography.headlineSmall
                            )
                        },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(id = R.drawable.box),
                                contentDescription = null,
                            )
                        }
                    )
                },
                modifier = Modifier.clickable {
                    selectedItem(itemDetails)
                }
            )
        }

        SortField.BOX -> {
            ListItem(
                colors = listItemColors,
                headlineContent = {
                    Text(
                        text = itemDetails.name,
                    )
                },
                supportingContent = {
                    Text(
                        text = itemDetails.expirationDate.formatDate() ?: "",
                    )
                },
                trailingContent = {
                    Text(
                        text = "x${itemDetails.amount}",
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                modifier = Modifier.clickable {
                    selectedItem(itemDetails)
                }
            )
        }

        SortField.NAME -> {
            ListItem(
                colors = listItemColors,
                headlineContent = {
                    Text(
                        text = itemDetails.expirationDate.formatDate() ?: "",
                    )
                },
                supportingContent = {
                    Text(
                        text = "Amount: ${itemDetails.amount}",
                    )
                },
                trailingContent = {
                    TextWithIcon(
                        text = {
                            Text(
                                text = itemDetails.box,
                                style = MaterialTheme.typography.headlineSmall
                            )
                        },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(id = R.drawable.box),
                                contentDescription = null,
                            )
                        }
                    )
                },
                modifier = Modifier.clickable {
                    selectedItem(itemDetails)
                }
            )
        }
    }

    HorizontalDivider()
}