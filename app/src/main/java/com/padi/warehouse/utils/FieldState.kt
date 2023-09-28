package com.padi.warehouse.utils

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class FieldState<T>(
    fieldValue: T,
    val validate: (T) -> Pair<Boolean, String>
) {
    private var _value by mutableStateOf(fieldValue)

    var value: T
        get() {
            return _value
        }
        set(value) {
            _value = value
            val res = this.validate(value)
            hasError = res.first
            errorMessage = res.second
        }

    var hasError: Boolean by mutableStateOf(false)
        private set

    var errorMessage: String by mutableStateOf("")
        private set

    init {
        val res = this.validate(fieldValue)
        hasError = res.first
        errorMessage = res.second
    }
}