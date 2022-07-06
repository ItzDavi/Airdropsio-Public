package com.example.airdropsio

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class CustomAdapter (private val mList: List<ItemsViewModel>, var context: Context) : RecyclerView.Adapter<CustomAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.card_view_design, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val itemsViewModel = mList[position]

        val imageURL = itemsViewModel.image

        Glide.with(context).load(imageURL).into(holder.imageView)

        holder.textView.text = itemsViewModel.text

        holder.itemView.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING)

            val context = holder.itemView.context as Activity
            val intent = Intent(context, AirdropActivity::class.java)
            intent.putExtra("title", holder.textView.text)
            context.startActivity(intent)
            context.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left)
        }
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
        val textView: TextView = itemView.findViewById(R.id.AD_cardview_title)
    }
}