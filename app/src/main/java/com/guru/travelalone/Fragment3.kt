package com.guru.travelalone

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.guru.travelalone.adapter.MypagePostScrapListAdapter
import com.guru.travelalone.item.MypagePostScrapListItem
import com.kakao.sdk.user.UserApiClient

class Fragment3 : Fragment() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_3, container, false)

        val mypagePostScrapListView: ListView = view.findViewById(R.id.mypagescraplistview)
        val mypagePostScrapList = arrayListOf<MypagePostScrapListItem>()
        val mypagePostScrapAdapter = MypagePostScrapListAdapter(requireContext(), mypagePostScrapList)
        mypagePostScrapListView.adapter = mypagePostScrapAdapter

        val currentUser = auth.currentUser
        if (currentUser != null) {
            Log.d("UserCheck", "Firebase user detected")
            loadFirebaseUserData(currentUser.uid, mypagePostScrapList, mypagePostScrapAdapter)
        } else {
            Log.d("UserCheck", "Checking Kakao user")
            UserApiClient.instance.me { user, error ->
                if (error != null) {
                    Log.e("Kakao", "사용자 정보 요청 실패", error)
                    Toast.makeText(requireContext(), "사용자 정보 요청 실패", Toast.LENGTH_SHORT).show()
                } else if (user != null) {
                    val kakaoUserId = user.id.toString()
                    Log.d("UserCheck", "Kakao user detected with ID: $kakaoUserId")
                    loadKakaoUserData(kakaoUserId, mypagePostScrapList, mypagePostScrapAdapter)
                }
            }
        }

        return view
    }

    private fun loadFirebaseUserData(
        userId: String,
        mypagePostScrapList: ArrayList<MypagePostScrapListItem>,
        mypagePostScrapAdapter: MypagePostScrapListAdapter
    ) {
        Log.d("Firestore", "Loading data for Firebase user ID: $userId")
        db.collection("scrap")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { result ->
                mypagePostScrapList.clear()
                val postIds = mutableListOf<String>()
                for (document in result) {
                    val postId = document.getString("postId") ?: ""
                    if (postId.isNotEmpty()) {
                        postIds.add(postId)
                    }
                }
                Log.d("Firestore", "Post IDs: $postIds")
                fetchPostsFromIds(postIds, mypagePostScrapList, mypagePostScrapAdapter)
            }
            .addOnFailureListener { exception ->
                Log.e("Firestore", "Error getting documents", exception)
                Toast.makeText(requireContext(), "Error getting documents: $exception", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadKakaoUserData(
        kakaoUserId: String,
        mypagePostScrapList: ArrayList<MypagePostScrapListItem>,
        mypagePostScrapAdapter: MypagePostScrapListAdapter
    ) {
        Log.d("Firestore", "Loading data for Kakao user ID: $kakaoUserId")
        db.collection("scrap")
            .whereEqualTo("userId", kakaoUserId)
            .get()
            .addOnSuccessListener { result ->
                mypagePostScrapList.clear()
                val postIds = mutableListOf<String>()
                for (document in result) {
                    val postId = document.getString("postId") ?: ""
                    if (postId.isNotEmpty()) {
                        postIds.add(postId)
                    }
                }
                Log.d("Firestore", "Post IDs: $postIds")
                fetchPostsFromIds(postIds, mypagePostScrapList, mypagePostScrapAdapter)
            }
            .addOnFailureListener { exception ->
                Log.e("Firestore", "Error getting documents", exception)
                Toast.makeText(requireContext(), "Error getting documents: $exception", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchPostsFromIds(
        postIds: List<String>,
        mypagePostScrapList: ArrayList<MypagePostScrapListItem>,
        mypagePostScrapAdapter: MypagePostScrapListAdapter
    ) {
        if (postIds.isEmpty()) {
            Log.d("Firestore", "No post IDs found")
            return
        }

        Log.d("Firestore", "Fetching posts with IDs: $postIds")
        db.collection("posts")
            .whereIn(FieldPath.documentId(), postIds)
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val str_title = document.getString("title") ?: ""
                    val str_content = document.getString("content") ?: ""
                    val str_image = document.getString("imageUrl") ?: ""
                    mypagePostScrapList.add(MypagePostScrapListItem(str_image, str_title, str_content))
                }
                Log.d("Firestore", "Fetched posts: $mypagePostScrapList")
                mypagePostScrapAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Log.e("Firestore", "Error fetching posts", exception)
                Toast.makeText(requireContext(), "Error getting documents: $exception", Toast.LENGTH_SHORT).show()
            }
    }
}
