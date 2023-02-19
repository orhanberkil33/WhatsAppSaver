package com.example.whatsappsaver

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.whatsappsaver.databinding.ActivityMainBinding
import com.example.whatsappsaver.Adapters.ViewPagerAdapter
import com.google.android.material.tabs.TabLayoutMediator

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding  = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val adapter = ViewPagerAdapter(supportFragmentManager, lifecycle)
        val tablayout = binding!!.tabLayout
        val viewPager = binding!!.viewPager2

        viewPager.adapter = adapter

        TabLayoutMediator(tablayout, viewPager) { tab, position ->
            when (position) {
                0 -> {
                    tab.text = "Resimler"
                }

                1 -> {
                    tab.text = "Videolar"
                }

                2->{
                    tab.text ="Kaydedilen"
                }
            }
        }.attach()



    }

}