package com.guru.travelalone

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.guru.travelalone.adapter.MypageTripListAdapter
import com.guru.travelalone.item.MypageTripListItem
import com.kakao.sdk.user.UserApiClient
import java.text.SimpleDateFormat
import java.util.Locale

class Fragment2 : Fragment() {
    // Firestore 인스턴스 초기화
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()

    // 데이터 포맷
    private val dateFormat = SimpleDateFormat("yyyy.MM.dd", Locale.KOREAN)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_2, container, false)
        val mypagetriplistview: ListView = view.findViewById(R.id.mypagetriplistview)
        val postList = arrayListOf<MypageTripListItem>()
        val tripIds = mutableListOf<String>()
        val mypagetripadapter = MypageTripListAdapter(requireContext(), postList)
        mypagetriplistview.adapter = mypagetripadapter

        fun handleNoValidTripDate(userId: String) {
            Log.d("UserID", "No valid documents found for user_id: $userId")
        }

        fun handleTripDateDocuments(userId: String) {
            db.collection("tripdate")
                .whereEqualTo("user_id", userId)
                .get()
                .addOnSuccessListener { result ->
                    postList.clear() // 데이터가 중복되지 않도록 리스트를 초기화
                    for (document in result) {
                        val str_id = document.id
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
                            "서울" -> R.drawable.img_seoul
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
                        postList.add(MypageTripListItem(ContextCompat.getDrawable(requireContext(), drawableRes)!!, str_title, str_date, str_location))
                        tripIds.add(document.id)
                        Log.d("FirestoreData", "Document data: $document")
                    }
                    mypagetripadapter.notifyDataSetChanged()
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(requireContext(), "Error getting documents: $exception", Toast.LENGTH_SHORT).show()
                }
        }

        fun fetchTripDateFromFirebase(userId: String) {
            Log.d("UserID", "Current User ID: $userId")
            db.collection("tripdate")
                .whereEqualTo("user_id", userId)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val documents = task.result
                        if (documents != null && !documents.isEmpty) {
                            handleTripDateDocuments(userId)
                        } else {
                            handleNoValidTripDate(userId)
                        }
                    } else {
                        Log.d("UserID", "Error getting documents: ", task.exception)
                        handleNoValidTripDate(userId)
                    }
                }
        }

        fun fetchKakaoUserProfileAndTripDate() {
            UserApiClient.instance.me { user, error ->
                if (error != null) {
                    Log.e("Kakao", "사용자 정보 요청 실패", error)
                    Toast.makeText(requireContext(), "사용자 정보 요청 실패", Toast.LENGTH_SHORT).show()
                } else if (user != null) {
                    val kakaoUserId = user.id.toString()  // Kakao 사용자의 id를 가져옴
                    fetchTripDateFromFirebase(kakaoUserId)  // Firebase에서 tripdate 가져오기
                }
            }
        }

        fun fetchTripDate() {
            val auth = FirebaseAuth.getInstance()
            val currentUser = auth.currentUser

            if (currentUser != null) {
                // Firebase 사용자로부터 데이터 가져오기
                fetchTripDateFromFirebase(currentUser.uid)
            } else {
                // Kakao 사용자로부터 데이터 가져오기
                fetchKakaoUserProfileAndTripDate()
            }
        }

        fun showDeleteConfirmationDialog(position: Int) {
            val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_delete_confirmation, null)

            val alertDialog = AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create()

            dialogView.findViewById<Button>(R.id.btnCancel).setOnClickListener {
                alertDialog.dismiss()
            }

            dialogView.findViewById<Button>(R.id.btnDelete).setOnClickListener {
                val selectedTripId = tripIds[position]
                // Firestore에서 항목 삭제
                db.collection("tripdate").document(selectedTripId)
                    .delete()
                    .addOnSuccessListener {
                        tripIds.removeAt(position)
                        postList.removeAt(position)
                        mypagetripadapter.notifyDataSetChanged()
                        Toast.makeText(requireContext(), "일정이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                        alertDialog.dismiss()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(requireContext(), "Error deleting document: $e", Toast.LENGTH_SHORT).show()
                        alertDialog.dismiss()
                    }
            }

            alertDialog.show()
        }

        // 아이템 클릭 리스너 설정
        mypagetriplistview.setOnItemClickListener { _, _, position, _ ->
            // 클릭된 아이템 삭제
            showDeleteConfirmationDialog(position)
        }

        // TripDate 불러오면서 사용자 정보 조회
        fetchTripDate()

        val bt_trip_add: Button = view.findViewById(R.id.bt_add)
        bt_trip_add.setOnClickListener {
            val intent = Intent(requireContext(), TripDate_Activity::class.java)
            startActivity(intent)
        }

        return view
    }
}
