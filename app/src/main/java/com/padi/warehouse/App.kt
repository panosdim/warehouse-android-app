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
    PERMISSION_REQUEST(2)
}

enum class DRAWABLE(val index: Int) {
    LEFT(0),
    TOP(1),
    RIGHT(2),
    BOTTOM(3)
}

// TODO: Add Offline capabilities for Firebase Database
var user = FirebaseAuth.getInstance().currentUser
val database = FirebaseDatabase.getInstance()
var items: MutableList<Item> = mutableListOf()