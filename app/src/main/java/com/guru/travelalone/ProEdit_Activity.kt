package com.guru.travelalone

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.guru.travelalone.databinding.ActivityProEditBinding
import com.kakao.sdk.user.Constants
import com.kakao.sdk.user.UserApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

                    binding.saveButton.setOnClickListener {
                        Log.d(Constants.TAG, "Save button clicked")
                        val introduceText = introduce.text.toString()
                        val member = Member(
                            nickname = nickname,
                            profileImageUrl = profileImageUrl,
                            introduce = introduceText
                        )
                        Log.d(Constants.TAG, "Member to save: $member")
                        saveMemberToDatabase(member)
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

        // 데이터베이스에 저장된 모든 멤버를 로그로 출력
        binding.viewMembersButton.setOnClickListener {
            viewAllMembers()
        }
    }

    private fun saveMemberToDatabase(member: Member) {
        Log.d(Constants.TAG, "saveMemberToDatabase called with member: $member")
        val memberDao = MemberDatabase.getDatabase(applicationContext).memberDao()
        lifecycleScope.launch(Dispatchers.IO) { // Dispatchers.IO를 사용하여 IO 스레드에서 실행되도록 지정
            try {
                Log.d(Constants.TAG, "Attempting to insert member into database")
                memberDao.insert(member)
                Log.i(Constants.TAG, "Member 저장 성공: $member")
            } catch (e: Exception) {
                Log.e(Constants.TAG, "Member 저장 중 예외 발생", e)
            }
        }
    }

    private fun viewAllMembers() {
        val memberDao = MemberDatabase.getDatabase(applicationContext).memberDao()
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val members = memberDao.getAllMembers()
                withContext(Dispatchers.Main) {
                    members.forEach { member ->
                        Log.d(Constants.TAG, "Member: $member")
                    }
                }
            } catch (e: Exception) {
                Log.e(Constants.TAG, "멤버 조회 중 예외 발생", e)
            }
        }
    }
}
