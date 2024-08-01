package com.guru.travelalone

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.guru.travelalone.databinding.ActivityBudgetchargeBinding
import com.kakao.sdk.user.UserApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Budgetcharge_Activity : AppCompatActivity() {
    private val binding by lazy { ActivityBudgetchargeBinding.inflate(layoutInflater) }

    // Firebase Auth 불러오기
    private lateinit var auth: FirebaseAuth
    private var currentUser: FirebaseUser? = null
    private val db = FirebaseFirestore.getInstance()

    private var userUid: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        // Firebase Auth 초기화
        auth = FirebaseAuth.getInstance()
        currentUser = auth.currentUser

        loadUserProfile()

        binding.spendbtn.setOnClickListener {
            val intent = Intent(this@Budgetcharge_Activity, Budgetspend_Activity::class.java)
            startActivity(intent)
            finish()
        }

        binding.saveButton.setOnClickListener {
            val chargeMoney = binding.chargeMoney.text.toString()
            val chargeMemo = binding.chargeMemo.text.toString()

            // 입력값 검증
            if (chargeMoney.isBlank() || chargeMemo.isBlank()) {
                Toast.makeText(this, "충전 금액과 메모를 작성해주세요.", Toast.LENGTH_SHORT).show()
            } else {
                // 코루틴을 사용하여 비동기 함수 호출
                CoroutineScope(Dispatchers.Main).launch {
                    createCharge(chargeMoney, chargeMemo)

                    // createCharge 함수가 완료된 후 Budget_Activity로 이동
                    val intent = Intent(this@Budgetcharge_Activity, Budget_Activity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
        }
    }

    private suspend fun createCharge(chargeMoney: String, chargeMemo: String) {
        val currentUser = auth.currentUser
        var userId: String = ""

        if (currentUser != null) {
            // Firebase 로그인 사용자 처리
            userId = currentUser.uid.toString()
        } else {
            // 카카오 로그인 사용자 처리
            UserApiClient.instance.me { user, error ->
                if (error != null) {
                    Log.e("Kakao", "사용자 정보 요청 실패", error)
                } else if (user != null) {
                    userId = user.id.toString()
                }

            }
        }
        val id = CounterHelper.getChargeId()
        val budgetId = 123L // 예산 ID를 적절히 설정
        val currentDate = Date()
//        val formattedDate = getFormattedDate(currentDate)

        val budgetCharge = BudgetCharge(
            id = id,
            user_uid = userId,
            budget_id = budgetId,
            charge_amount = chargeMoney,
            charge_memo = chargeMemo,
            charge_date = currentDate
        )

        try {
            db.collection("budget_charges")
                .add(budgetCharge)
                .addOnSuccessListener {
                    Log.d("Budgetcharge_Activity", "DocumentSnapshot successfully written!")
                }
                .addOnFailureListener { e ->
                    Log.w("Budgetcharge_Activity", "Error adding document", e)
                }
        } catch (e: Exception) {
            Log.e("Budgetcharge_Activity", "Exception during document write: ${e.message}", e)
        }
    }

    private fun loadUserProfile() {
        if (currentUser != null) {
            // Firebase 로그인 유저 정보 가져오기
            userUid = currentUser?.uid.toString()
        } else {
            // Kakao 로그인 유저 정보 가져오기
            UserApiClient.instance.me { user, error ->
                if (error != null) {
                    Log.e("Kakao", "사용자 정보 요청 실패", error)
                } else if (user != null) {
                    val nickname = user.kakaoAccount?.profile?.nickname
                    userUid = user.id.toString()
                }
            }
        }
    }

    fun getFormattedDate(timestamp: Date): String {
        val sdf = SimpleDateFormat("yyyy.MM.dd.HH.mm", Locale.getDefault())
        return sdf.format(timestamp)
    }


}
