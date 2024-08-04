package com.guru.travelalone

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.guru.travelalone.adapter.MypagePostListAdapter
import com.guru.travelalone.item.MypagePostListItem
import com.kakao.sdk.user.UserApiClient

class Fragment1 : Fragment() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_1, container, false)
        val mypagepostlistview: ListView = view.findViewById(R.id.mypagepostlistview)
        val mypagepostList = arrayListOf<MypagePostListItem>()
        val mypagepostadapter = MypagePostListAdapter(requireContext(), mypagepostList)
        mypagepostlistview.adapter = mypagepostadapter

        mypagepostlistview.setOnItemClickListener { _, _, position, _ ->
            val selectedItem = mypagepostList[position]
            val postId = selectedItem.postId // MypagePostListItem에 postId가 저장되어 있어야 합니다.
            val formattedPostId = "${auth.currentUser?.uid}_$postId"

            // 상세보기 화면으로 이동
            val intent = Intent(requireContext(), Community_Detail_Activity::class.java)
            println(formattedPostId)
            intent.putExtra("POST_ID", postId)
            startActivity(intent)
        }

        fun handleNoValidPost(userId: String) {
            Log.d("UserID", "No valid documents found for user_id: $userId")
        }

        fun fetchPostFromFirebase(userId: String) {
            db.collection("posts")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener { result ->
                    mypagepostList.clear() // 데이터가 중복되지 않도록 리스트를 초기화
                    for (document in result) {
                        val postId = document.id
                        val str_title = document.getString("title") ?: ""
                        val str_content = document.getString("content") ?: ""
                        val str_image = document.getString("imageUrl") ?: ""
                        val post = MypagePostListItem(postId, str_image, str_title, str_content)
                        mypagepostList.add(post)
                        Log.d("FirestoreData", "Document data: $document")
                    }
                    mypagepostadapter.notifyDataSetChanged()
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(requireContext(), "Error getting documents: $exception", Toast.LENGTH_SHORT).show()
                }
        }

        fun fetchKakaoUserProfileAndPost() {
            UserApiClient.instance.me { user, error ->
                if (error != null) {
                    Log.e("Kakao", "사용자 정보 요청 실패", error)
                    Toast.makeText(requireContext(), "사용자 정보 요청 실패", Toast.LENGTH_SHORT).show()
                } else if (user != null) {
                    val kakaoUserId = user.id.toString()  // Kakao 사용자의 id를 가져옴
                    fetchPostFromFirebase(kakaoUserId)  // Firebase에서 post 가져오기
                }
            }
        }

        fun fetchPost() {
            val currentUser = auth.currentUser

            if (currentUser != null) {
                // Firebase 사용자로부터 데이터 가져오기
                fetchPostFromFirebase(currentUser.uid)
            } else {
                // Kakao 사용자로부터 데이터 가져오기
                fetchKakaoUserProfileAndPost()
            }
        }

        // TripDate 불러오면서 사용자 정보 조회
        fetchPost()

        return view
    }
}
