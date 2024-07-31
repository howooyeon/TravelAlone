package com.guru.travelalone.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.guru.travelalone.item.MypagePostListItem
import com.guru.travelalone.R

class MypagePostListAdapter(val context: Context, val items : ArrayList<MypagePostListItem>) :BaseAdapter()
{
    override fun getCount(): Int = items.size

    override fun getItem(position: Int): Any = items[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View{
        val view: View = LayoutInflater.from(context).inflate(R.layout.listview_mypage_post, null)
        val img = view.findViewById<ImageView>(R.id.image)
        val title = view.findViewById<TextView>(R.id.title)
        val sub = view.findViewById<TextView>(R.id.sub)

        val item = items[position]
        img.setImageDrawable(item.img)
        title.text = item.title
        sub.text = item.sub

        return view
    }
}