package com.guru.travelalone.item

data class CommunityPostListItem(
    val title: String? = null,
    val content: String? = null,
    val isPublic: Boolean? = null, // 공개, 비공개 여부
    val imageUrl: String? = null, 
    val userId: String? = null,
    val userEmail: String? = null,
    val date: String? = null, // 여행 일정
    val location: String? = null, // 여행 지역
    val nickname: String? = null,        // 작성자 닉네임
    val profileImageUrl: String? = null,  // 작성자 프로필 이미지 URL 
    val createdAt: String? = null, // 작성 시간
    var id: String = "", // postId
)