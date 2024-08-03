package com.guru.travelalone

import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.guru.travelalone.databinding.ActivityLoginBinding
import com.kakao.sdk.auth.AuthApiClient
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient

class Login_Activity : AppCompatActivity() {

    private val binding by lazy { ActivityLoginBinding.inflate(layoutInflater) }
    private val db = FirebaseFirestore.getInstance()
    private lateinit var auth: FirebaseAuth

    // 카카오 로그인 콜백 함수
    private val mCallback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
        if (error != null) {
            Log.e(TAG, "로그인 실패 $error")
        } else if (token != null) {
            Log.d(TAG, "로그인 성공 ${token.accessToken}")
            checkUserProfile()
        }
    }

    // 버튼 클릭 리스너
    private val onClickListener = View.OnClickListener { v ->
        when (v?.id) {
            binding.btnLogin.id -> {
                if (UserApiClient.instance.isKakaoTalkLoginAvailable(this)) {
                    UserApiClient.instance.loginWithKakaoTalk(this) { token, error ->
                        if (error != null) {
                            Log.e(TAG, "로그인 실패 $error")
                            if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                                return@loginWithKakaoTalk
                            } else {
                                UserApiClient.instance.loginWithKakaoAccount(
                                    this,
                                    callback = mCallback
                                )
                            }
                        } else if (token != null) {
                            Log.d(TAG, "로그인 성공 ${token.accessToken}")
                            Toast.makeText(this, "로그인 성공!", Toast.LENGTH_SHORT).show()
                            checkUserProfile()
                        }
                    }
                } else {
                    UserApiClient.instance.loginWithKakaoAccount(this, callback = mCallback)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        // Firebase Auth 초기화
        auth = FirebaseAuth.getInstance()

        // 카카오 API 키 로드
        val kakaoApiKey = BuildConfig.KAKAO_API_KEY

        // 카카오 SDK 초기화
        KakaoSdk.init(this, kakaoApiKey)

        // Firebase에 로그인된 사용자가 있는지 확인
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // 사용자가 로그인된 경우 Home_Activity로 이동
            moveHomePage(currentUser)
        }

        // 카카오에 로그인된 사용자가 있는지 확인
        if (AuthApiClient.instance.hasToken()) {
            UserApiClient.instance.accessTokenInfo { _, error ->
                if (error == null) {
                    checkUserProfile()
                }
            }
        }

        binding.btnLogin.setOnClickListener(onClickListener)

        binding.emailLoginBtn.setOnClickListener {
            email_login()
        }
    }

    // 사용자 프로필 확인
    private fun checkUserProfile() {
        UserApiClient.instance.me { user, error ->
            val targetNickname = user?.kakaoAccount?.profile?.nickname

            if (error != null) {
                Log.e(TAG, "사용자 정보 요청 실패 $error")
                // 로그인 실패 시 로그인 화면으로 이동
                val intent = Intent(this, Login_Activity::class.java)
                startActivity(intent)
                finish()
            } else {
                db.collection("members")
                    .whereEqualTo("nickname", targetNickname)
                    .get()
                    .addOnSuccessListener { result ->
                        if (result.isEmpty) {
                            Log.d("NoMatch", "닉네임이 일치하는 문서가 없음: $targetNickname")
                            val intent = Intent(this, ProEdit_Activity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            finish()
                        } else {
                            for (document in result) {
                                Log.d("DocumentID", document.id)
                                val intent = Intent(this, Home_Activity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                                finish()
                            }
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.w("Error", "문서 가져오기 오류: ", exception)
                    }
            }
        }
    }

    // 이메일 로그인
    fun email_login() {
        if (binding.emailEdt.text.toString().isNullOrEmpty() || binding.pwEdt.text.toString().isNullOrEmpty()) {
            Toast.makeText(this, "이메일 혹은 비밀 번호를 입력해주세요", Toast.LENGTH_SHORT).show()
        } else {
            signinAndssignup()
        }
    }

    // 이메일로 회원가입 및 로그인
    fun signinAndssignup() {
        auth.createUserWithEmailAndPassword(binding.emailEdt.text.toString(), binding.pwEdt.text.toString())
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // 이메일로 성공적으로 계정을 만들었을 경우
                    moveProEditPage(task.result?.user)
                } else if (task.exception?.message.isNullOrEmpty()) {
                    Toast.makeText(this, task.exception?.message, Toast.LENGTH_SHORT).show()
                } else {
                    // 이메일의 계정이 이미 존재하는 경우
                    sigininEmail()
                }
            }
    }

    // 이메일로 로그인
    fun sigininEmail() {
        auth.signInWithEmailAndPassword(binding.emailEdt.text.toString(), binding.pwEdt.text.toString())
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // 로그인 성공
                    moveHomePage(task.result?.user)
                } else {
                    Toast.makeText(this, task.exception?.message, Toast.LENGTH_SHORT).show()
                }
            }
    }

    // Home_Activity로 이동
    fun moveHomePage(user: FirebaseUser?) {
        if (user != null) {
            startActivity(Intent(this, Home_Activity::class.java))
            finish()
        }
    }

    // ProEdit_Activity로 이동
    fun moveProEditPage(user: FirebaseUser?) {
        if (user != null) {
            startActivity(Intent(this, NormalPro_Activity::class.java))
            finish()
        }
    }

    companion object {
        private const val TAG = "Login_Activity"
    }
}
