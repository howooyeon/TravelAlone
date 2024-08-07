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

class CommunityPostListAdapter(
    context: Context,
    private val posts: List<CommunityPostListItem>,
    private val itemClickListener: (CommunityPostListItem) -> Unit
) : ArrayAdapter<CommunityPostListItem>(context, 0, posts) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val post = getItem(position)
        val view: View = convertView ?: LayoutInflater.from(context).inflate(R.layout.listview_community_post, parent, false)

        val imageView: ImageView = view.findViewById(R.id.image)
        val profileImageView: de.hdodenhof.circleimageview.CircleImageView = view.findViewById(R.id.image_profile)
        val titleTextView: TextView = view.findViewById(R.id.title)
        val nameTextView: TextView = view.findViewById(R.id.name)
        val dateTextView: TextView = view.findViewById(R.id.date)
        val contentTextView: TextView = view.findViewById(R.id.sub)

        post?.let {
            nameTextView.text = it.nickname
            titleTextView.text = it.title
            contentTextView.text = it.content
            dateTextView.text = it.date

            // 이미지 첨부시 가져오기
            if (!it.imageUrl.isNullOrEmpty()) {
                Glide.with(context)
                    .load(it.imageUrl)
                    .placeholder(R.drawable.sample_image_placeholder)
                    .error(R.drawable.sample_image_placeholder)
                    .into(imageView)
            } else {
                imageView.visibility = View.GONE
            }

            // 프로필 사진 존재시, 글라이더로 가져오기
            if (!it.profileImageUrl.isNullOrEmpty()) {
                Glide.with(context)
                    .load(it.profileImageUrl)
                    .placeholder(R.drawable.samplepro)
                    .error(R.drawable.samplepro)
                    .into(profileImageView)
            } else {
                profileImageView.setImageResource(R.drawable.samplepro)
            }

            // post item set하기
            view.tag = it

            // 뷰 클릭시, 태그 사용해서 post 가져오기
            view.setOnClickListener {
                val clickedPost = view.tag as? CommunityPostListItem
                clickedPost?.let { itemClickListener(it) }
            }
        }

        return view
    }
}
