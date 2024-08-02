package com.guru.travelalone.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.guru.travelalone.R
import com.guru.travelalone.model.Message

class MessageAdapter(private val messageList: List<Message>) : RecyclerView.Adapter<MessageAdapter.ViewHolder>() {

    // ViewHolder 생성: ViewHolder를 초기화하고 chat_item 레이아웃을 인플레이트하여 반환
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.chat_item, parent, false)
        return ViewHolder(view)
    }

    // ViewHolder에 데이터 바인딩: 메시지가 사용자에 의해 보내졌는지, 봇에 의해 보내졌는지에 따라 뷰를 업데이트
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val message = messageList[position]
        if (message.sentBy == Message.SENT_BY_ME) {
            holder.leftChatView.visibility = View.GONE
            holder.rightChatView.visibility = View.VISIBLE
            holder.rightChatTv.text = message.message
        } else {
            holder.rightChatView.visibility = View.GONE
            holder.leftChatView.visibility = View.VISIBLE
            holder.leftChatTv.text = message.message
        }
    }

    // 아이템 개수 반환: 메시지 리스트의 사이즈를 반환
    override fun getItemCount(): Int {
        return messageList.size
    }

    // ViewHolder 클래스: 각 메시지 아이템의 뷰를 홀딩
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val leftChatView: LinearLayout = itemView.findViewById(R.id.left_chat_view)
        val rightChatView: LinearLayout = itemView.findViewById(R.id.right_chat_view)
        val leftChatTv: TextView = itemView.findViewById(R.id.left_chat_tv)
        val rightChatTv: TextView = itemView.findViewById(R.id.right_chat_tv)
    }
}
