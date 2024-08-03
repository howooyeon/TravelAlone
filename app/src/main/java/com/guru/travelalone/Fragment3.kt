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

class Fragment3 : Fragment() {

    // Firestore 인스턴스 초기화
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
        val mypagePostScrapAdapter =
            MypagePostScrapListAdapter(requireContext(), mypagePostScrapList)
        mypagePostScrapListView.adapter = mypagePostScrapAdapter

        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            Log.d("UserID", "Current User ID: $userId")
            db.collection("scrap")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener { result ->
                    mypagePostScrapList.clear() // 데이터가 중복되지 않도록 리스트를 초기화
                    val postIds = mutableListOf<String>()
                    for (document in result) {
                        val postId = document.getString("postId") ?: ""
                        if (postId.isNotEmpty()) {
                            postIds.add(postId)
                        }
                    }

                    // 각 postId에 대해 포스트 데이터 가져오기
                    fetchPostsFromIds(postIds, mypagePostScrapList, mypagePostScrapAdapter)
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(
                        requireContext(),
                        "Error getting documents: $exception",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }

        return view
    }

    private fun fetchPostsFromIds(
        postIds: List<String>,
        mypagePostScrapList: ArrayList<MypagePostScrapListItem>,
        mypagePostScrapAdapter: MypagePostScrapListAdapter
    ) {
        if (postIds.isEmpty()) {
            return
        }

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

                mypagePostScrapAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(
                    requireContext(),
                    "Error getting documents: $exception",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }
}
