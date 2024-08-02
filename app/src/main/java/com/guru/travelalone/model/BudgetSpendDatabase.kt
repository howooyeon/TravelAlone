package com.guru.travelalone

import java.util.Date

data class BudgetSpend(
    val id: Long = 0,
    val user_uid: String = "",
    val budget_id: Long = 0,
    val spend_amount: String = "",
    val spend_category: SpendCategory = SpendCategory.ETC,
    val spend_store: String = "",
    val spend_memo: String = "",
    val spend_date: Date
) {
    // 기본 생성자
    constructor() : this(0, "", 0, "", SpendCategory.ETC, "", "", Date())
}
enum class SpendCategory {
    SHOPPING, FOOD, ETC
}
