package com.padi.warehouse.utils

import android.annotation.SuppressLint
import androidx.recyclerview.widget.RecyclerView
import com.padi.warehouse.items

enum class SortField {
    NAME, EXP_DATE, BOX
}

enum class SortDirection {
    ASC, DESC
}

var sortField: SortField = SortField.EXP_DATE
var sortDirection: SortDirection = SortDirection.ASC

@SuppressLint("NotifyDataSetChanged")
fun sortItems(itemsAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>?) {
    when (sortField) {
        SortField.NAME -> {
            when (sortDirection) {
                SortDirection.ASC -> items.sortBy { it.name }
                SortDirection.DESC -> items.sortByDescending { it.name }
            }
        }

        SortField.EXP_DATE -> {
            when (sortDirection) {
                SortDirection.ASC -> items.sortWith(expDateAsc)
                SortDirection.DESC -> items.sortWith(expDateDesc)
            }
        }

        SortField.BOX -> {
            when (sortDirection) {
                SortDirection.ASC -> items.sortBy { it.box }
                SortDirection.DESC -> items.sortByDescending { it.box }
            }
        }
    }
    itemsAdapter?.notifyDataSetChanged()
}