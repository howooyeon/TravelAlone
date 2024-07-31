package com.guru.travelalone.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.guru.travelalone.R
import com.guru.travelalone.item.MypageTripListItem

class MypageTripListAdapter(val context: Context, val items : ArrayList<MypageTripListItem>) : BaseAdapter()
{
    override fun getCount(): Int = items.size

    override fun getItem(position: Int): Any = items[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View = LayoutInflater.from(context).inflate(R.layout.listview_mypage_trip, null)
        val location_img = view.findViewById<ImageView>(R.id.location_image)
        val title = view.findViewById<TextView>(R.id.title)
        val date = view.findViewById<TextView>(R.id.date)

        val item = items[position]
        location_img.setImageDrawable(item.location_img)
        title.text = item.title
        date.text = item.date

        return view
    }
}