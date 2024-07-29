package com.guru.travelalone

import android.os.Bundle
import android.widget.Spinner
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.ArrayAdapter
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class Community_Write_Activity : AppCompatActivity() {

    // Spinner 추가
    lateinit var regionSpinner: Spinner
    private lateinit var startDateSpinner: Spinner
    private lateinit var endDateSpinner: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_community_write)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Spinner 초기화
        regionSpinner = findViewById(R.id.region)
        startDateSpinner = findViewById(R.id.start_date)
        endDateSpinner = findViewById(R.id.end_date)

        // Spinner에 어댑터 설정
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.region_list,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        regionSpinner.adapter = adapter

        // Generate date lists
        val dates = generateDateList()

        // Create ArrayAdapter for the start and end date Spinners
        val dateAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, dates)
        dateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // Set the adapters to the Spinners
        startDateSpinner.adapter = dateAdapter
        endDateSpinner.adapter = dateAdapter
    }

    private fun generateDateList(): List<String> {
        val dates = mutableListOf<String>()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance()

        for (i in 0..364) {
            dates.add(sdf.format(calendar.time))
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        return dates
    }
}
