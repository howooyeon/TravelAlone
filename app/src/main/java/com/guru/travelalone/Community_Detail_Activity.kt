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

        // Initialize Firestore and FirebaseAuth
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        currentUser = auth.currentUser

        // Load user profile and get user ID if necessary
        loadUserProfile()

        // Get post ID from intent
        val postId = intent.getStringExtra("POST_ID")

        // Find views
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

        // Set click listener for back button
        backButton.setOnClickListener {
            finish() // Close the current activity and return to the previous one
        }

        // Load post details
        postId?.let {
            firestore.collection("posts").document(it).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val post = document.toObject(CommunityPostListItem::class.java)
                        post?.let {
                            nameTextView.text = it.nickname
                            dateTextView.text = it.date
                            timeTextView.text = it.createdAt
                            titleTextView.text = it.title
                            contentTextView.text = it.content

                            if (it.imageUrl == "android.resource://com.guru.travelalone/drawable/sample_image_placeholder") {
                                imageView.visibility = View.GONE
                            } else {
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

                            // Hide edit and delete buttons if the current user is not the author
                            if (isKakaoUser) {
                                if (it.nickname == currentUserNickname) {
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
                    }
                }
                .addOnFailureListener { exception ->
                    // Handle the error
                }
        }
        textView2.setOnClickListener {
            postId?.let { id ->
                // Fetch the current post data
                firestore.collection("posts").document(id).get()
                    .addOnSuccessListener { document ->
                        if (document != null) {
                            val post = document.toObject(CommunityPostListItem::class.java)
                            post?.let {
                                if (it.imageUrl == "android.resource://com.guru.travelalone/drawable/sample_image_placeholder") {
                                    // Update the imageUrl to null if it matches the placeholder
                                    updatePostImageUrlToNull(id)
                                } else {
                                    // If imageUrl does not match placeholder, no need to update
                                    openEditPostActivity(id)
                                }
                            }
                        }
                    }
                    .addOnFailureListener { exception ->
                        // Handle the error
                        Log.e("Community_Detail", "Error fetching post: ", exception)
                    }
            }
        }

        deleteButton.setOnClickListener {
            postId?.let { id ->
                // Show confirmation dialog
                showDeleteConfirmationDialog(id)
            }
        }

        // Set click listener for bookmark image view
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

    private fun updatePostImageUrlToNull(postId: String) {
        val postRef = firestore.collection("posts").document(postId)
        postRef.update("imageUrl", null)
            .addOnSuccessListener {
                // Successfully updated the document, now open the edit activity
                openEditPostActivity(postId)
            }
            .addOnFailureListener { exception ->
                // Handle the error
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
                    // Handle the error
                }
        }
    }

    private fun addBookmark(postId: String) {
        val userId = if (isKakaoUser) currentKakaoUserId else currentFirebaseUserId
        userId?.let { id ->
            val bookmarkId = "${id}_$postId"
            val bookmarkRef = firestore.collection("scrap").document(bookmarkId)
            bookmarkRef.set(mapOf("postId" to postId))
                .addOnSuccessListener {
                    // Successfully added the bookmark
                }
                .addOnFailureListener { exception ->
                    // Handle the error
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
                    // Successfully removed the bookmark
                }
                .addOnFailureListener { exception ->
                    // Handle the error
                }
        }
    }

    private fun openEditPostActivity(postId: String) {
        val intent = Intent(this, Community_Write_Activity::class.java)
        intent.putExtra("postId", postId)
        startActivity(intent)
    }

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
                    // Successfully removed the bookmark
                    // Now delete the post
                    firestore.collection("posts").document(postId).delete()
                        .addOnSuccessListener {
                            // Successfully deleted the document
                            finish() // Close the activity
                        }
                        .addOnFailureListener { exception ->
                            // Handle the error
                        }
                }
                .addOnFailureListener { exception ->
                    // Handle the error
                }
        }
    }
}
