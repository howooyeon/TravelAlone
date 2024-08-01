package com.guru.travelalone

import android.Manifest
import android.content.ContentResolver
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
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
import java.util.UUID

class Community_Write_Activity : AppCompatActivity() {

    private lateinit var imageButton: ImageButton
    private lateinit var selectedImageView: ImageView
    private lateinit var titleEditText: TextInputEditText
    private lateinit var contentEditText: TextInputEditText
    private lateinit var privacySwitch: Switch
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_community_write)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val title = intent.getStringExtra("title")
        val date = intent.getStringExtra("date")
        val location = intent.getStringExtra("location")

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
        privacySwitch = findViewById(R.id.switch2)
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

        privacySwitch.setOnCheckedChangeListener { _, isChecked ->
            privacySwitch.text = if (isChecked) "공개" else "비공개"
        }

        submitButton.setOnClickListener {
            submitPost(date, location)
        }
    }

    private fun fetchUserProfile() {
        if (currentUser != null) {
            val email = currentUser?.email
            FirebaseFirestore.getInstance().collection("members")
                .whereEqualTo("login_id", email)
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        userNickname = document.getString("editnickname") ?: "닉네임 없음"
                        userProfileImageUrl = document.getString("profileImageUrl") ?: ""
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
                    userNickname = user.kakaoAccount?.profile?.nickname ?: "닉네임 없음"
                    // Kakao SDK에는 프로필 이미지 URL을 제공하지 않으므로 별도의 처리 필요
                }
            }
        }
    }

    private fun submitPost(date: String?, location: String?) {
        val title = titleEditText.text.toString()
        val content = contentEditText.text.toString()
        val isPrivate = privacySwitch.isChecked
        var userId: String?
        var userEmail: String?

        if (currentUser != null) {
            userId = currentUser?.uid
            userEmail = currentUser?.email
        } else {
            UserApiClient.instance.me { user, error ->
                if (error != null) {
                    Toast.makeText(this, "Kakao 로그인 실패", Toast.LENGTH_SHORT).show()
                    return@me
                }
                userId = user?.id.toString()
                userEmail = user?.kakaoAccount?.email
                savePostToFirestore(title, content, isPrivate, null, userId, userEmail, date, location, userNickname, userProfileImageUrl)
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
                        savePostToFirestore(title, content, isPrivate, imageUrl, userId, userEmail, date, location, userNickname, userProfileImageUrl)
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "이미지 업로드 실패", Toast.LENGTH_SHORT).show()
                }
        } else {
            savePostToFirestore(title, content, isPrivate, null, userId, userEmail, date, location, userNickname, userProfileImageUrl)
        }
    }

    private fun savePostToFirestore(
        title: String,
        content: String,
        isPrivate: Boolean,
        imageUrl: String?,
        userId: String?,
        userEmail: String?,
        date: String?,
        location: String?,
        nickname: String?,
        profileImageUrl: String?
    ) {
        // Use the placeholder image if imageUrl is null
        val finalImageUrl = imageUrl ?: getUriFromDrawable(R.drawable.sample_image_placeholder).toString()

        val post = hashMapOf(
            "title" to title,
            "content" to content,
            "isPrivate" to isPrivate,
            "imageUrl" to finalImageUrl,
            "timestamp" to System.currentTimeMillis(),
            "userId" to userId,
            "userEmail" to userEmail,
            "date" to date,
            "location" to location,
            "nickname" to nickname,
            "profileImageUrl" to profileImageUrl
        )

        FirebaseFirestore.getInstance().collection("posts")
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
