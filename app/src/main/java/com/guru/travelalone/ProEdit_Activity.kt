package com.guru.travelalone

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.guru.travelalone.databinding.ActivityProEditBinding
import com.kakao.sdk.user.Constants
import com.kakao.sdk.user.UserApiClient

class ProEdit_Activity : AppCompatActivity() {

    private val binding by lazy { ActivityProEditBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        // 사용자 정보 요청
        UserApiClient.instance.me { user, error ->
            if (error != null) {
                Log.e(Constants.TAG, "사용자 정보 요청 실패 $error")
            } else if (user != null) {
                Log.d(Constants.TAG, "사용자 정보 요청 성공 : $user")
                binding.txtNickName.text = user.kakaoAccount?.profile?.nickname
                // 프로필 사진 로드
                val profileImageUrl = user.kakaoAccount?.profile?.profileImageUrl
                if (profileImageUrl != null) {
                    Glide.with(this)
                        .load(profileImageUrl)
                        .into(binding.kakaoPro) // 변경된 부분
                }
            }
        }


        // 로그아웃 버튼 클릭 리스너 설정
        binding.logoutButton.setOnClickListener {
            UserApiClient.instance.logout { error ->
                if (error != null) {
                    Log.e(Constants.TAG, "로그아웃 실패. SDK에서 토큰 삭제됨", error)
                } else {
                    Log.i(Constants.TAG, "로그아웃 성공. SDK에서 토큰 삭제됨")
                    // 로그아웃 후 Login_Activity로 이동
                    val intent = Intent(this@ProEdit_Activity, Login_Activity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
            }
        }
    }
}
