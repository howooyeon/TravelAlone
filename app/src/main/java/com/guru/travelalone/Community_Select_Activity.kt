package com.guru.travelalone

import android.os.Bundle
import android.util.Log
import android.widget.ListView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.guru.travelalone.adapter.MypageTripListAdapter
import com.guru.travelalone.item.MypageTripListItem
import java.text.SimpleDateFormat
import java.util.Locale

class Community_Select_Activity : AppCompatActivity() {

    lateinit var mypagetriplistview : ListView

    // Firestore 인스턴스 초기화
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()

    // 데이터 포맷
    private val dateFormat = SimpleDateFormat("yyyy.MM.dd", Locale.KOREAN)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_community_select)

        // mypagetripList---------------------------------
        mypagetriplistview = findViewById(R.id.mypagetriplistview)
        val postList = arrayListOf<MypageTripListItem>()
        val mypagetripadapter = MypageTripListAdapter(this, postList)
        mypagetriplistview.adapter = mypagetripadapter

        // Firestore에서 데이터 가져오기
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.email
            Log.d("UserID", "Current User ID: $userId")
            db.collection("tripdate")
                .whereEqualTo("user_id", userId)
                .get()
                .addOnSuccessListener { result ->
                    postList.clear() // 데이터가 중복되지 않도록 리스트를 초기화
                    for (document in result) {
                        val str_title = document.getString("title") ?: ""
                        val str_location = document.getString("location") ?: ""
                        val long_start_date = document.getLong("start_date") ?: 0L
                        val long_end_date = document.getLong("end_date")

                        val str_date = if (long_end_date != null && long_end_date.toInt() != 0) {
                            "${dateFormat.format(long_start_date)} ~ ${dateFormat.format(long_end_date)}"
                        } else {
                            "${dateFormat.format(long_start_date)}"
                        }

                        val drawableRes = when (str_location) {
                            "가평/양평" -> R.drawable.img_gapyeong_yangpyeong
                            "강릉/속초" -> R.drawable.img_gangneung_sokcho
                            "경주" -> R.drawable.img_gyeongju
                            "부산" -> R.drawable.img_busan
                            "여수" -> R.drawable.img_yeosu
                            "인천" -> R.drawable.img_incheon
                            "전주" -> R.drawable.img_jeonju
                            "제주" -> R.drawable.img_jeju
                            "춘천/홍천" -> R.drawable.img_chuncheon_hongcheon
                            "태안" -> R.drawable.img_taean
                            "통영/거제/남해" -> R.drawable.img_tongyeong_geoje_namhae
                            "포항/안동" -> R.drawable.img_pohang_andong
                            else -> R.color.gray // 기본 이미지 설정
                        }
                        postList.add(MypageTripListItem(ContextCompat.getDrawable(this, drawableRes)!!, str_title, str_date))
                        Log.d("FirestoreData", "Document data: $document")
                    }

                    mypagetripadapter.notifyDataSetChanged()
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Error getting documents: $exception", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
