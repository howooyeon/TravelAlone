package com.guru.travelalone

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.guru.travelalone.databinding.ActivityMypageBinding
import com.google.android.material.tabs.TabLayout

class Mypage_Activity : AppCompatActivity() {

    private lateinit var binding: ActivityMypageBinding

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
            // 기존에 있는 수정하기 연결
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

        setTabLayout()
    }

    private fun setTabLayout() {
        // 초기 tab 세팅
        binding.tabLayoutContainer.setBackgroundResource(R.color.babyblue)

        binding.tabLayoutContainer.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab!!.position) {
                    0 -> binding.tabLayoutContainer.setBackgroundResource(R.color.babyblue)
                    1 -> binding.tabLayoutContainer.setBackgroundResource(R.color.white)
                    2 -> binding.tabLayoutContainer.setBackgroundResource(R.color.black)
                    3 -> binding.tabLayoutContainer.setBackgroundResource(R.color.white)
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }
}
