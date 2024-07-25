package com.guru.travelalone
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.RewriteQueriesToDropUnusedColumns

@Dao
@RewriteQueriesToDropUnusedColumns
interface MemberDao {
    @Insert
    suspend fun insert(member: Member)

    @Query("SELECT * FROM members")
    suspend fun getAllMembers(): List<Member>

    @Query("SELECT * FROM members WHERE id = :id")
    suspend fun getMemberById(id: Int): Member? // 반환 타입을 Member로 설정
}

