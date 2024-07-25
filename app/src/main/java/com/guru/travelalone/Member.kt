package com.guru.travelalone
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "members")
data class Member(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "nickname") val nickname: String,
    @ColumnInfo(name = "profileImageUrl") val profileImageUrl: String,
    @ColumnInfo(name = "introduce") val introduce: String
)
