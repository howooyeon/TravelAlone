package com.guru.travelalone.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.guru.travelalone.R
import com.guru.travelalone.item.CommunityPostListItem

class CommunityPostListAdapter(context: Context, private val posts: List<CommunityPostListItem>)
    : ArrayAdapter<CommunityPostListItem>(context, 0, posts) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val post = getItem(position)
        val view: View = convertView ?: LayoutInflater.from(context).inflate(R.layout.listview_community_post, parent, false)

        val imageView: ImageView = view.findViewById(R.id.image)
        val profileImageView: de.hdodenhof.circleimageview.CircleImageView = view.findViewById(R.id.image_profile)
        val titleTextView: TextView = view.findViewById(R.id.title)
        val nameTextView: TextView = view.findViewById(R.id.name) // 닉네임 텍스트뷰
        val dateTextView: TextView = view.findViewById(R.id.date)
        val contentTextView: TextView = view.findViewById(R.id.sub)

        post?.let {
            // 닉네임 설정
            nameTextView.text = it.nickname // 닉네임 설정

            // 제목 설정
            titleTextView.text = it.title

            // 내용 설정
            contentTextView.text = it.content

            // 날짜 설정
            dateTextView.text = it.date

            // 이미지 처리
            if (!it.imageUrl.isNullOrEmpty()) {
                Glide.with(context)
                    .load(it.imageUrl)
                    .placeholder(R.drawable.sample_image_placeholder) // 로딩 중 표시할 기본 이미지
                    .error(R.drawable.sample_image_placeholder) // 로딩 실패 시 표시할 이미지
                    .into(imageView)
            } else {
                imageView.visibility = View.GONE
            }

            // 프로필 이미지 처리
            if (!it.profileImageUrl.isNullOrEmpty()) {
                Glide.with(context)
                    .load(it.profileImageUrl)
                    .placeholder(R.drawable.samplepro) // 로딩 중 표시할 기본 이미지
                    .error(R.drawable.samplepro) // 로딩 실패 시 표시할 이미지
                    .into(profileImageView)
            } else {
                profileImageView.setImageResource(R.drawable.samplepro) // 기본 프로필 이미지 설정
            }
        }

        return view
    }
}
