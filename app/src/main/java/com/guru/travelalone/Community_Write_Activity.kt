package com.guru.travelalone

import android.Manifest
import android.content.ContentResolver
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Switch
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.kakao.sdk.user.UserApiClient
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.UUID

class Community_Write_Activity : AppCompatActivity() {

    private lateinit var imageButton: ImageButton
    private lateinit var selectedImageView: ImageView
    private lateinit var titleEditText: TextInputEditText
    private lateinit var contentEditText: TextInputEditText
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
            selectedImageView.visibility = View.VISIBLE
            imageButton.visibility = View.GONE
        }
    }

    private var postId: String? = null
    private var date: String? = null
    private var location: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_community_write)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Intent에서 데이터 받기
        postId = intent.getStringExtra("postId") // 게시글 ID를 받아옵니다.

        // 기존 게시글 데이터를 불러옵니다.
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

        auth = FirebaseAuth.getInstance()
        currentUser = auth.currentUser

        // 닉네임과 프로필 이미지 URL을 가져오는 메서드 호출
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

        publicSwitch.setOnCheckedChangeListener { _, isChecked ->
            publicSwitch.text = if (isChecked) "공개" else "비공개"
        }

        submitButton.setOnClickListener {
            submitPost(date, location)
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
                    val imageUrl = document.getString("imageUrl")
                    if (imageUrl != null && imageUrl != getUriFromDrawable(R.drawable.sample_image_placeholder).toString()) {
                        selectedImageView.visibility = View.VISIBLE
                        imageButton.visibility = View.GONE
                        selectedImageView.setImageURI(Uri.parse(imageUrl))
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
            // Handle general login members
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
            // Handle Kakao login members
            UserApiClient.instance.me { user, error ->
                if (error != null) {
                    Toast.makeText(this, "Kakao 로그인 실패", Toast.LENGTH_SHORT).show()
                } else if (user != null) {
                    val kakaoNickname = user.kakaoAccount?.profile?.nickname
                    FirebaseFirestore.getInstance().collection("members")
                        .whereEqualTo("editnickname", kakaoNickname)
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

    private fun submitPost(date: String?, location: String?) {
        val title = titleEditText.text.toString()
        val content = contentEditText.text.toString()
        val isPublic = publicSwitch.isChecked
        var userId: String? = currentUser?.uid
        var userEmail: String? = currentUser?.email

        if (userId == null) {
            UserApiClient.instance.me { user, error ->
                if (error != null) {
                    Toast.makeText(this, "Kakao 로그인 실패", Toast.LENGTH_SHORT).show()
                    return@me
                }
                userId = user?.id.toString()
                userEmail = user?.kakaoAccount?.email
                savePostToFirestore(title, content, isPublic, selectedImageUri?.toString(), userId, userEmail, date, location, userNickname, userProfileImageUrl)
            }
            return
        }

        Toast.makeText(this, "게시글 등록 중, 잠시 기다려주세요.", Toast.LENGTH_SHORT).show()

        if (selectedImageUri != null) {
            val storageReference = FirebaseStorage.getInstance().reference.child("images/${UUID.randomUUID()}")
            storageReference.putFile(selectedImageUri!!)
                .addOnSuccessListener { taskSnapshot ->
                    taskSnapshot.metadata?.reference?.downloadUrl?.addOnSuccessListener { uri ->
                        val imageUrl = uri.toString()
                        savePostToFirestore(title, content, isPublic, imageUrl, userId, userEmail, date, location, userNickname, userProfileImageUrl)
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "이미지 업로드 실패", Toast.LENGTH_SHORT).show()
                }
        } else {
            savePostToFirestore(title, content, isPublic, null, userId, userEmail, date, location, userNickname, userProfileImageUrl)
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
        profileImageUrl: String?
    ) {
        val finalImageUrl = imageUrl ?: getUriFromDrawable(R.drawable.sample_image_placeholder).toString()

        val dateFormat = SimpleDateFormat("yy.MM.dd HH:mm", Locale.getDefault())
        val currentDateTime = dateFormat.format(System.currentTimeMillis())

        val post = hashMapOf(
            "title" to title,
            "content" to content,
            "isPublic" to isPublic,
            "imageUrl" to finalImageUrl,
            "timestamp" to System.currentTimeMillis(),
            "userId" to userId,
            "userEmail" to userEmail,
            "date" to date,
            "location" to location,
            "nickname" to nickname,
            "profileImageUrl" to profileImageUrl,
            "createdAt" to currentDateTime
        )

        val firestore = FirebaseFirestore.getInstance()
        if (postId != null) {
            firestore.collection("posts").document(postId!!)
                .set(post)
                .addOnSuccessListener {
                    Toast.makeText(this, "게시글이 수정되었습니다.", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, Community_Activity::class.java)
                    startActivity(intent)
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "게시글 수정 실패", Toast.LENGTH_SHORT).show()
                }
        } else {
            firestore.collection("posts")
                .add(post)
                .addOnSuccessListener {
                    Toast.makeText(this, "게시글이 등록되었습니다.", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, Community_Activity::class.java)
                    startActivity(intent)
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "게시글 등록 실패", Toast.LENGTH_SHORT).show()
                }
        }

        //        val postId = FirebaseFirestore.getInstance().collection("posts").document().id
//        FirebaseFirestore.getInstance().collection("posts").document(postId)
//            .set(post)
//            .addOnSuccessListener {
//                Toast.makeText(this, "게시글이 성공적으로 저장되었습니다.", Toast.LENGTH_SHORT).show()
//                finish()
//            }
//            .addOnFailureListener {
//                Toast.makeText(this, "게시글 저장 실패", Toast.LENGTH_SHORT).show()
//            }
    }

    private fun getUriFromDrawable(drawableId: Int): Uri {
        val resources = resources
        return Uri.parse(
            ContentResolver.SCHEME_ANDROID_RESOURCE + "://" +
                    resources.getResourcePackageName(drawableId) + '/' +
                    resources.getResourceTypeName(drawableId) + '/' +
                    resources.getResourceEntryName(drawableId))
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
