package com.padi.warehouse

import com.google.firebase.auth.FirebaseAuth

enum class MSG(val message: String) {
    LOG_OUT("com.panosdim.warehouse.logout"),
    FOOD_ITEM("com.panosdim.warehouse.food_item")
}

enum class RC(val code: Int) {
    SIGN_IN(0),
    ADD_FOOD(1),
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