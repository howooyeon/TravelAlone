package com.guru.travelalone

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.guru.travelalone.databinding.ActivityMyProEditBinding
import com.kakao.sdk.user.UserApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File

class MyProEdit_Activity : AppCompatActivity() {
    private val binding by lazy { ActivityMyProEditBinding.inflate(layoutInflater) }

    private val storage = FirebaseStorage.getInstance()
    private val storageRef = storage.reference

    private lateinit var auth: FirebaseAuth
    private var currentUser: FirebaseUser? = null
    private val db = FirebaseFirestore.getInstance()

    private var imageUri: Uri? = null

    // 갤러리 접근 권한 요청 런처
    private val requestPermissionLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                openGallery()
            } else {
                Toast.makeText(this, "갤러리 접근 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
            }
        }

    // 갤러리에서 사진 가져오기 런처
    private val pickImageLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data: Intent? = result.data
                data?.data?.let {
                    imageUri = it
                    binding.kakaoPro.setImageURI(imageUri)
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        currentUser = auth.currentUser

        loadUserProfile()

        // 프로필 사진 변경 버튼 클릭 리스너
        binding.plusBtn.setOnClickListener {
            checkPermission()

            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                openGallery()
            } else {
                requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }

        // 프로필 저장 버튼 클릭 리스너
        binding.saveButton.setOnClickListener {
            if (currentUser != null) {
                val email = currentUser?.email
                db.collection("members")
                    .whereEqualTo("login_id", email)
                    .get()
                    .addOnSuccessListener { result ->
                        for (document in result) {
                            val documentId = document.id
                            CoroutineScope(Dispatchers.Main).launch {
                                try {
                                    normal_edit_save(documentId)
                                } catch (e: Exception) {
                                    Log.e("MyProEdit_Activity", "프로필 저장 중 오류", e)
                                    Toast.makeText(
                                        this@MyProEdit_Activity,
                                        "프로필 저장 중 오류가 발생했습니다.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    }
            } else {
                UserApiClient.instance.me { user, error ->
                    if (error != null) {
                        Log.e("Kakao", "사용자 정보 요청 실패", error)
                    } else if (user != null) {
                        val kakaoNickname = user.kakaoAccount?.profile?.nickname ?: ""
                        Log.d("MyProEdit_Activity", "Kakao 로그인 유저 닉네임: $kakaoNickname")

                        db.collection("members")
                            .whereEqualTo("nickname", kakaoNickname)
                            .get()
                            .addOnSuccessListener { result ->
                                for (document in result) {
                                    val documentId = document.id
                                    CoroutineScope(Dispatchers.Main).launch {
                                        try {
                                            kakao_edit_save(documentId, kakaoNickname)
                                        } catch (e: Exception) {
                                            Log.e("MyProEdit_Activity", "프로필 저장 중 오류", e)
                                            Toast.makeText(
                                                this@MyProEdit_Activity,
                                                "프로필 저장 중 오류가 발생했습니다.",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                }
                            }
                    }
                }
            }
        }


        //뒤로 가기 버튼 클릭 리스너
        binding.back.setOnClickListener {
            val intent = Intent(this@MyProEdit_Activity, Mypage_Activity::class.java)
            startActivity(intent)
            finish()
        }
    }

    // 유저 프로필 정보 로드
    private fun loadUserProfile() {
        if (currentUser != null) {
            val email = currentUser?.email
            Log.d("MyProEdit_Activity", "Firebase 로그인 유저 이메일: $email")
            db.collection("members")
                .whereEqualTo("login_id", email)
                .get()
                .addOnSuccessListener { result ->
                    for (document in result) {
                        val profileImageUrl = document.getString("profileImageUrl")
                        val editNickname = document.getString("editnickname")
                        val introduce = document.getString("introduce")
                        if (profileImageUrl != null) {
                            Glide.with(this)
                                .load(profileImageUrl)
                                .into(binding.kakaoPro)
                        }
                        binding.txtNickName.setText(editNickname)
                        binding.Introduce.setText(introduce)
                    }
                }
                .addOnFailureListener { exception ->
                    Log.w("Firestore", "문서 가져오기 오류: ", exception)
                }
        } else {
            UserApiClient.instance.me { user, error ->
                if (error != null) {
                    Log.e("Kakao", "사용자 정보 요청 실패", error)
                } else if (user != null) {
                    val kakaoNickname = user.kakaoAccount?.profile?.nickname ?: ""
                    Log.d("MyProEdit_Activity", "Kakao 로그인 유저 닉네임: $kakaoNickname")
                    db.collection("members")
                        .whereEqualTo("nickname", kakaoNickname)
                        .get()
                        .addOnSuccessListener { result ->
                            for (document in result) {
                                val editNickname = document.getString("editnickname")
                                val introduce = document.getString("introduce")
                                val kakaoProfileImageUrl = document.getString("profileImageUrl")
                                if (kakaoProfileImageUrl != null) {
                                    Glide.with(this)
                                        .load(kakaoProfileImageUrl)
                                        .into(binding.kakaoPro)
                                }
                                binding.txtNickName.setText(editNickname)
                                binding.Introduce.setText(introduce)
                            }
                        }
                        .addOnFailureListener { exception ->
                            Log.w("Firestore", "문서 가져오기 오류: ", exception)
                        }
                }
            }
        }
    }

    // 갤러리 열기
    private fun openGallery() {
        val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
        pickImageLauncher.launch(gallery)
    }

    // 이미지 스토리지에 업로드
    private suspend fun uploadImageToStorage(uri: Uri): String {
        return withContext(Dispatchers.IO) {
            val file = Uri.fromFile(File(uri.path))
            val riversRef = storageRef.child("images/${file.lastPathSegment}")
            riversRef.putFile(uri).await()
            riversRef.downloadUrl.await().toString()
        }
    }

    // 비트맵 이미지를 스토리지에 업로드
    private suspend fun uploadBitmapToStorage(bitmap: Bitmap): String {
        return withContext(Dispatchers.IO) {
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val data = baos.toByteArray()
            val tempFile = storageRef.child("images/${System.currentTimeMillis()}.jpg")
            tempFile.putBytes(data).await()
            tempFile.downloadUrl.await().toString()
        }
    }

    // 일반 유저 프로필 저장
    private suspend fun normal_edit_save(documentId: String) {
        val editnickname = binding.txtNickName.text.toString().trim()
        val memberIntroduce = binding.Introduce.text.toString().trim()
        val loginId = currentUser?.email.toString()
        if (editnickname.isNotEmpty() && memberIntroduce.isNotEmpty()) {
            Toast.makeText(this@MyProEdit_Activity, "프로필 등록 중, 잠시 기다려주세요.", Toast.LENGTH_SHORT)
                .show()
            val profileImageUrl = if (imageUri != null) {
                uploadImageToStorage(imageUri!!)
            } else {
                val drawable = binding.kakaoPro.drawable
                if (drawable is BitmapDrawable) {
                    uploadBitmapToStorage(drawable.bitmap)
                } else {
                    ""
                }
            }
            saveUserProfile(documentId, editnickname, memberIntroduce, loginId, profileImageUrl)
            val intent = Intent(this@MyProEdit_Activity, Mypage_Activity::class.java)
            startActivity(intent)
        } else {
            Toast.makeText(this, "정보를 다 입력해주세요", Toast.LENGTH_SHORT).show()
        }
    }

    // 카카오 유저 프로필 저장
    private suspend fun kakao_edit_save(documentId: String, kakaoNickname: String) {
        val editnickname = binding.txtNickName.text.toString().trim()
        val memberIntroduce = binding.Introduce.text.toString().trim()
        val nickname = kakaoNickname

        if (editnickname.isNotEmpty() && memberIntroduce.isNotEmpty()) {
            Toast.makeText(this@MyProEdit_Activity, "프로필 등록 중, 잠시 기다려주세요.", Toast.LENGTH_SHORT)
                .show()
            val profileImageUrl = if (imageUri != null) {
                uploadImageToStorage(imageUri!!)
            } else {
                val drawable = binding.kakaoPro.drawable
                if (drawable is BitmapDrawable) {
                    uploadBitmapToStorage(drawable.bitmap)
                } else {
                    ""
                }
            }
            kakaosaveUserProfile(
                documentId,
                nickname,
                editnickname,
                memberIntroduce,
                profileImageUrl
            )
            val intent = Intent(this@MyProEdit_Activity, Mypage_Activity::class.java)
            startActivity(intent)
        } else {
            Toast.makeText(this, "정보를 다 입력해주세요", Toast.LENGTH_SHORT).show()
        }
    }

    // Firebase Firestore에 일반 유저 프로필 저장
    private fun saveUserProfile(
        documentId: String,
        editNickname: String,
        memberIntroduce: String,
        loginId: String,
        profileImageUrl: String
    ) {
        val member =
            Member(documentId.toLong(), "", editNickname, profileImageUrl, memberIntroduce, loginId)
        db.collection("members").document(documentId)
            .set(member)
            .addOnSuccessListener {
                Log.d("Firestore", "문서가 성공적으로 작성되었습니다!")
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "문서 작성 실패", e)
            }
    }

    // Firebase Firestore에 카카오 유저 프로필 저장
    private fun kakaosaveUserProfile(
        documentId: String,
        nickname: String,
        editNickname: String,
        memberIntroduce: String,
        profileImageUrl: String
    ) {
        val member = Member(
            documentId.toLong(),
            nickname,
            editNickname,
            profileImageUrl,
            memberIntroduce,
            ""
        )
        db.collection("members").document(documentId)
            .set(member)
            .addOnSuccessListener {
                Log.d("Firestore", "문서가 성공적으로 작성되었습니다!")
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "문서 작성 실패", e)
            }
    }

    // 권한 체크 및 요청
    private fun checkPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PermissionChecker.PERMISSION_GRANTED -> {
                openGallery()
            }

            shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE) -> {
                Toast.makeText(this, "갤러리 접근 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
            }

            else -> {
                requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    }
}