package com.guru.travelalone

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.widget.Button
import android.widget.ListView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.Locale

class ListViewTest_Activity : AppCompatActivity(){

    // 리스트뷰 어뎁터 설정
    lateinit var postlistview : ListView
    lateinit var dbMangager: DBManager_1
    lateinit var sqlitedb: SQLiteDatabase
    private val dateFormat = SimpleDateFormat("yyyy.MM.dd", Locale.KOREAN)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.listview_test)


        // 리스트뷰 어뎁터 설정
        postlistview = findViewById(R.id.postlistview)
        var postList = arrayListOf<MypagePostListItem>(
            MypagePostListItem(ContextCompat.getDrawable(this, R.drawable.img_gapyeong_yangpyeong)!!, "가평/양평", "테스트 날짜"),
            MypagePostListItem(ContextCompat.getDrawable(this, R.drawable.img_gangneung_sokcho)!!, "강릉/속초", "테스트 날짜"),
            MypagePostListItem(ContextCompat.getDrawable(this, R.drawable.img_gyeongju)!!, "경주", "테스트 날짜"),
            MypagePostListItem(ContextCompat.getDrawable(this, R.drawable.img_busan)!!, "부산", "테스트 날짜"),
            MypagePostListItem(ContextCompat.getDrawable(this, R.drawable.img_yeosu)!!, "여수", "테스트 날짜"),
            MypagePostListItem(ContextCompat.getDrawable(this, R.drawable.img_incheon)!!, "인천", "테스트 날짜"),
            MypagePostListItem(ContextCompat.getDrawable(this, R.drawable.img_jeonju)!!, "전주", "테스트 날짜"),
            MypagePostListItem(ContextCompat.getDrawable(this, R.drawable.img_jeju)!!, "제주", "테스트 날짜"),
            MypagePostListItem(ContextCompat.getDrawable(this, R.drawable.img_chuncheon_hongcheon)!!, "춘천/홍천", "테스트 날짜"),
            MypagePostListItem(ContextCompat.getDrawable(this, R.drawable.img_taean)!!, "태안", "테스트 날짜"),
            MypagePostListItem(ContextCompat.getDrawable(this, R.drawable.img_tongyeong_geoje_namhae)!!, "통영/거제/남해", "테스트 날짜"),
            MypagePostListItem(ContextCompat.getDrawable(this, R.drawable.img_pohang_andong)!!, "포항/안동", "테스트 날짜"),
        )
        val adapter = MypagePostListAdapter(this, postList)
        postlistview.adapter = adapter

        dbMangager = DBManager_1(this, "tripdate", null, 1)
        sqlitedb = dbMangager.readableDatabase
        var cursor: Cursor
        cursor = sqlitedb.rawQuery("SELECT * FROM tripdate;", null)
        while(cursor.moveToNext())
        {
            val str_title = cursor.getString(0)
            val str_location = cursor.getString(1)
            val long_start_date = cursor.getLong(2)
            val long_end_date: Long? = if (cursor.isNull(3)) null else cursor.getLong(3)
            if(long_end_date?.toInt() != 0)
            {
                postList.add(MypagePostListItem(ContextCompat.getDrawable(this, R.drawable.rain_1)!!, str_title, "${dateFormat.format(long_start_date)}" +" ~ "+ "${dateFormat.format(long_end_date)}"))
            }
            else
            {
                postList.add(MypagePostListItem(ContextCompat.getDrawable(this, R.drawable.rain_1)!!, str_title, "${dateFormat.format(long_start_date)}"))
            }
        }
        cursor.close()
        sqlitedb.close()
        dbMangager.close()
    }
}