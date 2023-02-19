package com.example.whatsappsaver.Adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.whatsappsaver.Fragments.ImageFragment
import com.example.whatsappsaver.Fragments.SavedFragment
import com.example.whatsappsaver.Fragments.VideoFragment

class ViewPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) :
    FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun getItemCount(): Int {
        return 3
    }

    override fun createFragment(position: Int): Fragment {

        return when (position) {
            0 -> {
                ImageFragment()
            }

            1 -> {
                VideoFragment()
            }
            2 -> {
                SavedFragment()
            }
            else -> {
                Fragment()
            }
        }
    }
}