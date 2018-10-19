package com.padi.warehouse

enum class MSG(val message: String) {
    LOG_OUT("com.panosdim.warehouse.logout")
}

enum class RC(val code: Int) {
    SIGN_IN(0),
    ADD_FOOD(1),
    CATEGORY(2),
    FILTER_INCOME(3),
    FILTER_EXPENSE(4),
    FILTER_ADD_EXPENSE(5),
    FILTER_DELETE_EXPENSE(6),
    FILTER_ADD_INCOME(7),
    FILTER_DELETE_INCOME(8)
}