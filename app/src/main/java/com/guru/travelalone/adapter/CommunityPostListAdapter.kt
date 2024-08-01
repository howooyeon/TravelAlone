package com.guru.travelalone.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.bumptech.glide.Glide
import com.guru.travelalone.R
import com.guru.travelalone.item.CommunityPostListItem

class CommunityPostListAdapter(private val context: Context, private val posts: List<CommunityPostListItem>)
    : ArrayAdapter<CommunityPostListItem>(context, 0, posts) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val post = getItem(position)
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.listview_community_post, parent, false)

        val imageView = view.findViewById<ImageView>(R.id.image)
        val profileImageView = view.findViewById<ImageView>(R.id.image_profile)
        val nameTextView = view.findViewById<TextView>(R.id.name)
        val titleTextView = view.findViewById<TextView>(R.id.title)
        val contentTextView = view.findViewById<TextView>(R.id.sub)
        val dateTextView = view.findViewById<TextView>(R.id.date)

        Glide.with(context).load(post?.imageUrl).into(imageView)
        Glide.with(context).load(post?.profileImageUrl).into(profileImageView)
        nameTextView.text = post?.name
        titleTextView.text = post?.title
        contentTextView.text = post?.content
        dateTextView.text = post?.date

        return view
    }
}
