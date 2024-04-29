package com.example.signuppract

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth

import com.google.firebase.firestore.FirebaseFirestore


class seeRequests : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var userArrayList: ArrayList<User>
    private lateinit var myAdapter: MyAdapter
    private lateinit var db : FirebaseFirestore
    private val currentUser = FirebaseAuth.getInstance().currentUser


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_see_requests)

        //initialise recyclerview, and adapter
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)
        userArrayList = arrayListOf()
        myAdapter = MyAdapter(userArrayList)
        recyclerView.adapter = myAdapter
        EventChangeListener()
    }

    fun declineRequest(view: View) {
        println("decline button clicked ")
        val position = view.tag as Int
        if (position >= 0 && position < userArrayList.size) {
            val user = userArrayList[position]
            userArrayList.removeAt(position)
            //delete from friendsRequest of user logged in
            db.collection("requests").document(currentUser?.uid.toString())
                .collection("friendRequests").document(user.email.toString()).delete()

            myAdapter.setUserList(userArrayList)
            Toast.makeText(this, "Request declined", Toast.LENGTH_SHORT).show()

        }
    }

    fun acceptRequest(view: View) {
        println("accept button clicked ")
        val position = view.tag as Int
        if (position >= 0 && position < userArrayList.size) {
        val user = userArrayList[position]

            val allFriendsRef = db.collection("requests").document(currentUser?.uid.toString())
            .collection("allFriends")
            var userIDName: String? = null
            val userIDRef = db.collection("users")
           userIDRef.whereEqualTo("email", user.email).get()
               .addOnSuccessListener { documents ->
                   for (document in documents) {
                       // Get the ID of the document where the field is found
                       val userIDName = document.id

                       val requesterRef = db.collection("requests").document(userIDName)
                           .collection("allFriends")
                       // Add the document to requesterRef
                       val data = hashMapOf(
                           "email" to currentUser?.email.toString()
                       ) // Use current user's email
                       val currentUserEmail = currentUser?.email
                       if (currentUserEmail != null) {
                           requesterRef.document(currentUserEmail)
                               .set(data)
                               .addOnSuccessListener {
                                   Log.d("Firestore", "Document added successfully to requesterRef")
                               }
                               .addOnFailureListener { e ->
                                   Log.e("Firestore Error", "Error adding document to requesterRef: ${e.message}")
                               } }
                   }
               }
               .addOnFailureListener { exception ->
                   Log.e("Firestore Error", "Error getting documents: ", exception)
               }

            user.email?.let {
            allFriendsRef.document(it).set(user)
                .addOnSuccessListener {
                    // remove the item from the ArrayList and notify the adapter
                    userArrayList.removeAt(position)
                    //delete from friendsRequest of user logged in
                    db.collection("requests").document(currentUser?.uid.toString())
                        .collection("friendRequests").document(user.email.toString()).delete()
                    //add to allFriends of user that did the request
                    myAdapter.setUserList(userArrayList)
                    Toast.makeText(this, "Request accepted", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Log.e("Firestore Error", "Error accepting request: ${e.message}")
                    Toast.makeText(this, "Failed to accept request", Toast.LENGTH_SHORT).show()
                }
        }
        }
        println("Accept is successsss")
    }

    private fun EventChangeListener() {
        //Fetches data from Firestore.
        //Listens for changes in the Firestore collection friendRequests under the current user's document.
        //Iterates through the documents, extracts email addresses, and adds them to the userArrayList.
        //Notifies the adapter that the data set has changed.
        val currentUserUid = currentUser?.uid

        db = FirebaseFirestore.getInstance()
        val currentUserRequestsRef = currentUserUid?.let { db.collection("requests").document(it) }
        currentUserRequestsRef?.collection("friendRequests")
            ?.addSnapshotListener { friendRequestsSnapshot, error ->
                if (error != null) {
                    Log.e("Firestore Error", error.message.toString())
                    return@addSnapshotListener
                }


                userArrayList.clear()
                for (friendRequestDocument in friendRequestsSnapshot?.documents ?: emptyList()) {

                    // Here, friendRequestDocument.id will give you the email address
                    val email = friendRequestDocument.id

                    if (email.contains("@")) {
                        // If the email contains "@", add it to the userArrayList
                        val user = User(email)
                        userArrayList.add(user)
                    }
              }

                myAdapter.notifyDataSetChanged()
            }

    }




    }
