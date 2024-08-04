package com.guru.travelalone

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.guru.travelalone.adapter.MessageAdapter
import com.guru.travelalone.model.Message
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class Travbot_activity : AppCompatActivity() {

    private lateinit var recycler_view: RecyclerView
    private lateinit var et_msg: EditText
    private lateinit var btn_send: ImageButton

    private lateinit var messageList: MutableList<Message>
    private lateinit var messageAdapter: MessageAdapter

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    companion object {
        val JSON: MediaType = "application/json; charset=utf-8".toMediaType()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_travbot)

        recycler_view = findViewById(R.id.recycler_view)
        et_msg = findViewById(R.id.et_msg)
        btn_send = findViewById(R.id.btn_send)

        recycler_view.setHasFixedSize(true)
        val manager = LinearLayoutManager(this)
        manager.stackFromEnd = true
        recycler_view.layoutManager = manager

        messageList = ArrayList()
        messageAdapter = MessageAdapter(messageList)
        recycler_view.adapter = messageAdapter

        // Toolbar 설정
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // 뒤로 가기 버튼 클릭 이벤트 설정
        val backButton = findViewById<ImageButton>(R.id.back_button)
        backButton.setOnClickListener { onBackPressedDispatcher.onBackPressed() }

        btn_send.setOnClickListener {
            val question = et_msg.text.toString().trim()
            addToChat(question, Message.SENT_BY_ME)
            et_msg.setText("")
            callAPI(question)
        }

        // 초기 환영 메시지 추가 (1.5초 지연)
        Handler(Looper.getMainLooper()).postDelayed({
            addWelcomeMessage()
        }, 1500)
    }

    // 메시지를 채팅에 추가
    private fun addToChat(message: String, sentBy: String) {
        runOnUiThread {
            messageList.add(Message(message, sentBy))
            messageAdapter.notifyDataSetChanged()
            recycler_view.smoothScrollToPosition(messageAdapter.itemCount)
        }
    }

    // 트래봇의 응답 메시지 추가
    private fun addResponse(response: String) {
        messageList.removeAt(messageList.size - 1)
        addToChat(response, Message.SENT_BY_BOT)
    }

    // welcome message 추가
    private fun addWelcomeMessage() {
        addToChat("안녕하세요! 당신의 여행 비서\uD83E\uDDF3 트래봇입니다 \uD83E\uDD16 무엇을 도와드릴까요?", Message.SENT_BY_BOT)
    }

    // OpenAI API를 호출
    private fun callAPI(question: String) {
        messageList.add(Message("...", Message.SENT_BY_BOT))

        // JSON 배열 및 객체 생성
        val arr = JSONArray()
        val baseAi = JSONObject()
        val userMsg = JSONObject()
        try {
            baseAi.put("role", "user")
            baseAi.put("content", "You are a helpful and kind AI Assistant who makes travel more enjoyable and convenient.")
            userMsg.put("role", "user")
            userMsg.put("content", question)
            arr.put(baseAi)
            arr.put(userMsg)
        } catch (e: JSONException) {
            throw RuntimeException(e)
        }

        // API 요청을 위한 JSON 객체 생성
        val jsonObject = JSONObject()
        try {
            jsonObject.put("model", BuildConfig.CHATGPT_MODEL)
            jsonObject.put("messages", arr)
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        // HTTP 요청 생성
        val body = jsonObject.toString().toRequestBody(JSON)
        val request = Request.Builder()
            .url("https://api.openai.com/v1/chat/completions")
            .header("Authorization", "Bearer " + BuildConfig.OPENAI_API)
            .post(body)
            .build()

        // HTTP 요청 실행
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                addResponse("Failed to load response due to " + e.message)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val jsonObject: JSONObject?
                    try {
                        jsonObject = JSONObject(response.body?.string()!!)
                        val jsonArray = jsonObject.getJSONArray("choices")
                        val result = jsonArray.getJSONObject(0).getJSONObject("message").getString("content")
                        addResponse(result.trim())
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                } else {
                    addResponse("Failed to load response due to " + response.body?.string())
                }
            }
        })
    }
}
