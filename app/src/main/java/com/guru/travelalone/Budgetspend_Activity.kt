package com.guru.travelalone

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.kakao.sdk.user.UserApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.guru.travelalone.databinding.ActivityBudgetspendBinding

class Budgetspend_Activity : AppCompatActivity() {
    private val binding by lazy { ActivityBudgetspendBinding.inflate(layoutInflater) }

    // Firebase Auth 불러오기
    private lateinit var auth: FirebaseAuth
    private var currentUser: FirebaseUser? = null
    private val db = FirebaseFirestore.getInstance()

    private var userUid: String? = null
    private var selectedCategory: SpendCategory? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        // Firebase Auth 초기화
        auth = FirebaseAuth.getInstance()
        currentUser = auth.currentUser

        loadUserProfile()

        setupSpinner()

        binding.chargebtn.setOnClickListener {
            val intent = Intent(this@Budgetspend_Activity, Budgetcharge_Activity::class.java)
            startActivity(intent)
        }

        binding.saveButton.setOnClickListener {
            val spendMoney = binding.spendMoney.text.toString()
            val spendStore = binding.spendStore.text.toString()
            val spendMemo = binding.spendMemo.text.toString()

            // 입력값 검증
            if (spendMoney.isBlank() || spendStore.isBlank() || spendMemo.isBlank()) {
                Toast.makeText(this, "정보를 다 입력해주세요.", Toast.LENGTH_SHORT).show()
            } else {
                // 코루틴을 사용하여 비동기 함수 호출
                CoroutineScope(Dispatchers.Main).launch {
                    createSpend(spendMoney, selectedCategory, spendStore, spendMemo)

                    val intent = Intent(this@Budgetspend_Activity, Budget_Activity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
        }
    }

    private fun setupSpinner() {
        val categories = resources.getStringArray(R.array.spending)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spendCate.adapter = adapter

        binding.spendCate.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedCategory = when (position) {
                    0 -> SpendCategory.SHOPPING
                    1 -> SpendCategory.FOOD
                    2 -> SpendCategory.ETC
                    else -> SpendCategory.ETC
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                selectedCategory = null
            }
        }
    }

    private suspend fun createSpend(spendMoney: String, spendCategory: SpendCategory?, spendStore: String, spendMemo: String) {
        val currentUser = auth.currentUser
        var userId: String = ""

        if (currentUser != null) {
            // Firebase 로그인 사용자 처리
            // Firebase 로그인 유저 정보 가져오기
            userId = currentUser?.uid.toString()

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

        val id = CounterHelper.getSpendId()
        val budgetId = 123L // 예산 ID를 적절히 설정
        val currentDate = Date()
//        val formattedDate = getFormattedDate(currentDate)

        val budgetSpend = BudgetSpend(
            id = id,
            user_uid = userId,
            budget_id = budgetId,
            spend_amount = spendMoney,
            spend_category = spendCategory ?: SpendCategory.ETC,
            spend_store = spendStore,
            spend_memo = spendMemo,
            spend_date = currentDate
        )

        try {
            db.collection("budget_spends")
                .add(budgetSpend)
                .addOnSuccessListener {
                    Log.d("Budgetspend_Activity", "DocumentSnapshot successfully written!")
                }
                .addOnFailureListener { e ->
                    Log.w("Budgetspend_Activity", "Error adding document", e)
                }
        } catch (e: Exception) {
            Log.e("Budgetspend_Activity", "Exception during document write: ${e.message}", e)
        }
    }

    private fun loadUserProfile() {
        if (currentUser != null) {
            // Firebase 로그인 유저 정보 가져오기
            val email = currentUser?.email

            db.collection("members")
                .whereEqualTo("login_id", email)
                .get()
                .addOnSuccessListener { result ->
                    for (document in result) {
                        userUid = currentUser?.uid.toString()
                        Log.d("Budgetspend_Activity", "User UID: $userUid")
                    }
                }
                .addOnFailureListener { e ->
                    Log.w("Firestore", "Error getting documents", e)
                }
        } else {
            // 카카오 로그인 유저 정보 가져오기
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
