package com.guru.travelalone

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.guru.travelalone.databinding.ActivityBudgetBinding
import com.kakao.sdk.user.UserApiClient
import java.text.NumberFormat
import java.util.Locale

class Budget_Activity : AppCompatActivity() {

    private val binding by lazy { ActivityBudgetBinding.inflate(layoutInflater) }
    private lateinit var transactionAdapter: TransactionAdapter
    private val transactionList = mutableListOf<Transaction>()

    // Firebase Auth 불러오기
    private lateinit var auth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()

    private var userUid: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        // 뒤로 가기 버튼 설정
        val backButton: ImageButton = findViewById(R.id.backbtn)
        backButton.setOnClickListener {
            finish() // 현재 액티비티를 종료하여 뒤로 가기 동작 수행
        }

        // RecyclerView 설정
        transactionAdapter = TransactionAdapter(transactionList)
        binding.green.adapter = transactionAdapter
        binding.green.layoutManager = LinearLayoutManager(this)

        // Firebase Auth 초기화
        auth = FirebaseAuth.getInstance()

        // 유저 프로필 로드 후 예산 정보 로드
        loadUserProfile()

        // 충전/지출 페이지로 넘어가는 플로팅 버튼
        binding.budgetFab.setOnClickListener {
            val intent = Intent(this@Budget_Activity, Budgetcharge_Activity::class.java)
            startActivity(intent)
            finish()
        }

        // 유저의 예산 초기화 버튼
        binding.resetbtn.setOnClickListener {
            resetUserBudgetData()
        }
    }


    //현재 로그인한 유저의 충전/지출 초기화 로직
    private fun resetUserBudgetData() {
        userUid?.let { uid ->
            // 충전 데이터 삭제
            db.collection("budget_charges")
                .whereEqualTo("user_uid", uid)
                .get()
                .addOnSuccessListener { result ->
                    for (document in result) {
                        db.collection("budget_charges").document(document.id).delete()
                            .addOnSuccessListener {
                                Log.d("Budget_Activity", "충전 데이터 삭제 성공: ${document.id}")
                            }
                            .addOnFailureListener { e ->
                                Log.w("Budget_Activity", "충전 데이터 삭제 실패: ${document.id}", e)
                            }
                    }
                }
                .addOnFailureListener { e ->
                    Log.w("Budget_Activity", "충전 데이터 가져오기 실패", e)
                }

            // 지출 데이터 삭제
            db.collection("budget_spends")
                .whereEqualTo("user_uid", uid)
                .get()
                .addOnSuccessListener { result ->
                    for (document in result) {
                        db.collection("budget_spends").document(document.id).delete()
                            .addOnSuccessListener {
                                Log.d("Budget_Activity", "지출 데이터 삭제 성공: ${document.id}")
                            }
                            .addOnFailureListener { e ->
                                Log.w("Budget_Activity", "지출 데이터 삭제 실패: ${document.id}", e)
                            }
                    }
                }
                .addOnFailureListener { e ->
                    Log.w("Budget_Activity", "지출 데이터 가져오기 실패", e)
                }
        }

        // 초기화 후 화면 갱신
        transactionList.clear()
        transactionAdapter.notifyDataSetChanged()
        updateBudgetUI(0, 0)
    }

    //예산 확인
    private fun loadBudget(uid: String) {
        Log.d("Budget_Activity", "현재 사용자 UID: $uid")

        var totalChargeAmount = 0
        var totalSpendAmount = 0

        // 충전 금액 합계 계산
        db.collection("budget_charges")
            .whereEqualTo("user_uid", uid)
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val charge = document.toObject(BudgetCharge::class.java)
                    val chargeAmount = charge.charge_amount.replace(",", "").toIntOrNull() ?: 0
                    totalChargeAmount += chargeAmount

                    val transaction = Transaction(
                        id = charge.id,
                        user_uid = charge.user_uid,
                        amount = charge.charge_amount,
                        category = null,
                        store = null,
                        memo = charge.charge_memo,
                        date = charge.charge_date,
                        type = TransactionType.CHARGE
                    )
                    transactionList.add(transaction)
                }

                // 지출 금액 합계 계산
                db.collection("budget_spends")
                    .whereEqualTo("user_uid", uid)
                    .get()
                    .addOnSuccessListener { spendResult ->
                        for (document in spendResult) {
                            val spend = document.toObject(BudgetSpend::class.java)
                            val spendAmount = spend.spend_amount.replace(",", "").toIntOrNull() ?: 0
                            totalSpendAmount += spendAmount

                            val transaction = Transaction(
                                id = spend.id,
                                user_uid = spend.user_uid,
                                amount = spend.spend_amount,
                                category = spend.spend_category.name,
                                store = spend.spend_store,
                                memo = spend.spend_memo,
                                date = spend.spend_date,
                                type = TransactionType.SPEND
                            )
                            transactionList.add(transaction)
                        }

                        // 날짜 순으로 정렬
                        transactionList.sortByDescending { it.date }

                        // 어댑터에 데이터 변경 알림
                        transactionAdapter.notifyDataSetChanged()

                        Log.d("Budget_Activity", "총 충전 금액: $totalChargeAmount")
                        Log.d("Budget_Activity", "총 지출 금액: $totalSpendAmount")

                        // 남은 금액과 지출 금액 계산 후 UI 업데이트
                        val remainingAmount = totalChargeAmount - totalSpendAmount
                        updateBudgetUI(totalSpendAmount, remainingAmount)
                    }
                    .addOnFailureListener { e ->
                        Log.w("Budget_Activity", "지출 문서 가져오기 오류", e)
                    }
            }
            .addOnFailureListener { e ->
                Log.w("Budget_Activity", "충전 문서 가져오기 오류", e)
            }
    }

    //예산 ui 업데이트 ( 화면 하단의 뷰들 불러옴)
    private fun updateBudgetUI(usedAmount: Int, remainingAmount: Int) {
        val numberFormat = NumberFormat.getInstance(Locale.getDefault())
        val formattedUsedAmount = numberFormat.format(usedAmount)
        val formattedRemainingAmount = numberFormat.format(remainingAmount)

        Log.d("Budget_Activity", "사용한 금액: $formattedUsedAmount, 남은 금액: $formattedRemainingAmount")
        binding.usedmoney.text = getString(R.string.used_amount_format, formattedUsedAmount)
        binding.leftmoney.text = getString(R.string.remaining_amount_format, formattedRemainingAmount)
    }

    private fun loadUserProfile() {
        val currentUser = auth.currentUser

        if (currentUser != null) {
            // Firebase 로그인 유저 정보 가져오기
            userUid = currentUser?.uid.toString()
            userUid?.let { loadBudget(it) }
        } else {
            // Kakao 로그인 유저 정보 가져오기
            UserApiClient.instance.me { user, error ->
                if (error != null) {
                    Log.e("Kakao", "사용자 정보 요청 실패", error)
                } else if (user != null) {
                    userUid = user.id.toString()
                    userUid?.let { loadBudget(it) }
                }
            }
        }
    }
}
