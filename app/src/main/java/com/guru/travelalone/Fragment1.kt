package com.guru.travelalone

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.guru.travelalone.adapter.MypagePostListAdapter
import com.guru.travelalone.item.MypagePostListItem
import com.guru.travelalone.item.MypageTripListItem
import com.kakao.sdk.user.UserApiClient

class Fragment1 : Fragment() {
    // Firestore 인스턴스 초기화
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_1, container, false)
        var mypagepostlistview : ListView = view.findViewById(R.id.mypagepostlistview)
        var mypagepostList = arrayListOf<MypagePostListItem>()
        val mypagepostadapter = MypagePostListAdapter(requireContext(), mypagepostList)
        mypagepostlistview.adapter = mypagepostadapter


        fun handleNoValidPost(userId: String) {
            Log.d("UserID", "No valid documents found for user_id: $userId")
        }

        fun fetchPostFromFirebase(userId : String) {
            db.collection("posts")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener { result ->
                    mypagepostList.clear() // 데이터가 중복되지 않도록 리스트를 초기화
                    for (document in result) {
                        val str_title = document.getString("title") ?: ""
                        val str_content = document.getString("content") ?: ""
                        val str_image = document.getString("imageUrl") ?: ""
                        if(str_image == "android.resource://com.guru.travelalone/drawable/sample_image_placeholder")
                        {
                            // str_image 가 흰색 사진이도록 해야함
                            mypagepostList.add(MypagePostListItem(str_image, str_title, str_content))
                        }
                        else
                        {
                            mypagepostList.add(MypagePostListItem(str_image, str_title, str_content),)
                        }

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
                    val kakaoNickname = user.kakaoAccount?.profile?.nickname ?: ""
                    db.collection("members")
                        .whereEqualTo("nickname", kakaoNickname)
                        .get()
                        .addOnSuccessListener { documents ->
                            if (documents != null && !documents.isEmpty) {
                                val email = documents.firstOrNull()?.getString("login_id")
                                if (email != null) {
                                    fetchPostFromFirebase(email)  // Firebase에서 tripdate 가져오기
                                } else {
                                    handleNoValidPost("Kakao")  // 이메일이 없는 경우 처리
                                }
                            } else {
                                handleNoValidPost("Kakao")  // 사용자 정보가 없는 경우 처리
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.w("Kakao", "문서 가져오기 실패: ", e)
                            handleNoValidPost("Kakao")
                        }
                }
            }
        }

        fun fetchPost() {
            val auth = FirebaseAuth.getInstance()
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