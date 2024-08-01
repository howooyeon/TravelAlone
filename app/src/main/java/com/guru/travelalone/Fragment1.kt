package com.guru.travelalone

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.guru.travelalone.adapter.MypagePostListAdapter
import com.guru.travelalone.item.MypagePostListItem
import com.guru.travelalone.item.MypageTripListItem

class Fragment1 : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_1, container, false)

        var mypagepostlistview : ListView = view.findViewById(R.id.mypagepostlistview)
        // add한 내용은 이 밑에 list에 들어갑니다
        var mypagepostList = arrayListOf<MypagePostListItem>(
            MypagePostListItem(ContextCompat.getDrawable(requireContext(), R.drawable.img_gangneung_sokcho)!!, "제목", "본문"),
            MypagePostListItem(ContextCompat.getDrawable(requireContext(), R.drawable.img_gangneung_sokcho)!!, "제목", "본문")
        )
        // add
        mypagepostList.add(MypagePostListItem(ContextCompat.getDrawable(requireContext(), R.drawable.img_gangneung_sokcho)!!, "제목", "본문"),)



        val mypagepostadapter = MypagePostListAdapter(requireContext(), mypagepostList)
        mypagepostlistview.adapter = mypagepostadapter

        return view
    }
}