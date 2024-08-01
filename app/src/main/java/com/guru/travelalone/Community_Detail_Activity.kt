package com.guru.travelalone

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity


class Community_Detail_Activity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_community_detail)

        // Retrieve the post ID from the intent
        val postId = intent.getStringExtra("POST_ID")
        // Use the post ID to fetch details or update UI accordingly
    }
}
