package com.example.signuppract

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MyAdapter2(private var userList : ArrayList<User>) : RecyclerView.Adapter<MyAdapter2.MyViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyAdapter2.MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.allfriendsitem,
            parent,false)


        return MyAdapter2.MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val user : User = userList[position]
        holder.email.text = user.email
        holder.deleteButton.tag = position
    }

    override fun getItemCount(): Int {
        return userList.size
    }


    fun setUserList(userArrayList: ArrayList<User>) {
        this.userList = userArrayList

        // Notify the adapter that the dataset has changed
        notifyDataSetChanged()

    }

    public class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val email : TextView = itemView.findViewById(R.id.userEmail)
        val deleteButton: Button = itemView.findViewById(R.id.deleteButton)





    }
}