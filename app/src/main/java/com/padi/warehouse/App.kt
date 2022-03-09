package com.padi.warehouse

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.padi.warehouse.model.Item

enum class MSG(val message: String) {
    ITEM("com.panosdim.warehouse.item")
}

enum class RC(val code: Int) {
    PERMISSION_REQUEST(0)
}

const val BACKEND_URL = "https://warehouse.cc.nf/api/v1/"
const val TAG = "WARE_HOUSE"
const val CHANNEL_ID = "Warehouse-Channel"

var user = FirebaseAuth.getInstance().currentUser
val database = FirebaseDatabase.getInstance()
var items: MutableList<Item> = mutableListOf()
