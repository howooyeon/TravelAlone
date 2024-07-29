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
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
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

//    private fun getKeyHash(): String? {
//        return try {
//            val info: PackageInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
//            for (signature in info.signatures) {
//                val md: MessageDigest = MessageDigest.getInstance("SHA")
//                md.update(signature.toByteArray())
//                return Base64.encodeToString(md.digest(), Base64.NO_WRAP)
//            }
//            null
//        } catch (e: PackageManager.NameNotFoundException) {
//            Log.e(TAG, "NameNotFoundException", e)
//            null
//        } catch (e: NoSuchAlgorithmException) {
//            Log.e(TAG, "NoSuchAlgorithmException", e)
//            null
//        }
//    }
}
