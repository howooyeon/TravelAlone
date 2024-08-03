package com.guru.travelalone

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.ListView
import android.widget.Spinner
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.guru.travelalone.adapter.CommunityPostListAdapter
import com.guru.travelalone.item.CommunityPostListItem
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale

class Community_Activity : AppCompatActivity() {

    // Firebase Firestore 인스턴스
    private lateinit var firestore: FirebaseFirestore

    // 하단바 ----------
    lateinit var homeButton: ImageButton
    lateinit var locateButton: ImageButton
    lateinit var travbotButton: ImageButton
    lateinit var mypageButton: ImageButton
    lateinit var communityButton: ImageButton

    // Spinner
    lateinit var regionSpinner: Spinner

    lateinit var communitypostlistview: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_community)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Firestore 초기화
        firestore = FirebaseFirestore.getInstance()

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

        // 지역에 따라 Firestore 에서 post 가져오기
        regionSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedRegion = parent.getItemAtPosition(position).toString()
                loadPosts(selectedRegion)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing
            }
        }

        locateButton.setOnClickListener {
            val intent = Intent(this, Locate_Activity::class.java)
            startActivity(intent)
        }

        travbotButton.setOnClickListener {
            val intent = Intent(this, Travbot_activity::class.java)
            startActivity(intent)
        }

        homeButton.setOnClickListener {
            val intent = Intent(this, Home_Activity::class.java)
            startActivity(intent)
        }

        communityButton.setOnClickListener {
            val intent = Intent(this, Community_Activity::class.java)
            startActivity(intent)
        }

        mypageButton.setOnClickListener {
            val intent = Intent(this, Mypage_Activity::class.java)
            startActivity(intent)
        }

        val fab: FloatingActionButton = findViewById(R.id.fab)
        fab.setOnClickListener {
            val intent = Intent(this, Community_Select_Activity::class.java)
            startActivity(intent)
        }
    }

    private fun loadPosts(selectedRegion: String) {
        // 전체 게시글 가져오기
        val query = if (selectedRegion == "전체") {
            firestore.collection("posts")
                .whereEqualTo("isPublic", true)
        } else { // 선택 지역에 따라 공개된 게시글만 가져오기
            firestore.collection("posts")
                .whereEqualTo("isPublic", true)
                .whereEqualTo("location", selectedRegion)
        }

        query.get()
            .addOnSuccessListener { result ->
                val communityPostList = arrayListOf<CommunityPostListItem>()
                for (document: QueryDocumentSnapshot in result) {
                    val post = document.toObject(CommunityPostListItem::class.java)
                    post.id = document.id
                    communityPostList.add(post)
                }
                // 작성 시간(dd.MM.yy HH:mm)에 따라 최신순으로 정렬
                communityPostList.sortWith(compareByDescending {
                    it.createdAt?.let { it1 -> parseDateString(it1) }
                })
                val communitypostadapter = CommunityPostListAdapter(
                    this,
                    communityPostList
                ) { post ->
                    // post 클릭시
                    val intent = Intent(this, Community_Detail_Activity::class.java).apply {
                        putExtra("POST_ID", post.id) // Pass the post ID
                    }
                    startActivity(intent)
                }
                communitypostlistview.adapter = communitypostadapter
            }
            .addOnFailureListener { exception ->
            }
    }

    // date 파싱
    private fun parseDateString(dateString: String): Long {
        val dateFormat = SimpleDateFormat("dd.MM.yy HH:mm", Locale.getDefault())
        return try {
            val date = dateFormat.parse(dateString)
            date?.time ?: 0L
        } catch (e: ParseException) {
            0L
        }
    }

}
