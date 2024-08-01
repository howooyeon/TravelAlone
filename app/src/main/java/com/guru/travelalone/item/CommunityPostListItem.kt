package com.guru.travelalone.item

data class CommunityPostListItem(
    val title: String? = null,
    val content: String? = null,
    val isPrivate: Boolean? = null,
    val imageUrl: String? = null,
    val userId: String? = null,
    val userEmail: String? = null,
    val date: String? = null,
    val location: String? = null,
    val nickname: String? = null,        // 닉네임 필드
    val profileImageUrl: String? = null,  // 프로필 이미지 URL 필드
    val time: String? = null,
    var id: String = "", // Add an ID field
)