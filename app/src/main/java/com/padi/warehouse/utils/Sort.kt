package com.padi.warehouse.utils

enum class SortField {
    NAME, EXP_DATE, BOX
}

enum class SortDirection {
    ASC, DESC
}

var sortField: SortField = SortField.EXP_DATE
var sortDirection: SortDirection = SortDirection.ASC