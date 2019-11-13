package com.padi.warehouse

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.padi.warehouse.item.Item

enum class MSG(val message: String) {
    ITEM("com.panosdim.warehouse.item")
}

enum class RC(val code: Int) {
    SIGN_IN(0),
    ITEM(1),
    PERMISSION_REQUEST(2)
}

enum class DRAWABLE(val index: Int) {
    RIGHT(2),
}

// TODO: Add Offline capabilities for Firebase Database
var user = FirebaseAuth.getInstance().currentUser
val database = FirebaseDatabase.getInstance()
var items: MutableList<Item> = mutableListOf()
const val CHANNEL_ID = "Warehouse-Channel"