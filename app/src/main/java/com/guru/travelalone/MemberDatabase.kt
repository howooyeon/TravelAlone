package com.guru.travelalone
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import android.util.Log

@Database(entities = [Member::class], version = 1, exportSchema = false)
abstract class MemberDatabase : RoomDatabase() {
    abstract fun memberDao(): MemberDao

    companion object {
        @Volatile private var INSTANCE: MemberDatabase? = null

        fun getDatabase(context: Context): MemberDatabase {
            Log.d("MemberDatabase", "getDatabase called")
            return INSTANCE ?: synchronized(this) {
                try {
                    val instance = Room.databaseBuilder(
                        context.applicationContext,
                        MemberDatabase::class.java,
                        "member_database"
                    ).build()
                    INSTANCE = instance
                    Log.d("MemberDatabase", "Database initialized: $instance")
                    instance
                } catch (e: Exception) {
                    Log.e("MemberDatabase", "Error initializing database", e)
                    throw e // 예외를 다시 던져 호출부에서 처리할 수 있도록 합니다.
                }
            }
        }
    }
}