package com.padi.warehouse.utils

import com.padi.warehouse.models.Item


enum class SortField(val title: String) {
    DATE("Date"), BOX("Box"), NAME("Name")
}

enum class SortDirection(val title: String) {
    ASC("Ascending"), DESC("Descending")
}

fun sort(
    items: List<Item>,
    sortField: SortField,
    sortDirection: SortDirection
): List<Item> {
    val data = items.toMutableList()
    when (sortField) {
        SortField.DATE -> when (sortDirection) {
            SortDirection.ASC -> data.sortBy { it.expirationDate }
            SortDirection.DESC -> data.sortByDescending { it.expirationDate }
        }

        SortField.BOX -> when (sortDirection) {
            SortDirection.ASC -> data.sortBy { it.box.toInt() }
            SortDirection.DESC -> data.sortByDescending { it.box.toInt() }
        }

        SortField.NAME -> when (sortDirection) {
            SortDirection.ASC -> data.sortBy { it.name }
            SortDirection.DESC -> data.sortByDescending { it.name }
        }
    }

    return data
}