package com.guru.travelalone.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.guru.travelalone.R
import com.guru.travelalone.item.CommunityPostListItem

class CommunityPostListAdapter(val context: Context, val items : ArrayList<CommunityPostListItem>) : BaseAdapter()
{
    override fun getCount(): Int = items.size

    override fun getItem(position: Int): Any = items[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View = LayoutInflater.from(context).inflate(R.layout.listview_community_post, null)
        val img = view.findViewById<ImageView>(R.id.image)
        val profile_img = view.findViewById<ImageView>(R.id.image_profile)
        val title = view.findViewById<TextView>(R.id.title)
        val name = view.findViewById<TextView>(R.id.name)
        val sub = view.findViewById<TextView>(R.id.sub)
        val date = view.findViewById<TextView>(R.id.date)
        // 북마크 최상위 레이어로 올리기
        val bookmark = view.findViewById<ImageView>(R.id.bookmark)
        bookmark.bringToFront()

        val item = items[position]
        img.setImageDrawable(item.img)
        profile_img.setImageDrawable(item.profile)
        title.text = item.title
        name.text = item.name
        sub.text = item.sub
        date.text = item.date

        return view
    }
}