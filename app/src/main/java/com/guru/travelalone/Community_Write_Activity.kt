package com.guru.travelalone

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class Community_Write_Activity : AppCompatActivity() {

    private lateinit var imageButton: ImageButton
    private lateinit var selectedImageView: ImageView
    private lateinit var titleEditText: TextInputEditText
    private lateinit var contentEditText: TextInputEditText
    private lateinit var privacySwitch: Switch
    private lateinit var submitButton: Button
    private var selectedImageUri: Uri? = null

    private val openGalleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            selectedImageView.setImageURI(it)
            selectedImageView.visibility = View.VISIBLE
            imageButton.visibility = View.GONE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_community_write)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        imageButton = findViewById(R.id.imageButton)
        selectedImageView = findViewById(R.id.selectedImageView)
        titleEditText = findViewById(R.id.titleEditText)
        contentEditText = findViewById(R.id.contentEditText)
        privacySwitch = findViewById(R.id.switch2)
        submitButton = findViewById(R.id.bt_reg)

        imageButton.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    PERMISSION_REQUEST_CODE
                )
            } else {
                openGalleryLauncher.launch("image/*")
            }
        }

        privacySwitch.setOnCheckedChangeListener { _, isChecked ->
            privacySwitch.text = if (isChecked) "공개" else "비공개"
        }

        submitButton.setOnClickListener {
            submitPost()
        }
    }

    private fun submitPost() {
        val title = titleEditText.text.toString()
        val content = contentEditText.text.toString()
        val isPrivate = privacySwitch.isChecked

        Toast.makeText(this, "게시글 등록 중, 잠시 기다려주세요.", Toast.LENGTH_SHORT).show()

        if (selectedImageUri != null) {
            val storageReference = FirebaseStorage.getInstance().reference.child("images/${UUID.randomUUID()}")
            storageReference.putFile(selectedImageUri!!)
                .addOnSuccessListener { taskSnapshot ->
                    taskSnapshot.metadata?.reference?.downloadUrl?.addOnSuccessListener { uri ->
                        val imageUrl = uri.toString()
                        savePostToFirestore(title, content, isPrivate, imageUrl)
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "이미지 업로드 실패", Toast.LENGTH_SHORT).show()
                }
        } else {
            savePostToFirestore(title, content, isPrivate, null)
        }
    }
    private fun savePostToFirestore(title: String, content: String, isPrivate: Boolean, imageUrl: String?) {
        val post = hashMapOf(
            "title" to title,
            "content" to content,
            "isPrivate" to isPrivate,
            "imageUrl" to imageUrl,
            "timestamp" to System.currentTimeMillis()
        )

        FirebaseFirestore.getInstance().collection("posts")
            .add(post)
            .addOnSuccessListener {
                Toast.makeText(this, "게시글이 등록되었습니다.", Toast.LENGTH_SHORT).show()
                finish() // Activity를 종료하고 이전 화면으로 돌아갑니다.
            }
            .addOnFailureListener {
                Toast.makeText(this, "게시글 등록 실패", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGalleryLauncher.launch("image/*")
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 2
    }
}
