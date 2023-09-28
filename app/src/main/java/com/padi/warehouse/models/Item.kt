package com.padi.warehouse.models

import kotlinx.serialization.Serializable

@Serializable
data class Item(
    var id: String? = null,
    var name: String = "",
    var expirationDate: String = "",
    var amount: String = "",
    var box: String = ""
)