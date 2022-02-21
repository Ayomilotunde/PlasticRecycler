package com.plasticrecycler.adapter

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.plasticrecycler.R
import com.plasticrecycler.activities.CreatePostActivity
import com.plasticrecycler.model.Post
import com.squareup.picasso.Picasso


class PostAdapter(private val postList: ArrayList<Post>): RecyclerView.Adapter<PostAdapter.MyViewHolder>() {

    private lateinit var mListener: OnItemClickListener


    interface OnItemClickListener {
        fun  onItemClick(position: Int) {

        }
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        mListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.post_items,
            parent,false)
        return MyViewHolder(itemView, mListener)

    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentitem = postList[position]

        holder.title.text = currentitem.title
        holder.description.text = currentitem.description
        holder.date.text = currentitem.date
        holder.time.text = currentitem.time
        Picasso.get().load(currentitem.postImage).into(holder.image)

    }

    override fun getItemCount(): Int {
       return postList.size
    }


    class MyViewHolder(itemView: View, listener: OnItemClickListener) : RecyclerView.ViewHolder(itemView) {

        val title: TextView  = itemView.findViewById(R.id.tvTitle)
        val description: TextView  = itemView.findViewById(R.id.tvDescription)
        val date: TextView  = itemView.findViewById(R.id.tvDate)
        val time: TextView  = itemView.findViewById(R.id.tvTime)
        val image: ImageView  = itemView.findViewById(R.id.imageView)

        init {
            itemView.setOnClickListener{
                listener.onItemClick(adapterPosition)
            }
        }
    }
}