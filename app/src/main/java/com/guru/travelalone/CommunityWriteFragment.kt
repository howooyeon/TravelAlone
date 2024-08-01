package com.guru.travelalone


import androidx.fragment.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

class CommunityWriteFragment : Fragment() {

    companion object {
        private const val ARG_TITLE = "title"
        private const val ARG_DATE = "date"

        fun newInstance(title: String?, date: String?, location: String?): CommunityWriteFragment {
            val fragment = CommunityWriteFragment()
            val args = Bundle()
            args.putString(ARG_TITLE, title)
            args.putString(ARG_DATE, date)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_community_write, container, false)

        // 데이터 설정
        val title = arguments?.getString(ARG_TITLE)
        val date = arguments?.getString(ARG_DATE)

        val titleTextView: TextView = view.findViewById(R.id.titleTextView)
        val dateTextView: TextView = view.findViewById(R.id.dateTextView)

        titleTextView.text = title
        dateTextView.text = date

        return view
    }
}