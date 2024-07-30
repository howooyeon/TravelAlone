package com.guru.travelalone

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.guru.travelalone.databinding.ActivityNormalProBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class NormalPro_Activity : AppCompatActivity() {
    private val db = FirebaseFirestore.getInstance()
    private val binding by lazy { ActivityNormalProBinding.inflate(layoutInflater) }

    private lateinit var auth: FirebaseAuth
    private var currentUser: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)  // 수정된 부분

        auth = FirebaseAuth.getInstance()
        currentUser = auth.currentUser


        binding.saveButton.setOnClickListener {
            val nickname = binding.txtNickName.text.toString().trim()
            val memberIntroduce = binding.Introduce.text.toString().trim()
            val login_id = currentUser?.email.toString()


            if (nickname.isNotEmpty() && memberIntroduce.isNotEmpty()) {
                CoroutineScope(Dispatchers.Main).launch {
                    saveUserProfile(nickname, memberIntroduce, login_id)

                    val intent = Intent(this@NormalPro_Activity, Home_Activity::class.java)
                    startActivity(intent)
                }
            } else {
                Toast.makeText(this, "정보를 다 입력해주세요", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private suspend fun saveUserProfile(nickname: String, memberIntroduce: String, login_id : String) {
        val memberId = CounterHelper.getNextMemberId()
        val member = Member(memberId , nickname, "", memberIntroduce, login_id)  // 프로필 이미지 URL은 비워두었음
        try {
            db.collection("members").document(memberId.toString())
                .set(member).await()  // 수정된 부분
            Log.d("NormalProfile", "DocumentSnapshot successfully written!")
        } catch (e: Exception) {
            Log.w("NormalProfile", "Error writing document", e)
        }
    }
}
