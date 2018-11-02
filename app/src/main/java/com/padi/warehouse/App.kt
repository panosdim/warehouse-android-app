package com.padi.warehouse

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.padi.warehouse.item.Item

enum class MSG(val message: String) {
    LOG_OUT("com.panosdim.warehouse.logout"),
    ITEM("com.panosdim.warehouse.item")
}

enum class RC(val code: Int) {
    SIGN_IN(0),
    ITEM(1),
    BARCODE_SCAN(2),
    FILTER_INCOME(3),
    FILTER_EXPENSE(4),
    FILTER_ADD_EXPENSE(5),
    FILTER_DELETE_EXPENSE(6),
    FILTER_ADD_INCOME(7),
    FILTER_DELETE_INCOME(8)
}

enum class DRAWABLE(val index: Int) {
    LEFT(0),
    TOP(1),
    RIGHT(2),
    BOTTOM(3)
}

var user = FirebaseAuth.getInstance().currentUser
val database = FirebaseDatabase.getInstance()
var items: MutableList<Item> = mutableListOf()