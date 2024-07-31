package com.guru.travelalone

import android.content.Intent
import android.os.Bundle
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
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class TripDate_Activity: AppCompatActivity() {
    val db = FirebaseFirestore.getInstance()
    private lateinit var bt_back : ImageButton
    private lateinit var spinner : Spinner
    private lateinit var bt_reg : Button
    private lateinit var et_title : EditText
    private lateinit var calendarView: CalendarView
    private lateinit var rangeTextView: TextView
    private var startDate: Long? = null
    private var endDate: Long? = null
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
        var str_spinner = ""
        calendarView = findViewById(R.id.calendarView)
        rangeTextView = findViewById(R.id.rangeTextView)
        bt_reg = findViewById(R.id.bt_reg)

        bt_back.setOnClickListener {
            val intent = Intent(
                this, Home_Activity::class.java
            )
            startActivity(intent)
        }

        // 스피너 : 지역 선택
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                str_spinner = parent.getItemAtPosition(position).toString()
            }
            // 아무 항목도 선택되지 않았을 때
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // 캘린더뷰 : 날짜 선택
        calendarView.setOnDateChangeListener { view, year, month, dayOfMonth ->
            val selectedDate = Calendar.getInstance()
            selectedDate.set(year, month, dayOfMonth)
            // 현재 날짜 선택
            val selectedDateInMillis = selectedDate.timeInMillis

            // 사용자가 날짜를 클릭했을 때 실행되는 코드
            if (startDate == null || (startDate != null && endDate != null)) {
                // startDate가 설정되지 않았거나, 이미 startDate와 endDate가 모두 설정된 경우
                // 새로운 범위를 시작하기 위해 startDate를 현재 선택된 날짜로 설정하고 endDate는 null로 초기화
                startDate = selectedDateInMillis
                endDate = null
                // 선택된 startDate를 표시
                rangeTextView.text = "${dateFormat.format(startDate)}"
            }
            else if (startDate != null && endDate == null) {
                // startDate는 설정되어 있고, endDate는 아직 설정되지 않은 경우
                if (selectedDateInMillis > startDate!!) {
                    // 현재 선택된 날짜가 startDate 이후인 경우
                    // endDate를 현재 선택된 날짜로 설정
                    endDate = selectedDateInMillis
                    rangeTextView.text = "${dateFormat.format(startDate)}   ~   ${dateFormat.format(endDate)}"
                } else {
                    // 현재 선택된 날짜가 startDate 이전인 경우
                    // startDate를 현재 선택된 날짜로 재설정
                    startDate = selectedDateInMillis
                    rangeTextView.text = "${dateFormat.format(startDate)}"
                }
            }
        }

        bt_reg.setOnClickListener {

            val str_title = et_title.text.toString()

            if (str_title.isNotEmpty() && str_spinner != "여행, 어디로 떠나시나요?" && startDate != null) {
                // Firestore에 저장할 데이터
                val tripData = hashMapOf(
                    "title" to str_title,
                    "location" to str_spinner,
                    "start_date" to startDate,
                    "end_date" to endDate
                )

                // Firestore에 데이터 추가
                db.collection("tripdate")
                    .add(tripData)
                    .addOnSuccessListener { documentReference ->
                        // 데이터 추가 성공 시 실행
                        val intent = Intent(this, Home_Activity::class.java)
                        startActivity(intent)
                    }
                    .addOnFailureListener { e ->
                        // 데이터 추가 실패 시 실행
                        Toast.makeText(this, "데이터 저장 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "모든 항목을 입력해주세요", Toast.LENGTH_SHORT).show()
            }
        }
    }
}