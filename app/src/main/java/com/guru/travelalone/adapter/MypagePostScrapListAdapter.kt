package com.guru.travelalone.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.guru.travelalone.R
import com.guru.travelalone.item.MypagePostScrapListItem

class MypagePostScrapListAdapter(val context: Context, val items : ArrayList<MypagePostScrapListItem>) :BaseAdapter()
{
    override fun getCount(): Int = items.size

    override fun getItem(position: Int): Any = items[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View{
        val view: View = LayoutInflater.from(context).inflate(R.layout.listview_mypage_scap, null)
        val img = view.findViewById<ImageView>(R.id.image)
        val title = view.findViewById<TextView>(R.id.title)
        val sub = view.findViewById<TextView>(R.id.sub)

        val item = items[position]
        Glide.with(context)
            .load(item.imgUrl)
            .into(img)
        title.text = item.title
        sub.text = item.sub

        return view
    }
}
