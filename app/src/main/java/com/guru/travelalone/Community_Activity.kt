package com.guru.travelalone

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Spinner
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.core.content.ContextCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.guru.travelalone.adapter.CommunityPostListAdapter
import com.guru.travelalone.item.CommunityPostListItem
import com.guru.travelalone.item.MypageTripListItem

class Community_Activity : AppCompatActivity() {

    // 하단바 ----------
    lateinit var homeButton: ImageButton
    lateinit var locateButton: ImageButton
    lateinit var travbotButton: ImageButton
    lateinit var mypageButton: ImageButton
    lateinit var communityButton: ImageButton
    // 하단바 ----------

    // Spinner 추가
    lateinit var regionSpinner: Spinner

    lateinit var communitypostlistview : ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_community)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 하단바 ----------
        homeButton = findViewById(R.id.homeButton)
        locateButton = findViewById(R.id.locateButton)
        travbotButton = findViewById(R.id.travbotButton)
        mypageButton = findViewById(R.id.mypageButton)
        communityButton = findViewById(R.id.commuButton)
        // Spinner 초기화
        regionSpinner = findViewById(R.id.location_spinner)

        // Spinner에 어댑터 설정
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.region_list,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        regionSpinner.adapter = adapter

        communitypostlistview = findViewById(R.id.communitypostlistview)
        var communitypostList = arrayListOf<CommunityPostListItem>(
            CommunityPostListItem(ContextCompat.getDrawable(this, R.drawable.normal_1)!!, ContextCompat.getDrawable(this, R.drawable.samplepro)!!,"이름","제목", "본문\n본문", "2024.07.02 ~ 2024.07.10"),
            CommunityPostListItem(ContextCompat.getDrawable(this, R.drawable.normal_1)!!, ContextCompat.getDrawable(this, R.drawable.samplepro)!!,"이름","제목", "본문", "2024.07.02 ~ 2024.07.10")
        )
        communitypostList.add(CommunityPostListItem(ContextCompat.getDrawable(this, R.drawable.normal_1)!!, ContextCompat.getDrawable(this, R.drawable.samplepro)!!,"이름","제목", "본문", "날짜"))

        val communitypostadapter = CommunityPostListAdapter(this, communitypostList)
        communitypostlistview.adapter = communitypostadapter

        locateButton.setOnClickListener {
            val intent = Intent(
                this@Community_Activity,
                Locate_Activity::class.java
            )
            startActivity(intent)
        }

        travbotButton.setOnClickListener {
            val intent = Intent(
                this@Community_Activity,
                Travbot_activity::class.java
            )
            startActivity(intent)
        }

        homeButton.setOnClickListener {
            val intent = Intent(
                this@Community_Activity,
                Home_Activity::class.java
            )
            startActivity(intent)
        }

        communityButton.setOnClickListener {
            val intent = Intent(
                this@Community_Activity,
                Community_Activity::class.java
            )
            startActivity(intent)
        }

        mypageButton.setOnClickListener {
            val intent = Intent(
                this@Community_Activity,
                Mypage_Activity::class.java
            )
            startActivity(intent)
        }

        val fab: FloatingActionButton = findViewById(R.id.fab)
        fab.setOnClickListener {
            val intent = Intent(this, Community_Select_Activity::class.java)
            startActivity(intent)
        }
    }
}