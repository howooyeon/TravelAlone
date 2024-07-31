package com.guru.travelalone

data class Member(
    val id: Long = 0,
    val nickname: String, //카카오 로그인 경우 본명으로 기입
    val editnickname : String, //카카오 회원 후 지정한 닉네임은 여기에 저장 , 일반 로그인도 닉네임은 여기에 저장됨
    val profileImageUrl: String,
    val introduce: String,
    val login_id : String = ""
)
