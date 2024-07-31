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
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.guru.travelalone.databinding.ActivityNormalProBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class NormalPro_Activity : AppCompatActivity() {
    private val db = FirebaseFirestore.getInstance()
    private val binding by lazy { ActivityNormalProBinding.inflate(layoutInflater) }
    private val storage = FirebaseStorage.getInstance()
    private val storageRef = storage.reference

    private lateinit var auth: FirebaseAuth
    private var currentUser: FirebaseUser? = null
    private var imageUri: Uri? = null

    private var nickname = ""
    private var editnickname = ""

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

        auth = FirebaseAuth.getInstance()
        currentUser = auth.currentUser

        if (currentUser == null) {
            Toast.makeText(this, "로그인 정보가 없습니다. 다시 로그인 해주세요.", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, Login_Activity::class.java)
            startActivity(intent)
            finish()
            return
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

        binding.saveButton.setOnClickListener {
            editnickname = binding.txtNickName.text.toString().trim()
            val memberIntroduce = binding.Introduce.text.toString().trim()
            val loginId = currentUser?.email.toString()
            if (editnickname.isNotEmpty() && memberIntroduce.isNotEmpty()) {
                Toast.makeText(this@NormalPro_Activity, "프로필 등록 중, 잠시 기다려주세요.", Toast.LENGTH_SHORT).show()
                CoroutineScope(Dispatchers.Main).launch {
                    try {
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
                        saveUserProfile(nickname, editnickname, memberIntroduce, loginId, profileImageUrl)
                        val intent = Intent(this@NormalPro_Activity, Home_Activity::class.java)
                        startActivity(intent)
                    } catch (e: Exception) {
                        Log.e("NormalPro_Activity", "Error saving profile", e)
                        Toast.makeText(this@NormalPro_Activity, "프로필 저장 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "정보를 다 입력해주세요", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openGallery() {
        val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
        pickImageLauncher.launch(gallery)
    }

    private suspend fun uploadImageToStorage(uri: Uri): String {
        return withContext(Dispatchers.IO) {
            val file = Uri.fromFile(File(uri.path))
            val riversRef = storageRef.child("images/${file.lastPathSegment}")
            riversRef.putFile(uri).await()
            riversRef.downloadUrl.await().toString()
        }
    }

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

    private suspend fun saveUserProfile(nickname: String, editnickname: String, memberIntroduce: String, loginId: String, profileImageUrl: String) {
        val memberId = CounterHelper.getNextMemberId()
        val member = Member(memberId, nickname, editnickname, profileImageUrl, memberIntroduce, loginId)
        try {
            db.collection("members").document(memberId.toString())
                .set(member).await()
            Log.d("NormalProfile", "DocumentSnapshot successfully written!")
        } catch (e: Exception) {
            Log.w("NormalProfile", "Error writing document", e)
        }
    }
}
