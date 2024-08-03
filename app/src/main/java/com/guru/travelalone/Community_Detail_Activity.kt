package com.guru.travelalone

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.guru.travelalone.item.CommunityPostListItem
import com.kakao.sdk.user.UserApiClient

class Community_Detail_Activity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var isBookmarked: Boolean = false
    private var currentUser: FirebaseUser? = null
    private var currentKakaoUserId: String? = null
    private var currentFirebaseUserId: String? = null
    private var isKakaoUser: Boolean = false
    private var currentUserNickname: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_community_detail)

        //  Firestore와 FirebaseAuth 초기화
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        currentUser = auth.currentUser

        // 로그인한 user profile과 userID 가져오기
        loadUserProfile()

        // intent로부터 postID 가져오기
        val postId = intent.getStringExtra("POST_ID")

        // views 관련
        val backButton: ImageButton = findViewById(R.id.back)
        val imageView: ImageView = findViewById(R.id.image)
        val profileImageView: de.hdodenhof.circleimageview.CircleImageView = findViewById(R.id.image_profile)
        val nameTextView: TextView = findViewById(R.id.name)
        val dateTextView: TextView = findViewById(R.id.date)
        val timeTextView: TextView = findViewById(R.id.time)
        val titleTextView: TextView = findViewById(R.id.title)
        val contentTextView: TextView = findViewById(R.id.sub)
        val textView2: TextView = findViewById(R.id.textView2)
        val deleteButton: TextView = findViewById(R.id.textView3)
        val bookmarkImageView: ImageView = findViewById(R.id.bookmark)

        // 뒤로가기 버튼 클릭시
        backButton.setOnClickListener {
            finish() // 이전 화면으로 돌아가기
        }

        // post detail load하기
        postId?.let {
            firestore.collection("posts").document(it).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val post = document.toObject(CommunityPostListItem::class.java)
                        post?.let {
                            nameTextView.text = it.nickname
                            dateTextView.text = it.date
                            timeTextView.text = it.createdAt
                            titleTextView.text = it.title
                            contentTextView.text = it.content

                            // 첨부된 이미지가 없어, sample_image_placeholder값이 저장 되어 있으면, 안 보이게 처리
                            if (it.imageUrl == "android.resource://com.guru.travelalone/drawable/sample_image_placeholder") {
                                imageView.visibility = View.GONE
                            } else { // 첨부된 이미지가 있으면 글라이더를 이용해 가져오기
                                imageView.visibility = View.VISIBLE
                                Glide.with(this)
                                    .load(it.imageUrl)
                                    .placeholder(R.drawable.sample_image_placeholder)
                                    .error(R.drawable.sample_image_placeholder)
                                    .into(imageView)
                            }

                            if (!it.profileImageUrl.isNullOrEmpty()) {
                                Glide.with(this)
                                    .load(it.profileImageUrl)
                                    .placeholder(R.drawable.samplepro)
                                    .error(R.drawable.samplepro)
                                    .into(profileImageView)
                            } else {
                                profileImageView.setImageResource(R.drawable.samplepro)
                            }

                            checkIfBookmarked(postId)

                            // 로그인한 유저와 작성자의 정보가 같은지 판별 후, 수정 삭제 권한 부여
                            if (isKakaoUser) {
                                if (it.userId == currentKakaoUserId) {
                                    textView2.visibility = View.VISIBLE
                                    deleteButton.visibility = View.VISIBLE
                                } else {
                                    textView2.visibility = View.GONE
                                    deleteButton.visibility = View.GONE
                                }
                            } else {
                                if (it.userId == currentUser?.uid) {
                                    textView2.visibility = View.VISIBLE
                                    deleteButton.visibility = View.VISIBLE
                                } else {
                                    textView2.visibility = View.GONE
                                    deleteButton.visibility = View.GONE
                                }
                            }
                        }
                    } else {
                        showPostNotFoundMessage()
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("Community_Detail", "Error fetching post: ", exception)
                    showPostNotFoundMessage()
                }
        } ?: run {
            showPostNotFoundMessage()
        }

        textView2.setOnClickListener {
            postId?.let { id ->
                // post 수정하기
                firestore.collection("posts").document(id).get()
                    .addOnSuccessListener { document ->
                        if (document != null) {
                            val post = document.toObject(CommunityPostListItem::class.java)
                            post?.let {
                                if (it.imageUrl == "android.resource://com.guru.travelalone/drawable/sample_image_placeholder") {
                                    // placeholder 사진 값일 경우, imageUrl 초기화
                                    updatePostImageUrlToNull(id)
                                } else {
                                    // 이미지 존재할 경우, 초기화 미진행
                                    openEditPostActivity(id)
                                }
                            }
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.e("Community_Detail", "Error fetching post: ", exception)
                    }
            }
        }

        // 삭제 버튼 클릭시
        deleteButton.setOnClickListener {
            postId?.let { id ->
                showDeleteConfirmationDialog(id)
            }
        }

        // 북마크 아이콘 클릭시
        bookmarkImageView.setOnClickListener {
            postId?.let { id ->
                if (isKakaoUser || currentUser != null) {
                    isBookmarked = !isBookmarked
                    if (isBookmarked) {
                        bookmarkImageView.setImageResource(R.drawable.scrap)
                        Toast.makeText(this, "게시글이 저장되었습니다.", Toast.LENGTH_SHORT).show()
                        addBookmark(id)
                    } else {
                        bookmarkImageView.setImageResource(R.drawable.bookmark)
                        removeBookmark(id)
                    }
                }
            }
        }
    }

    // 삭제된 게시물 클릭할 경우, 예외처리
    private fun showPostNotFoundMessage() {
        Toast.makeText(this, "존재하지 않는 게시물입니다.", Toast.LENGTH_SHORT).show()
        finish()
    }
    
    private fun updatePostImageUrlToNull(postId: String) {
        val postRef = firestore.collection("posts").document(postId)
        postRef.update("imageUrl", null)
            .addOnSuccessListener {
                openEditPostActivity(postId)
            }
            .addOnFailureListener { exception ->
                Log.e("Community_Detail", "Error updating imageUrl: ", exception)
            }
    }

    private fun loadUserProfile() {
        currentUser?.let { user ->
            // Firebase 로그인 유저 정보 가져오기
            currentFirebaseUserId = user.uid
            firestore.collection("members").document(user.uid).get()
                .addOnSuccessListener { document ->
                    currentUserNickname = document.getString("nickname") // Assuming 'nickname' is stored in user document
                }
        } ?: run {
            // Kakao 로그인 유저 정보 가져오기
            UserApiClient.instance.me { user, error ->
                if (error != null) {
                    Log.e("Kakao", "사용자 정보 요청 실패", error)
                } else if (user != null) {
                    currentKakaoUserId = user.id.toString()
                    isKakaoUser = true
                    currentUserNickname = user.kakaoAccount?.profile?.nickname // Assuming Kakao API provides the nickname
                }
            }
        }
    }

    private fun checkIfBookmarked(postId: String) {
        val userId = if (isKakaoUser) currentKakaoUserId else currentFirebaseUserId
        userId?.let { id ->
            val bookmarkId = "${id}_$postId"
            firestore.collection("scrap").document(bookmarkId).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        isBookmarked = true
                        findViewById<ImageView>(R.id.bookmark).setImageResource(R.drawable.scrap)
                    } else {
                        isBookmarked = false
                        findViewById<ImageView>(R.id.bookmark).setImageResource(R.drawable.bookmark)
                    }
                }
                .addOnFailureListener { exception ->
                }
        }
    }

    private fun addBookmark(postId: String) {
        val userId = if (isKakaoUser) currentKakaoUserId else currentFirebaseUserId
        userId?.let { id ->
            val bookmarkId = "${id}_$postId"
            val bookmarkRef = firestore.collection("scrap").document(bookmarkId)
            bookmarkRef.set(mapOf("userId" to id, "postId" to postId))
                .addOnSuccessListener {
                    // 북마크 성공적으로 추가
                }
                .addOnFailureListener { exception ->
                }
        }
    }

    private fun removeBookmark(postId: String) {
        val userId = if (isKakaoUser) currentKakaoUserId else currentFirebaseUserId
        userId?.let { id ->
            val bookmarkId = "${id}_$postId"
            val bookmarkRef = firestore.collection("scrap").document(bookmarkId)
            bookmarkRef.delete()
                .addOnSuccessListener {
                    // 북마크 성공적으로 추가
                }
                .addOnFailureListener { exception ->
                }
        }
    }

    // 게시글 수정하러 가기
    private fun openEditPostActivity(postId: String) {
        val intent = Intent(this, Community_Write_Activity::class.java)
        intent.putExtra("postId", postId)
        startActivity(intent)
    }

    // 삭제 확인 다이얼로그
    private fun showDeleteConfirmationDialog(postId: String) {
        AlertDialog.Builder(this)
            .setTitle("삭제 확인")
            .setMessage("정말로 이 게시글을 삭제하시겠습니까?")
            .setPositiveButton("확인") { _, _ ->
                deletePost(postId)
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun deletePost(postId: String) {
        val userId = if (isKakaoUser) currentKakaoUserId else currentFirebaseUserId
        userId?.let { id ->
            val bookmarkId = "${id}_$postId"
            firestore.collection("scrap").document(bookmarkId).delete()
                .addOnSuccessListener {
                    // 북마크 제거 성공
                    // post 삭제
                    firestore.collection("posts").document(postId).delete()
                        .addOnSuccessListener {
                            finish()
                        }
                        .addOnFailureListener { exception ->
                        }
                }
                .addOnFailureListener { exception ->
                }
        }
    }
}
