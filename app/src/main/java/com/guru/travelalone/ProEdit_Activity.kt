package com.guru.travelalone

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.guru.travelalone.databinding.ActivityProEditBinding
import com.kakao.sdk.user.Constants
import com.kakao.sdk.user.UserApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class ProEdit_Activity : AppCompatActivity() {
    private val db = FirebaseFirestore.getInstance()
    private val binding by lazy { ActivityProEditBinding.inflate(layoutInflater) }
    private val storage = FirebaseStorage.getInstance()
    private val storageRef = storage.reference

    private var nickname = ""  //유저 본명
    private var profileImageUrl = "" //유저 프로필 경로
    private var member_introduce = "" //유저 소개글
    private var editnickname = "" //유저 닉네임
    private var imageUri: Uri? = null //갤러리 사진 가져온 경로

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

        // 카카오 로그인 시 사용자 정보 요청
        try {
            UserApiClient.instance.me { user, error ->
                if (error != null) {
                    Log.e(Constants.TAG, "사용자 정보 요청 실패 $error")
                } else if (user != null) {
                    Log.d(Constants.TAG, "사용자 정보 요청 성공 : $user")
                    nickname = user.kakaoAccount?.profile?.nickname ?: "" // 카카오에 저장된 유저 본명
                    profileImageUrl = user.kakaoAccount?.profile?.profileImageUrl ?: "" // 카카오 프로필 사진 경로
                    member_introduce = binding.Introduce.text.toString() // 소개글 가져오기

                    binding.txtNickName.hint = nickname // 유저 본명을 닉네임 힌트로 설정
                    if (profileImageUrl.isNotEmpty()) {
                        Glide.with(this)
                            .load(profileImageUrl)
                            .into(binding.kakaoPro) // 기본적으로 카카오 프로필을 유저의 프로필로 설정
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(Constants.TAG, "사용자 정보 요청 중 예외 발생", e)
        }

        // 플러스 버튼 클릭 시 갤러리 열기
        binding.plusBtn.setOnClickListener {
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

        // 저장 버튼 클릭 시 프로필 저장
        binding.saveButton.setOnClickListener {
            editnickname = binding.txtNickName.text.toString().trim()
            member_introduce = binding.Introduce.text.toString().trim()
            if (editnickname.isNotEmpty() && member_introduce.isNotEmpty()) {
                Toast.makeText(this@ProEdit_Activity, "프로필 등록 중, 잠시 기다려주세요.", Toast.LENGTH_SHORT)
                    .show()
                CoroutineScope(Dispatchers.Main).launch {
                    try {
                        if (imageUri == null && profileImageUrl.isNotEmpty()) {
                            Log.d("ProEdit_Activity", "스토리지에 카카오 프로필 업로드 중")
                            profileImageUrl = withContext(Dispatchers.IO) {
                                uploadImageUrlToStorage(profileImageUrl)
                            }
                        } else if (imageUri != null) {
                            Log.d("ProEdit_Activity", "로컬 이미지를 프로필에 업로드 중")
                            profileImageUrl = withContext(Dispatchers.IO) {
                                uploadImageToStorage(imageUri!!)
                            }
                        }
                        saveUserProfile() // 프로필 데이터베이스에 저장
                        val intent = Intent(this@ProEdit_Activity, Home_Activity::class.java)
                        startActivity(intent)
                    } catch (e: Exception) {
                        Log.e("ProEdit_Activity", "프로필 저장 중 오류", e)
                        Toast.makeText(
                            this@ProEdit_Activity,
                            "프로필 저장 중 오류가 발생했습니다.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } else {
                Toast.makeText(this, "정보를 다 입력해주세요", Toast.LENGTH_SHORT).show()
            }
        }
    }


    //갤러리 접근
    private fun openGallery() {
        val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
        pickImageLauncher.launch(gallery)
    }

    //스토리지에 사진 업로드
    private suspend fun uploadImageToStorage(uri: Uri): String {
        return withContext(Dispatchers.IO) {
            try {
                val file = Uri.fromFile(File(uri.path))
                val riversRef = storageRef.child("images/${file.lastPathSegment}")
                riversRef.putFile(uri).await()
                riversRef.downloadUrl.await().toString()
            } catch (e: Exception) {
                Log.e("ProEdit_Activity", "스토리지에 사진 업로드 오류", e)
                throw e
            }
        }
    }


    private suspend fun uploadImageUrlToStorage(imageUrl: String): String {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL(imageUrl)
                val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
                connection.doInput = true
                connection.connect()
                val inputStream: InputStream = connection.inputStream
                val tempFile = File.createTempFile("tempImage", "jpg")
                tempFile.outputStream().use { inputStream.copyTo(it) }

                val fileUri = Uri.fromFile(tempFile)
                uploadImageToStorage(fileUri)
            } catch (e: Exception) {
                Log.e("ProEdit_Activity", "url로 부터 이미지 다운 실패", e)
                throw e
            }
        }
    }

    //프로필 저장
    private suspend fun saveUserProfile() {
        val memberId = CounterHelper.getNextMemberId()
        val member = Member(memberId, nickname, editnickname, profileImageUrl, binding.Introduce.text.toString())
        db.collection("members").document(memberId.toString())
            .set(member)
            .addOnSuccessListener {
                Log.d("Firestore", "프로필 저장 성공")
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "프로필 저장 실패", e)
            }
    }
}
