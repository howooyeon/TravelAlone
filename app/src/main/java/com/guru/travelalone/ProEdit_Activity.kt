package com.guru.travelalone

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.guru.travelalone.databinding.ActivityProEditBinding
import com.kakao.sdk.user.Constants
import com.kakao.sdk.user.UserApiClient

class ProEdit_Activity : AppCompatActivity() {

    private val binding by lazy { ActivityProEditBinding.inflate(layoutInflater) }
    lateinit var introduce: EditText


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        introduce = findViewById(R.id.Introduce)

        Log.d("ProEdit_Activity", "onCreate called")

        // 사용자 정보 요청
        try {
            UserApiClient.instance.me { user, error ->
                if (error != null) {
                    Log.e(Constants.TAG, "사용자 정보 요청 실패 $error")
                } else if (user != null) {
                    Log.d(Constants.TAG, "사용자 정보 요청 성공 : $user")
                    val nickname = user.kakaoAccount?.profile?.nickname ?: ""
                    val profileImageUrl = user.kakaoAccount?.profile?.profileImageUrl ?: ""

                    binding.txtNickName.text = nickname
                    if (profileImageUrl.isNotEmpty()) {
                        Glide.with(this)
                            .load(profileImageUrl)
                            .into(binding.kakaoPro)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(Constants.TAG, "사용자 정보 요청 중 예외 발생", e)
        }

        // 로그아웃 버튼 클릭 리스너 설정
        binding.logoutButton.setOnClickListener {
            try {
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
            } catch (e: Exception) {
                Log.e(Constants.TAG, "로그아웃 중 예외 발생", e)
            }
        }

        // viewMembersButton 클릭 리스너 설정
        binding.viewMembersButton.setOnClickListener {
            val intent = Intent(this@ProEdit_Activity, MainActivity::class.java)
            startActivity(intent)
        }
    }
}
