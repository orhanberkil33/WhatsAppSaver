package com.example.whatsappsaver

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Binder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.example.whatsappsaver.databinding.ActivityViewImageBinding

class ViewImageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityViewImageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_image)
        binding = ActivityViewImageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val fileUri = intent.getStringExtra("fileuri")

        Glide.with(this).load(fileUri).into(binding.viewImage)

        binding.wpShare.setOnClickListener {
            val sharingIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/jpg"
                putExtra(Intent.EXTRA_STREAM, Uri.parse(fileUri))
            }
            startActivity(Intent.createChooser(sharingIntent, "Fotoğraf gönderiliyor..."))
        }

        binding.contentWahatsapp.setOnClickListener {
            val sharingIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/jpg"
                putExtra(Intent.EXTRA_STREAM, Uri.parse(fileUri))
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                setPackage("com.whatsapp")
            }

            try {
                startActivity(sharingIntent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(this, "Whatsapp uygulaması yüklü değil", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this, "Fotoğraf paylaşılamadı", Toast.LENGTH_SHORT).show()
            }
        }
    }
}