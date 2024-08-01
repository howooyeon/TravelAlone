package com.guru.travelalone

import android.content.Intent
import android.os.Bundle
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

class Community_Activity : AppCompatActivity() {

    // Firebase Firestore instance
    private lateinit var firestore: FirebaseFirestore

    // 하단바 ----------
    lateinit var homeButton: ImageButton
    lateinit var locateButton: ImageButton
    lateinit var travbotButton: ImageButton
    lateinit var mypageButton: ImageButton
    lateinit var communityButton: ImageButton
    // 하단바 ----------

    // Spinner 추가
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

        // Initialize Firestore
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

        // Load posts from Firestore
        loadPosts()

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
            val intent = Intent(this, Community_Write_Activity::class.java)
            startActivity(intent)
        }
    }

    private fun loadPosts() {
        firestore.collection("posts")
            .get()
            .addOnSuccessListener { result ->
                val communityPostList = arrayListOf<CommunityPostListItem>()
                for (document: QueryDocumentSnapshot in result) {
                    val post = document.toObject(CommunityPostListItem::class.java)
                    post.id = document.id // Set the Firestore document ID
                    communityPostList.add(post)
                }
                val communitypostadapter = CommunityPostListAdapter(
                    this,
                    communityPostList
                ) { post ->
                    // Handle item click
                    val intent = Intent(this, Community_Detail_Activity::class.java).apply {
                        putExtra("POST_ID", post.id) // Pass the post ID
                    }
                    startActivity(intent)
                }
                communitypostlistview.adapter = communitypostadapter
            }
            .addOnFailureListener { exception ->
                // Handle the error
            }
    }
}
