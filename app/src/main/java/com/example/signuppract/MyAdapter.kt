package com.example.signuppract

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
//this is the adapter class responsible for connecting the data to the recyclerview for display
// RecyclerView.Adapter is a class provided by android - base adapter for providing data to a recyclerview
class MyAdapter(private var userList : ArrayList<User>) : RecyclerView.Adapter<MyAdapter.MyViewHolder>() {

    //This method creates a new viewholder inflating the item.xml
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyAdapter.MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item,
            parent,false)





        return MyViewHolder(itemView)
    }


    // bind data to the views inside each item of the RecyclerView
    //It gets the User object from the userList based on the
    // position and sets the email to the corresponding TextView in the ViewHolder.
    override fun onBindViewHolder(holder: MyAdapter.MyViewHolder, position: Int) {
        val user : User = userList[position]
        holder.email.text = user.email
        holder.acceptButton.tag = position
        holder.declineButton.tag = position


    }
    //returns total no. of items in arraylist
    override fun getItemCount(): Int {
        return userList.size
    }

    fun setUserList(userArrayList: ArrayList<User>) {
        this.userList = userArrayList

        // Notify the adapter that the dataset has changed
        notifyDataSetChanged()

    }

    //This is a ViewHolder class that holds references to the views for each item in the RecyclerView.
    //It holds a TextView (email) which represents the user email.
    public class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val email : TextView = itemView.findViewById(R.id.userEmail)
        val acceptButton: Button = itemView.findViewById(R.id.acceptButton)
        val declineButton: Button = itemView.findViewById(R.id.declineButton)




    }
}