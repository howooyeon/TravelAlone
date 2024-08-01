package com.guru.travelalone.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.guru.travelalone.R
import com.guru.travelalone.item.CommunityPostListItem
import com.squareup.picasso.Picasso

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
            titleTextView.text = it.title
            contentTextView.text = it.content
            dateTextView.text = it.date
            nameTextView.text = it.nickname // 닉네임 설정

            if (!it.imageUrl.isNullOrEmpty()) {
                Picasso.get().load(it.imageUrl).into(imageView)
            } else {
                imageView.visibility = View.GONE
            }

            if (!it.profileImageUrl.isNullOrEmpty()) {
                Picasso.get().load(it.profileImageUrl).into(profileImageView)
            } else {
                profileImageView.setImageResource(R.color.gray) // 기본 이미지 설정
            }
        }

        return view
    }
}
