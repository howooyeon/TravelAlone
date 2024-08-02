package com.guru.travelalone

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.guru.travelalone.databinding.ActivityMypageBinding
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.kakao.sdk.user.UserApiClient

class Mypage_Activity : AppCompatActivity() {

    private lateinit var binding: ActivityMypageBinding
    private val db = FirebaseFirestore.getInstance()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMypageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 시스템 바 패딩 설정
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 각 버튼의 클릭 이벤트 설정
        binding.editButton.setOnClickListener {
            val intent = Intent(this, MyProEdit_Activity::class.java)
            startActivity(intent)
        }

        binding.locateButton.setOnClickListener {
            val intent = Intent(this@Mypage_Activity, Locate_Activity::class.java)
            startActivity(intent)
        }

        binding.travbotButton.setOnClickListener {
            val intent = Intent(this@Mypage_Activity, Travbot_activity::class.java)
            startActivity(intent)
        }

        binding.homeButton.setOnClickListener {
            val intent = Intent(this@Mypage_Activity, Home_Activity::class.java)
            startActivity(intent)
        }

        binding.commuButton.setOnClickListener {
            val intent = Intent(this@Mypage_Activity, Community_Activity::class.java)
            startActivity(intent)
        }

        binding.mypageButton.setOnClickListener {
            val intent = Intent(this@Mypage_Activity, Mypage_Activity::class.java)
            startActivity(intent)
        }

        // 사용자 프로필 정보 가져오기
        fetchUserProfile()

        // 탭 레이아웃 설정
        setTabLayout()
    }

    private fun fetchUserProfile() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            // Firebase에서 사용자 프로필 정보 가져오기
            val email = currentUser.email
            db.collection("members")
                .whereEqualTo("login_id", email)
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        val nickname = document.getString("editnickname") ?: "닉네임 없음"
                        val introduction = document.getString("introduce") ?: "소개 없음"
                        val profileImageUrl = document.getString("profileImageUrl") ?: ""

                        Log.d("Firebase", "닉네임: $nickname")
                        Log.d("Firebase", "소개: $introduction")
                        Log.d("Firebase", "프로필 이미지 URL: $profileImageUrl")

                        binding.nickname.text = nickname
                        binding.introduction.text = introduction

                        if (profileImageUrl.isNotEmpty()) {
                            Log.d("Firebase", "프로필 이미지 로드 시도: $profileImageUrl")
                            Glide.with(this)
                                .load(profileImageUrl)
                                .into(binding.kakaoPro)
                        } else {
                            Log.d("Firebase", "프로필 이미지 URL이 비어 있습니다.")
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Log.w("Firebase", "문서 가져오기 실패: ", e)
                    Toast.makeText(this, "문서 가져오기 실패", Toast.LENGTH_SHORT).show()
                }
        } else {
            // Kakao 사용자 프로필 정보 가져오기
            UserApiClient.instance.me { user, error ->
                if (error != null) {
                    Log.e("Kakao", "사용자 정보 요청 실패", error)
                    Toast.makeText(this, "사용자 정보 요청 실패", Toast.LENGTH_SHORT).show()
                } else if (user != null) {
                    val kakaoNickname = user.kakaoAccount?.profile?.nickname ?: ""
                    db.collection("members")
                        .whereEqualTo("nickname", kakaoNickname)
                        .get()
                        .addOnSuccessListener { documents ->
                            for (document in documents) {
                                val nickname = document.getString("editnickname") ?: "닉네임 없음"
                                val introduction = document.getString("introduce") ?: "소개 없음"
                                val profileImageUrl = document.getString("profileImageUrl") ?: ""

                                Log.d("Kakao", "닉네임: $nickname")
                                Log.d("Kakao", "소개: $introduction")
                                Log.d("Kakao", "프로필 이미지 URL: $profileImageUrl")

                                binding.nickname.text = nickname
                                binding.introduction.text = introduction

                                if (profileImageUrl.isNotEmpty()) {
                                    Log.d("Kakao", "프로필 이미지 로드 시도: $profileImageUrl")
                                    Glide.with(this)
                                        .load(profileImageUrl)
                                        .into(binding.kakaoPro)
                                } else {
                                    Log.d("Kakao", "프로필 이미지 URL이 비어 있습니다.")
                                }
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.w("Kakao", "문서 가져오기 실패: ", e)
                            Toast.makeText(this, "문서 가져오기 실패", Toast.LENGTH_SHORT).show()
                        }
                }
            }
        }
    }

    val fragment1: Fragment = Fragment1()
    val fragment2: Fragment = Fragment2()
    val fragment3: Fragment = Fragment3()
    private fun setTabLayout() {
        // 초기 tab 설정
        binding.tabLayoutContainer.setBackgroundResource(R.color.gray)

        // 기본으로 첫 번째 탭이 선택된 상태로 시작
        supportFragmentManager.beginTransaction().replace(R.id.frame, fragment1).commit()

        binding.tabLayoutContainer.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {

                val selected: Fragment? = when (tab!!.position) {
                    0 -> fragment1
                    1 -> fragment2
                    2 -> fragment3
                    else -> fragment1
                }

                if (selected != null) {
                    supportFragmentManager.beginTransaction().replace(R.id.frame, selected).commit()
                }

            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
        // 첫 번째 탭을 선택 상태로 설정
        binding.tabLayoutContainer.getTabAt(0)?.select()
    }
}
