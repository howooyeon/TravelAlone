package com.guru.travelalone

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Switch
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import coil.load
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.kakao.sdk.user.UserApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.coroutines.resumeWithException

class Community_Write_Activity : AppCompatActivity() {

    private lateinit var imageButton: ImageButton
    private lateinit var cardView: CardView
    private lateinit var selectedImageView: ImageView
    private lateinit var titleEditText: EditText
    private lateinit var contentEditText: EditText
    private lateinit var publicSwitch: Switch
    private lateinit var submitButton: Button
    private var selectedImageUri: Uri? = null

    private lateinit var auth: FirebaseAuth
    private var currentUser: FirebaseUser? = null
    private var userNickname: String? = null
    private var userProfileImageUrl: String? = null

    private val openGalleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            selectedImageView.setImageURI(it)
            cardView.visibility = View.VISIBLE
            selectedImageView.visibility = View.VISIBLE
        }
    }

    private var postId: String? = null
    private var date: String? = null
    private var location: String? = null
    private var existingImageUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_community_write)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        postId = intent.getStringExtra("postId")

        if (postId != null) {
            fetchPostData(postId!!)
        }

        val title = intent.getStringExtra("title")
        date = intent.getStringExtra("date")
        location = intent.getStringExtra("location")

        if (savedInstanceState == null) {
            val fragment = CommunityWriteFragment.newInstance(title, date, location)
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit()
        }

        imageButton = findViewById(R.id.imageButton)
        selectedImageView = findViewById(R.id.selectedImageView)
        titleEditText = findViewById(R.id.titleEditText)
        contentEditText = findViewById(R.id.contentEditText)
        publicSwitch = findViewById(R.id.switch2)
        submitButton = findViewById(R.id.bt_reg)
        cardView = findViewById(R.id.cardView)

        auth = FirebaseAuth.getInstance()
        currentUser = auth.currentUser

        fetchUserProfile()

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

        submitButton.setOnClickListener {
            val title = titleEditText.text.toString()
            val content = contentEditText.text.toString()

            if (title.isBlank() || content.isBlank()) {
                Toast.makeText(this, "제목과 내용을 모두 입력해 주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Toast.makeText(this, "게시글 등록 중, 잠시 기다려주세요.", Toast.LENGTH_SHORT).show()

            submitPost(date, location, existingImageUrl)
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
                Toast.makeText(this, "권한이 거부되었습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchPostData(postId: String) {
        FirebaseFirestore.getInstance().collection("posts").document(postId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    titleEditText.setText(document.getString("title"))
                    contentEditText.setText(document.getString("content"))
                    publicSwitch.isChecked = document.getBoolean("isPublic") ?: false
                    date = document.getString("date")
                    location = document.getString("location")
                    existingImageUrl = document.getString("imageUrl")

                    selectedImageUri = if (existingImageUrl != null && existingImageUrl!!.isNotEmpty()) {
                        Uri.parse(existingImageUrl)
                    } else {
                        null
                    }

                    if (selectedImageUri != null) {
                        selectedImageView.load(selectedImageUri)
                        selectedImageView.visibility = View.VISIBLE
                        cardView.visibility = View.VISIBLE
                    } else {
                        selectedImageView.setImageResource(R.drawable.sample_image_placeholder)
                        cardView.visibility = View.GONE
                    }
                } else {
                    Toast.makeText(this, "게시글을 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "게시글 불러오기 실패", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchUserProfile() {
        if (currentUser != null) {
            val email = currentUser?.email
            FirebaseFirestore.getInstance().collection("members")
                .whereEqualTo("login_id", email)
                .get()
                .addOnSuccessListener { documents ->
                    if (documents.isEmpty) {
                        Toast.makeText(this, "프로필 정보가 존재하지 않습니다.", Toast.LENGTH_SHORT).show()
                    } else {
                        for (document in documents) {
                            userNickname = document.getString("editnickname") ?: "닉네임 없음"
                            userProfileImageUrl = document.getString("profileImageUrl") ?: ""
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "프로필 정보 가져오기 실패", Toast.LENGTH_SHORT).show()
                }
        } else {
            UserApiClient.instance.me { user, error ->
                if (error != null) {
                    Toast.makeText(this, "Kakao 로그인 실패", Toast.LENGTH_SHORT).show()
                } else if (user != null) {
                    val kakaoNickname = user.kakaoAccount?.profile?.nickname
                    FirebaseFirestore.getInstance().collection("members")
                        .whereEqualTo("nickname", kakaoNickname)
                        .get()
                        .addOnSuccessListener { documents ->
                            if (documents.isEmpty) {
                                Toast.makeText(this, "프로필 정보가 존재하지 않습니다.", Toast.LENGTH_SHORT).show()
                            } else {
                                for (document in documents) {
                                    userNickname = document.getString("editnickname") ?: "닉네임 없음"
                                    userProfileImageUrl = document.getString("profileImageUrl") ?: ""
                                }
                            }
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "프로필 정보 가져오기 실패", Toast.LENGTH_SHORT).show()
                        }
                }
            }
        }
    }

    private suspend fun getKakaoUserId(): String? = suspendCancellableCoroutine { continuation ->
        UserApiClient.instance.me { user, error ->
            if (error != null) {
                continuation.resumeWithException(error)
            } else {
                continuation.resume(user?.id.toString()) {
                    // onCancellation 작업 처리
                    UserApiClient.instance.logout { logoutError ->
                        if (logoutError != null) {
                            Log.e("Kakao", "Logout error: $logoutError")
                        }
                    }
                }
            }
        }
    }

    // 기존 submitPost 메서드 내에서 userId 설정
    private fun submitPost(date: String?, location: String?, existingImageUrl: String?) {
        val title = titleEditText.text.toString()
        val content = contentEditText.text.toString()
        val isPublic = publicSwitch.isChecked
        var userId: String = ""
        var userEmail: String = ""

        // Firebase Auth 초기화
        auth = FirebaseAuth.getInstance()
        currentUser = auth.currentUser

        //파이어 베이스 로그인 유저인 경우
        if (currentUser != null) {
            // Firebase 로그인 유저 정보 가져오기
            userId = currentUser?.uid.toString()
            userEmail = currentUser?.email.toString()
            continuePostSubmission(date, location, existingImageUrl, title, content, isPublic, userId, userEmail)
        } else {
            // Kakao 로그인 유저 정보 가져오기
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    userId = getKakaoUserId() ?: ""
                    continuePostSubmission(date, location, existingImageUrl, title, content, isPublic, userId, userEmail)
                } catch (e: Exception) {
                    Log.e("Kakao", "사용자 정보 요청 실패", e)
                    Toast.makeText(this@Community_Write_Activity, "Kakao 사용자 정보를 가져오는 데 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun processPostSubmission(
        title: String,
        content: String,
        isPublic: Boolean,
        imageUri: Uri?,
        userId: String?,
        userEmail: String?,
        date: String?,
        location: String?,
        nickname: String?,
        profileImageUrl: String?,
        currentTime: String,
        existingImageUrl: String?
    ) {
        if (imageUri != null) {
            val storageReference = FirebaseStorage.getInstance().reference.child("images/${UUID.randomUUID()}")
            val uploadTask = storageReference.putFile(imageUri)

            uploadTask.addOnSuccessListener { taskSnapshot ->
                taskSnapshot.storage.downloadUrl.addOnSuccessListener { uri ->
                    savePostToFirestore(title, content, isPublic, uri.toString(), userId, userEmail, date, location, nickname, profileImageUrl, currentTime)
                }
            }.addOnFailureListener {
                savePostToFirestore(title, content, isPublic, existingImageUrl, userId, userEmail, date, location, nickname, profileImageUrl, currentTime)
            }
        } else {
            savePostToFirestore(title, content, isPublic, existingImageUrl, userId, userEmail, date, location, nickname, profileImageUrl, currentTime)
        }
    }

    private fun savePostToFirestore(
        title: String,
        content: String,
        isPublic: Boolean,
        imageUrl: String?,
        userId: String?,
        userEmail: String?,
        date: String?,
        location: String?,
        nickname: String?,
        profileImageUrl: String?,
        currentTime: String
    ) {
        val finalImageUrl = imageUrl ?: "android.resource://com.guru.travelalone/drawable/sample_image_placeholder"

        val postData = hashMapOf(
            "title" to title,
            "content" to content,
            "isPublic" to isPublic,
            "imageUrl" to finalImageUrl,
            "userId" to userId,
            "userEmail" to userEmail,
            "date" to date,
            "location" to location,
            "nickname" to nickname,
            "profileImageUrl" to profileImageUrl,
            "createdAt" to currentTime
        )

        val firestore = FirebaseFirestore.getInstance()
        val collection = firestore.collection("posts")
        val document = if (postId == null) collection.document() else collection.document(postId!!)
        document.set(postData)
            .addOnSuccessListener {
                Toast.makeText(this, "게시글이 저장되었습니다.", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, Community_Activity::class.java)
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "게시글 저장 실패", Toast.LENGTH_SHORT).show()
                Log.e("Community_Write_Activity", "게시글 저장 실패", e)
            }
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 100
    }

    private fun continuePostSubmission(date: String?, location: String?, existingImageUrl: String?, title: String, content: String, isPublic: Boolean, userId: String, userEmail: String) {
        val currentTime = System.currentTimeMillis()
        val sdf = SimpleDateFormat("yy.MM.dd HH:mm", Locale.getDefault())
        val formattedDate = sdf.format(Date(currentTime))

        processPostSubmission(
            title,
            content,
            isPublic,
            selectedImageUri,
            userId,
            userEmail,
            date,
            location,
            userNickname,
            userProfileImageUrl,
            formattedDate,
            existingImageUrl
        )
    }
}



