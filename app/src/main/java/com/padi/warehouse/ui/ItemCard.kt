package com.padi.warehouse.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.padi.warehouse.models.Item
import com.padi.warehouse.paddingLarge
import com.padi.warehouse.paddingSmall
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
fun ItemCard(
    aggregationField: String?,
    items: List<Item>,
    selectedItem: (item: Item) -> Unit
) {
    val sortViewModel: SortViewModel = viewModel()
    val sortField =
        sortViewModel.sortField.collectAsStateWithLifecycle(initialValue = SortField.DATE)

    val today = LocalDate.now()
    val nextMonthLastDay = today.with(TemporalAdjusters.firstDayOfNextMonth())
        .with(TemporalAdjusters.lastDayOfMonth())

    val darkTheme: Boolean = isSystemInDarkTheme()
    var cardColors = CardDefaults.cardColors()

    if (sortField.value == SortField.DATE) {
        aggregationField?.toLocalDate()?.let {
            if (it.isBefore(today)) {
                cardColors =
                    CardDefaults.cardColors(containerColor = if (darkTheme) expiredDark else expiredLight)
            }

            if ((it.isBefore(nextMonthLastDay) || it.isEqual(nextMonthLastDay))
                && it.isAfter(today)
            ) {
                cardColors =
                    CardDefaults.cardColors(containerColor = if (darkTheme) expiresSoonDark else expiresSoonLight)
            }
        }
    }

    Card(
        modifier = Modifier
            .padding(paddingSmall)
            .fillMaxWidth()
            .wrapContentHeight(),
        shape = MaterialTheme.shapes.medium,
        colors = cardColors
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.padding(paddingLarge)) {
                Text(
                    text = if (sortField.value == SortField.DATE) {
                        aggregationField?.formatDate() ?: ""
                    } else {
                        aggregationField.toString()
                    },
                    textAlign = TextAlign.Center,
                    style = if (sortField.value == SortField.NAME) {
                        MaterialTheme.typography.titleSmall
                    } else {
                        MaterialTheme.typography.headlineSmall
                    },
                    modifier = Modifier
                        .padding(bottom = paddingLarge)
                        .fillMaxWidth(),
                    color = MaterialTheme.colorScheme.onSurface,
                )

                items.forEachIndexed { index, itemDetails ->
                    ItemDetails(itemDetails = itemDetails, selectedItem = selectedItem)
                    if (index < items.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier
                                .padding(paddingLarge),
                        )
                    }
                }
            }
        }
    }
}