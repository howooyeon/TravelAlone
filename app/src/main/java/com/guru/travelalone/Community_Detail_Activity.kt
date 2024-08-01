package com.guru.travelalone

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.guru.travelalone.item.CommunityPostListItem

class Community_Detail_Activity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_community_detail)

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance()

        // Get post ID from intent
        val postId = intent.getStringExtra("POST_ID")

        // Find views
        val imageView: ImageView = findViewById(R.id.image)
        val profileImageView: de.hdodenhof.circleimageview.CircleImageView =
            findViewById(R.id.image_profile)
        val nameTextView: TextView = findViewById(R.id.name)
        val dateTextView: TextView = findViewById(R.id.date)
        val timeTextView: TextView = findViewById(R.id.time)
        val titleTextView: TextView = findViewById(R.id.title)
        val contentTextView: TextView = findViewById(R.id.sub)
        val textView2: TextView = findViewById(R.id.textView2)

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
}
