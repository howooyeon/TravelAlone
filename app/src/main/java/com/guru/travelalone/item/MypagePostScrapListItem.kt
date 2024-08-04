package com.guru.travelalone.item

data class MypagePostScrapListItem(
    var postId: String, // 게시물 ID 추가
    val imgUrl: String,  // 이미지 URL
    val title: String,
    val sub: String
)
