package com.guru.travelalone

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
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

class Community_Detail_Activity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var isBookmarked: Boolean = false
    private var currentUser: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_community_detail)

        // Initialize Firestore and FirebaseAuth
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        currentUser = auth.currentUser

        // Get post ID from intent
        val postId = intent.getStringExtra("POST_ID")

        // Find views
        val imageView: ImageView = findViewById(R.id.image)
        val profileImageView: de.hdodenhof.circleimageview.CircleImageView = findViewById(R.id.image_profile)
        val nameTextView: TextView = findViewById(R.id.name)
        val dateTextView: TextView = findViewById(R.id.date)
        val timeTextView: TextView = findViewById(R.id.time)
        val titleTextView: TextView = findViewById(R.id.title)
        val contentTextView: TextView = findViewById(R.id.sub)
        val textView2: TextView = findViewById(R.id.textView2)
        val deleteButton: TextView = findViewById(R.id.textView3) // Assuming there's a delete button in the layout
        val bookmarkImageView: ImageView = findViewById(R.id.bookmark)

        // Load post details
        postId?.let {
            firestore.collection("posts").document(it).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val post = document.toObject(CommunityPostListItem::class.java)
                        post?.let {
                            // Set the data to the views
                            nameTextView.text = it.nickname
                            dateTextView.text = it.date
                            timeTextView.text = it.createdAt // Use createdAt for the time field
                            titleTextView.text = it.title
                            contentTextView.text = it.content

                            if (!it.imageUrl.isNullOrEmpty()) {
                                Glide.with(this)
                                    .load(it.imageUrl)
                                    .placeholder(R.drawable.sample_image_placeholder)
                                    .error(R.drawable.sample_image_placeholder)
                                    .into(imageView)
                            } else {
                                imageView.setImageResource(R.color.gray) // or set visibility to GONE
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

                            // Check if the post is already bookmarked
                            checkIfBookmarked(postId)
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    // Handle the error
                }
        }

        textView2.setOnClickListener {
            postId?.let { id ->
                // Update the post to set image URLs to null
                updatePostImagesToNull(id)

                // Start the edit activity
                openEditPostActivity(id)
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
                currentUser?.let { user ->
                    isBookmarked = !isBookmarked
                    if (isBookmarked) {
                        bookmarkImageView.setImageResource(R.drawable.scrap)
                        Toast.makeText(this, "게시글이 저장되었습니다.", Toast.LENGTH_SHORT).show()
                        addBookmark(user.uid, id)
                    } else {
                        bookmarkImageView.setImageResource(R.drawable.bookmark)
                        removeBookmark(user.uid, id)
                    }
                }
            }
        }
    }

    private fun checkIfBookmarked(postId: String) {
        currentUser?.let { user ->
            val bookmarkId = "${user.uid}_$postId"
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

    private fun addBookmark(userId: String, postId: String) {
        val bookmarkId = "${userId}_$postId"
        val bookmarkRef = firestore.collection("scrap").document(bookmarkId)
        bookmarkRef.set(mapOf("postId" to postId))
            .addOnSuccessListener {
                // Successfully added the bookmark
            }
            .addOnFailureListener { exception ->
                // Handle the error
            }
    }

    private fun removeBookmark(userId: String, postId: String) {
        val bookmarkId = "${userId}_$postId"
        val bookmarkRef = firestore.collection("scrap").document(bookmarkId)
        bookmarkRef.delete()
            .addOnSuccessListener {
                // Successfully removed the bookmark
            }
            .addOnFailureListener { exception ->
                // Handle the error
            }
    }

    private fun openEditPostActivity(postId: String) {
        val intent = Intent(this, Community_Write_Activity::class.java)
        intent.putExtra("postId", postId)
        startActivity(intent)
    }

    private fun updatePostImagesToNull(postId: String) {
        val postRef = firestore.collection("posts").document(postId)
        postRef.update(
            mapOf(
                "imageUrl" to null,
            )
        ).addOnSuccessListener {
            // Successfully updated the document
        }.addOnFailureListener { exception ->
            // Handle the error
        }
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
        // First, delete bookmarks related to this post
        currentUser?.let { user ->
            val bookmarkId = "${user.uid}_$postId"
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
