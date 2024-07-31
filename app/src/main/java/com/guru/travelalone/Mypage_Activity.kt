package com.guru.travelalone

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.guru.travelalone.databinding.ActivityMypageBinding
import com.google.android.material.tabs.TabLayout
import com.google.firebase.firestore.FirebaseFirestore
import com.kakao.sdk.user.Constants
import com.kakao.sdk.user.UserApiClient

class Mypage_Activity : AppCompatActivity() {

    private lateinit var binding: ActivityMypageBinding
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMypageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.editButton.setOnClickListener {
            val intent = Intent(this, ProEdit_Activity::class.java)
            startActivity(intent)
        }

        binding.locateButton.setOnClickListener {
            val intent = Intent(
                this@Mypage_Activity,
                Locate_Activity::class.java
            )
            startActivity(intent)
        }

        binding.travbotButton.setOnClickListener {
            val intent = Intent(
                this@Mypage_Activity,
                Travbot_activity::class.java
            )
            startActivity(intent)
        }

        binding.homeButton.setOnClickListener {
            val intent = Intent(
                this@Mypage_Activity,
                Home_Activity::class.java
            )
            startActivity(intent)
        }

        binding.commuButton.setOnClickListener {
            val intent = Intent(
                this@Mypage_Activity,
                Community_Activity::class.java
            )
            startActivity(intent)
        }

        binding.mypageButton.setOnClickListener {
            val intent = Intent(
                this@Mypage_Activity,
                Mypage_Activity::class.java
            )
            startActivity(intent)
        }

        fetchUserProfile()
        setTabLayout()
    }

    private fun fetchUserProfile() {
        try {
            UserApiClient.instance.me { user, error ->
                if (error != null) {
                    Log.e(Constants.TAG, "사용자 정보 요청 실패 $error")
                    Toast.makeText(this, "사용자 정보 요청 실패", Toast.LENGTH_SHORT).show()
                } else if (user != null) {
                    val userId = user.id.toString()
                    Log.d(Constants.TAG, "사용자 ID: $userId")

                    db.collection("members").document().get()
                        .addOnSuccessListener { document ->
                            if (document != null) {
                                val nickname = document.getString("nickname") ?: ""
                                val introduction = document.getString("introduction") ?: ""
                                val profileImageUrl = document.getString("profileImageUrl") ?: ""

                                Log.d(Constants.TAG, "nickname: $nickname")
                                Log.d(Constants.TAG, "introduction: $introduction")
                                Log.d(Constants.TAG, "profileImageUrl: $profileImageUrl")

                                binding.nickname.text = nickname
                                binding.introduction.text = introduction

                                if (profileImageUrl.isNotEmpty()) {
                                    Glide.with(this)
                                        .load(profileImageUrl)
                                        .into(binding.kakaoPro)
                                }
                            } else {
                                Log.d(Constants.TAG, "No such document")
                                Toast.makeText(this, "No such document", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.w(Constants.TAG, "Error getting documents: ", e)
                            Toast.makeText(this, "Error getting documents", Toast.LENGTH_SHORT).show()
                        }
                }
            }
        } catch (e: Exception) {
            Log.e(Constants.TAG, "사용자 정보 요청 중 예외 발생", e)
            Toast.makeText(this, "사용자 정보 요청 중 예외 발생", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setTabLayout() {
        // 초기 tab 세팅
        binding.tabLayoutContainer.setBackgroundResource(R.color.gray)

        binding.tabLayoutContainer.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab!!.position) {
                    0 -> binding.tabLayoutContainer.setBackgroundResource(R.color.gray)
                    1 -> binding.tabLayoutContainer.setBackgroundResource(R.color.gray)
                    2 -> binding.tabLayoutContainer.setBackgroundResource(R.color.gray)
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }
}
