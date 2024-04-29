package com.example.signuppract

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore

class Messages : AppCompatActivity() {
    var db = Firebase.firestore
    private lateinit var auth: FirebaseAuth
    private lateinit var recyclerView: RecyclerView
    private lateinit var messageArrayList: ArrayList<Message>
    private lateinit var myAdapter: messagesAdapter
    private val currentUser = FirebaseAuth.getInstance().currentUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_messages)
        auth = FirebaseAuth.getInstance()

        recyclerView = findViewById(R.id.recyclerView3)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)
        messageArrayList = arrayListOf()
        myAdapter = messagesAdapter(messageArrayList)

        recyclerView.adapter = myAdapter
        EventChangeListener()
    }

    private fun EventChangeListener(){
        val messagesRef = db.collection("messages")



        messagesRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                // Handle errors
                return@addSnapshotListener
            }

            if (snapshot != null && !snapshot.isEmpty) {
                messageArrayList.clear()
                for (document in snapshot.documents) {
                    val email = document.getString("email")
                    val message = document.getString("message")
                    if (email != null && message != null) {
                        messageArrayList.add(Message(email, message))
                    }
                }
                myAdapter.notifyDataSetChanged()
            }
        }

    }

    fun showBottomSheet(view: View) {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.bottomsheetmessages,null)
        val btnEnter = view.findViewById<Button>(R.id.sendMessage)
        val exitButton = view.findViewById<Button>(R.id.exitButton)
        val inputMessage = view.findViewById<EditText>(R.id.MessageInput)

        exitButton.setOnClickListener {
            dialog.dismiss()
        }

        btnEnter.setOnClickListener {
            val inputText = inputMessage.text.toString()
            val currentUserEmail = auth.currentUser?.email.toString()



            if (currentUserEmail != null && inputText.isNotEmpty()) {

                db.collection("messages").document(currentUserEmail.toString())


                val messageData = hashMapOf(
                    "message" to inputText,
                    "email" to currentUserEmail
                )
                val messagesCollection = db.collection("messages")


            messagesCollection.add(messageData)
                .addOnSuccessListener { documentReference ->

                    inputMessage.text.clear()

                    dialog.dismiss()
                    Toast.makeText(this, "Message sent", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    // Handle errors
                    println("Error adding document: $e")
                    Toast.makeText(this, "Failed to send message", Toast.LENGTH_SHORT).show()
                }
        }



        }

        dialog.setCancelable(false)
        dialog.setContentView(view)
        dialog.show()


    }
}