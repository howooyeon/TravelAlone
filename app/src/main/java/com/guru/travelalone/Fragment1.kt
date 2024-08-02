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
        // add한 내용은 이 밑에 list에 들어갑니다
        var mypagepostList = arrayListOf<MypagePostListItem>(

        )
        // add
        //mypagepostList.add(MypagePostListItem(ContextCompat.getDrawable(requireContext(), R.drawable.img_gangneung_sokcho)!!, "제목", "본문"),)



        val mypagepostadapter = MypagePostListAdapter(requireContext(), mypagepostList)
        mypagepostlistview.adapter = mypagepostadapter

        // Firestore에서 데이터 가져오기
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            Log.d("UserID", "Current User ID: $userId")
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

        return view
    }
}