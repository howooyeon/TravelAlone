package com.guru.travelalone
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.RewriteQueriesToDropUnusedColumns

@Dao
@RewriteQueriesToDropUnusedColumns
interface MemberDao {
    @Insert
    fun insert(member: Member)

    @Query("SELECT * FROM members")
    fun getAllMembers(): List<Member> // 반환 타입을 List<Member>로 설정

    @Query("SELECT * FROM members WHERE id = :id")
    fun getMemberById(id: Int): Member? // 반환 타입을 Member로 설정
}
