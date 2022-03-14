package com.padi.warehouse.utils

import com.padi.warehouse.adapters.ItemAdapter
import com.padi.warehouse.items

enum class SortField {
    NAME, EXP_DATE, BOX
}

enum class SortDirection {
    ASC, DESC
}

var sortField: SortField = SortField.EXP_DATE
var sortDirection: SortDirection = SortDirection.ASC

fun sortItems(adapter: ItemAdapter) {
    val sortedItems = items.map { it.copy() }.toMutableList()
    when (sortField) {
        SortField.NAME -> {
            when (sortDirection) {
                SortDirection.ASC -> sortedItems.sortBy { it.name }
                SortDirection.DESC -> sortedItems.sortByDescending { it.name }
            }
        }

        SortField.EXP_DATE -> {
            when (sortDirection) {
                SortDirection.ASC -> sortedItems.sortWith(expDateAsc)
                SortDirection.DESC -> sortedItems.sortWith(expDateDesc)
            }
        }

        SortField.BOX -> {
            when (sortDirection) {
                SortDirection.ASC -> sortedItems.sortBy { it.box }
                SortDirection.DESC -> sortedItems.sortByDescending { it.box }
            }
        }
    }
    adapter.updateItemsList(sortedItems)
}