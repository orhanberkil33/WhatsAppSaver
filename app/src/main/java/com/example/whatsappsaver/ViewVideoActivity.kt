package com.example.whatsappsaver

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.MediaController
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.example.whatsappsaver.databinding.ActivityViewVideoBinding

class ViewVideoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityViewVideoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_video)


        binding = ActivityViewVideoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val fileUri = intent.getStringExtra("fileuri")

        try {
            val mediaController = MediaController(this)
            mediaController.setAnchorView(binding.wpVideo)
            binding.wpVideo.setMediaController(mediaController)
            binding.wpVideo.setVideoURI(Uri.parse(fileUri))
            binding.wpVideo.setOnPreparedListener {
                binding.wpVideo.start()
            }

            binding.wpVideo.setOnCompletionListener {
                binding.wpVideo.start()
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

        binding.wpShare.setOnClickListener {

            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "video/mp4"
            shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(fileUri))

            startActivity(Intent.createChooser(shareIntent, "Video gönderiliyor..."))
        }

        binding.contentWp.setOnClickListener {

            val sharingIntent = Intent(Intent.ACTION_SEND)
            sharingIntent.type = "video/mp4"
            sharingIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(fileUri))
            sharingIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            sharingIntent.setPackage("com.whatsapp")

            try {
                startActivity(sharingIntent)
            } catch (e: Exception) {
                Toast.makeText(this, "Video Paylaşılamadı", Toast.LENGTH_SHORT).show()
            }
        }
    }
}