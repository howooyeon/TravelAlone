package com.guru.travelalone

data class Budget(
    val id: Long = 0,
    val user_uid: String, //연결된 유저 아이디
    val total_amount : String, //총 잔액
    val used_amount: String, //총 사용 금액
)
