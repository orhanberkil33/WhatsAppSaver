package com.example.whatsappsaver.Adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.whatsappsaver.ViewImageActivity
import com.example.whatsappsaver.ViewVideoActivity
import com.example.whatsappsaver.databinding.ItemLayoutBinding
import com.example.whatsappsaver.model.WpStatusDTO

class WpStatusAdapter(
    private val context:Context,
    var wpStatus: List<WpStatusDTO>,
    private val clickListener: (WpStatusDTO) -> Unit
) : RecyclerView.Adapter<WpStatusAdapter.MyViewHolder>() {


    class MyViewHolder(val itemLayoutBinding: ItemLayoutBinding):RecyclerView.ViewHolder(itemLayoutBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {

        val layoutInflater = LayoutInflater.from(parent.context)
        val itemLayoutBinding = ItemLayoutBinding.inflate(layoutInflater)
        return MyViewHolder(itemLayoutBinding)

    }

    override fun getItemCount() = wpStatus.size



    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        val item = wpStatus[position]

        if (item.fileUri.endsWith(".mp4")){

            holder.itemLayoutBinding.wpPlay.visibility = View.VISIBLE
        }
        else{
            holder.itemLayoutBinding.wpImage.visibility = View.GONE
        }

        Glide.with(context.applicationContext).load(item.fileUri).into(holder.itemLayoutBinding.wpImage)

        holder.itemLayoutBinding.wpDownload.setOnClickListener {
            clickListener(item)
        }

        holder.itemView.setOnClickListener {

            if (item.fileUri.endsWith(".mp4")){

                val intent=Intent(context,ViewVideoActivity::class.java)
                intent.putExtra("fileUri",item.fileUri)
                context.startActivity(intent)

            }else{
                val intent=Intent(context, ViewImageActivity::class.java)
                intent.putExtra("fileUri",item.fileUri)
                context.startActivity(intent)
            }
        }




    }


}