package com.example.whatsappsaver.Adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.whatsappsaver.ViewImageActivity
import com.example.whatsappsaver.ViewVideoActivity
import com.example.whatsappsaver.databinding.ItemLayoutBinding
import java.io.File

class FileListAdapter(private val list: List<File>, val context: Context) :
    RecyclerView.Adapter<FileListAdapter.MyViewHolder>() {


    class MyViewHolder(val itemLayoutBinding: ItemLayoutBinding) :
        RecyclerView.ViewHolder(itemLayoutBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val itemLayoutBinding = ItemLayoutBinding.inflate(layoutInflater)
        return MyViewHolder(itemLayoutBinding)
    }

    override fun getItemCount() = list.size


    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = list[position]

        if (item.toUri().toString().endsWith(".mp4")) {

            holder.itemLayoutBinding.wpPlay.visibility = View.VISIBLE
        } else {
            holder.itemLayoutBinding.wpImage.visibility = View.GONE
        }
        holder.itemLayoutBinding.wpDownload.visibility = View.GONE

        Glide.with(context.applicationContext).load(item.path.toString())
            .into(holder.itemLayoutBinding.wpImage)


        holder.itemView.setOnClickListener {
            if (item.toUri().toString().endsWith(".mp4")) {

                val intent = Intent(context, ViewVideoActivity::class.java)
                intent.putExtra("fileUri", item.path.toString())
                context.startActivity(intent)

            } else {

                val intent = Intent(context, ViewImageActivity::class.java)
                intent.putExtra("fileUri", item.path.toString())
                context.startActivity(intent)

            }
        }


    }


}