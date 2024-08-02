package com.guru.travelalone

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.Button
import android.widget.CalendarView
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.kakao.sdk.user.UserApiClient
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class TripDate_Activity : AppCompatActivity() {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    private lateinit var bt_back: ImageButton
    private lateinit var spinner: Spinner
    private lateinit var bt_reg: Button
    private lateinit var et_title: EditText
    private lateinit var calendarView: CalendarView
    private lateinit var rangeTextView: TextView
    private var startDate: Long? = null
    private var endDate: Long? = null
    private var str_spinner: String = ""  // 클래스 멤버 변수로 선언
    private val dateFormat = SimpleDateFormat("MM월 dd일", Locale.KOREAN)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_tripdate)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        bt_back = findViewById(R.id.back)
        et_title = findViewById(R.id.et_title)
        spinner = findViewById(R.id.location_spinner)
        calendarView = findViewById(R.id.calendarView)
        rangeTextView = findViewById(R.id.rangeTextView)
        bt_reg = findViewById(R.id.bt_reg)

        bt_back.setOnClickListener {
            val intent = Intent(this, Home_Activity::class.java)
            startActivity(intent)
        }

        // 스피너 : 지역 선택
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                str_spinner = parent.getItemAtPosition(position).toString()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // 캘린더뷰 : 날짜 선택
        calendarView.setOnDateChangeListener { view, year, month, dayOfMonth ->
            val selectedDate = Calendar.getInstance().apply {
                set(year, month, dayOfMonth)
            }
            val selectedDateInMillis = selectedDate.timeInMillis

            if (startDate == null || (startDate != null && endDate != null)) {
                startDate = selectedDateInMillis
                endDate = null
                rangeTextView.text = "${dateFormat.format(startDate)}"
            } else if (startDate != null && endDate == null) {
                if (selectedDateInMillis > startDate!!) {
                    endDate = selectedDateInMillis
                    rangeTextView.text = "${dateFormat.format(startDate)}   ~   ${dateFormat.format(endDate)}"
                } else {
                    startDate = selectedDateInMillis
                    rangeTextView.text = "${dateFormat.format(startDate)}"
                }
            }
        }

        bt_reg.setOnClickListener {
            val str_title = et_title.text.toString()

            if (str_title.isNotEmpty() && str_spinner != "여행, 어디로 떠나시나요?" && startDate != null) {
                val currentUser = auth.currentUser
                if (currentUser != null) {
                    // Firebase 인증된 사용자일 경우
                    val userId = currentUser.uid
                    saveTripData(userId)
                } else {
                    // 카카오 로그인 상태 확인
                    UserApiClient.instance.me { user, error ->
                        if (error != null) {
                            Log.e("Kakao", "사용자 정보 요청 실패", error)
                            Toast.makeText(this, "카카오 사용자 정보 요청 실패", Toast.LENGTH_SHORT).show()
                        } else if (user != null) {
                            // KakaoUID 받아와서 값 넘김 DB에서 동일하게 UID로 불러오기 가능
                            val kakaoUserId = user?.id.toString()
                            saveTripData(kakaoUserId)
                        }
                    }
                }
            } else {
                Toast.makeText(this, "모든 항목을 입력해주세요", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveTripData(userId: String) {
        val tripData = hashMapOf(
            "title" to et_title.text.toString(),
            "location" to str_spinner,
            "start_date" to startDate,
            "end_date" to endDate,
            "user_id" to userId
        )

        db.collection("tripdate")
            .add(tripData)
            .addOnSuccessListener { documentReference ->
                val intent = Intent(this, Home_Activity::class.java)
                startActivity(intent)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "데이터 저장 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
