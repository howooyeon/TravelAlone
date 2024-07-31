package com.guru.travelalone

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.widget.ListView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.guru.travelalone.adapter.CommunityPostListAdapter
import com.guru.travelalone.adapter.MypagePostListAdapter
import com.guru.travelalone.adapter.MypageTripListAdapter
import com.guru.travelalone.item.CommunityPostListItem
import com.guru.travelalone.item.MypagePostListItem
import com.guru.travelalone.item.MypageTripListItem
import java.text.SimpleDateFormat
import java.util.Locale

class ListViewTest_Activity : AppCompatActivity(){

    // 리스트뷰 어뎁터 설정
    lateinit var mypagepostlistview : ListView
    lateinit var mypagetriplistview : ListView
    lateinit var communitypostlistview : ListView
    lateinit var dbMangager: DBManager_1
    lateinit var sqlitedb: SQLiteDatabase
    private val dateFormat = SimpleDateFormat("yyyy.MM.dd", Locale.KOREAN)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.listview_test)


        // mypagepostList
        mypagepostlistview = findViewById(R.id.mypagepostlistview)
        var mypagepostList = arrayListOf<MypagePostListItem>(
            MypagePostListItem(ContextCompat.getDrawable(this, R.drawable.img_gangneung_sokcho)!!, "제목", "본문")
        )
        val mypagepostadapter = MypagePostListAdapter(this, mypagepostList)
        mypagepostlistview.adapter = mypagepostadapter


        // mypagetripList
        mypagetriplistview = findViewById(R.id.mypagetriplistview)
        var postList = arrayListOf<MypageTripListItem>(
        )
        val mypagetripadapter = MypageTripListAdapter(this, postList)
        mypagetriplistview.adapter = mypagetripadapter

        dbMangager = DBManager_1(this, "tripdate", null, 1)
        sqlitedb = dbMangager.readableDatabase
        var cursor: Cursor
        cursor = sqlitedb.rawQuery("SELECT * FROM tripdate;", null)
        while(cursor.moveToNext())
        {
            val str_title = cursor.getString(0)
            val str_location = cursor.getString(1)
            var str_date = ""
            val long_start_date = cursor.getLong(2)
            val long_end_date: Long? = if (cursor.isNull(3)) null else cursor.getLong(3)
            if(long_end_date?.toInt() != 0)
            {
                str_date = "${dateFormat.format(long_start_date)}" +" ~ "+ "${dateFormat.format(long_end_date)}"
            }
            else
            {
                str_date = "${dateFormat.format(long_start_date)}"
            }
            if(str_location == "가평/양평")
            {
                postList.add(MypageTripListItem(ContextCompat.getDrawable(this, R.drawable.img_gapyeong_yangpyeong)!!, str_title, str_date))
            }
            else if(str_location == "강릉/속초")
            {
                postList.add(MypageTripListItem(ContextCompat.getDrawable(this, R.drawable.img_gangneung_sokcho)!!, str_title, str_date))
            }
            else if(str_location == "경주")
            {
                postList.add(MypageTripListItem(ContextCompat.getDrawable(this, R.drawable.img_gyeongju)!!, str_title, str_date))
            }
            else if(str_location == "부산")
            {
                postList.add(MypageTripListItem(ContextCompat.getDrawable(this, R.drawable.img_busan)!!, str_title, str_date))
            }
            else if(str_location == "여수")
            {
                postList.add(MypageTripListItem(ContextCompat.getDrawable(this, R.drawable.img_yeosu)!!, str_title, str_date))
            }
            else if(str_location == "인천")
            {
                postList.add(MypageTripListItem(ContextCompat.getDrawable(this, R.drawable.img_incheon)!!, str_title, str_date))
            }
            else if(str_location == "전주")
            {
                postList.add(MypageTripListItem(ContextCompat.getDrawable(this, R.drawable.img_jeonju)!!, str_title, str_date))
            }
            else if(str_location == "제주")
            {
                postList.add(MypageTripListItem(ContextCompat.getDrawable(this, R.drawable.img_jeju)!!, str_title, str_date))
            }
            else if(str_location == "춘천/홍천")
            {
                postList.add(MypageTripListItem(ContextCompat.getDrawable(this, R.drawable.img_chuncheon_hongcheon)!!, str_title, str_date))
            }
            else if(str_location == "태안")
            {
                postList.add(MypageTripListItem(ContextCompat.getDrawable(this, R.drawable.img_taean)!!, str_title, str_date))
            }
            else if(str_location == "통영/거제/남해")
            {
                postList.add(MypageTripListItem(ContextCompat.getDrawable(this, R.drawable.img_tongyeong_geoje_namhae)!!, str_title, str_date))
            }
            else if(str_location == "포항/안동")
            {
                postList.add(MypageTripListItem(ContextCompat.getDrawable(this, R.drawable.img_pohang_andong)!!, str_title, str_date))
            }
        }
        cursor.close()
        sqlitedb.close()
        dbMangager.close()

        // communitypostlist
        communitypostlistview = findViewById(R.id.communitypostlistview)
        var communitypostList = arrayListOf<CommunityPostListItem>(
            CommunityPostListItem(ContextCompat.getDrawable(this, R.drawable.normal_1)!!, ContextCompat.getDrawable(this, R.drawable.samplepro)!!,"이름","제목", "본문", "날짜")
        )
        val communitypostadapter = CommunityPostListAdapter(this, communitypostList)
        communitypostlistview.adapter = communitypostadapter
    }
}