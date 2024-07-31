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
import java.io.File

class ProEdit_Activity : AppCompatActivity() {
    private val db = FirebaseFirestore.getInstance()
    private val binding by lazy { ActivityProEditBinding.inflate(layoutInflater) }

    private val storage = FirebaseStorage.getInstance()
    private val storageRef = storage.reference

    lateinit var introduce: EditText
    private var nickname = ""
    private var profileImageUrl = ""
    private var member_introduce = ""
    private var editnickname = ""
    private var imageUri: Uri? = null

    // 갤러리 open
    private val requestPermissionLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                openGallery()
            } else {
                Toast.makeText(this, "갤러리 접근 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
            }
        }

    // 가져온 사진 보여주기
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

        introduce = findViewById(R.id.Introduce)

        Log.d("ProEdit_Activity", "onCreate called")

        // 사용자 정보 요청
        try {
            UserApiClient.instance.me { user, error ->
                if (error != null) {
                    Log.e(Constants.TAG, "사용자 정보 요청 실패 $error")
                } else if (user != null) {
                    Log.d(Constants.TAG, "사용자 정보 요청 성공 : $user")
                    nickname = user.kakaoAccount?.profile?.nickname ?: ""
                    profileImageUrl = user.kakaoAccount?.profile?.profileImageUrl ?: ""
                    member_introduce = introduce.text.toString()

                    binding.txtNickName.hint = nickname
                    if (profileImageUrl.isNotEmpty()) {
                        Glide.with(this)
                            .load(profileImageUrl)
                            .into(binding.kakaoPro)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(Constants.TAG, "사용자 정보 요청 중 예외 발생", e)
        }

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

        // Save button 클릭 리스너 설정
        binding.saveButton.setOnClickListener {
            editnickname = binding.txtNickName.text.toString().trim()
            if (imageUri == null) {
                Toast.makeText(this, "이미지를 선택해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    saveUserProfile()
                    val intent = Intent(this@ProEdit_Activity, Home_Activity::class.java)
                    startActivity(intent)
                } catch (e: Exception) {
                    Log.e("ProEdit_Activity", "Error saving profile", e)
                    Toast.makeText(this@ProEdit_Activity, "프로필 저장 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun openGallery() {
        val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
        pickImageLauncher.launch(gallery)
    }

    private suspend fun uploadImageToStorage(uri: Uri): String {
        val file = Uri.fromFile(File(uri.path))
        val riversRef = storageRef.child("images/${file.lastPathSegment}")
        riversRef.putFile(uri).await()
        return riversRef.downloadUrl.await().toString()
    }

    private suspend fun saveUserProfile() {
        val memberId = CounterHelper.getNextMemberId()
        if (imageUri != null) {
            profileImageUrl = uploadImageToStorage(imageUri!!)
        }
        val member = Member(memberId, nickname, editnickname, profileImageUrl, introduce.text.toString())
        db.collection("members").document(memberId.toString())
            .set(member)
            .addOnSuccessListener {
                Log.d("Firestore", "DocumentSnapshot successfully written!")
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error writing document", e)
            }
    }
}
