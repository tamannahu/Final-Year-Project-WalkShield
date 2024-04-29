package com.example.signuppract

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class messagesAdapter(private var messageList : ArrayList<Message>) : RecyclerView.Adapter<messagesAdapter.MyViewHolder>() {

    public class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val email : TextView = itemView.findViewById(R.id.userEmail)
        val message : TextView = itemView.findViewById(R.id.userMessage)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): messagesAdapter.MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.messageitem,
            parent,false)

        return messagesAdapter.MyViewHolder(itemView)

    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val message : Message = messageList[position]
        holder.email.text = message.email
        holder.message.text = message.message

    }
}