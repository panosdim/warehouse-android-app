package com.padi.warehouse.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Item(
    var id: String? = null,
    var name: String? = null,
    var exp_date: String? = null,
    var amount: String? = null,
    var box: String? = null
) : Parcelable