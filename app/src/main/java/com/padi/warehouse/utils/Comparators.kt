package com.padi.warehouse.utils

import com.padi.warehouse.model.Item

val expDateAsc = Comparator<Item> { p1, p2 ->
    when {
        p1.exp_date.isNullOrEmpty() -> 1
        p2.exp_date.isNullOrEmpty() -> -1
        p1.exp_date.toString() > p2.exp_date.toString() -> 1
        p1.exp_date == p2.exp_date -> 0
        else -> -1
    }
}

val expDateDesc = Comparator<Item> { p1, p2 ->
    when {
        p1.exp_date.isNullOrEmpty() -> -1
        p2.exp_date.isNullOrEmpty() -> 1
        p1.exp_date.toString() > p2.exp_date.toString() -> -1
        p1.exp_date == p2.exp_date -> 0
        else -> -1
    }
}