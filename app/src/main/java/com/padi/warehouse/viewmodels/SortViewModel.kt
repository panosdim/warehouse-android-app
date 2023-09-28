package com.padi.warehouse.viewmodels

import androidx.lifecycle.ViewModel
import com.padi.warehouse.utils.SortDirection
import com.padi.warehouse.utils.SortField
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class SortViewModel : ViewModel() {
    private val _sortField = MutableStateFlow(SortField.DATE)
    val sortField: Flow<SortField> = _sortField
    private val _sortDirection = MutableStateFlow(SortDirection.ASC)
    var sortDirection: Flow<SortDirection> = _sortDirection

    fun setSortField(sortField: String) {
        SortField.values().find { it.title == sortField }?.let {
            _sortField.value = it
        }
    }

    fun setSortDirection(sortDirection: String) {
        SortDirection.values().find { it.title == sortDirection }?.let {
            _sortDirection.value = it
        }
    }
}