package com.guru.travelalone

import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.guru.travelalone.databinding.ActivityLoginBinding
import com.kakao.sdk.auth.AuthApiClient
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.common.util.Utility
import com.kakao.sdk.user.UserApiClient
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

class Login_Activity : AppCompatActivity() {

    private val binding by lazy { ActivityLoginBinding.inflate(layoutInflater) }
    private val db = FirebaseFirestore.getInstance()
    private lateinit var auth: FirebaseAuth

    private val mCallback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
        if (error != null) {
            Log.e(TAG, "로그인 실패 $error")
        } else if (token != null) {
            Log.d(TAG, "로그인 성공 ${token.accessToken}")
            checkUserProfile()
        }
    }

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

        // Load the Kakao API Key from BuildConfig
        val kakaoApiKey = BuildConfig.KAKAO_API_KEY

        // Initialize Kakao SDK
        KakaoSdk.init(this, kakaoApiKey)

        // Log the key hash
        // Log.d(TAG, "Key Hash: ${getKeyHash()}")

        if (AuthApiClient.instance.hasToken()) {
            UserApiClient.instance.accessTokenInfo { _, error ->
                if (error == null) {
                    checkUserProfile()
                }
            }
        }

        binding.btnLogin.setOnClickListener(onClickListener)

        auth = Firebase.auth

        binding.emailLoginBtn.setOnClickListener {
            email_login()
        }


//        binding.release.setOnClickListener{
//            val intent = Intent(this@Login_Activity, Release_Activity::class.java)
//            startActivity(intent)
//            finish()
//        }

    }

    private fun checkUserProfile() {

        UserApiClient.instance.me { user, error ->
            val targetNickname = user?.kakaoAccount?.profile?.nickname

            if (error != null) {
                Log.e(TAG, "사용자 정보 요청 실패 $error")
                // 로그인 실패 시 로그인 화면으로 이동
                val intent = Intent(this, Login_Activity::class.java)
                startActivity(intent)
                finish()
//            } else if (user != null && user.kakaoAccount?.profile?.nickname == null) {
//                // 사용자 정보가 있을 때 MainActivity로 이동
//                val intent = Intent(this, MainActivity::class.java)
//                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//                startActivity(intent)
//                finish()
//            } else {
//                // 사용자 정보가 없을 때 ProEdit_Activity로 이동
//
//            }
            } else {
                db.collection("members")
                    .whereEqualTo("nickname", targetNickname)
                    .get()
                    .addOnSuccessListener { result ->
                        if (result.isEmpty) {
                            Log.d("NoMatch", "No documents found with nickname: $targetNickname")
                            val intent = Intent(this, ProEdit_Activity::class.java)
                            intent.flags =
                                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            finish()
                        } else {
                            for (document in result) {
                                Log.d("DocumentID", document.id)
                                val intent = Intent(this, Home_Activity::class.java)
                                intent.flags =
                                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                                finish()
                            }
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.w("Error", "Error getting documents: ", exception)
                    }
            }

        }
    }

    fun email_login() {
        if (binding.emailEdt.text.toString().isNullOrEmpty() || binding.pwEdt.text.toString()
                .isNullOrEmpty()
        ) {
            Toast.makeText(this, "이메일 혹은 비밀 번호를 입력해주세요", Toast.LENGTH_SHORT).show()
        } else {
            signinAndssignup()
        }
    }

    fun signinAndssignup() {
        auth.createUserWithEmailAndPassword(
            binding.emailEdt.text.toString(),
            binding.pwEdt.text.toString()
        )
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    //이메일로 성공적으로 계정을 만들었을 경우
                    moveProEditPage(task.result?.user)
                } else if (task.exception?.message.isNullOrEmpty()) {
                    Toast.makeText(this, task.exception?.message, Toast.LENGTH_SHORT).show()
                } else {
                    //이메일의 계정이 이미 존재하는 경우
                    sigininEmail()
                }
            }
    }

    fun sigininEmail() {
        auth.signInWithEmailAndPassword(
            binding.emailEdt.text.toString(),
            binding.pwEdt.text.toString()
        )
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    //로그인 성공
                    moveHomePage(task.result?.user)
                } else {
                    Toast.makeText(this, task.exception?.message, Toast.LENGTH_SHORT).show()
                }

            }
    }

    fun moveHomePage(user: FirebaseUser?) {
        if (user != null) {
            startActivity(Intent(this, Home_Activity::class.java))
            finish()
        }
    }

    fun moveProEditPage(user: FirebaseUser?) {
        if (user != null) {
            startActivity(Intent(this, NormalPro_Activity::class.java))
            finish()
        }
    }


}
